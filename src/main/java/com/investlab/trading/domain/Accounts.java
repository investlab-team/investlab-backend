package com.investlab.trading.domain;

import com.investlab.market.common.BaseTimeEntity;
import com.investlab.market.domain.record.Market;
import com.investlab.trading.domain.record.AccountStatus;
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
 * accounts · 모의계좌 (A+B 공동 모델)
 * 계좌 개설 시 accounts.current_period_id(DEFERRABLE FK)와 performance_periods.account_id가 서로를 가리키므로,
 * ID를 @GeneratedValue에 맡기지 않고 생성 시점에 미리 부여한다. 한 트랜잭션에서 함께 저장한다.
 * user_id, current_period_id 등 다른 테이블 FK는 연관관계 대신 UUID 값으로 보관한다 (복합 FK는 DB가 보장).
 */
@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Accounts extends BaseTimeEntity {

    /** 초기자금 1,000만원 고정 (입력값으로 받지 않는다) */
    public static final BigDecimal INITIAL_CAPITAL = new BigDecimal("10000000");

    @Id
    private UUID id;

    /** users.id (identity 모듈) */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false, length = 40)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(market_code)
    @Column(name = "market", nullable = false, columnDefinition = "market_code")
    private Market market;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(account_status)
    @Column(name = "status", nullable = false, columnDefinition = "account_status")
    private AccountStatus status;

    /** 현재 현금, 0 이상 */
    @Column(name = "cash_balance", nullable = false, precision = 28, scale = 8)
    private BigDecimal cashBalance;

    /** 매수 예약금 합계, 0 이상 · 현금 이하 */
    @Column(name = "reserved_cash", nullable = false, precision = 28, scale = 8)
    private BigDecimal reservedCash;

    /** 현재 performance_periods.id (DEFERRABLE) */
    @Column(name = "current_period_id", nullable = false)
    private UUID currentPeriodId;

    /** 랭킹 노출 제외 */
    @Column(name = "ranking_opt_out", nullable = false)
    private boolean rankingOptOut;

    /** 낙관적 잠금용 버전 */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Builder
    public Accounts(UUID id, UUID userId, String name, Market market, UUID currentPeriodId) {
        this.id = id != null ? id : UUID.randomUUID();
        this.userId = userId;
        this.name = name;
        this.market = market;
        this.status = AccountStatus.ACTIVE;
        this.cashBalance = INITIAL_CAPITAL;
        this.reservedCash = BigDecimal.ZERO;
        this.currentPeriodId = currentPeriodId;
        this.rankingOptOut = false;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    /** 주문에 쓸 수 있는 현금 (현금 - 예약금) */
    public BigDecimal availableCash() {
        return cashBalance.subtract(reservedCash);
    }

    public void reserveCash(BigDecimal amount) {
        if (amount.signum() < 0 || availableCash().compareTo(amount) < 0) {
            throw new IllegalStateException("가용 현금이 부족합니다.");
        }
        this.reservedCash = reservedCash.add(amount);
    }

    public void releaseCash(BigDecimal amount) {
        if (amount.signum() < 0 || reservedCash.compareTo(amount) < 0) {
            throw new IllegalStateException("해제할 예약금이 예약 합계보다 큽니다.");
        }
        this.reservedCash = reservedCash.subtract(amount);
    }

    /** 체결 등으로 현금이 변동될 때 사용 (매수는 음수, 매도는 양수). 결과는 0 이상이어야 한다. */
    public void applyCashChange(BigDecimal amount) {
        BigDecimal next = cashBalance.add(amount);
        if (next.signum() < 0) {
            throw new IllegalStateException("현금이 부족합니다.");
        }
        this.cashBalance = next;
    }

    /** 자금 초기화: 현금 1,000만원, 예약금 0, 새 성과구간으로 교체 */
    public void reset(UUID newPeriodId) {
        this.cashBalance = INITIAL_CAPITAL;
        this.reservedCash = BigDecimal.ZERO;
        this.currentPeriodId = newPeriodId;
    }

    public void changeRankingOptOut(boolean optOut) {
        this.rankingOptOut = optOut;
    }

    /** 해지는 상태 변경이며 행은 삭제하지 않는다. 해지 계좌도 조회는 가능. */
    public void close(Instant closedAt) {
        this.status = AccountStatus.CLOSED;
        this.closedAt = closedAt;
    }
}
