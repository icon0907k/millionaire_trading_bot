package afterwork.millionaire.trading.overseas;

import afterwork.millionaire.indicator.RSICalculatorService;
import afterwork.millionaire.trading.oauth.OauthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class OverseasStockRsiTraderBot {

    private int dollarAmount = 1000;
    private static final Map<Double, Double> RSI_INVESTMENT_THRESHOLDS = Map.of(
            50.0, 7.0,  // RSI <= 50: 7% 투자
            45.0, 9.0,  // RSI <= 45: 9% 투자
            40.0, 11.0, // RSI <= 40: 11% 투자
            35.0, 13.0, // RSI <= 35: 13% 투자
            30.0, 15.0, // RSI <= 30: 15% 투자
            25.0, 17.0,  // RSI <= 25: 17% 투자
            20.0, 19.0,  // RSI <= 20: 19% 투자
            15.0, 21.0  // RSI <= 15: 21% 투자
    );

    private final OverseasStockTimeTraderBotService overseasStockTimeTraderBotService;
    private final OauthService oauthService;

    /**
     * 매일 자정 직후 OAuth 액세스 토큰을 발급받는 스케줄러
     */
    @Scheduled(cron = "22 0 0 * * *", zone = "Asia/Seoul")
    public void issueToken() {
        log.info("OAuth 토큰 발급 시작...");
        oauthService.getAccessToken()
                .doOnNext(token -> log.info("OAuth 토큰 발급 성공: {}", token))
                .doOnError(error -> log.error("토큰 발급 실패: {}", error.getMessage()))
                .subscribe();
    }

    /**
     * 장 시간(20:29 KST)에 RSI 기반 투자 로직을 실행하는 스케줄러
     */
    @Scheduled(cron = "30 29 20 * * *", zone = "Asia/Seoul")
    public void executeMarketLogic() {
        log.info("장 시간 RSI 트레이딩 로직 실행: {}", ZonedDateTime.now());

        // 해외 주식 분 단위 데이터 가져오기
        double rsi = RSICalculatorService.calculateRSI(
                overseasStockTimeTraderBotService.getOverseasStockMinuteData().block(), 14);

        // RSI 기반 투자 금액 계산
        double investmentAmount = calculateInvestmentAmount(rsi);
        log.info("RSI: {}, 투자 금액: ${}", rsi, investmentAmount);

        // TODO: 실제 투자 로직 추가 (예: 주문 실행)
    }

    /**
     * RSI 값을 기반으로 투자 금액을 계산하는 메서드
     */
    private double calculateInvestmentAmount(double rsi) {
        return RSI_INVESTMENT_THRESHOLDS.entrySet().stream()
                .filter(entry -> rsi <= entry.getKey())
                .mapToDouble(entry -> dollarAmount * (entry.getValue() / 100))
                .findFirst()
                .orElse(0.0); // RSI > 50일 경우 투자하지 않음
    }
}