package me.cocoblue.passkeysample.security;

import me.cocoblue.passkeysample.domain.auth.PasskeyRecordsEntity;
import me.cocoblue.passkeysample.domain.auth.PasskeyRecordsRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.util.Assert;

@Log4j2
public class PassKeyCustomUserCredentialRepository implements UserCredentialRepository {

	private final PasskeyRecordsRepository passkeyRecordsRepository;

	public PassKeyCustomUserCredentialRepository(
			PasskeyRecordsRepository passkeyRecordsRepository) {
		this.passkeyRecordsRepository = passkeyRecordsRepository;
	}

	@Override
	public void delete(Bytes credentialId) {
		log.debug("PassKeyCustomUserCredentialRepository.delete Called.");
		Assert.notNull(credentialId, "credentialId cannot be null");
		passkeyRecordsRepository.deleteById(credentialId.toBase64UrlString());
	}

	/**
	 * /register 요청 시에 호출되는 함수 (Register - 4)
	 * 중복 확인까지 끝나면, 실제로 PassKey Records Entity 에 저장
	 *
	 * @see me.cocoblue.passkeysample.security.PassKeyCustomUserCredentialRepository#findByCredentialId(Bytes)
	 * @param credentialRecord WebAuthn4J에서 생성된 객체
	 */
	@Override
	public void save(CredentialRecord credentialRecord) {
		log.debug("PassKeyCustomUserCredentialRepository.save Called.");
		Assert.notNull(credentialRecord, "credentialRecord cannot be null");

		PasskeyRecordsEntity passkeyRecordsEntity = transform(credentialRecord);
		passkeyRecordsEntity.setUuid(UUID.randomUUID().toString());
		// TODO: 이 부분을 기존 user 테이블과 연동해야함.
		passkeyRecordsRepository.save(passkeyRecordsEntity);
	}

	/**
	 * /register 요청 시에 호출되는 함수 (Register - 3)
	 * 등록 시도한 Credential ID를 기반으로 중복 등록 여부를 확인
	 *
	 * @see me.cocoblue.passkeysample.security.PassKeyCustomUserCredentialRepository#findByUserId(Bytes)
	 * @param credentialId Credential ID
	 * @return List<CredentialRecordDto> 해당 유저가 가지고 있는 CredentialRecordDto 목록
	 */
	@Override
	public CredentialRecord findByCredentialId(Bytes credentialId) {
		log.debug("PassKeyCustomUserCredentialRepository.findByCredentialId Called. credentialId: {}", credentialId);
		Assert.notNull(credentialId, "credentialId cannot be null");
		Optional<PasskeyRecordsEntity> optionalCredentialRecord = passkeyRecordsRepository.findByCredentialId(credentialId.toBase64UrlString());
		log.debug("The Matching CredentialRecordDto exists: {}", optionalCredentialRecord.isPresent());

		return optionalCredentialRecord.map(this::transform).orElse(null);
	}

	/**
	 * 1번에서 찾은 PublicKeyCredentialUserEntity 를 기반으로 CredentialRecordDto 를 찾음. (Register - 3)
	 * PassKeyRecordsEntity 를 찾아서 CredentialRecordDto 로 변환하여 반환
	 * exclude 항목을 만들기 위해서 호출할 것으로 생각함.
	 *
	 * @param userId PassKey User ID
	 * @return List<CredentialRecordDto> 해당 유저가 가지고 있는 CredentialRecordDto 목록
	 */
	@Override
	public List<CredentialRecord> findByUserId(Bytes userId) {
		log.debug("PassKeyCustomUserCredentialRepository.findByUserId Called.");
		Assert.notNull(userId, "userId cannot be null");
		var credentialRecordEntities = passkeyRecordsRepository.findByUserId(userId.toBase64UrlString());
		return credentialRecordEntities.stream().map(this::transform).toList();
	}

	private CredentialRecord transform(PasskeyRecordsEntity passkeyRecordsEntity) {
		var transports = Stream.of(
				passkeyRecordsEntity
						.getTransports()
						.split(",")
		).map(AuthenticatorTransport::valueOf).collect(Collectors.toCollection(HashSet::new));
		return ImmutableCredentialRecord.builder()
				.credentialId(Bytes.fromBase64(passkeyRecordsEntity.getCredentialId()))
				.credentialType(PublicKeyCredentialType.valueOf(passkeyRecordsEntity.getCredentialType()))
				.attestationClientDataJSON(Bytes.fromBase64(passkeyRecordsEntity.getAttestationClientDataJSON()))
				.attestationObject(Bytes.fromBase64(passkeyRecordsEntity.getAttestationObject()))
				.backupEligible(passkeyRecordsEntity.isBackupEligible())
				.backupState(passkeyRecordsEntity.isBackupState())
				.created(Instant.ofEpochMilli(passkeyRecordsEntity.getCreated()))
				.lastUsed(Instant.ofEpochMilli(passkeyRecordsEntity.getLastUsed()))
				.label(passkeyRecordsEntity.getLabel())
				.userEntityUserId(Bytes.fromBase64(passkeyRecordsEntity.getUserId()))
				.publicKey(new ImmutablePublicKeyCose(Bytes.fromBase64(passkeyRecordsEntity.getPublicKey()).getBytes()))
				.signatureCount(passkeyRecordsEntity.getSignatureCount())
				.uvInitialized(passkeyRecordsEntity.isUvInitialized())
				.transports(transports)
				.build();
	}

	/**
	 * WebAuth4J에서 제공한 CredentialRecordDto 객체를 PasskeyRecordsEntity 로 변환
	 *
	 * @param credentialRecord WebAuth4J에서 제공한 객체
	 * @return PasskeyRecordsEntity 로 변환된 객체
	 */
	private PasskeyRecordsEntity transform(final CredentialRecord credentialRecord) {
		final String transports = credentialRecord.getTransports().stream()
				.map(AuthenticatorTransport::getValue)
				.collect(Collectors.joining(","));
		credentialRecord.getTransports().forEach(t -> log.debug("t is {}", t));

		return PasskeyRecordsEntity.builder()
				// PassKey 하나를 내부에서 분류하는 ID
				.credentialId(credentialRecord.getCredentialId().toBase64UrlString())
				// passkey_users 테이블의 ID
				// TODO: 이 부분을 기존 user 테이블과 연동해야함.
				.userId(credentialRecord.getUserEntityUserId().toBase64UrlString())
				// 생성일
				.created(credentialRecord.getCreated().toEpochMilli())
				// 마지막 사용일
				.lastUsed(credentialRecord.getLastUsed().toEpochMilli())
				// 라벨 (사용자가 지정)
				.label(credentialRecord.getLabel())
				// AttestationClientDataJSON
				.attestationClientDataJSON(credentialRecord.getAttestationClientDataJSON().toBase64UrlString())
				// AttestationObject
				.attestationObject(credentialRecord.getAttestationObject().toBase64UrlString())
				// 백업 가능 여부
				.backupEligible(credentialRecord.isBackupEligible())
				// 백업 상태
				.backupState(credentialRecord.isBackupState())
				// 서명 횟수 (사용한 횟수)
				.signatureCount(credentialRecord.getSignatureCount())
				// UV 초기화 여부
				.uvInitialized(credentialRecord.isUvInitialized())
				// 사용 가능한 트랜스포트
				.transports(transports)
				// Credential Type
				.credentialType(credentialRecord.getCredentialType().getValue())
				// 공개키
				.publicKey(new Bytes(credentialRecord.getPublicKey().getBytes()).toBase64UrlString())
				.build();
	}

}
