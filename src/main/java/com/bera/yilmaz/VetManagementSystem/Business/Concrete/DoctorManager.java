package com.bera.yilmaz.VetManagementSystem.Business.Concrete;

import com.bera.yilmaz.VetManagementSystem.Business.Abs.IDoctorService;
import com.bera.yilmaz.VetManagementSystem.Core.Exception.NotFoundException;
import com.bera.yilmaz.VetManagementSystem.Core.Utilies.Msg;
import com.bera.yilmaz.VetManagementSystem.Dao.DoctorRepo;
import com.bera.yilmaz.VetManagementSystem.Dto.Request.Doctor.DoctorSaveRequest;
import com.bera.yilmaz.VetManagementSystem.Entitiy.Appointment;
import com.bera.yilmaz.VetManagementSystem.Entitiy.AvailableDate;
import com.bera.yilmaz.VetManagementSystem.Entitiy.Doctor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorManager implements IDoctorService {
    private final DoctorRepo doctorRepo;

    public DoctorManager(DoctorRepo doctorRepo) {
        this.doctorRepo = doctorRepo;
    }
    @Override
    public Doctor save(Doctor doctor) {
        return this.doctorRepo.save(doctor);
    }

    @Override
    public Doctor get(long id) {
        return this.doctorRepo.findById(id).orElseThrow(() -> new NotFoundException(Msg.NOT_FOUND));
    }

    public Doctor update(long id, DoctorSaveRequest request) {
        Optional<Doctor> optionalDoctor = doctorRepo.findById(id);
        if (optionalDoctor.isPresent()) {
            Doctor doctor = optionalDoctor.get();
            doctor.setName(request.getName());
            doctor.setPhone(request.getPhone());
            doctor.setMail(request.getMail());
            doctor.setAddress(request.getAddress());
            doctor.setCity(request.getCity());
            return doctorRepo.save(doctor);
        } else {
            throw new NotFoundException(Msg.NOT_FOUND);
        }
    }
    public Doctor deleteById(long id) {
        Optional<Doctor> doctorFromDb = doctorRepo.findById(id);
        if (doctorFromDb.isPresent()) {
            Doctor deletedDoctor = doctorFromDb.get();
            doctorRepo.delete(deletedDoctor);
            return deletedDoctor;
        } else {
            throw new NotFoundException(id + " id'li doktor sistemde bulunamadı!!!");
        }
    }
    public List<Doctor> findAll(){
        return this.doctorRepo.findAll();
    }

    @Override
    public boolean existByMail(String mail) {
        return doctorRepo.existsByMail(mail);
    }

    public boolean isDoctorAvailable(Long doctorId, LocalDateTime appointmentDateTime) {
        Optional<Doctor> optionalDoctor = doctorRepo.findById(doctorId);

        if (optionalDoctor.isPresent()) {
            Doctor doctor = optionalDoctor.get();

            // Doktorun uygun olduğu tarihleri kontrol et
            List<AvailableDate> availableDates = doctor.getDoctorAvailableDate();

            for (AvailableDate availableDate : availableDates) {
                LocalDate availableDateOnly = availableDate.getAvailable_date();

                // Sadece tarih kontrolü yapılır
                if (appointmentDateTime.toLocalDate().isEqual(availableDateOnly)) {
                    // Doktorun uygun olduğu bir tarih bulunduğunda randevu saatini kontrol et
                    return isAppointmentTimeAvailable(doctor, appointmentDateTime.toLocalTime());
                }
            }

            return false;
        } else {
            throw new NotFoundException("Doktor bulunamadı!");
        }
    }

    private boolean isAppointmentTimeAvailable(Doctor doctor, LocalTime appointmentTime) {
        // Doktorun randevu saatleri
        List<Appointment> doctorAppointments = doctor.getAppointment();

        for (Appointment appointment : doctorAppointments) {
            LocalDateTime existingAppointmentDateTime = appointment.getAppointmentDate();

            // Sadece saat kontrolü yapılır
            if (existingAppointmentDateTime.toLocalTime().equals(appointmentTime)) {
                // Doktorun bu saatte başka bir randevusu var
                System.out.println("doktor dolu");
                return false;
            }
        }
        // Doktorun bu saatte başka bir randevusu yok
        System.out.println("doktor boş");
        return true;
    }
}
