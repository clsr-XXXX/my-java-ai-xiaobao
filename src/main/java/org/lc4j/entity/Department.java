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
 * 科室信息表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("department")
public class Department {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer floor;
    private String phone;
    private String workingHours;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
