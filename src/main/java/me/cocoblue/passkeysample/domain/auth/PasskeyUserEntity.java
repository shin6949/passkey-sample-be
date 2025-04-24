package me.cocoblue.passkeysample.domain.auth;

import me.cocoblue.passkeysample.domain.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "passkey_users", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id", unique = true)
})
public class PasskeyUserEntity {

    @Builder
    public PasskeyUserEntity(String passKeyUserId, UserEntity userId) {
        this.passKeyUserId = passKeyUserId;
        this.userId = userId;
    }

    @Id
    @Column(name = "passkey_user_id", nullable = false)
    private String passKeyUserId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "uuid")
    private UserEntity userId;
}
