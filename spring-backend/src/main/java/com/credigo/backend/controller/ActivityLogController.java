package com.credigo.backend.controller;

import com.credigo.backend.dto.ActivityLogResponse;
import com.credigo.backend.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    
    @Autowired
    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ActivityLogResponse>> getUserLogs(@PathVariable Integer userId) {
        List<ActivityLogResponse> logs = activityLogService.getUserLogs(userId);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/user/{userId}/paged")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<ActivityLogResponse>> getUserLogsPaged(
            @PathVariable Integer userId, 
            Pageable pageable) {
        Page<ActivityLogResponse> logs = activityLogService.getUserLogs(userId, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/type/{activityType}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ActivityLogResponse>> getLogsByType(@PathVariable String activityType) {
        List<ActivityLogResponse> logs = activityLogService.getLogsByType(activityType);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ActivityLogResponse>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<ActivityLogResponse> logs = activityLogService.getLogsByDateRange(start, end);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/{logId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ActivityLogResponse> getLogById(@PathVariable Long logId) {
        ActivityLogResponse log = activityLogService.getLogById(logId);
        return ResponseEntity.ok(log);
    }
}
