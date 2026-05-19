package org.lc4j.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lc4j.entity.*;
import org.lc4j.mapper.*;
import org.lc4j.service.DoctorAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 医生预约服务实现
 */
@Slf4j
@Service
public class DoctorAppointmentServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorAppointmentService {

    @Autowired
    private PatientAppointmentMapper appointmentMapper;

    @Autowired
    private DoctorScheduleMapper scheduleMapper;

    @Autowired
    private AppointmentSlotMapper slotMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private AppointmentRuleMapper ruleMapper;

    @Autowired
    private AppointmentEvaluationMapper evaluationMapper;

    @Autowired
    private AppointmentReminderMapper reminderMapper;

    @Autowired
    private PatientInfoMapper patientInfoMapper;

    // ======================== 1. 查询相关方法 ========================

    /**
     * 获取医生的可用排班日期列表
     * @param doctorId 医生ID
     * @return 可预约的日期列表
     */
    public List<LocalDate> getAvailableScheduleDates(Long doctorId) {
        // 获取预约规则
        AppointmentRule rule = getAppointmentRule(doctorId);
        
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.plusDays(rule.getAdvanceDays());
        LocalDate endDate = today.plusDays(rule.getMaxAdvanceDays());

        // 查询该医生在此时间段内的排班
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<DoctorSchedule>()
                .eq(DoctorSchedule::getDoctorId, doctorId)
                .between(DoctorSchedule::getScheduleDate, startDate, endDate)
                .eq(DoctorSchedule::getStatus, 1)
                .orderByAsc(DoctorSchedule::getScheduleDate);

        return scheduleMapper.selectList(wrapper)
                .stream()
                .map(DoctorSchedule::getScheduleDate)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取指定日期医生的可用号源
     * @param doctorId 医生ID
     * @param appointmentDate 预约日期
     * @return 可用号源列表
     */
    public List<Map<String, Object>> getAvailableSlots(Long doctorId, LocalDate appointmentDate) {
        List<Map<String, Object>> availableSlots = new ArrayList<>();

        // 获取该日期的医生排班
        LambdaQueryWrapper<DoctorSchedule> scheduleWrapper = new LambdaQueryWrapper<DoctorSchedule>()
                .eq(DoctorSchedule::getDoctorId, doctorId)
                .eq(DoctorSchedule::getScheduleDate, appointmentDate)
                .eq(DoctorSchedule::getStatus, 1);

        List<DoctorSchedule> schedules = scheduleMapper.selectList(scheduleWrapper);

        for (DoctorSchedule schedule : schedules) {
            // 查询该时间段的号源
            LambdaQueryWrapper<AppointmentSlot> slotWrapper = new LambdaQueryWrapper<AppointmentSlot>()
                    .eq(AppointmentSlot::getDoctorId, doctorId)
                    .eq(AppointmentSlot::getScheduleDate, appointmentDate)
                    .eq(AppointmentSlot::getTimeSlot, schedule.getTimeSlot())
                    .eq(AppointmentSlot::getStatus, 0);  // 只显示可预约的

            List<AppointmentSlot> slots = slotMapper.selectList(slotWrapper);

            for (AppointmentSlot slot : slots) {
                Map<String, Object> slotInfo = new HashMap<>();
                slotInfo.put("slotId", slot.getId());
                slotInfo.put("timeSlot", schedule.getTimeSlot());
                slotInfo.put("startTime", schedule.getStartTime());
                slotInfo.put("endTime", schedule.getEndTime());
                slotInfo.put("slotNumber", slot.getSlotNumber());
                slotInfo.put("fee", findDoctor(doctorId).getRegistrationFee());
                availableSlots.add(slotInfo);
            }
        }

        return availableSlots;
    }

    /**
     * 获取医生详细信息（包括评分、患者数等）
     */
    public Map<String, Object> getDoctorDetail(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            return null;
        }

        // 获取科室信息
        Department department = departmentMapper.selectById(doctor.getDepartmentId());

        // 获取医生的平均评分
        LambdaQueryWrapper<AppointmentEvaluation> evalWrapper = new LambdaQueryWrapper<AppointmentEvaluation>()
                .eq(AppointmentEvaluation::getDoctorId, doctorId);
        List<AppointmentEvaluation> evaluations = evaluationMapper.selectList(evalWrapper);
        
        Double averageRating = evaluations.isEmpty() ? 5.0 :
                evaluations.stream()
                        .mapToDouble(AppointmentEvaluation::getRating)
                        .average()
                        .orElse(5.0);

        Map<String, Object> doctorInfo = new HashMap<>();
        doctorInfo.put("id", doctor.getId());
        doctorInfo.put("name", doctor.getName());
        doctorInfo.put("title", doctor.getTitle());
        doctorInfo.put("department", department.getName());
        doctorInfo.put("specialties", doctor.getSpecialties());
        doctorInfo.put("introduction", doctor.getIntroduction());
        doctorInfo.put("registrationFee", doctor.getRegistrationFee());
        doctorInfo.put("totalPatients", doctor.getTotalPatients());
        doctorInfo.put("consultationCount", doctor.getConsultationCount());
        doctorInfo.put("rating", String.format("%.1f", averageRating));
        doctorInfo.put("evaluationCount", evaluations.size());

        return doctorInfo;
    }

