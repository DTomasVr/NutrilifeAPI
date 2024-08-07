package com.nutrilife.fitnessservice.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.modelmapper.internal.bytebuddy.asm.Advice.Return;
import org.springframework.stereotype.Service;
import com.nutrilife.fitnessservice.exception.ResourceNotFoundException;
import com.nutrilife.fitnessservice.exception.UserNotFound;
import com.nutrilife.fitnessservice.mapper.MeetingMapper;
import com.nutrilife.fitnessservice.mapper.ScheduleMapper;
import com.nutrilife.fitnessservice.mapper.WeeklyScheduleMapper;
import com.nutrilife.fitnessservice.model.dto.MeetingRequestDTO;
import com.nutrilife.fitnessservice.model.dto.MeetingResponseDTO;
import com.nutrilife.fitnessservice.model.dto.ScheduleResponseDTO;
import com.nutrilife.fitnessservice.model.dto.WeeklyScheduleResponseDTO;
import com.nutrilife.fitnessservice.model.entity.CustomerProfile;
import com.nutrilife.fitnessservice.model.entity.Meeting;
import com.nutrilife.fitnessservice.model.entity.Schedule;
import com.nutrilife.fitnessservice.model.entity.SpecialistProfile;
import com.nutrilife.fitnessservice.model.entity.WeeklySchedule;
import com.nutrilife.fitnessservice.model.enums.MeetStatus;
import com.nutrilife.fitnessservice.model.enums.ScheduleStatus;
import com.nutrilife.fitnessservice.repository.CustomerProfileRepository;
import com.nutrilife.fitnessservice.repository.MeetingRepository;
import com.nutrilife.fitnessservice.repository.ScheduleRepository;
import com.nutrilife.fitnessservice.repository.SpecialistProfileRepository;

