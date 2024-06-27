package com.nutrilife.fitnessservice.model.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingReportDTO {
    private LocalDate date;
    private Long count;
}
