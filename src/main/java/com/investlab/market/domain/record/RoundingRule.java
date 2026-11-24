package com.investlab.market.domain.record;

public record RoundingRule(RoundingPolicy amount, RoundingPolicy fee, RoundingPolicy tax) {
}
