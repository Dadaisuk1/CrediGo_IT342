package com.credigo.backend.entity;

// Optional: For tracking external top-up API status explicitly
public enum ExternalApiStatus {
  INITIATED,
  PROCESSING,
  SUCCESS,
  FAILED,
  UNKNOWN
}
