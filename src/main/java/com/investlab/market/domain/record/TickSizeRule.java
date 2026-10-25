package com.investlab.market.domain.record;

import java.math.BigDecimal;

public record TickSizeRule(BigDecimal below, BigDecimal tick) {
}
