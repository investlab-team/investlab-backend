package com.investlab.trading.domain;

import com.investlab.trading.domain.record.OrderSide;
import com.investlab.trading.domain.record.PriceBasis;
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
 * executions · 체결 (변경하지 않는 기록이라 updated_at 없음)
 * order_id UQ: 주문당 최대 1회(전량 체결).
 * account_id, period_id, instrument_id, side는 비정규화 열로, 체결 트랜잭션에서 orders 값을 그대로 복사한다.
 * (order_id, account_id, period_id, instrument_id, side) 복합 FK로 DB가 orders와의 일치를 강제한다.
 */
@Entity
@Table(name = "executions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Executions {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // 운영에서는 UUIDv7 생성기로 교체 권장
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    /** 비정규화 — orders 값 복사 */
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    /** 비정규화 — 복합 FK (account_id, period_id) */
    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    /** 비정규화 — orders 값 복사 */
    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    /** 비정규화 — BUY / SELL */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(order_side)
    @Column(name = "side", nullable = false, columnDefinition = "order_side")
    private OrderSide side;

    /** 체결가 */
    @Column(name = "price", nullable = false, precision = 28, scale = 8)
    private BigDecimal price;

    /** 체결수량 (= 주문수량) */
    @Column(name = "quantity", nullable = false, precision = 28, scale = 8)
    private BigDecimal quantity;

    /** 가격 × 수량 */
    @Column(name = "gross_amount", nullable = false, precision = 28, scale = 8)
    private BigDecimal grossAmount;

    /** 실제 수수료 */
    @Column(name = "fee", nullable = false, precision = 28, scale = 8)
    private BigDecimal fee;

    /** 실제 세금. 매수는 0 */
    @Column(name = "tax", nullable = false, precision = 28, scale = 8)
    private BigDecimal tax;

    /** 매도 확정 손익. 매수는 0 */
    @Column(name = "realized_pnl", nullable = false, precision = 28, scale = 8)
    private BigDecimal realizedPnl;

    /** LAST(최신가) / CLOSING(동시호가 종가) */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(price_basis)
    @Column(name = "price_basis", nullable = false, columnDefinition = "price_basis")
    private PriceBasis priceBasis;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    /** 사용한 가격의 시세시각 */
    @Column(name = "quote_as_of", nullable = false)
    private Instant quoteAsOf;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public Executions(UUID orderId, UUID accountId, UUID periodId, UUID instrumentId, OrderSide side,
                      BigDecimal price, BigDecimal quantity, BigDecimal grossAmount, BigDecimal fee,
                      BigDecimal tax, BigDecimal realizedPnl, PriceBasis priceBasis,
                      Instant executedAt, Instant quoteAsOf) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.periodId = periodId;
        this.instrumentId = instrumentId;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.grossAmount = grossAmount;
        this.fee = fee != null ? fee : BigDecimal.ZERO;
        this.tax = tax != null ? tax : BigDecimal.ZERO;
        this.realizedPnl = realizedPnl != null ? realizedPnl : BigDecimal.ZERO;
        this.priceBasis = priceBasis != null ? priceBasis : PriceBasis.LAST;
        this.executedAt = executedAt != null ? executedAt : Instant.now();
        this.quoteAsOf = quoteAsOf;
    }
}
