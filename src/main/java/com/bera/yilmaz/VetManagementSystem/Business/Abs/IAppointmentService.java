package com.bera.yilmaz.VetManagementSystem.Business.Abs;

import com.bera.yilmaz.VetManagementSystem.Dto.Response.AppointmentResponse;
import com.bera.yilmaz.VetManagementSystem.Entitiy.Appointment;
import com.bera.yilmaz.VetManagementSystem.Entitiy.Doctor;

import java.time.LocalDateTime;
import java.util.List;

public interface IAppointmentService {
    Appointment save(Appointment appointment);
    Appointment update(long id, Appointment appointment);
    Appointment delete(long id);
    List<Appointment> getAll();
    Doctor getDoctorById(long id);

    List<Appointment> getAppointmentsByDateAndDoctor(LocalDateTime appointmentDate, LocalDateTime endOfDay, long doctorId);

    List<AppointmentResponse> getAppointmentsByDateAndAnimalId(LocalDateTime dateTime, LocalDateTime endOfDay, long animalId);

}
