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
 * 预约评价表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("appointment_evaluation")
public class AppointmentEvaluation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appointmentId;
    private String patientName;
    private Long doctorId;
    private Integer rating;  // 1-5
    private String comment;
    private Integer environmentRating;
    private Integer serviceRating;
    private Integer waitTimeRating;
    private Integer wouldRecommend;
    private LocalDateTime createTime;
}