    // ======================== 2. 预约相关方法 ========================

    /**
     * 预约医生（核心方法）
     * @param patientName 患者姓名
     * @param patientPhone 患者电话
     * @param patientIdCard 患者身份证
     * @param doctorId 医生ID
     * @param appointmentDate 预约日期
     * @param timeSlot 时间段（上午/下午）
     * @return 预约结果
     */
    @Transactional
    public Map<String, Object> bookAppointment(
            String patientName,
            String patientPhone,
            String patientIdCard,
            Long doctorId,
            LocalDate appointmentDate,
            String timeSlot) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证参数
            if (!validateBookingParams(patientName, patientPhone, patientIdCard, doctorId, appointmentDate, timeSlot)) {
                result.put("success", false);
                result.put("message", "参数验证失败");
                return result;
            }

            // 2. 检查预约规则
            if (!checkAppointmentRule(patientIdCard, doctorId, appointmentDate)) {
                result.put("success", false);
                result.put("message", "不符合预约规则");
                return result;
            }

            // 3. 查找可用号源
            AppointmentSlot availableSlot = findAvailableSlot(doctorId, appointmentDate, timeSlot);
            if (availableSlot == null) {
                result.put("success", false);
                result.put("message", "该时间段已满号，无法预约");
                return result;
            }

            // 4. 获取医生和科室信息
            Doctor doctor = doctorMapper.selectById(doctorId);
            Department department = departmentMapper.selectById(doctor.getDepartmentId());

            // 5. 获取该时间段的号源号码
            LambdaQueryWrapper<AppointmentSlot> slotWrapper = new LambdaQueryWrapper<AppointmentSlot>()
                    .eq(AppointmentSlot::getDoctorId, doctorId)
                    .eq(AppointmentSlot::getScheduleDate, appointmentDate)
                    .eq(AppointmentSlot::getTimeSlot, timeSlot)
                    .eq(AppointmentSlot::getStatus, 0)
                    .orderByAsc(AppointmentSlot::getSlotNumber)
                    .last("LIMIT 1");
            
            AppointmentSlot slot = slotMapper.selectOne(slotWrapper);
            
            // 6. 创建预约记录
            PatientAppointment appointment = PatientAppointment.builder()
                    .patientName(patientName)
                    .patientPhone(patientPhone)
                    .patientIdCard(patientIdCard)
                    .doctorId(doctorId)
                    .doctorName(doctor.getName())
                    .departmentId(doctor.getDepartmentId())
                    .departmentName(department.getName())
                    .appointmentDate(appointmentDate)
                    .timeSlot(timeSlot)
                    .registrationFee(doctor.getRegistrationFee())
                    .queueNumber(slot.getSlotNumber())
                    .status(0)  // 待确认
                    .appointmentSource("APP")
                    .createTime(LocalDateTime.now())
                    .build();

