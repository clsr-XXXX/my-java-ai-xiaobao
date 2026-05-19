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
 * 预约规则表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("appointment_rule")
public class AppointmentRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private String ruleName;
    private String description;
    private Integer advanceDays;
    private Integer maxAdvanceDays;
    private Integer allowSameDayCancel;
    private Integer cancellationDeadline;
    private Integer maxAppointments;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
