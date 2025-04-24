package me.cocoblue.passkeysample.security;

import me.cocoblue.passkeysample.domain.auth.PasskeyUserEntity;
import me.cocoblue.passkeysample.domain.auth.PasskeyUserRepository;
import me.cocoblue.passkeysample.domain.user.UserEntity;
import me.cocoblue.passkeysample.domain.user.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.util.Assert;

@Log4j2
@RequiredArgsConstructor
public class PassKeyPublicKeyCredentialUserEntityRepository implements PublicKeyCredentialUserEntityRepository {

	private final PasskeyUserRepository passkeyUserRepository;
	private final UserRepository userRepository;

	@Override
	public PublicKeyCredentialUserEntity findById(Bytes id) {
		log.debug("PassKeyPublicKeyCredentialUserEntityRepository.findById: {}", id);
		Assert.notNull(id, "id cannot be null");
		final Optional<PasskeyUserEntity> userPasskeyEntityOptional = passkeyUserRepository.findByPassKeyUserId(id.toBase64UrlString());
		if (userPasskeyEntityOptional.isEmpty()) {
			log.error("PasskeyUserEntity is not found. id: {}", id);
			return null;
		}

		return toPublicKeyCredentialUserEntity(userPasskeyEntityOptional.get(), userPasskeyEntityOptional.get()
				.getUserId());
	}

	/**
	 * Register Option을 요청 시에 username 으로 PassKeyUserEntity 를 찾는다. (Register - 1)
	 * exclude 항목을 만들기 위해서 호출할 것으로 생각함.
	 * User를 찾은 후, PublicKeyCredentialUserEntity로 변환하여 반환한다.
	 *
	 * @param username 사용자 이름
	 * @return PublicKeyCredentialUserEntity
	 */
	@Override
	public PublicKeyCredentialUserEntity findByUsername(String username) {
		log.debug("PassKeyPublicKeyCredentialUserEntityRepository.findByUsername: {}", username);
		Assert.notNull(username, "username cannot be null");
		Optional<PasskeyUserEntity> userPasskeyEntityOptional = passkeyUserRepository.findByUserId_Email(username);
		userPasskeyEntityOptional.ifPresent(
				passkeyUserEntity -> log.debug("username: {}", passkeyUserEntity.getUserId().getEmail()));

    return userPasskeyEntityOptional.map(
        passkeyUserEntity -> toPublicKeyCredentialUserEntity(passkeyUserEntity, passkeyUserEntity
            .getUserId())).orElse(null);
	}

	@Override
	public void save(PublicKeyCredentialUserEntity publicKeyCredentialUserEntity) {
		log.debug("PassKeyPublicKeyCredentialUserEntityRepository.save Called!");

		final Optional<UserEntity> userEntityOptional = userRepository.findByEmail(publicKeyCredentialUserEntity.getName());
		if (userEntityOptional.isEmpty()) {
			log.error("UserEntity is not found. username: {}", publicKeyCredentialUserEntity.getName());
			return;
		}

    final PasskeyUserEntity passkeyUserEntity = toPassKeyUserEntity(publicKeyCredentialUserEntity, userEntityOptional.get());
		this.passkeyUserRepository.save(passkeyUserEntity);
	}

	@Override
	public void delete(Bytes id) {
		this.passkeyUserRepository.deleteById(id.toBase64UrlString());
	}

	private PasskeyUserEntity toPassKeyUserEntity(PublicKeyCredentialUserEntity input, UserEntity userEntity) {
		return PasskeyUserEntity.builder()
				.passKeyUserId(input.getId().toBase64UrlString())
				.userId(userEntity)
				.build();
	}

	private PublicKeyCredentialUserEntity toPublicKeyCredentialUserEntity(PasskeyUserEntity input, UserEntity userEntity) {
		return ImmutablePublicKeyCredentialUserEntity
				.builder()
				// PassKey User ID
				.id(Bytes.fromBase64(input.getPassKeyUserId()))
				.name(userEntity.getEmail())
				.displayName(userEntity.getName())
				.build();
	}
}