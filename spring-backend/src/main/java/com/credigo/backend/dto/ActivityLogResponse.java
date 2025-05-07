package com.credigo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {
    
    private Long id;
    private Integer userId;
    private String username;
    private String activity;
    private String activityType;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;
}
