package com.investlab.trading.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * account_resets · 초기화 이력
 * UQ(account_id, reset_date_kst): 계좌당 KST 하루 1회. 보유종목은 매도 체결 없이 제거한다.
 */
@Entity
@Table(
        name = "account_resets",
        uniqueConstraints = @UniqueConstraint(
                name = "account_resets_daily_uq",
                columnNames = {"account_id", "reset_date_kst"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountResets {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // 운영에서는 UUIDv7 생성기로 교체 권장
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    /** 종료된 성과구간 */
    @Column(name = "old_period_id", nullable = false)
    private UUID oldPeriodId;

    /** 새 성과구간 */
    @Column(name = "new_period_id", nullable = false)
    private UUID newPeriodId;

    /** KST 실행일 */
    @Column(name = "reset_date_kst", nullable = false)
    private LocalDate resetDateKst;

    /** 초기화 전 현금 */
    @Column(name = "cash_before", nullable = false, precision = 28, scale = 8)
    private BigDecimal cashBefore;

    /** 초기화 후 현금 (10,000,000) */
    @Column(name = "cash_after", nullable = false, precision = 28, scale = 8)
    private BigDecimal cashAfter;

    /** 함께 취소된 미체결 주문 수 */
    @Column(name = "canceled_order_count", nullable = false)
    private int canceledOrderCount;

    /** 제거된 보유종목 수 (API 응답값) */
    @Column(name = "holdings_cleared", nullable = false)
    private int holdingsCleared;

    @Column(name = "reset_at", nullable = false)
    private Instant resetAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public AccountResets(UUID accountId, UUID oldPeriodId, UUID newPeriodId, LocalDate resetDateKst,
                         BigDecimal cashBefore, BigDecimal cashAfter, int canceledOrderCount,
                         int holdingsCleared, Instant resetAt) {
        this.accountId = accountId;
        this.oldPeriodId = oldPeriodId;
        this.newPeriodId = newPeriodId;
        this.resetDateKst = resetDateKst;
        this.cashBefore = cashBefore;
        this.cashAfter = cashAfter != null ? cashAfter : Accounts.INITIAL_CAPITAL;
        this.canceledOrderCount = canceledOrderCount;
        this.holdingsCleared = holdingsCleared;
        this.resetAt = resetAt != null ? resetAt : Instant.now();
    }
}
