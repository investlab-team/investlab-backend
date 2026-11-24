package com.investlab.identity.repository;

import com.investlab.identity.domain.User;
import com.investlab.identity.domain.UserRole;
import com.investlab.identity.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:identity_repository;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("local")
@ContextConfiguration(classes = UserRepositoryTests.JpaConfig.class)
class UserRepositoryTests {
    @Configuration(proxyBeanMethods = false)
    @EntityScan(basePackageClasses = User.class)
    @EnableJpaRepositories(basePackageClasses = UserRepository.class)
    static class JpaConfig {}

    @Autowired UserRepository users;
    @Autowired jakarta.persistence.EntityManager entityManager;

    @Test void savesAndReloadsEmailUserWithDefaults() {
        var saved = users.saveAndFlush(User.registerWithEmail("  Member@Example.com  ", "encoded-hash", "회원"));
        var id = saved.getId();
        entityManager.clear();
        var reloaded = users.findById(id).orElseThrow();
        assertThat(id.version()).isEqualTo(7);
        assertThat(reloaded.getEmail()).isEqualTo("member@example.com");
        assertThat(reloaded.getStatus()).isEqualTo(UserStatus.PENDING_EMAIL);
        assertThat(reloaded.getRole()).isEqualTo(UserRole.USER);
        assertThat(reloaded.getFailedLoginCount()).isZero();
        assertThat(reloaded.getCreatedAt()).isNotNull();
        assertThat(reloaded.getUpdatedAt()).isNotNull();
        assertThat(reloaded.getLockedUntil()).isNull();
        assertThat(reloaded.getWithdrawalRequestedAt()).isNull();
        assertThat(reloaded.getDeletionScheduledAt()).isNull();
        assertThat(users.findByEmailIgnoreCase("MEMBER@EXAMPLE.COM")).isPresent();
        assertThat(users.existsByEmailIgnoreCase("MEMBER@EXAMPLE.COM")).isTrue();
        assertThat(users.existsByNickname("회원")).isTrue();
        assertThat(users.findByEmailIgnoreCase("missing@example.com")).isEmpty();
    }

    @Test void rejectsDuplicateEmailAtDatabaseLevel() {
        users.saveAndFlush(User.registerWithEmail("member@example.com", "hash", "회원1"));
        assertThatThrownBy(() -> users.saveAndFlush(
            User.registerWithEmail("MEMBER@example.com", "hash", "회원2")))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test void rejectsDuplicateNicknameAtDatabaseLevel() {
        users.saveAndFlush(User.registerWithEmail("first@example.com", "hash", "동일닉네임"));
        assertThatThrownBy(() -> users.saveAndFlush(
            User.registerWithEmail("second@example.com", "hash", "동일닉네임")))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test void allowsMultipleGoogleUsersBeforeNicknameOnboarding() {
        var first = users.saveAndFlush(User.registerWithGoogle("first@example.com"));
        users.saveAndFlush(User.registerWithGoogle("second@example.com"));
        entityManager.clear();
        var user = users.findById(first.getId()).orElseThrow();
        assertThat(user.getNickname()).isNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_NICKNAME);
        var password = entityManager.createNativeQuery("SELECT password_hash FROM users WHERE id = :id")
            .setParameter("id", user.getId()).getSingleResult();
        assertThat(password).isNull();
    }

    @Test void rejectsActiveUserWithoutNicknameAtDatabaseLevel() {
        var user = users.saveAndFlush(User.registerWithGoogle("pending@example.com"));
        assertThatThrownBy(() -> entityManager.createNativeQuery("UPDATE users SET status = 'ACTIVE' WHERE id = :id")
            .setParameter("id", user.getId()).executeUpdate())
            .isInstanceOf(jakarta.persistence.PersistenceException.class);
    }

    @Test void rejectsMissingEmailRegistrationFields() {
        assertThatIllegalArgumentException().isThrownBy(() -> User.registerWithEmail(" ", "hash", "회원"));
        assertThatIllegalArgumentException().isThrownBy(() -> User.registerWithEmail("a@example.com", " ", "회원"));
        assertThatIllegalArgumentException().isThrownBy(() -> User.registerWithEmail("a@example.com", "hash", " "));
        assertThatIllegalArgumentException().isThrownBy(() -> User.registerWithEmail("a@example.com", "hash", "가".repeat(31)));
    }
}
