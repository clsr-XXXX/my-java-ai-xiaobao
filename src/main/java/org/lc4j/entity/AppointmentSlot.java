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

/**
 * 预约号源表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("appointment_slot")
public class AppointmentSlot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private LocalDate scheduleDate;
    private String timeSlot;  // 上午/下午
    private Integer slotNumber;  // 号码
    private Integer status;  // 0-可预约 1-已预约 2-未就诊 3-已就诊
    private Long patientAppointmentId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}