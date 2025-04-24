package me.cocoblue.passkeysample.domain.common;

import me.cocoblue.passkeysample.domain.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "objects", indexes = {
    @Index(name = "idx_objects_name", columnList = "name")
})
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "object_type")
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
public abstract class ObjectEntity {

  protected ObjectEntity(ObjectType objectType) {
    this.objectType = objectType;
  }

  @Id
  @GeneratedValue(generator = "uuid2")
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "id", nullable = false)
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(name = "object_type", insertable = false, updatable = false)
  private ObjectType objectType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private UserEntity ownerUserEntity;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "enabled", nullable = false, columnDefinition = "boolean default true")
  private boolean enabled;

  @Column(name = "deleted", nullable = false, columnDefinition = "boolean default false")
  private boolean deleted;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
