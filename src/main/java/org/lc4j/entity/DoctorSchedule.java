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
 * 医生排班表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("doctor_schedule")
public class DoctorSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeSlot;  // 上午/下午/全天
    private Integer maxPatients;
    private Integer currentPatients;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
