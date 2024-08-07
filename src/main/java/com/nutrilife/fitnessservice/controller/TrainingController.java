package com.nutrilife.fitnessservice.controller;

import com.nutrilife.fitnessservice.model.dto.*;
import com.nutrilife.fitnessservice.service.TrainingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trainings")
@AllArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingResponseDTO createTraining(@RequestBody TrainingRequestDTO trainingRequestDTO) {
        return trainingService.createTraining(trainingRequestDTO);
    }

    @GetMapping("/{id}")
    public TrainingResponseDTO getTrainingById(@PathVariable Long id) {
        return trainingService.getTrainingById(id);
    }

    @GetMapping
    public List<TrainingResponseDTO> getAllTrainings() {
        return trainingService.getAllTrainings();
    }

    @PutMapping("/{id}")
    public TrainingResponseDTO updateTraining(@PathVariable Long id, @RequestBody TrainingUpdateDTO trainingUpdateDTO) {
        return trainingService.updateTraining(id, trainingUpdateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTraining(@PathVariable Long id) {
        trainingService.deleteTraining(id);
    }

    @GetMapping("/type/{type}")
    public List<TrainingResponseDTO> getTrainingsByExerciseType(@PathVariable String type) {
        return trainingService.getTrainingsByExerciseType(type);
    }

    @GetMapping("/name/{name}")
    public List<TrainingResponseDTO> getTrainingsByName(@PathVariable String name) {
        return trainingService.getTrainingsByName(name);
    }

    @GetMapping("/goal/{goal}")
    public List<TrainingResponseDTO> getTrainingsByPhysicalGoal(@PathVariable String goal) {
        return trainingService.getTrainingsByPhysicalGoal(goal);
    }

    @GetMapping("/duration/{duration}")
    public List<TrainingResponseDTO> getTrainingsByDuration(@PathVariable float duration) {
        return trainingService.getTrainingsByDuration(duration);
    }

    @GetMapping("/kcalories/{kCalories}")
    public List<TrainingResponseDTO> getTrainingsByKCalories(@PathVariable float kCalories) {
        return trainingService.getTrainingsByKCalories(kCalories);
    }

    @GetMapping("/report")
    public List<TrainingReportDTO> generateTrainingReport(@RequestParam String startDate, @RequestParam String endDate) {
        return trainingService.generateTrainingReport(startDate, endDate);
    }
}
