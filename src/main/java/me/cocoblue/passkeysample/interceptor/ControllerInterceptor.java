package me.cocoblue.passkeysample.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

@Log4j2
@Component
public class ControllerInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) throws IOException {
    log.info("Request URI: {}", request.getRequestURI());

    // Debug 시, Header와 Body를 로깅
    logHeaders(request);
    logBody(request);

    return true;
  }

  private void logHeaders(HttpServletRequest request) {
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      String headerValue = request.getHeader(headerName);
      log.debug("Header: {} = {}", headerName, headerValue);
    }
  }

  private void logBody(HttpServletRequest request) throws IOException {
    ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
    if (wrapper == null) {
      wrapper = new ContentCachingRequestWrapper(request);
    }

    byte[] buf = wrapper.getContentAsByteArray();
    if (buf.length > 0) {
      String body = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
      log.debug("Request Body: {}", body);
    }
  }
}
