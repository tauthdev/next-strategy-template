# next-strategy-template

넥스트증권 모의투자 자동매매 전략을 시작하는 템플릿입니다.
[next-trading-core](https://github.com/hanaset/next-trading-core) 프레임워크 위에서 **전략 클래스 하나만 작성**하면 봇이 완성됩니다.

## 시작하기

### 1. 이 템플릿으로 새 레포 만들기

GitHub 에서 **Use this template** 버튼을 누르거나:

```bash
gh repo create my-strategy --template hanaset/next-strategy-template --clone
```

### 2. API 키 설정

[넥스트증권 OpenAPI 가이드](https://docs.nextsecurities.dev/)에서 모의투자 키를 발급받고, 환경변수로 넣습니다:

```bash
export NEXT_CLIENT_ID=pk_test_...
export NEXT_CLIENT_SECRET=sk_test_...
export NEXT_ACCOUNT_ID=acc_main
```

(또는 `src/main/resources/application-local.yml` 을 만들어 넣으세요 — gitignore 되어 있습니다)

### 3. 실행

```bash
./gradlew bootRun
```

미국 정규장 시간이 되면 엔진이 예제 전략(`ExampleMovingAverageStrategy`)을 1분 주기로 호출하기 시작합니다.

### 4. 내 전략 작성

`ExampleMovingAverageStrategy.kt` 를 지우고 본인의 전략으로 교체하세요:

```kotlin
@Component
class MyStrategy : TradingStrategy {

    override val spec = StrategySpec(
        name = "my-strategy",          // clientOrderId 프리픽스로 사용됨
        symbols = listOf("TSLA"),      // 감시할 종목
        candleInterval = CandleInterval.HOUR_1,
        candleLimit = 24,              // 컨텍스트로 공급받을 캔들 개수
        pollInterval = Duration.ofSeconds(30),
    )

    override fun decide(context: StrategyContext): List<Signal> {
        // context.candles("TSLA")  : 캔들 (과거→최신)
        // context.quote("TSLA")    : 현재가 스냅샷
        // context.holding("TSLA")  : 보유 포지션
        // context.openOrders       : 미체결 주문
        // context.buyingPower      : 주문 가능 현금
        return emptyList()  // 할 일 없으면 빈 리스트
    }
}
```

핵심 규칙:
- **매수**: `Signal.Buy(symbol, quantity)` — `takeProfitPrice`/`stopLossPrice` 를 주면 체결 후 코어가 자동으로 익절/손절합니다
- **매도**: `Signal.Sell(symbol, quantity)` — 보유 수량 이상은 자동으로 잘립니다 (공매도 불가)
- **취소**: `Signal.Cancel(orderId)`
- 전략 안에서 API 직접 호출/스레드 생성 금지 — 데이터는 전부 `StrategyContext` 로 공급됩니다

## 참고

- 전략 여러 개를 한 앱에 등록해도 됩니다 (`TradingStrategy` 빈을 여러 개 만들면 각자의 주기로 실행)
- 연속 실패가 5회 누적되면 안전을 위해 자동 정지됩니다 (미체결 전량 취소 + 신규 주문 차단)
- 실전 전략 예시: [larry-strategy](https://github.com/hanaset/larry-strategy) — 변동성 돌파 전략
