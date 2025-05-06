# Passkey Sample Application BE
Passkey 인증을 사용한 샘플 App의 백엔드입니다.

## Tech Stack
- JDK 17+
- Spring Boot 3.4.2
- Spring Data JPA
- Spring Security (WebAuthn4J 포함)
- MySQL 8
- JJWT (JWT)
- AWS SDK for Java (S3)

## Database
이 App은 MySQL을 기반으로 만들어 졌으며, 다른 DB에서 사용하고 싶다면, application.yml 파일을 적절하게 수정하세요.

## Environment Variables
- APP_DB_HOST: 데이터베이스 호스트 주소
- APP_DB_PORT: 데이터베이스 포트 번호
- APP_DB_NAME: 데이터베이스 이름
- APP_DB_USER: 데이터베이스 사용자 이름
- APP_DB_PASSWORD: 데이터베이스 비밀번호
- APP_JWT_ACCESS_PRIVATE_KEY_PATH: JWT Access Token 개인 키 파일 경로 (기본값: classpath:default_access_private_key.pem)
- APP_JWT_REFRESH_PRIVATE_KEY_PATH: JWT Refresh Token 개인 키 파일 경로 (기본값: classpath:default_refresh_private_key.pem)
- APP_JWT_ACCESS_TOKEN_EXPIRATION: JWT Access Token 만료 시간 (밀리초, 기본값: 900000)
- APP_JWT_REFRESH_TOKEN_EXPIRATION: JWT Refresh Token 만료 시간 (밀리초, 기본값: 86400000)
- APP_S3_ENDPOINT: S3 호환 스토리지 엔드포인트 URL
- APP_S3_ACCESS_KEY: S3 액세스 키
- APP_S3_SECRET_KEY: S3 시크릿 키
- APP_S3_REGION: S3 리전 (기본값: ap-northeast-2)
- APP_S3_BUCKET_NAME: S3 버킷 이름
- APP_S3_ENABLE_PATH_STYLE_ACCESS: S3 경로 스타일 액세스 활성화 여부 (기본값: true)
- APP_FILE_ALLOW_TO_UPLOAD: 업로드 허용 파일 확장자 목록 (기본값: .png,.jpg,.jpeg,.gif,.webp,.svg)
- APP_BASE_URL: 애플리케이션 기본 URL (기본값: http://localhost:8080)
- APP_STATIC_URL: 정적 파일 제공 URL (S3 프록시 등)

## Frontend
이 App은 별도의 [이 Repo의 FE](https://github.com/shin6949/passkey-sample-fe)와 함께 구동되어야 하나로 사용할 수 있습니다.