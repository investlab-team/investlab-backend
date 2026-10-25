package com.investlab.market.domain;

import com.investlab.market.domain.record.CandleInterval;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriceCandleId implements Serializable {

    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(candle_interval)
    @Column(name = "interval", nullable = false, columnDefinition = "candle_interval")
    private CandleInterval interval;

    /** 봉 시작시각 */
    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    public PriceCandleId(UUID instrumentId, CandleInterval interval, Instant openedAt) {
        this.instrumentId = instrumentId;
        this.interval = interval;
        this.openedAt = openedAt;
    }
}
