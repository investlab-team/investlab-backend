package com.investlab.market.domain.record;

// BD ENUM : instrument_status
public enum InstrumentsStatus {
    TRADABLE, // 거래 기능
    HALTED,   // 거래 정지
    DELISTED  // 상장 폐지
}
