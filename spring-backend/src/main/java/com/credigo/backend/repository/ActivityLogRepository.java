package com.credigo.backend.repository;

import com.credigo.backend.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    // Find logs by user ID
    List<ActivityLog> findByUserId(Integer userId);
    
    // Find paginated logs by user ID
    Page<ActivityLog> findByUserId(Integer userId, Pageable pageable);
    
    // Find logs by activity type
    List<ActivityLog> findByActivityType(String activityType);
    
    // Find logs between time range
    List<ActivityLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Find logs by user and activity type
    List<ActivityLog> findByUserIdAndActivityType(Integer userId, String activityType);
}
