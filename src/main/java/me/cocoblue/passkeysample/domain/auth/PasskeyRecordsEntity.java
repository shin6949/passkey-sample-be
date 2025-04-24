package me.cocoblue.passkeysample.domain.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "passkey_records", indexes = {
    @Index(name = "idx_passkey_records_user_id", columnList = "userId"),
    @Index(name = "idx_passkey_records_public_key", columnList = "publicKey"),
})
public class PasskeyRecordsEntity {

    @Builder
    public PasskeyRecordsEntity(String uuid, String credentialId, String credentialType, String userId, String publicKey, long signatureCount, boolean uvInitialized, String transports, boolean backupEligible, boolean backupState, String attestationObject, String attestationClientDataJSON, Long created, Long lastUsed, String label) {
        this.uuid = uuid;
        this.credentialId = credentialId;
        this.credentialType = credentialType;
        this.userId = userId;
        this.publicKey = publicKey;
        this.signatureCount = signatureCount;
        this.uvInitialized = uvInitialized;
        this.transports = transports;
        this.backupEligible = backupEligible;
        this.backupState = backupState;
        this.attestationObject = attestationObject;
        this.attestationClientDataJSON = attestationClientDataJSON;
        this.created = created;
        this.lastUsed = lastUsed;
        this.label = label;
    }

    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "uuid", nullable = false, unique = true)
    private String uuid;

    @Id
    @Column(name = "credential_id", nullable = false, unique = true)
    private String credentialId;

    private String credentialType;

    private String userId;

    @Column(columnDefinition = "TEXT")
    private String publicKey;

    private long signatureCount;

    private boolean uvInitialized;

    private String transports;

    private boolean backupEligible;

    private boolean backupState;

    @Column(columnDefinition = "TEXT")
    private String attestationObject;

    @Column(columnDefinition = "TEXT")
    private String attestationClientDataJSON;

    private Long created;

    private Long lastUsed;

    private String label;

    @Override
    public String toString() {
        return "PasskeyCredentialRecordsEntity{" +
            "uuid='" + uuid + '\'' +
            ", credentialId='" + credentialId + '\'' +
            ", credentialType='" + credentialType + '\'' +
            ", userId='" + userId + '\'' +
            ", signatureCount=" + signatureCount +
            ", uvInitialized=" + uvInitialized +
            ", transports='" + transports + '\'' +
            ", backupEligible=" + backupEligible +
            ", backupState=" + backupState +
            ", attestationObject='" + attestationObject + '\'' +
            ", attestationClientDataJSON='" + attestationClientDataJSON + '\'' +
            ", created=" + created +
            ", lastUsed=" + lastUsed +
            ", label='" + label + '\'' +
            '}';
    }
}
