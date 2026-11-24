package com.investlab.trading.domain.record;

/** DB ENUM: order_status */
public enum OrderStatus {
    PENDING,
    FILLED,
    CANCELED,   // 사용자 취소 (초기화로 인한 취소 포함)
    EXPIRED,    // 시스템 자동 만료 (주식 장 마감 / 가상화폐 연속 10회 시세 없음)
    REJECTED    // 체결 직전 재검증 실패 (잔액 부족 등)
}
