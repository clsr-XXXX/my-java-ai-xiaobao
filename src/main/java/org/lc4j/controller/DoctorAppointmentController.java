package org.lc4j.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.lc4j.service.impl.DoctorAppointmentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "医生预约系统")
@RestController
@RequestMapping("/api/appointment")
@Slf4j
public class DoctorAppointmentController {

    @Autowired
    private DoctorAppointmentServiceImpl appointmentService;

    /**
     * 获取医生的可预约日期列表
     */
    @Operation(summary = "获取医生的可预约日期")
    @GetMapping("/available-dates/{doctorId}")
    public ResponseEntity<Map<String, Object>> getAvailableDates(@PathVariable Long doctorId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<LocalDate> dates = appointmentService.getAvailableScheduleDates(doctorId);
            response.put("success", true);
            response.put("data", dates);
            response.put("count", dates.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取指定日期的可用号源
     */
    @Operation(summary = "获取医生的可用号源")
    @GetMapping("/available-slots/{doctorId}/{date}")
    public ResponseEntity<Map<String, Object>> getAvailableSlots(
            @PathVariable Long doctorId,
            @PathVariable String date) {
        Map<String, Object> response = new HashMap<>();
        try {
            LocalDate appointmentDate = LocalDate.parse(date);
            List<Map<String, Object>> slots = appointmentService.getAvailableSlots(doctorId, appointmentDate);
            response.put("success", true);
            response.put("data", slots);
            response.put("count", slots.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取医生详细信息
     */
    @Operation(summary = "获取医生详细信息")
    @GetMapping("/doctor-detail/{doctorId}")
    public ResponseEntity<Map<String, Object>> getDoctorDetail(@PathVariable Long doctorId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> doctorInfo = appointmentService.getDoctorDetail(doctorId);
            response.put("success", true);
            response.put("data", doctorInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 预约医生（核心接口）
     */
    @Operation(summary = "预约医生")
    @PostMapping("/book")
    public ResponseEntity<Map<String, Object>> bookAppointment(
            @RequestParam String patientName,
            @RequestParam String patientPhone,
            @RequestParam String patientIdCard,
            @RequestParam Long doctorId,
            @RequestParam String appointmentDate,
            @RequestParam String timeSlot) {
        try {
            LocalDate date = LocalDate.parse(appointmentDate);
            Map<String, Object> result = appointmentService.bookAppointment(
                    patientName, patientPhone, patientIdCard, doctorId, date, timeSlot);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 取消预约
     */
    @Operation(summary = "取消预约")
    @PostMapping("/cancel/{appointmentId}")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestParam(required = false) String cancelReason) {
        try {
            Map<String, Object> result = appointmentService.cancelAppointment(appointmentId, cancelReason);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 确认预约（医生端）
     */
    @Operation(summary = "确认预约")
    @PostMapping("/confirm/{appointmentId}")
    public ResponseEntity<Map<String, Object>> confirmAppointment(@PathVariable Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = appointmentService.confirmAppointment(appointmentId);
            response.put("success", result);
            response.put("message", result ? "✅ 确认成功" : "❌ 确认失败");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 标记已就诊
     */
    @Operation(summary = "标记已就诊")
    @PostMapping("/mark-visited/{appointmentId}")
    public ResponseEntity<Map<String, Object>> markAsVisited(@PathVariable Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = appointmentService.markAsVisited(appointmentId);
            response.put("success", result);
            response.put("message", result ? "✅ 标记成功" : "❌ 标记失败");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 提交预约评价
     */
    @Operation(summary = "提交预约评价")
    @PostMapping("/evaluate")
    public ResponseEntity<Map<String, Object>> submitEvaluation(
            @RequestParam Long appointmentId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) Integer environmentRating,
            @RequestParam(required = false) Integer serviceRating,
            @RequestParam(required = false) Integer waitTimeRating) {
        try {
            Map<String, Object> result = appointmentService.submitEvaluation(
                    appointmentId, rating, comment, environmentRating, serviceRating, waitTimeRating);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}