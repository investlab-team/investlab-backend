package com.investlab.trading.domain;

import com.investlab.market.common.BaseTimeEntity;
import com.investlab.trading.domain.record.HogaLevel;
import com.investlab.trading.domain.record.OrderSide;
import com.investlab.trading.domain.record.OrderSource;
import com.investlab.trading.domain.record.OrderStatus;
import com.investlab.trading.domain.record.OrderType;
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
 * orders · 주문
 * (account_id, period_id)는 performance_periods의 복합 FK, instrument_id는 market 모듈의 instruments.id.
 * 다른 모듈 엔티티는 직접 참조하지 않고 UUID 값만 보관한다.
 * 상태 구분: CANCELED = 사용자 취소, EXPIRED = 시스템 자동 만료, REJECTED = 체결 직전 재검증 실패.
 * 체결 보류는 PENDING을 유지하고 stale_batch_count만 올린다.
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // 운영에서는 UUIDv7 생성기로 교체 권장
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    /** instruments.id (market 모듈) */
    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(order_side)
    @Column(name = "side", nullable = false, columnDefinition = "order_side")
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(order_type)
    @Column(name = "type", nullable = false, columnDefinition = "order_type")
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(order_source)
    @Column(name = "source", nullable = false, columnDefinition = "order_source")
    private OrderSource source;

    /** ASK_1 / BID_1, ORDERBOOK일 때만 값이 있다. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(hoga_level)
    @Column(name = "hoga_level", columnDefinition = "hoga_level")
    private HogaLevel hogaLevel;

    /** 0 초과, 시장 수량단위 검증 */
    @Column(name = "quantity", nullable = false, precision = 28, scale = 8)
    private BigDecimal quantity;

    /** LIMIT일 때 필수, 0 초과 */
    @Column(name = "limit_price", precision = 28, scale = 8)
    private BigDecimal limitPrice;

    /** 예약 산정 기준가. 시장가는 접수 시점 캐시 현재가 */
    @Column(name = "reference_price", nullable = false, precision = 28, scale = 8)
    private BigDecimal referencePrice;

    /** 매수 예약금 (수수료 포함, 시장가는 버퍼 반영). 매도는 0 */
    @Column(name = "reserved_cash", nullable = false, precision = 28, scale = 8)
    private BigDecimal reservedCash;

    /** 매도 예약수량. 매수는 0 */
    @Column(name = "reserved_qty", nullable = false, precision = 28, scale = 8)
    private BigDecimal reservedQty;

    /** 접수 당시 수수료율 */
    @Column(name = "fee_rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal feeRate;

    /** 접수 당시 매도 세율 */
    @Column(name = "tax_rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal taxRate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(order_status)
    @Column(name = "status", nullable = false, columnDefinition = "order_status")
    private OrderStatus status;

    /** 국내 주식은 당일 장 마감, 가상화폐는 NULL */
    @Column(name = "expires_at")
    private Instant expiresAt;

    /** 유효 시세 없이 넘어간 연속 배치 수 */
    @Column(name = "stale_batch_count", nullable = false)
    private int staleBatchCount;

    /** 취소 · 만료 · 거부 · 체결보류 사유 */
    @Column(name = "reason_code", length = 60)
    private String reasonCode;

    @Builder
    public Orders(UUID accountId, UUID periodId, UUID instrumentId, OrderSide side, OrderType type,
                  OrderSource source, HogaLevel hogaLevel, BigDecimal quantity, BigDecimal limitPrice,
                  BigDecimal referencePrice, BigDecimal reservedCash, BigDecimal reservedQty,
                  BigDecimal feeRate, BigDecimal taxRate, Instant expiresAt) {
        this.accountId = accountId;
        this.periodId = periodId;
        this.instrumentId = instrumentId;
        this.side = side;
        this.type = type;
        this.source = source != null ? source : OrderSource.FORM;
        this.hogaLevel = hogaLevel;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
        this.referencePrice = referencePrice;
        this.reservedCash = reservedCash != null ? reservedCash : BigDecimal.ZERO;
        this.reservedQty = reservedQty != null ? reservedQty : BigDecimal.ZERO;
        this.feeRate = feeRate;
        this.taxRate = taxRate;
        this.status = OrderStatus.PENDING;
        this.expiresAt = expiresAt;
        this.staleBatchCount = 0;
    }

    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }

    /** 체결 완료 (주문당 전량 체결 1회) */
    public void fill() {
        requirePending();
        this.status = OrderStatus.FILLED;
    }

    /** 사용자 취소. 초기화로 인한 취소는 reasonCode = RESET */
    public void cancel(String reasonCode) {
        requirePending();
        this.status = OrderStatus.CANCELED;
        this.reasonCode = reasonCode;
    }

    /** 시스템 자동 만료 (장 마감 / 가상화폐 연속 시세 없음) */
    public void expire(String reasonCode) {
        requirePending();
        this.status = OrderStatus.EXPIRED;
        this.reasonCode = reasonCode;
    }

    /** 체결 직전 재검증 실패 (잔액 부족 등) */
    public void reject(String reasonCode) {
        requirePending();
        this.status = OrderStatus.REJECTED;
        this.reasonCode = reasonCode;
    }

    /** 유효 시세가 없어 체결을 보류한 배치 수를 1 올린다. */
    public void markStale() {
        requirePending();
        this.staleBatchCount += 1;
    }

    /** 정상 시세로 평가되면 0으로 되돌린다. */
    public void resetStale() {
        this.staleBatchCount = 0;
    }

    private void requirePending() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 주문만 변경할 수 있습니다. status=" + status);
        }
    }
}
