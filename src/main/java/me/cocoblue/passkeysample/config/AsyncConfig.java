package me.cocoblue.passkeysample.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean("s3UploadExecutor")
  public Executor s3Executor() {
    return new ThreadPoolTaskExecutor() {{
      setCorePoolSize(10);
      setMaxPoolSize(50);
      setQueueCapacity(100);
      setThreadNamePrefix("S3-Upload-");
    }};
  }

  @Bean("thumbnailExecutor")
  public Executor thumbnailExecutor() {
    return new ThreadPoolTaskExecutor() {{
      setCorePoolSize(5);
      setMaxPoolSize(10);
      setQueueCapacity(20);
      setThreadNamePrefix("Thumbnail-");
    }};
  }

  @Bean("deleteExecutor")
  public Executor deleteExecutor() {
    return new ThreadPoolTaskExecutor() {{
      setCorePoolSize(5);
      setMaxPoolSize(10);
      setQueueCapacity(20);
      setThreadNamePrefix("DeleteS3-");
    }};
  }
}
