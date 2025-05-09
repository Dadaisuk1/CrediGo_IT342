package com.credigo.backend.service;

import com.credigo.backend.dto.ActivityLogResponse;
import com.credigo.backend.entity.ActivityLog;
import com.credigo.backend.entity.User;
import com.credigo.backend.repository.ActivityLogRepository;
import com.credigo.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogServiceImpl.class);
    
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public ActivityLogServiceImpl(ActivityLogRepository activityLogRepository, UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional
    public ActivityLog logActivity(Integer userId, String activity, String activityType, String details, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        ActivityLog activityLog = new ActivityLog();
        activityLog.setUser(user);
        activityLog.setActivity(activity);
        activityLog.setActivityType(activityType);
        activityLog.setDetails(details);
        activityLog.setIpAddress(ipAddress);
        
        ActivityLog savedLog = activityLogRepository.save(activityLog);
        log.info("Activity logged for user {}: {}", userId, activity);
        return savedLog;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getUserLogs(Integer userId) {
        List<ActivityLog> logs = activityLogRepository.findByUserId(userId);
        return logs.stream()
                .map(this::mapToActivityLogResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getUserLogs(Integer userId, Pageable pageable) {
        return activityLogRepository.findByUserId(userId, pageable)
                .map(this::mapToActivityLogResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getLogsByType(String activityType) {
        List<ActivityLog> logs = activityLogRepository.findByActivityType(activityType);
        return logs.stream()
                .map(this::mapToActivityLogResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        List<ActivityLog> logs = activityLogRepository.findByCreatedAtBetween(start, end);
        return logs.stream()
                .map(this::mapToActivityLogResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ActivityLogResponse getLogById(Long logId) {
        ActivityLog log = activityLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Activity log not found with id: " + logId));
        return mapToActivityLogResponse(log);
    }
    
    @Override
    public ActivityLogResponse mapToActivityLogResponse(ActivityLog activityLog) {
        ActivityLogResponse dto = new ActivityLogResponse();
        dto.setId(activityLog.getId());
        dto.setUserId(activityLog.getUser().getId());
        dto.setUsername(activityLog.getUser().getUsername());
        dto.setActivity(activityLog.getActivity());
        dto.setActivityType(activityLog.getActivityType());
        dto.setDetails(activityLog.getDetails());
        dto.setIpAddress(activityLog.getIpAddress());
        dto.setCreatedAt(activityLog.getCreatedAt());
        return dto;
    }
}
