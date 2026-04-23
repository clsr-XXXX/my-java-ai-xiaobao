package org.lc4j.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.lc4j.entity.Appointment;
import org.lc4j.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AppointmentTools {
    @Autowired
    private AppointmentService appointmentService;

    @Tool(name="预约挂号", value = "根据参数，先执行工具方法queryDepartment查询是否可预约，并直接给用户回答是否可预约，并让用户确认所有预约信息，用户确认后再进行预约。如果用户没有提供具体的医生姓名，请从向量存储中找到一位医生。")
    public String bookAppointment(Appointment appointment){
        try {
            // 关键：转换时间格式
            String normalizedTime = normalizeTime(appointment.getTime());
            if (normalizedTime == null) {
                return "时间格式错误，请提供有效的时间，例如：14:30:00 或 上午";
            }
            appointment.setTime(normalizedTime);

            //查找数据库中是否包含对应的预约记录
            Appointment appointmentDB = appointmentService.getOne(appointment);
            if(appointmentDB == null){
                appointment.setId(null);//防止大模型幻觉设置了id
                if(appointmentService.save(appointment)){
                    return "预约成功，您的预约信息为：\n" +
                            "科室：" + appointment.getDepartment() + "\n" +
                            "日期：" + appointment.getDate() + "\n" +
                            "时间：" + appointment.getTime() + "\n" +
                            "医生：" + (appointment.getDoctorName() != null ? appointment.getDoctorName() : "待分配");
                }else{
                    return "预约失败";
                }
            }
            return "您在相同的科室和时间已有预约";
        } catch (Exception e) {
            return "预约出错：" + e.getMessage();
        }
    }


    @Tool(name="取消预约挂号", value = "根据参数，查询预约是否存在，如果存在则删除预约记录并返回取消预约成功，否则返回取消预约失败")
    public String cancelAppointment(Appointment appointment){
        try {
            // 转换时间格式
            String normalizedTime = normalizeTime(appointment.getTime());
            if (normalizedTime == null) {
                return "时间格式错误，请提供有效的时间";
            }
            appointment.setTime(normalizedTime);

            Appointment appointmentDB = appointmentService.getOne(appointment);
            if(appointmentDB != null){
                //删除预约记录
                if(appointmentService.removeById(appointmentDB.getId())){
                    return "取消预约成功";
                }else{
                    return "取消预约失败";
                }
            }
            //取消失败
            return "您没有预约记录，请核对预约科室和时间";
        } catch (Exception e) {
            return "取消预约出错：" + e.getMessage();
        }
    }

    @Tool(name = "查询是否有号源", value="根据科室名称，日期，时间和医生查询是否有号源，并返回给用户")
    public boolean queryDepartment(
            @P(value = "科室名称") String name,
            @P(value = "日期") String date,
            @P(value = "时间，可选值：上午、下午") String time,
            @P(value = "医生名称", required = false) String doctorName
    ) {
        System.out.println("查询是否有号源");
        System.out.println("科室名称：" + name);
        System.out.println("日期：" + date);
        System.out.println("时间：" + time);
        System.out.println("医生名称：" + doctorName);
        return true;
    }

    /**
     * 规范化时间格式
     */
    private String normalizeTime(String time) {
        if (time == null || time.isEmpty()) {
            return null;
        }

        time = time.trim();

        // 格式1: "HH:mm:ss" 或 "HH:mm"（已经是标准格式）
        if (time.matches("\\d{1,2}:\\d{2}(:\\d{2})?")) {
            return time;
        }

        // 格式2: "上午" 或 "下午"（转换为标准时间段）
        if ("上午".equals(time)) {
            return "09:00:00";
        }
        if ("下午".equals(time)) {
            return "14:00:00";
        }

        // 格式3: "HH时mm分"（中文格式）
        if (time.contains("时") && time.contains("分")) {
            Pattern pattern = Pattern.compile("(\\d{1,2})时(\\d{2})分");
            Matcher matcher = pattern.matcher(time);
            if (matcher.find()) {
                int hour = Integer.parseInt(matcher.group(1));
                int minute = Integer.parseInt(matcher.group(2));
                return String.format("%02d:%02d:00", hour, minute);
            }
        }

        // 格式4: "上午/下午 + 时间"
        if (time.contains("上午") || time.contains("下午")) {
            boolean isPM = time.contains("下午");
            Pattern pattern = Pattern.compile("(\\d{1,2})(?:时|:)(\\d{2})?");
            Matcher matcher = pattern.matcher(time);
            if (matcher.find()) {
                int hour = Integer.parseInt(matcher.group(1));
                int minute = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;

                if (isPM && hour != 12) {
                    hour += 12;
                } else if (!isPM && hour == 12) {
                    hour = 0;
                }

                return String.format("%02d:%02d:00", hour, minute);
            }
        }

        // 格式5: "HHam/HHpm"
        if (time.toLowerCase().endsWith("am") || time.toLowerCase().endsWith("pm")) {
            boolean isPM = time.toLowerCase().endsWith("pm");
            String timeStr = time.substring(0, time.length() - 2).trim();

            try {
                String[] parts = timeStr.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

                if (isPM && hour != 12) {
                    hour += 12;
                } else if (!isPM && hour == 12) {
                    hour = 0;
                }

                return String.format("%02d:%02d:00", hour, minute);
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }
}