package com.investlab.trading.domain;

import com.investlab.market.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * holdings · 보유종목
 * UQ(period_id, instrument_id). 전량 매도 체결 시 행을 삭제한다(수량 0 행을 남기지 않음).
 * 초기화 시에도 해당 구간의 모든 행을 삭제한다.
 */
@Entity
@Table(
        name = "holdings",
        uniqueConstraints = @UniqueConstraint(
                name = "holdings_period_instr_uq",
                columnNames = {"period_id", "instrument_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holdings extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // 운영에서는 UUIDv7 생성기로 교체 권장
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    /** 현재 성과구간 */
    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    /** instruments.id (market 모듈) */
    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    /** 보유수량, DB CHECK > 0 */
    @Column(name = "quantity", nullable = false, precision = 28, scale = 8)
    private BigDecimal quantity;

    /** 매도 예약수량, 0 이상 · 보유수량 이하 */
    @Column(name = "reserved_qty", nullable = false, precision = 28, scale = 8)
    private BigDecimal reservedQty;

    /** 매수 수수료 포함 취득원가 */
    @Column(name = "cost_basis", nullable = false, precision = 28, scale = 8)
    private BigDecimal costBasis;

    /** cost_basis ÷ quantity */
    @Column(name = "average_price", nullable = false, precision = 28, scale = 8)
    private BigDecimal averagePrice;

    @Builder
    public Holdings(UUID accountId, UUID periodId, UUID instrumentId, BigDecimal quantity,
                    BigDecimal costBasis, BigDecimal averagePrice) {
        this.accountId = accountId;
        this.periodId = periodId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.reservedQty = BigDecimal.ZERO;
        this.costBasis = costBasis;
        this.averagePrice = averagePrice;
    }

    /** 매도 주문에 쓸 수 있는 수량 (보유수량 - 예약수량) */
    public BigDecimal availableQuantity() {
        return quantity.subtract(reservedQty);
    }

    public void reserve(BigDecimal qty) {
        if (qty.signum() < 0 || availableQuantity().compareTo(qty) < 0) {
            throw new IllegalStateException("매도 가능 수량이 부족합니다.");
        }
        this.reservedQty = reservedQty.add(qty);
    }

    public void release(BigDecimal qty) {
        if (qty.signum() < 0 || reservedQty.compareTo(qty) < 0) {
            throw new IllegalStateException("해제할 예약수량이 예약 합계보다 큽니다.");
        }
        this.reservedQty = reservedQty.subtract(qty);
    }
}
