package com.investlab.trading.domain;

import com.investlab.trading.domain.record.CashEventKind;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * cash_events · 현금 원장 (추가만 하는 기록이라 updated_at 없음)
 * RESET 행은 새 성과구간에 기록한다. 예약 · 예약 해제는 현금 변동이 아니므로 남기지 않는다.
 * 검증: 계좌의 전체 원장 amount 합 = accounts.cash_balance.
 */
@Entity
@Table(name = "cash_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CashEvents {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // 운영에서는 UUIDv7 생성기로 교체 권장
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    /** 소속 성과구간 (RESET은 새 구간) */
    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(cash_event_kind)
    @Column(name = "kind", nullable = false, columnDefinition = "cash_event_kind")
    private CashEventKind kind;

    /** BUY · SELL이면 필수, OPEN · RESET은 NULL */
    @Column(name = "execution_id", unique = true)
    private UUID executionId;

    /** BUY = -(체결금액+수수료), SELL = 체결금액-수수료-세금, RESET = 10,000,000 - 직전 현금 */
    @Column(name = "amount", nullable = false, precision = 28, scale = 8)
    private BigDecimal amount;

    /** 변동 직후 현금 */
    @Column(name = "balance_after", nullable = false, precision = 28, scale = 8)
    private BigDecimal balanceAfter;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public CashEvents(UUID accountId, UUID periodId, CashEventKind kind, UUID executionId,
                      BigDecimal amount, BigDecimal balanceAfter, Instant occurredAt) {
        this.accountId = accountId;
        this.periodId = periodId;
        this.kind = kind;
        this.executionId = executionId;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }
}
