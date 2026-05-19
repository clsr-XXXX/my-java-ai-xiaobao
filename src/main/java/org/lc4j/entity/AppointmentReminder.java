package org.lc4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预约提醒表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("appointment_reminder")
public class AppointmentReminder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appointmentId;
    private String patientPhone;
    private String reminderType;  // SMS/EMAIL/PUSH
    private LocalDateTime reminderTime;
    private Integer status;  // 0-未发送 1-已发送 2-失败
    private String errorMessage;
    private LocalDateTime sendTime;
    private LocalDateTime createTime;
}
