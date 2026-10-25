package com.investlab.market.domain;


import com.investlab.market.common.BaseTimeEntity;
import com.investlab.market.domain.record.Market;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * market_calendars · 시장 운영일 (v1.1 신규)
 * 국내 주식 전용. 가상화폐는 행을 두지 않으며 24시간 운영으로 본다.
 * 정규장(open_at ~ close_at) 밖의 주식 주문은 접수 거부(이월 없음).
 * closing_auction_at ~ close_at 사이 접수된 시장가 주문은 장 마감 후 첫 배치에서 당일 종가로 체결.
 */
@Entity
@Table(name = "market_calendars")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketCalendars extends BaseTimeEntity {

    @EmbeddedId
    private MarketCalendarsId id;

    /** FK: market_policies.market (PK의 market 컬럼과 공유) */
    @MapsId("market")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market", nullable = false)
    private MarketPolicies marketPolicy;

    /** 휴장일 false */
    @Column(name = "is_open", nullable = false)
    private boolean open;

    /** 정규장 시작 (휴장일이면 NULL) */
    @Column(name = "open_at")
    private Instant openAt;

    /** 장 마감 동시호가 시작 */
    @Column(name = "closing_auction_at")
    private Instant closingAuctionAt;

    /** 정규장 마감 */
    @Column(name = "close_at")
    private Instant closeAt;

    @Builder
    public MarketCalendars(MarketPolicies marketPolicy, LocalDate tradeDate, boolean open,
                          Instant openAt, Instant closingAuctionAt, Instant closeAt) {
        this.marketPolicy = marketPolicy;
        this.id = new MarketCalendarsId(marketPolicy.getMarket(), tradeDate);
        this.open = open;
        this.openAt = openAt;
        this.closingAuctionAt = closingAuctionAt;
        this.closeAt = closeAt;
    }

    public Market getMarket() {
        return id.getMarket();
    }

    public LocalDate getTradeDate() {
        return id.getTradeDate();
    }

    /** 정규장 시간 내 접수 가능 여부 (open_at 이상, close_at 미만) */
    public boolean isRegularSession(Instant now) {
        return open && openAt != null && closeAt != null
            && !now.isBefore(openAt) && now.isBefore(closeAt);
    }

    /** 동시호가 구간 여부 (closing_auction_at 이상, close_at 미만) */
    public boolean isClosingAuction(Instant now) {
        return open && closingAuctionAt != null && closeAt != null
            && !now.isBefore(closingAuctionAt) && now.isBefore(closeAt);
    }
}