            appointmentMapper.insert(appointment);

            // 7. 更新号源状态
            AppointmentSlot updateSlot = new AppointmentSlot();
            updateSlot.setId(slot.getId());
            updateSlot.setStatus(1);  // 已预约
            updateSlot.setPatientAppointmentId(appointment.getId());
            slotMapper.updateById(updateSlot);

            // 8. 创建预约提醒
            createAppointmentReminder(appointment);

            // 9. 返回预约成功信息
            result.put("success", true);
            result.put("message", "✅ 预约成功");
            result.put("appointmentId", appointment.getId());
            result.put("queueNumber", slot.getSlotNumber());
            result.put("appointmentInfo", new HashMap<String, Object>() {{
                put("医生", doctor.getName());
                put("科室", department.getName());
                put("日期", appointmentDate);
                put("时间段", timeSlot);
                put("排队号", slot.getSlotNumber());
                put("挂号费", doctor.getRegistrationFee() + "元");
            }});

            log.info("预约成功：患者={}, 医生={}, 日期={}", patientName, doctor.getName(), appointmentDate);

            return result;

        } catch (Exception e) {
            log.error("预约失败", e);
            result.put("success", false);
            result.put("message", "预约失败：" + e.getMessage());
            return result;
        }
    }

    /**
     * 取消预约
     */
    @Transactional
    public Map<String, Object> cancelAppointment(Long appointmentId, String cancelReason) {
        Map<String, Object> result = new HashMap<>();

        try {
            PatientAppointment appointment = appointmentMapper.selectById(appointmentId);
            if (appointment == null) {
                result.put("success", false);
                result.put("message", "预约不存在");
                return result;
            }

            // 检查是否可以取消
            if (appointment.getStatus() == 4) {  // 已取消
                result.put("success", false);
                result.put("message", "预约已取消，无需再次取消");
                return result;
            }

            if (appointment.getStatus() == 2) {  // 已就诊
                result.put("success", false);
                result.put("message", "已就诊的预约无法取消");
                return result;
            }

            // 检查是否超过取消截止时间
            AppointmentRule rule = getAppointmentRule(appointment.getDoctorId());
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                    appointment.getAppointmentDate(),
                    LocalTime.of(12, 0)  // 假设下午12点
            );
            
            long hoursUntilAppointment = ChronoUnit.HOURS.between(LocalDateTime.now(), appointmentDateTime);
            if (hoursUntilAppointment < Math.abs(rule.getCancellationDeadline())) {
                result.put("success", false);
                result.put("message", "超过取消截止时间，无法取消");
                return result;
            }

            // 更新预约状态
            PatientAppointment updateAppointment = new PatientAppointment();
            updateAppointment.setId(appointmentId);
            updateAppointment.setStatus(4);  // 已取消
            updateAppointment.setCancelReason(cancelReason);
            updateAppointment.setCancelTime(LocalDateTime.now());
            appointmentMapper.updateById(updateAppointment);

            // 释放号源
            LambdaQueryWrapper<AppointmentSlot> slotWrapper = new LambdaQueryWrapper<AppointmentSlot>()
                    .eq(AppointmentSlot::getPatientAppointmentId, appointmentId);
            AppointmentSlot slot = slotMapper.selectOne(slotWrapper);
            if (slot != null) {
                AppointmentSlot updateSlot = new AppointmentSlot();
                updateSlot.setId(slot.getId());
                updateSlot.setStatus(0);  // 可预约
                updateSlot.setPatientAppointmentId(null);
                slotMapper.updateById(updateSlot);
            }

            result.put("success", true);
            result.put("message", "✅ 取消成功");
            log.info("取消预约：appointmentId={}", appointmentId);

            return result;

        } catch (Exception e) {
            log.error("取消预约失败", e);
            result.put("success", false);
            result.put("message", "取消失败：" + e.getMessage());
            return result;
        }
    }

    /**
     * 确认预约（医生端）
     */
    @Transactional
    public boolean confirmAppointment(Long appointmentId) {
        PatientAppointment appointment = new PatientAppointment();
        appointment.setId(appointmentId);
        appointment.setStatus(1);  // 已确认
        appointment.setUpdateTime(LocalDateTime.now());
        return appointmentMapper.updateById(appointment) > 0;
    }

    /**
     * 标记已就诊
     */
    @Transactional
    public boolean markAsVisited(Long appointmentId) {
        PatientAppointment appointment = new PatientAppointment();
        appointment.setId(appointmentId);
        appointment.setStatus(2);  // 已就诊
        appointment.setUpdateTime(LocalDateTime.now());
        
        int result = appointmentMapper.updateById(appointment);
        
        if (result > 0) {
            // 更新医生的咨询次数
            PatientAppointment originalAppointment = appointmentMapper.selectById(appointmentId);
            Doctor doctor = doctorMapper.selectById(originalAppointment.getDoctorId());
            doctor.setConsultationCount((doctor.getConsultationCount() == null ? 0 : doctor.getConsultationCount()) + 1);
            doctorMapper.updateById(doctor);
        }
        
        return result > 0;
    }

    // ======================== 3. 评价相关方法 ========================

    /**
     * 提交预约评价
     */
    @Transactional
    public Map<String, Object> submitEvaluation(
            Long appointmentId,
            Integer rating,
            String comment,
            Integer environmentRating,
            Integer serviceRating,
            Integer waitTimeRating) {

        Map<String, Object> result = new HashMap<>();

        try {
            PatientAppointment appointment = appointmentMapper.selectById(appointmentId);
            if (appointment == null) {
                result.put("success", false);
                result.put("message", "预约不存在");
                return result;
            }

            // 创建评价记录
            AppointmentEvaluation evaluation = AppointmentEvaluation.builder()
                    .appointmentId(appointmentId)
                    .patientName(appointment.getPatientName())
                    .doctorId(appointment.getDoctorId())
                    .rating(rating)
                    .comment(comment)
                    .environmentRating(environmentRating)
                    .serviceRating(serviceRating)
                    .waitTimeRating(waitTimeRating)
                    .wouldRecommend(rating >= 4 ? 1 : 0)
                    .createTime(LocalDateTime.now())
                    .build();

            evaluationMapper.insert(evaluation);

            // 更新医生评分
            updateDoctorRating(appointment.getDoctorId());

            result.put("success", true);
            result.put("message", "✅ 评价成功");
            log.info("评价成功：appointmentId={}", appointmentId);

            return result;

        } catch (Exception e) {
            log.error("评价失败", e);
            result.put("success", false);
            result.put("message", "评价失败：" + e.getMessage());
            return result;
        }
    }

    // ======================== 4. 辅助私有方法 ========================

    /**
     * 验证预约参数
     */
    private boolean validateBookingParams(String patientName, String patientPhone, String patientIdCard,
                                         Long doctorId, LocalDate appointmentDate, String timeSlot) {
        if (patientName == null || patientName.isEmpty()) {
            return false;
        }
        if (patientPhone == null || patientPhone.isEmpty()) {
            return false;
        }
        if (patientIdCard == null || patientIdCard.isEmpty()) {
            return false;
        }
        if (doctorId == null || doctorId <= 0) {
            return false;
        }
        if (appointmentDate == null || appointmentDate.isBefore(LocalDate.now())) {
            return false;
        }
        if (timeSlot == null || (!timeSlot.equals("上午") && !timeSlot.equals("下午"))) {
            return false;
        }
        return true;
    }

    /**
     * 检查预约规则
     */
    private boolean checkAppointmentRule(String patientIdCard, Long doctorId, LocalDate appointmentDate) {
        AppointmentRule rule = getAppointmentRule(doctorId);

        // 检查提前预约天数
        long daysBefore = ChronoUnit.DAYS.between(LocalDate.now(), appointmentDate);
        if (daysBefore < rule.getAdvanceDays() || daysBefore > rule.getMaxAdvanceDays()) {
            return false;
        }

        // 检查患者同时预约数量
        LambdaQueryWrapper<PatientAppointment> appointmentWrapper = new LambdaQueryWrapper<PatientAppointment>()
                .eq(PatientAppointment::getPatientIdCard, patientIdCard)
                .in(PatientAppointment::getStatus, 0, 1);  // 待确认或已确认

        long appointmentCount = appointmentMapper.selectCount(appointmentWrapper);
        if (appointmentCount >= rule.getMaxAppointments()) {
            return false;
        }

        return true;
    }

    /**
     * 查找可用号源
     */
    private AppointmentSlot findAvailableSlot(Long doctorId, LocalDate appointmentDate, String timeSlot) {
        LambdaQueryWrapper<AppointmentSlot> wrapper = new LambdaQueryWrapper<AppointmentSlot>()
                .eq(AppointmentSlot::getDoctorId, doctorId)
                .eq(AppointmentSlot::getScheduleDate, appointmentDate)
                .eq(AppointmentSlot::getTimeSlot, timeSlot)
                .eq(AppointmentSlot::getStatus, 0);  // 可预约

        return slotMapper.selectOne(wrapper);
    }

    /**
     * 获取预约规则
     */
    private AppointmentRule getAppointmentRule(Long doctorId) {
        // 优先获取医生特定规则，否则获取全院规则
        LambdaQueryWrapper<AppointmentRule> wrapper = new LambdaQueryWrapper<AppointmentRule>()
                .eq(AppointmentRule::getDoctorId, doctorId)
                .eq(AppointmentRule::getStatus, 1);

        AppointmentRule rule = ruleMapper.selectOne(wrapper);
        if (rule == null) {
            // 获取全院规则
            wrapper = new LambdaQueryWrapper<AppointmentRule>()
                    .isNull(AppointmentRule::getDoctorId)
                    .eq(AppointmentRule::getStatus, 1);
            rule = ruleMapper.selectOne(wrapper);
        }

        return rule;
    }

    /**
     * 创建预约提醒
     */
    private void createAppointmentReminder(PatientAppointment appointment) {
        // 预约前1天提醒
        LocalDateTime reminderTime = LocalDateTime.of(appointment.getAppointmentDate().minusDays(1), LocalTime.of(10, 0));

        AppointmentReminder reminder = AppointmentReminder.builder()
                .appointmentId(appointment.getId())
                .patientPhone(appointment.getPatientPhone())
                .reminderType("SMS")
                .reminderTime(reminderTime)
                .status(0)  // 未发送
                .createTime(LocalDateTime.now())
                .build();

        reminderMapper.insert(reminder);
    }

    /**
     * 查找医生
     */
    private Doctor findDoctor(Long doctorId) {
        return doctorMapper.selectById(doctorId);
    }

    /**
     * 更新医生评分
     */
    private void updateDoctorRating(Long doctorId) {
        LambdaQueryWrapper<AppointmentEvaluation> wrapper = new LambdaQueryWrapper<AppointmentEvaluation>()
                .eq(AppointmentEvaluation::getDoctorId, doctorId);

        List<AppointmentEvaluation> evaluations = evaluationMapper.selectList(wrapper);
        if (evaluations.isEmpty()) {
            return;
        }

        Double averageRating = evaluations.stream()
                .mapToDouble(AppointmentEvaluation::getRating)
                .average()
                .orElse(5.0);

        Doctor doctor = new Doctor();
        doctor.setId(doctorId);
        doctor.setRating(averageRating);
        doctorMapper.updateById(doctor);
    }


}
