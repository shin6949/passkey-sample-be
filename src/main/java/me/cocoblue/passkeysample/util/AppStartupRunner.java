package me.cocoblue.passkeysample.util;

import com.amazonaws.services.s3.AmazonS3;
import me.cocoblue.passkeysample.domain.common.ApplicationSettingEntity;
import me.cocoblue.passkeysample.domain.common.ApplicationSettingKey;
import me.cocoblue.passkeysample.domain.common.ApplicationSettingRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@DependsOn("amazonS3Client")
public class AppStartupRunner implements CommandLineRunner {
  private final ApplicationSettingRepository applicationSettingRepository;
  private final AmazonS3 amazonS3Client;

  public AppStartupRunner(ApplicationSettingRepository applicationSettingRepository,
      AmazonS3 amazonS3Client) {
    this.applicationSettingRepository = applicationSettingRepository;
    this.amazonS3Client = amazonS3Client;
  }

  @Override
  @Transactional
  public void run(String[] args) {
    log.info("App Startup Running Process Start.");
    log.info("Check the Environment Variables are set properly.");
    checkEnvironmentVariables();

    checkApplicationSetting();
    checkS3Config();

    log.info("Application Startup Process is Done. Application is Ready to Use.");
  }

  private void checkEnvironmentVariables() {
    final String accessPrivateKeyPath = System.getenv("APP_JWT_ACCESS_PRIVATE_KEY_PATH");
    final String refreshPrivateKeyPath = System.getenv("APP_JWT_REFRESH_PRIVATE_KEY_PATH");

    if (accessPrivateKeyPath == null || accessPrivateKeyPath.isEmpty()) {
      log.warn("APP_JWT_ACCESS_PRIVATE_KEY_PATH is not set. using default private key.\nBut it is HIGHLY DANGEROUS in production environment.");
    }

    if (refreshPrivateKeyPath == null || refreshPrivateKeyPath.isEmpty()) {
      log.warn("APP_JWT_REFRESH_PRIVATE_KEY_PATH is not set. using default private key.\nBut it is HIGHLY DANGEROUS in production environment.");
    }
  }

  @Transactional
  void checkApplicationSetting() {
    final List<ApplicationSettingKey> applicationSettingKeys = List.of(
        ApplicationSettingKey.values());
    final List<ApplicationSettingEntity> toInsertApplicationValues = new ArrayList<>();

    applicationSettingKeys.forEach(applicationSettingKey -> {
      if (applicationSettingRepository.findApplicationSettingEntityBySettingKeyEquals(
          applicationSettingKey.getKeyName()).isEmpty()) {
        log.info("Application Setting Key {} is not found. Create Default Value.",
            applicationSettingKey.getKeyName());
        toInsertApplicationValues.add(ApplicationSettingEntity.builder()
            .settingKey(applicationSettingKey.getKeyName())
            .settingValue(applicationSettingKey.getDefaultValue())
            .build());
      }
    });

    applicationSettingRepository.saveAll(toInsertApplicationValues);
    log.info("Application Setting Check and Insert Process is Done.");
  }

  @Transactional
  void checkS3Config() {
    Optional<ApplicationSettingEntity> s3SettingsOptional = applicationSettingRepository.findApplicationSettingEntityBySettingKeyEquals(
        ApplicationSettingKey.S3_ENABLED.getKeyName());
    ApplicationSettingEntity s3SettingEntity = s3SettingsOptional.orElse(null);

    if(s3SettingsOptional.isPresent()) {
      s3SettingEntity = s3SettingsOptional.get();
    } else {
      s3SettingEntity = ApplicationSettingEntity.builder()
          .settingKey(ApplicationSettingKey.S3_ENABLED.getKeyName())
          .settingValue(ApplicationSettingKey.S3_ENABLED.getDefaultValue())
          .build();
      applicationSettingRepository.save(s3SettingEntity);
    }

    if(amazonS3Client == null) {
      log.info("Amazon S3 client is not configured.");
      s3SettingEntity.setSettingValue("false");
    } else {
      log.info("Amazon S3 client is configured.");
      s3SettingEntity.setSettingValue("true");
    }

    applicationSettingRepository.save(s3SettingEntity);
  }
}
