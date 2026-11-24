package com.investlab.identity.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** ERD v1.1 users. 인증·토큰 발급은 별도 서비스의 책임입니다. */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "users_email_uq", columnNames = "email"),
    @UniqueConstraint(name = "users_nickname_uq", columnNames = "nickname")
}, check = @CheckConstraint(name = "users_state_check",
    constraint = "(status <> 'ACTIVE' OR nickname IS NOT NULL) AND failed_login_count >= 0"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(nullable = false, columnDefinition = "citext")
    private String email;

    // 원문 비밀번호가 아닌 PasswordEncoder로 생성한 해시만 전달합니다.
    @Getter(AccessLevel.NONE)
    @Column(name = "password_hash", columnDefinition = "text")
    private String passwordHash;

    @Column(length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "withdrawal_requested_at")
    private Instant withdrawalRequestedAt;

    @Column(name = "deletion_scheduled_at")
    private Instant deletionScheduledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private User(String email, String passwordHash, String nickname, UserStatus status) {
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.status = status;
        this.role = UserRole.USER;
    }

    public static User registerWithEmail(String email, String passwordHash, String nickname) {
        requireNonBlank(passwordHash, "passwordHash");
        requireNonBlank(nickname, "nickname");
        if (nickname.length() > 30) {
            throw new IllegalArgumentException("nickname must not exceed 30 characters");
        }
        return new User(email, passwordHash, nickname, UserStatus.PENDING_EMAIL);
    }

    public static User registerWithGoogle(String verifiedEmail) {
        return new User(verifiedEmail, null, null, UserStatus.PENDING_NICKNAME);
    }

    public static String normalizeEmail(String email) {
        requireNonBlank(email, "email");
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private static void requireNonBlank(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
