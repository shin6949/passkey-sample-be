package me.cocoblue.passkeysample.service.common;

import static com.google.common.io.Files.getFileExtension;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import me.cocoblue.passkeysample.domain.common.ApplicationSettingEntity;
import me.cocoblue.passkeysample.domain.common.ApplicationSettingKey;
import me.cocoblue.passkeysample.domain.common.ApplicationSettingRepository;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
@RequiredArgsConstructor
public class S3Service {
  private final AmazonS3 amazonS3;
  private final ApplicationSettingRepository applicationSettingRepository;

  @Value("${app.s3.endpoint:}")
  private String s3Endpoint;

  @Value("${app.s3.bucket-name:}")
  private String s3BucketName;

  @Value("${app.static-url:}")
  private String s3StaticUrl;

  @Value("${app.file.allow-to-upload-type}")
  private String allowToUploadTypeString;

  private Set<String> allowToUploadType;

  @PostConstruct
  protected void init() {
    allowToUploadType = Set.of(allowToUploadTypeString.split(","));

    if (s3Endpoint == null) {
      s3Endpoint = "";
    }

    if (s3BucketName == null) {
      s3BucketName = "";
    }

    if (s3StaticUrl == null) {
      s3StaticUrl = "";
    }

    if (s3StaticUrl.endsWith("/")) {
      s3StaticUrl = s3StaticUrl.substring(0, s3StaticUrl.length() - 1);;
    }

    if (s3Endpoint.endsWith("/")) {
      s3Endpoint += s3Endpoint.substring(0, s3Endpoint.length() - 1);;
    }
  }

  @Value("${app.s3.bucket-name}")
  private String bucketName;

  @Async("s3UploadExecutor")
  public CompletableFuture<Void> uploadOriginalFileAsync(final MultipartFile file, final String directory, final String fileName) throws IOException {
    log.info("Uploading original file: {}", fileName);
    if(amazonS3 == null) {
      log.error("File upload is not available. The reason is that Amazon S3 client is not configured. Check your application properties.");
      throw new IllegalStateException("Amazon S3 client is not configured");
    }

    final String normalizedDir = directory.endsWith("/") ? directory : directory + "/";
    final String fullKey = normalizedDir + fileName;

    final ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getSize());
    metadata.setContentType(file.getContentType());

    try (InputStream inputStream = file.getInputStream()) {
      final PutObjectRequest putRequest = new PutObjectRequest(
          bucketName,
          fullKey,
          inputStream,
          metadata
      );
      putRequest.setCannedAcl(CannedAccessControlList.AuthenticatedRead);

      amazonS3.putObject(putRequest);

      return CompletableFuture.completedFuture(null);
    }
  }

  @Async("deleteExecutor")
  public CompletableFuture<Void> deleteFile(final String fullKey) {
    final DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(
        bucketName,
        fullKey
    );

    amazonS3.deleteObject(deleteObjectRequest);

    return CompletableFuture.completedFuture(null);
  }

  public String uploadProfileResizedFile(byte[] webpData, final String directory, final String fileName) throws IOException {
    log.info("Uploading profile resized file: {}", fileName);
    if(amazonS3 == null) {
      log.error("File upload is not available. The reason is that Amazon S3 client is not configured. Check your application properties.");
      throw new IllegalStateException("Amazon S3 client is not configured");
    }

    final String normalizedDir = directory.endsWith("/") ? directory : directory + "/";
    final String fullKey = normalizedDir + fileName;

    final ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(webpData.length);
    metadata.setContentType("image/webp");

    final PutObjectRequest putRequest = new PutObjectRequest(
        bucketName,
        fullKey,
        new ByteArrayInputStream(webpData),
        metadata
    );

    amazonS3.putObject(putRequest);

    return fullKey;
  }

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) throw new IllegalArgumentException("Empty file");

    String extension = getFileExtension(file.getOriginalFilename());
    if (!allowToUploadType.contains(extension.toLowerCase())) {
      throw new IllegalArgumentException("Invalid file type");
    }
  }

  public String configureS3URL(final String relativeUrl) {
    final ApplicationSettingEntity applicationSettingEntity = applicationSettingRepository.findApplicationSettingEntityBySettingKeyEquals(
        ApplicationSettingKey.S3_ENABLED.getKeyName()
    ).orElseGet(() -> ApplicationSettingEntity.builder()
        .settingKey(ApplicationSettingKey.S3_ENABLED.getKeyName())
        .settingValue(ApplicationSettingKey.S3_ENABLED.getDefaultValue())
        .build()
    );

    // S3이 구성되지 않은 상황이라면 null을 반환
    if(applicationSettingEntity.getSettingValue().equals("false")) {
      return null;
    }

    // S3이 구성되지 않은 상황이라면 null을 반환
    if(s3StaticUrl.isEmpty() && s3Endpoint.isEmpty()) {
      return null;
    }

    String prefix = "";

    if(s3StaticUrl.isEmpty()) {
      prefix = String.format("%s/%s", s3Endpoint, s3BucketName);
    } else {
      prefix = String.format("%s/%s", s3StaticUrl, s3BucketName);
    }

    return prefix + relativeUrl;
  }
}
