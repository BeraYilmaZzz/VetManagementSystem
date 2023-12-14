package com.bera.yilmaz.VetManagementSystem.Business.Concrete;

import com.bera.yilmaz.VetManagementSystem.Business.Abs.IAppointmentService;
import com.bera.yilmaz.VetManagementSystem.Business.Abs.IDoctorService;
import com.bera.yilmaz.VetManagementSystem.Core.Config.ModelMapper.IModelMapperService;
import com.bera.yilmaz.VetManagementSystem.Core.Exception.NotFoundException;
import com.bera.yilmaz.VetManagementSystem.Dao.AppointmentRepo;
import com.bera.yilmaz.VetManagementSystem.Dto.Response.AppointmentResponse;
import com.bera.yilmaz.VetManagementSystem.Entitiy.Appointment;
import com.bera.yilmaz.VetManagementSystem.Entitiy.Doctor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentManager implements IAppointmentService {
    private final IModelMapperService modelMapper;

    private final AppointmentRepo appointmentRepo;
    private final IDoctorService doctorService;

    public AppointmentManager(IModelMapperService modelMapper, AppointmentRepo appointmentRepo, IDoctorService doctorService) {
        this.modelMapper = modelMapper;
        this.appointmentRepo = appointmentRepo;
        this.doctorService = doctorService;
    }

    @Override
    public Appointment save(Appointment appointment) {
        // Randevu eklemeden önce çakışan randevuları kontrol et
        checkConflictingAppointments(appointment);

        // Eğer doktor null değilse randevuyu kaydet
        if (appointment.getDoctor() != null) {
            return appointmentRepo.save(appointment);
        } else {
            throw new RuntimeException("Doktor bilgisi boş olamaz!");
        }
    }

    @Override
    public Appointment update(long id, Appointment appointment) {
        // İlgili işlemleri burada gerçekleştirin.
        return appointmentRepo.save(appointment);
    }

    @Override
    public Appointment delete(long id) {
        Optional<Appointment> appointmentFromDb = appointmentRepo.findById(id);
        if (appointmentFromDb.isPresent()) {
            Appointment deletedAppointment = appointmentFromDb.get();
            appointmentRepo.delete(deletedAppointment);
            return deletedAppointment;
        } else {
            throw new NotFoundException(id + " id'li randevu sistemde bulunamadı!!!");
        }
    }

    @Override
    public List<Appointment> getAll() {
        return appointmentRepo.findAll();
    }

    @Override
    public Doctor getDoctorById(long id) {
        return doctorService.get(id);
    }

    @Override
    public List<Appointment> getAppointmentsByDateAndDoctor(LocalDateTime startDate, LocalDateTime endDate, long doctorId) {
        Doctor doctor = doctorService.get(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doktor bulunamadı!");
        }

        return appointmentRepo.findByDoctorIdAndDateTimeBetween(
                doctor.getId(),
                startDate,
                endDate
        );
    }

    @Override
    public List<AppointmentResponse> getAppointmentsByDateAndAnimalId(LocalDateTime startDate, LocalDateTime endDate, long animalId) {
        List<Appointment> appointments = appointmentRepo.findByAnimalIdAndDateTimeBetween(
                animalId,
                startDate,
                endDate
        );

        return appointments.stream()
                .filter(appointment -> startDate.isBefore(appointment.getAppointmentDate()) && endDate.isAfter(appointment.getAppointmentDate()))
                .map(appointment -> modelMapper.forResponse().map(appointment, AppointmentResponse.class))
                .collect(Collectors.toList());
    }

    private void checkConflictingAppointments(Appointment newAppointment) {
        // Doktor null değilse çakışan randevuları kontrol et
        if (newAppointment.getDoctor() != null) {
            List<Appointment> conflictingAppointments = appointmentRepo.findConflictingAppointmentsForDoctor(
                    newAppointment.getDoctor().getId(),
                    newAppointment.getAppointmentDate()
            );

            if (!conflictingAppointments.isEmpty()) {
                throw new RuntimeException("Doktorun bu saatte başka bir randevusu bulunmaktadır!");
            }
        } else {
            throw new RuntimeException("Doktor bilgisi boş olamaz!");
        }
    }
}
