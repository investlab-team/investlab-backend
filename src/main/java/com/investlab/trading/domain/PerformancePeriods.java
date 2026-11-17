package com.investlab.trading.domain;

import com.investlab.market.common.BaseTimeEntity;
import com.investlab.trading.domain.record.PeriodStartReason;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * performance_periods · 성과구간 (A+B 공동 모델)
 * 계좌당 열린 구간(ended_at IS NULL)은 1개 (DB 부분 UQ). UQ(account_id, id)는 주문·체결·보유·원장의 복합 FK 대상.
 * Accounts와 순환 참조이므로 ID는 생성 시점에 미리 부여한다.
 */
@Entity
@Table(name = "performance_periods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformancePeriods extends BaseTimeEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    /** 개설 또는 초기화 시각 */
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    /** 현재 구간은 NULL */
    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "initial_capital", nullable = false, precision = 28, scale = 8)
    private BigDecimal initialCapital;

    /** 구간 누적 실현손익. 화면의 '누적 실현손익'은 이 값 */
    @Column(name = "realized_pnl", nullable = false, precision = 28, scale = 8)
    private BigDecimal realizedPnl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(period_start_reason)
    @Column(name = "start_reason", nullable = false, columnDefinition = "period_start_reason")
    private PeriodStartReason startReason;

    @Builder
    public PerformancePeriods(UUID id, UUID accountId, Instant startedAt, PeriodStartReason startReason) {
        this.id = id != null ? id : UUID.randomUUID();
        this.accountId = accountId;
        this.startedAt = startedAt != null ? startedAt : Instant.now();
        this.initialCapital = Accounts.INITIAL_CAPITAL;
        this.realizedPnl = BigDecimal.ZERO;
        this.startReason = startReason;
    }

    public boolean isOpen() {
        return endedAt == null;
    }

    public void end(Instant endedAt) {
        this.endedAt = endedAt;
    }

    /** 매도 체결의 실현손익을 누적한다. */
    public void addRealizedPnl(BigDecimal pnl) {
        this.realizedPnl = realizedPnl.add(pnl);
    }
}
