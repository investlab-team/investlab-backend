package com.investlab.market.domain;

import com.investlab.market.domain.record.Market;
import com.investlab.market.domain.record.RoundingRule;
import com.investlab.market.domain.record.TickSizeRule;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Getter
@Table(name="market_policies")
public class MarketPolicies {

    public MarketPolicies() {
    }

    @Builder
    public MarketPolicies(Market market, Boolean enabled, BigDecimal feeRate, BigDecimal sellTaxRate, List<TickSizeRule> tickSizeRules, BigDecimal quantityStep, BigDecimal minOrderAmount, BigDecimal marketOrderBufferRate, int quoteMaxAgeSeconds, Integer staleExpireBatches, RoundingRule roundingRule, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.market = market;
        this.enabled = enabled;
        this.feeRate = feeRate;
        this.sellTaxRate = sellTaxRate;
        this.tickSizeRules = tickSizeRules;
        this.quantityStep = quantityStep;
        this.minOrderAmount = minOrderAmount;
        this.marketOrderBufferRate = marketOrderBufferRate;
        this.quoteMaxAgeSeconds = quoteMaxAgeSeconds;
        this.staleExpireBatches = staleExpireBatches;
        this.roundingRule = roundingRule;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Id
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(market_code)
    @Column(name = "market", nullable = false, columnDefinition = "market_code")
    private Market market;

    // 정책 및 데이터 준비 완료 시 TRUE
    private Boolean enabled;

    private BigDecimal feeRate;

    private BigDecimal sellTaxRate;


    /* 가격대별 공통 호가단위 표 (JSONB) 예 : [{"minPrice" : 0, "maxPrice" : 2000, "tick" : 1}, ...]*/
    @JdbcTypeCode(SqlTypes.JSON)
    private List<TickSizeRule> tickSizeRules; // 변수 타입 확인 요망

    // 수량 단위 (주식 1, 가상화폐 소수 정밀도)
    private BigDecimal quantityStep;

    // 최소 주문금액 (시장 공통)
    private BigDecimal minOrderAmount;

    // 시장가 매수 예약 버퍼, 기본 0.05 (예약금 = 현재가 x (1 + 버퍼) x 수량 + 수수료 )
    private BigDecimal marketOrderBufferRate;

    // 체결에 쓸 수 있는 시세 최대 경과 시간(초)
    private int quoteMaxAgeSeconds;

    // 연속 시세 없음 자동만료 기준 배치 수, CRYPTO_KRW = 10, KR_STOCK = NULL
    private Integer staleExpireBatches;

    // 금액, 수수료, 세금 반올림 기준 (JSONB)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rounding_rule", nullable = false, columnDefinition = "jsonb")
    private RoundingRule roundingRule; // 변수 타입 확인 요망

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // 호가단위 검증: 72,050원 -> 20만원 미안 구간(100단위) -> 50원이 남으므로 false
    public boolean isValidTick(BigDecimal price) {
        BigDecimal tick = tickSizeRules.stream()
            .filter(r -> r.below() == null || price.compareTo(r.below()) < 0)
            .findFirst() // 규칙은 below 오름차수으로 저장
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

