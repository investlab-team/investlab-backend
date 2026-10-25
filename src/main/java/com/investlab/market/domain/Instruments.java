package com.investlab.market.domain;


import com.investlab.market.common.BaseTimeEntity;
import com.investlab.market.domain.record.InstrumentsStatus;
import com.investlab.market.domain.record.Market;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * instruments · 종목
 * UQ(market, symbol). 호가단위·수량단위·최소주문금액은 종목이 아니라 market_policies의 시장 공통값을 쓴다.
 */
@Entity
@Table(
    name = "instruments",
    uniqueConstraints = @UniqueConstraint(
        name = "instruments_market_symbol_uq",
        columnNames = {"market", "symbol"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Instruments extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // 운영에서는 UUIDv7 생성기로 교체 권장
    private UUID id;

    /** 주식이면 companies.id, 가상화폐는 NULL (companies 엔티티는 이번 범위 밖이라 ID만 보관) */
    @Column(name = "company_id")
    private UUID companyId;

    /** FK: market_policies.market (DB에서 FK 보장) */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(market_code)
    @Column(name = "market", nullable = false, columnDefinition = "market_code")
    private Market market;

    /** 외부 종목코드 */
    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // PostgreSQL ENUM 타입(instrument_status)
    @Column(name = "status", nullable = false, columnDefinition = "instrument_status")
    private InstrumentsStatus status;

    /** CHAR(3), 현재 KRW만 허용 (DB CHECK) */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency", nullable = false, length = 3, columnDefinition = "char(3)")
    private String currency;

    @Builder
    public Instruments(UUID companyId, Market market, String symbol, String name,
                      InstrumentsStatus status, String currency) {
        this.companyId = companyId;
        this.market = market;
        this.symbol = symbol;
        this.name = name;
        this.status = status != null ? status : InstrumentsStatus.TRADABLE;
        this.currency = currency != null ? currency : "KRW";
    }

    public boolean isTradable() {
        return status == InstrumentsStatus.TRADABLE;
    }

    public void halt() {
        this.status = InstrumentsStatus.HALTED;
    }

    public void resume() {
        this.status = InstrumentsStatus.TRADABLE;
    }

    public void delist() {
        this.status = InstrumentsStatus.DELISTED;
    }

    public void rename(String name) {
        this.name = name;
    }
}
