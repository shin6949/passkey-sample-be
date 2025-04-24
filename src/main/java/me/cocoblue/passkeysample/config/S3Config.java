package me.cocoblue.passkeysample.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Log4j2
@Configuration
public class S3Config {

  @Value("${app.s3.access-key:}")
  private String accessKey;

  @Value("${app.s3.secret-key:}")
  private String secretKey;

  @Value("${app.s3.endpoint:}")
  private String endpoint;

  @Value("${app.s3.region:ap-northeast-2}")
  private String region;

  @Value("${app.s3.enable-path-style-access:true}")
  private String enablePathStyleAccessString;

  @Bean
  public AmazonS3 amazonS3Client() {
    // 필수 프로퍼티 체크
    if (!isS3Configured()) {
      log.error("There is no Amazon S3 configuration. Check your application properties.");
      return null;
    }

    final boolean pathStyleAccess = Boolean.parseBoolean(enablePathStyleAccessString);

    return AmazonS3ClientBuilder.standard()
        .withCredentials(new AWSStaticCredentialsProvider(
            new BasicAWSCredentials(accessKey, secretKey)))
        .withEndpointConfiguration(new EndpointConfiguration(endpoint, region))
        .withPathStyleAccessEnabled(pathStyleAccess)
        .build();
  }

  private boolean isS3Configured() {
    log.debug("Checking Amazon S3 configuration");

    return StringUtils.hasText(accessKey) &&
        StringUtils.hasText(secretKey) &&
        StringUtils.hasText(endpoint);
  }
}