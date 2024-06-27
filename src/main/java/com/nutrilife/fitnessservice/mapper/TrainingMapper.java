package com.nutrilife.fitnessservice.mapper;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.nutrilife.fitnessservice.model.dto.TrainingReportDTO;
import com.nutrilife.fitnessservice.model.dto.TrainingRequestDTO;
import com.nutrilife.fitnessservice.model.dto.TrainingResponseDTO;
import com.nutrilife.fitnessservice.model.dto.TrainingUpdateDTO;
import com.nutrilife.fitnessservice.model.entity.Training;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class TrainingMapper {
    private final ModelMapper modelMapper;

    public Training convertToEntity(TrainingRequestDTO trainingRequestDTO) {
        return modelMapper.map(trainingRequestDTO, Training.class);
    }

    public TrainingResponseDTO convertToDTO(Training training) {
        return modelMapper.map(training, TrainingResponseDTO.class);
    }

    public void updateEntityFromDTO(TrainingUpdateDTO dto, Training entity) {
        modelMapper.map(dto, entity);
    }

    public TrainingReportDTO convertTrainingReportDTO(Object[] result) {
        TrainingReportDTO dto = new TrainingReportDTO();
        dto.setCount((Long) result[0]);
        dto.setDate((LocalDate) result[1]);
        return dto;
    } 
}
