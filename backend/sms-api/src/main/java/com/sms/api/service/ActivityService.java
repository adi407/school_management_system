package com.sms.api.service;

import com.sms.api.dto.activity.ActivityDto;
import com.sms.api.dto.activity.CreateActivityRequest;
import com.sms.api.entity.Activity;
import com.sms.api.repository.ActivityRepository;
import com.sms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> list(UUID schoolId) {
        return activityRepository.findBySchoolIdOrderByNameAsc(schoolId)
            .stream().map(this::toDto).toList();
    }

    public ActivityDto create(UUID schoolId, CreateActivityRequest req) {
        Activity a = new Activity();
        a.setSchoolId(schoolId);
        a.setName(req.name());
        a.setCategory(req.category());
        a.setCoach(req.coach());
        a.setSchedule(req.schedule());
        a.setCapacity(req.capacity() > 0 ? req.capacity() : 30);
        a.setStatus(req.status() != null ? req.status() : "ACTIVE");
        return toDto(activityRepository.save(a));
    }

    public ActivityDto update(UUID schoolId, UUID id, CreateActivityRequest req) {
        Activity a = activityRepository.findByIdAndSchoolId(id, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Activity", id));
        a.setName(req.name());
        a.setCategory(req.category());
        a.setCoach(req.coach());
        a.setSchedule(req.schedule());
        if (req.capacity() > 0) a.setCapacity(req.capacity());
        if (req.status() != null) a.setStatus(req.status());
        return toDto(activityRepository.save(a));
    }

    public void delete(UUID schoolId, UUID id) {
        Activity a = activityRepository.findByIdAndSchoolId(id, schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("Activity", id));
        activityRepository.delete(a);
    }

    private ActivityDto toDto(Activity a) {
        return new ActivityDto(a.getId(), a.getName(), a.getCategory(),
            a.getCoach(), a.getSchedule(), a.getCapacity(), a.getStatus());
    }
}
