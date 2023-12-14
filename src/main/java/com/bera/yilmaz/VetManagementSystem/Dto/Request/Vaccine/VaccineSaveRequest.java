package com.bera.yilmaz.VetManagementSystem.Dto.Request.Vaccine;

import com.bera.yilmaz.VetManagementSystem.Entitiy.Animal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VaccineSaveRequest {
    private String name;
    private String code;
    private LocalDate protectionStratDate;
    private LocalDate protectionFinishDate;
    private Animal animal;
}
