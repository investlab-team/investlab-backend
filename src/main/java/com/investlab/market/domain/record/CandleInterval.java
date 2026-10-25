package com.investlab.market.domain.record;

/**
    * DB ENUM: candle_interval
 * 분봉(권장)은 DB에서 ALTER TYPE candle_interval ADD VALUE ... 로 추가한 뒤 여기에도 같은 값을 추가한다.
 */
public enum CandleInterval {
    DAY,
    WEEK,
    MONTH
}
