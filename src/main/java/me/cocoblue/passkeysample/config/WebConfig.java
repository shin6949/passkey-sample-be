package me.cocoblue.passkeysample.config;

import com.google.common.base.CaseFormat;
import me.cocoblue.passkeysample.interceptor.ControllerInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final ControllerInterceptor controllerInterceptor;

  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor(controllerInterceptor);
  }

  /**
    Controller에서 @ModelAttribute를 사용하여 RequestParam을 받을 때, CamelCase로 데이터가 받아지지 않기에,
    Config에서 변환하는 코드를 추가함.

    출처: https://cocococo331.tistory.com/12
   */
  @Bean
  public OncePerRequestFilter snakeCaseConverterFilter() {
    return new OncePerRequestFilter() {
      @Override
      protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
          FilterChain filterChain) throws ServletException, IOException {

        final Map<String, String[]> parameters = new ConcurrentHashMap<>();

        // 파라미터의 키 값을 snake_case에서 camelCase 변환 후 맵에 값을 가지고 있음
        for (String param : request.getParameterMap().keySet()) {
          String camelCaseParam = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, param);

          parameters.put(camelCaseParam, request.getParameterValues(param));
          parameters.put(param, request.getParameterValues(param));
        }

        // 필터체인을 이용하여, request에 해당 값을 추가하여 반환
        filterChain.doFilter(new HttpServletRequestWrapper(request) {

          @Override
          public String getParameter(String name) {
            return parameters.containsKey(name) ? parameters.get(name)[0] : null;
          }

          @Override
          public Enumeration<String> getParameterNames() {
            return Collections.enumeration(parameters.keySet());
          }

          @Override
          public String[] getParameterValues(String name) {
            return parameters.get(name);
          }

          @Override
          public Map<String, String[]> getParameterMap() {
            return parameters;
          }

        }, response);
      }
    };
  }

  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeHeaders(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setMaxPayloadLength(64000);
    return loggingFilter;
  }
}