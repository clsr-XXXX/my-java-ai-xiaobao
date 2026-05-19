package org.lc4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.lc4j.entity.Doctor;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
}
