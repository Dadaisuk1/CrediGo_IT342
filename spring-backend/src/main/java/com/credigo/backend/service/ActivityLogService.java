package com.credigo.backend.service;

import com.credigo.backend.dto.ActivityLogResponse;
import com.credigo.backend.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogService {
    
    // Log a user activity
    ActivityLog logActivity(Integer userId, String activity, String activityType, String details, String ipAddress);
    
    // Get logs for a specific user
    List<ActivityLogResponse> getUserLogs(Integer userId);
    
    // Get paginated logs for a specific user
    Page<ActivityLogResponse> getUserLogs(Integer userId, Pageable pageable);
    
    // Get logs by type
    List<ActivityLogResponse> getLogsByType(String activityType);
    
    // Get logs within a date range
    List<ActivityLogResponse> getLogsByDateRange(LocalDateTime start, LocalDateTime end);
    
    // Get activity log by ID
    ActivityLogResponse getLogById(Long logId);
    
    // Map entity to DTO
    ActivityLogResponse mapToActivityLogResponse(ActivityLog activityLog);
}
