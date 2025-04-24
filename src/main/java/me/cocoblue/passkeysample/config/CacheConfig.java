package me.cocoblue.passkeysample.config;

import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@Configuration
public class CacheConfig {
  @Bean
  public CacheManager cacheManager() {
    ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
    cacheManager.setAllowNullValues(false);
    cacheManager.setCacheNames(
        List.of(
        "users", "users_api_resp", "email_check_result", "profile_users", "revoked_tokens",
        "agent", "agents_list", "site_parent", "site_parent_list",
            "site_parent_count_by_agent_id",
            "site_group_count_by_agent_id",
            "noti_base_list",
            "noti_mattermost_webhook", "noti_mattermost_webhook_url"
        )
    );

    return cacheManager;
  }
}
