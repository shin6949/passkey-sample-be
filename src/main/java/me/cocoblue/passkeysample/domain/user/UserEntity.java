package me.cocoblue.passkeysample.domain.user;

import jakarta.persistence.*;
import me.cocoblue.passkeysample.domain.common.BaseTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class UserEntity extends BaseTime {

  @Builder
  public UserEntity(String id, String email, String password, String name, String profileUrl,
      boolean useGravatar, UserRole role, boolean enabled) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.name = name;
    this.profileUrl = profileUrl;
    this.useGravatar = useGravatar;
    this.role = role;
    this.enabled = enabled;
  }

  @Id
  @GeneratedValue(generator = "uuid2")
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "uuid", nullable = false)
  private String id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "profile_url")
  private String profileUrl;

  @Transient
  private String fullProfileUrl;

  @Column(name = "use_gravatar", nullable = false)
  @ColumnDefault("1")
  private boolean useGravatar = true;

  @Column(name = "role", nullable = false)
  @Enumerated(EnumType.STRING)
  private UserRole role;

  @Column(name = "enabled", nullable = false)
  @ColumnDefault("1")
  private boolean enabled = true;

  @Override
  public String toString() {
    return "UserEntity{" +
        "uuid='" + id + '\'' +
        ", email='" + email + '\'' +
        ", password=<DELETED>" +
        ", name='" + name + '\'' +
        ", profileUrl='" + profileUrl + '\'' +
        ", fullProfileUrl='" + fullProfileUrl + '\'' +
        ", useGravatar=" + useGravatar +
        ", role=" + role +
        ", enabled=" + enabled +
        '}';
  }
}
