package com.example.strategy

import com.tripleauth.nexttrading.client.dto.CandleInterval
import com.tripleauth.nexttrading.strategy.Signal
import com.tripleauth.nexttrading.strategy.StrategyContext
import com.tripleauth.nexttrading.strategy.StrategySpec
import com.tripleauth.nexttrading.strategy.TradingStrategy
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration

/**
 * 예제 전략: 20일 이동평균 돌파 매수.
 *
 * 이 파일을 지우고 본인의 전략으로 교체하세요.
 * `TradingStrategy` 를 구현한 `@Component` 클래스는 엔진이 자동으로 찾아 실행합니다.
 */
@Component
class ExampleMovingAverageStrategy : TradingStrategy {

    override val spec = StrategySpec(
        name = "example-ma20",
        symbols = listOf("AAPL"),
        candleInterval = CandleInterval.DAY_1,
        candleLimit = 20,
        pollInterval = Duration.ofSeconds(60),
    )

    override fun decide(context: StrategyContext): List<Signal> {
        val symbol = spec.symbols.first()

        val candles = context.candles(symbol)
        if (candles.size < spec.candleLimit) return emptyList()

        val price = context.quote(symbol)?.price ?: return emptyList()

        // 이미 보유 중이거나 미체결 주문이 있으면 대기 (익절/손절은 브라켓이 처리)
        if (context.hasPosition(symbol) || context.hasOpenOrder(symbol)) return emptyList()

        val ma20 = candles.map { it.close }
            .reduce(BigDecimal::add)
            .divide(BigDecimal(candles.size), 4, RoundingMode.HALF_EVEN)

        if (price <= ma20) return emptyList()

        return listOf(
            Signal.Buy(
                symbol = symbol,
                quantity = BigDecimal.ONE,
                takeProfitPrice = price.multiply(BigDecimal("1.04")).setScale(2, RoundingMode.HALF_EVEN),
                stopLossPrice = price.multiply(BigDecimal("0.98")).setScale(2, RoundingMode.HALF_EVEN),
            ),
        )
    }
}
