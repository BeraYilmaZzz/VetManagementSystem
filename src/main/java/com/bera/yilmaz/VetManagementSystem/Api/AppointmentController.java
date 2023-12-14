package com.bera.yilmaz.VetManagementSystem.Api;

import com.bera.yilmaz.VetManagementSystem.Business.Abs.IAppointmentService;
import com.bera.yilmaz.VetManagementSystem.Business.Abs.IDoctorService;
import com.bera.yilmaz.VetManagementSystem.Core.Config.ModelMapper.IModelMapperService;
import com.bera.yilmaz.VetManagementSystem.Core.Result.ResultData;
import com.bera.yilmaz.VetManagementSystem.Core.Utilies.ResultHelper;
import com.bera.yilmaz.VetManagementSystem.Dto.Request.Appointment.AppointmentSaveRequest;
import com.bera.yilmaz.VetManagementSystem.Dto.Response.AppointmentResponse;
import com.bera.yilmaz.VetManagementSystem.Entitiy.Appointment;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/appointments")
public class AppointmentController {
    private final IDoctorService doctorService;
    private final IAppointmentService appointmentService;
    private final IModelMapperService modelMapper;

    public AppointmentController(IDoctorService doctorService, IAppointmentService appointmentService, IModelMapperService modelMapper) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.modelMapper = modelMapper;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResultData<AppointmentResponse> save(@RequestBody @Valid AppointmentSaveRequest request) {
        // Randevu için gelen talep verileri
        LocalDateTime appointmentDate = request.getAppointmentDate();
        Long doctorId = request.getDoctor().getId(); // veya direkt olarak Doctor nesnesini kullanabilirsiniz
        // Doktorun uygun olduğu tarihleri kontrol et
        AppointmentResponse response = null;
        if (!doctorService.isDoctorAvailable(doctorId, appointmentDate)) {
            return ResultHelper.notAvailable(response);
            // Doktor uygun değilse hata mesajı döndür
        } else {
            // Doktor uygunsa işlemi devam ettir
            Appointment appointmentToSave = modelMapper.forRequest().map(request, Appointment.class);
            Appointment savedAppointment = appointmentService.save(appointmentToSave);
            response = modelMapper.forResponse().map(savedAppointment, AppointmentResponse.class);
            return ResultHelper.created(response);
        }
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResultData<Appointment> update(@PathVariable long id , @RequestBody @Valid Appointment appointment) {
        Appointment updateApp = appointmentService.update(id,appointment);
        return ResultHelper.success(updateApp);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public ResultData<List<Appointment>> findAll() {
        List<Appointment> appointments = appointmentService.getAll();
        return ResultHelper.success(appointments);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResultData<Appointment> delete(@PathVariable long id) {
        Appointment deletedAppointment = appointmentService.delete(id);
        Appointment response = modelMapper.forResponse().map(deletedAppointment, Appointment.class);
        return ResultHelper.success(response);
    }
    @GetMapping("/get/doctor-date/{doctorId}/{date}")
    @ResponseStatus(HttpStatus.OK)
    public ResultData<List<AppointmentResponse>> findAllByDoctorAndDate(
            @PathVariable long doctorId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        List<AppointmentResponse> responseList = appointmentService.getAppointmentsByDateAndDoctor(
                        startOfDay,
                        endOfDay,
                        doctorId)
                .stream()
                .map(appointment -> modelMapper.forResponse().map(appointment, AppointmentResponse.class))
                .collect(Collectors.toList());
        return ResultHelper.success(responseList);
    }

    @GetMapping("/filter_date_and_animal/{animalId}/{date}")
    @ResponseStatus(HttpStatus.OK)
    public ResultData<List<Appointment >> findAllByAnimalAndDate(
            @PathVariable long animalId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        List<Appointment> responseList = appointmentService.getAppointmentsByDateAndAnimalId(startOfDay,endOfDay,animalId)
                .stream()
                .map(appointment -> modelMapper.forResponse().map(appointment, Appointment.class))
                .collect(Collectors.toList());
        return ResultHelper.success(responseList);
    }
}