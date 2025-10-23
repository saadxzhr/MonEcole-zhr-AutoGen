// com/myschool/backend/utilisateur/model/BaseEntity.java
package com.szschoolmanager.auth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

  @CreationTimestamp
  @Column(name = "createdAt", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updatedAt")
  private LocalDateTime updatedAt;
}
