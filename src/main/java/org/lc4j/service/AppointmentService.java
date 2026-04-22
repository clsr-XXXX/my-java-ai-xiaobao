package org.lc4j.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.lc4j.entity.Appointment;


public interface AppointmentService extends IService<Appointment> {
    Appointment getOne(Appointment appointment);
}
