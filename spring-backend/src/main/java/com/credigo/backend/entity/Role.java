package com.credigo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_id")
  private Integer id;

  @Column(name = "role_name", nullable = false, unique = true, length = 50)
  private String roleName; // e.g., "USER", "ADMIN"

  // Relationship to UserRoles (inverse side)
  @OneToMany(mappedBy = "role")
  private Set<UserRole> userRoles;
}
