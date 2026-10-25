package com.investlab.market.domain;

import com.investlab.market.common.BaseTimeEntity;
import com.investlab.market.domain.record.CandleInterval;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * price_candles · 봉 데이터
 * 현재가·최우선 호가는 Redis 캐시에 두고 영구 테이블을 만들지 않는다.
 * DB CHECK: low <= min(open, close), high >= max(open, close), open/low/close > 0, volume >= 0
 */

@Entity
@NoArgsConstructor
@Getter
@Table(name = "price_candles")
public class PriceCandles extends BaseTimeEntity {

    @EmbeddedId
    private PriceCandleId id;

    @MapsId("instrumentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instruments instrument;

    @Column(name = "open", nullable = false, precision = 28, scale = 8)
    private BigDecimal open;

    @Column(name = "high", nullable = false, precision = 28, scale = 8)
    private BigDecimal high;

    @Column(name = "low", nullable = false, precision = 28, scale = 8)
    private BigDecimal low;

    @Column(name = "close", nullable = false, precision = 28, scale = 8)
    private BigDecimal close;

    @Column(name = "volume", nullable = false, precision = 28, scale = 8)
    private BigDecimal volume;

    /** 시세 공급자 */
    @Column(name = "provider", nullable = false)
    private String provider;

    /** 수집시각 */
    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    @Builder
    public PriceCandle(Instruments instrument, CandleInterval interval, Instant openedAt,
                       BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,
                       BigDecimal volume, String provider, Instant fetchedAt) {
        this.instrument = instrument;
        this.id = new PriceCandleId(instrument.getId(), interval, openedAt);
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.provider = provider;
        this.fetchedAt = fetchedAt;
    }

    public CandleInterval getInterval() {
        return id.getInterval();
    }

    public Instant getOpenedAt() {
        return id.getOpenedAt();
    }
}
