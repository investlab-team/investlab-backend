package com.investlab.market.domain;

import com.investlab.market.common.BaseTimeEntity;
import com.investlab.market.domain.record.Market;
import com.investlab.market.domain.record.RoundingRule;
import com.investlab.market.domain.record.TickSizeRule;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Table(name="market_policies")
public class MarketPolicies extends BaseTimeEntity {

    public MarketPolicies() {
    }

    @Builder
    public MarketPolicies(Market market, Boolean enabled, BigDecimal feeRate, BigDecimal sellTaxRate, List<TickSizeRule> tickSizeRules, BigDecimal quantityStep, BigDecimal minOrderAmount, BigDecimal marketOrderBufferRate, int quoteMaxAgeSeconds, Integer staleExpireBatches, RoundingRule roundingRule) {
        this.market = market;
        this.enabled = enabled != null ? enabled : Boolean.FALSE; // DB DEFAULT false
        this.feeRate = feeRate;
        this.sellTaxRate = sellTaxRate;
        this.tickSizeRules = tickSizeRules;
        this.quantityStep = quantityStep;
        this.minOrderAmount = minOrderAmount;
        this.marketOrderBufferRate = marketOrderBufferRate != null ? marketOrderBufferRate : new BigDecimal("0.05"); // DB DEFAULT 0.05
        this.quoteMaxAgeSeconds = quoteMaxAgeSeconds;
        this.staleExpireBatches = staleExpireBatches;
        this.roundingRule = roundingRule;
    }

    @Id
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(market_code)
    @Column(name = "market", nullable = false, columnDefinition = "market_code")
    private Market market;

    // 정책 및 데이터 준비 완료 시 TRUE
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "fee_rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal feeRate;

    @Column(name = "sell_tax_rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal sellTaxRate;


    /* 가격대별 공통 호가단위 표 (JSONB) 예 : [{"minPrice" : 0, "maxPrice" : 2000, "tick" : 1}, ...]*/
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tick_size_rules", nullable = false, columnDefinition = "jsonb")
    private List<TickSizeRule> tickSizeRules;

    // 수량 단위 (주식 1, 가상화폐 소수 정밀도)
    @Column(name = "quantity_step", nullable = false, precision = 28, scale = 8)
    private BigDecimal quantityStep;

    // 최소 주문금액 (시장 공통)
    @Column(name = "min_order_amount", nullable = false, precision = 28, scale = 8)
    private BigDecimal minOrderAmount;

    // 시장가 매수 예약 버퍼, 기본 0.05 (예약금 = 현재가 x (1 + 버퍼) x 수량 + 수수료 )
    @Column(name = "market_order_buffer_rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal marketOrderBufferRate;

    // 체결에 쓸 수 있는 시세 최대 경과 시간(초)
    @Column(name = "quote_max_age_seconds", nullable = false)
    private int quoteMaxAgeSeconds;

    // 연속 시세 없음 자동만료 기준 배치 수, CRYPTO_KRW = 10, KR_STOCK = NULL
    @Column(name = "stale_expire_batches")
    private Integer staleExpireBatches;

    // 금액, 수수료, 세금 반올림 기준 (JSONB)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rounding_rule", nullable = false, columnDefinition = "jsonb")
    private RoundingRule roundingRule;

    // 호가단위 검증: 72,050원 -> 20만원 미만 구간(100단위) -> 50원이 남으므로 false
    public boolean isValidTick(BigDecimal price) {
        BigDecimal tick = tickSizeRules.stream()
            .filter(r -> r.below() == null || price.compareTo(r.below()) < 0)
            .findFirst() // 규칙은 below 오름차순으로 저장
            .map(TickSizeRule::tick)
            .orElseThrow(); // exception 만들어서 던지기 (추가)
        return price.remainder(tick).signum() == 0;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    /** 시장가 매수 예약 기준 배율 (1 + 버퍼) */
    public BigDecimal marketOrderReserveMultiplier() {
        return BigDecimal.ONE.add(marketOrderBufferRate);
    }

}

