package me.cocoblue.passkeysample.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Entity
@Table(name = "application_setting")
@NoArgsConstructor
public class ApplicationSettingEntity {

  @Builder
  public ApplicationSettingEntity(String settingKey, String settingValue) {
    this.settingKey = settingKey;
    this.settingValue = settingValue;
  }

  @Id
  @Column(name = "setting_key", nullable = false)
  private String settingKey;

  @Setter
  @Column(name = "setting_value", nullable = false)
  private String settingValue;

}
