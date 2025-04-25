package com.credigo.backend.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // Important for composite keys
@Embeddable
public class UserRoleId implements Serializable {
  private Integer userId;
  private Integer roleId;
}
