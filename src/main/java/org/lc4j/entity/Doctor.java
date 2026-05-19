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
 * 医生信息表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("doctor")
public class Doctor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String title;
    private Long departmentId;
    private String specialties;
    private String introduction;
    private String imageUrl;
    private Integer registrationFee;
    private Integer consultationFee;
    private Integer totalPatients;
    private Integer consultationCount;
    private Double rating;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}












