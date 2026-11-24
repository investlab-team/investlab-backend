package com.investlab.trading.domain.record;

/** DB ENUM: order_source */
public enum OrderSource {
    FORM,       // 주문 폼
    ORDERBOOK   // 호가 클릭 (API의 HOGA = LIMIT + ORDERBOOK)
}
