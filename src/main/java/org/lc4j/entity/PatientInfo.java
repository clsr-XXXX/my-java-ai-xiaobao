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
 * 患者信息表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("patient_info")
public class PatientInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String idCard;
    private String phone;
    private String gender;
    private Integer age;
    private String email;
    private String address;
    private String medicalHistory;
    private String allergies;
    private String emergencyContact;
    private String emergencyPhone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}