import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final ScheduleRepository scheduleRepository;
    private final SpecialistProfileRepository specialistProfileRepository;
    private final ScheduleMapper scheduleMapper;
    private final MeetingMapper meetingMapper;
    private final CustomerProfileRepository customerProfileRepository;
    
    private List<MeetingResponseDTO> getMeetingResponseDTOs(List<Meeting> meetingsList) {
        List<MeetingResponseDTO> meetingDTOs = meetingMapper.convertToListDTO(meetingsList);
        List<Long> specId = new ArrayList<>();
        List<Long> custmerId = new ArrayList<>();
        meetingsList.forEach(meeting->{
            if(meeting.getSchedule() != null){
                specId.add(meeting.getSchedule().getWeeklySchedule().getSpecialistProfile().getSpecId());
            }else{
                specId.add(null);
            }
            if (meeting.getCustomerProfile() != null) {
                custmerId.add(meeting.getCustomerProfile().getCustId());
            }else{custmerId.add(null);}
            
        });
        
        IntStream.range(0, meetingDTOs.size())
            .forEach(i -> {
                meetingDTOs.get(i).setSpecialistId(specId.get(i % specId.size()));
                meetingDTOs.get(i).setCustomerId(custmerId.get(i % custmerId.size())); 
            });

        return meetingDTOs;
    }
    
    ///CRUD 
    public List<MeetingResponseDTO> getAllMeetings() {
        List<Meeting> meetingsList = meetingRepository.findAll();
        return getMeetingResponseDTOs(meetingsList);
    }

    public MeetingResponseDTO getMeetingById(Long meetingId) {
        
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));
        return meetingMapper.convertToDTO(meeting);
    }


    public MeetingResponseDTO createMeeting(Long scheduleId,Long CustomerId) {
        
        CustomerProfile customerProfile = customerProfileRepository.findById(CustomerId)
        .orElseThrow(() -> new UserNotFound("Customer not found with id: " + CustomerId));
        
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));
        
        if (schedule.getStatus() != ScheduleStatus.ACTIVE) {
            throw new UserNotFound("Cannot create a meeting for a schedule that is not ACTIVE.");
        }
        if (schedule.getStatus() == ScheduleStatus.OCCUPIED){
            throw new UserNotFound("Cannot create a meeting for a schedule that is OCCUPIED.");
        }
        
        Meeting meeting = meetingMapper.convertToEntity(meetingMapper.createMeetingRequestDTO(schedule));
        meeting.setSchedule(schedule);
        meeting.setCustomerProfile(customerProfile);

        Meeting savedMeeting = meetingRepository.save(meeting);
        MeetingResponseDTO responseDTO = meetingMapper.convertToDTO(savedMeeting);
        responseDTO.setScheduleId(scheduleId);
        if(schedule.getWeeklySchedule()!= null){
            WeeklySchedule weeklySchedule = schedule.getWeeklySchedule();
            responseDTO.setSpecialistId(weeklySchedule.getSpecialistProfile().getSpecId());
        }else{responseDTO.setSpecialistId(null);}
        
        responseDTO.setCustomerId(CustomerId);
        schedule.setStatus(ScheduleStatus.OCCUPIED);
        schedule.setMeeting(savedMeeting);
        scheduleRepository.save(schedule);
        return responseDTO ;
    }

    public MeetingResponseDTO updateMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));
    
        // Convertir el status de la reunión a enum MeetStatus
        meeting.setStatus(MeetStatus.NO_ASSIST);
    
        Meeting updatedMeeting = meetingRepository.save(meeting);
        return meetingMapper.convertToDTO(updatedMeeting);
    }
    
    
    public void deleteMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));

        Schedule schedule = meeting.getSchedule();
        if (schedule != null) {
            // Desvincular el meeting del schedule y actualizar el estado del schedule
            meeting.setSchedule(null);
            if(meeting.getSchedule()==null){
                meetingRepository.save(meeting);
            }
             // Guardar los cambios en el meeting para desvincularlo del schedule
            schedule.setMeeting(null);
            schedule.setStatus(ScheduleStatus.ACTIVE);
            scheduleRepository.save(schedule); // Guardar los cambios en el schedule
        }
        
        meetingRepository.delete(meeting);
    }
        
    

    /////funciones especiales
    public List<ScheduleResponseDTO> getDailySchedulesForCurrentWeek(Long specialistId) {
        SpecialistProfile specialistProfile = specialistProfileRepository.findById(specialistId)
            .orElseThrow(() -> new UserNotFound("Perfil de especialista no encontrado con el ID: " + specialistId));
        
        // Obtener la fecha de inicio de la semana actual (lunes)
        LocalDate startOfWeek = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        // Obtener la fecha de fin de la semana actual (domingo)
        LocalDate endOfWeek = startOfWeek.plusDays(6);
    
        WeeklySchedule weeklySchedule = specialistProfile.getWeeklySchedules().stream()
        .filter(ws -> !ws.getStartDate().isBefore(startOfWeek) && !ws.getEndDate().isAfter(endOfWeek))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Weekly schedule not found for the current week"));

        List<Schedule> dailySchedules = weeklySchedule.getScheduleList().stream()
        .filter(schedule -> !schedule.getDate().isBefore(startOfWeek) && !schedule.getDate().isAfter(endOfWeek))
        .sorted(Comparator
                .comparing((Schedule s) -> s.getDate().getDayOfWeek())
                .thenComparing(Schedule::getStartTime))
        .collect(Collectors.toList());
        
        return scheduleMapper.convertToListDTO(dailySchedules);
    }
    
    public List<MeetingResponseDTO> getMeetingByCustomerId(Long customerId){
        CustomerProfile customerProfile = customerProfileRepository.findById(customerId)
        .orElseThrow(() -> new UserNotFound("Perfil de cliente no encontrado con el ID: " + customerId));
        
        List<Meeting> meetingsList = customerProfile.getMeetings();
        return getMeetingResponseDTOs(meetingsList);
    }

}
