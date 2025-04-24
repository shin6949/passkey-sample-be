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
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "audit_log")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "object_type")
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
public abstract class AuditLogEntity {

  @Id
  @GeneratedValue(generator = "uuid2")
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "id", nullable = false)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_user_id", nullable = false)
  private UserEntity actionUserEntity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_object_id", nullable = false)
  private ObjectEntity targetObjectEntity;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_type", nullable = false)
  private AuditLogActionType actionType;

  @CreationTimestamp
  @Column(name = "action_time", nullable = false, updatable = false)
  private LocalDateTime actionTime;

}
