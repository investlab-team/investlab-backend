package com.investlab.market.domain;

import com.investlab.market.domain.record.Market;
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
import java.time.LocalDate;

/** market_calendars 복합 PK (market, trade_date) */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketCalendarsId implements Serializable {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(market_code) 사용
    @Column(name = "market", nullable = false, columnDefinition = "market_code")
    private Market market;

    /** KST 거래일 */
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    public MarketCalendarsId(Market market, LocalDate tradeDate) {
        this.market = market;
        this.tradeDate = tradeDate;
    }
}
