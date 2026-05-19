package org.lc4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 患者预约表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("patient_appointment")
public class PatientAppointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private String patientIdCard;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String timeSlot;  // 上午/下午
    private Integer registrationFee;
    private Integer queueNumber;
    private Integer status;  // 0-待确认 1-已确认 2-已就诊 3-未就诊 4-已取消
    private String appointmentSource;
    private String notes;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}



