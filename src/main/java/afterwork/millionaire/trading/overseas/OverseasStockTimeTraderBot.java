package afterwork.millionaire.trading.overseas;

import afterwork.millionaire.indicator.SummerTimeCheckSchedule;
import afterwork.millionaire.trading.oauth.OauthService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class OverseasStockTimeTraderBot {

    private final OverseasStockTimeTraderBotService overseasStockService;
    private final OauthService oauthService;
    private final SummerTimeCheckSchedule summerTimeCheckSchedule;


    /**
     * 애플리케이션 초기화 메서드.
     */
    @PostConstruct
    public void initialize() {
        log.info("OverseasStockTimeTraderBot 초기화 완료");
    }

    /**
     * 매일 특정 시간에 OAuth 토큰 발급.
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void issueToken() {
        log.info("토큰 발급 작업 시작...");
        oauthService.getAccessToken()
                .doOnSuccess(token -> log.info("토큰 발급 완료: {}", token))
                .doOnError(error -> log.error("토큰 발급 실패: {}", error.getMessage()))
                .subscribe();
    }

    /**
     * 매수 작업 스케줄러.
     */
    @Scheduled(cron = "0 1 2 * * *", zone = "Asia/Seoul")
    public void performBuyTask() {
        log.info("해외 주식 매수 작업 시작...");
        executeTradingSteps()
                .doOnSuccess(unused -> log.info("매수 작업 완료"))
                .doOnError(error -> log.error("매수 작업 중 오류 발생: {}", error.getMessage()))
                .subscribe();
    }

    /**
     * 매도 작업 스케줄러.
     */
    @Scheduled(cron = "0 59 5 * * *", zone = "Asia/Seoul")
    public void performSellTask() {
        log.info("해외 주식 매도 작업 시작...");
        overseasStockService.overseasStockTradingOrder("00")
                .doOnSuccess(orderResponse -> log.info("매도 주문 성공: {}", orderResponse))
                .doOnError(error -> log.error("매도 주문 실패: {}", error.getMessage()))
                .subscribe();
    }

    /**
     * 매수 작업을 단계별로 실행.
     */
    private Mono<Void> executeTradingSteps() {
        return queryTradingStatus()
                .flatMap(status -> {
                    log.info("트레이딩 상태 조회 완료: {}", status);
                    return getCurrentPrice();
                })
                .flatMap(price -> {
                    log.info("현재 체결가: {}", price);
                    return getBuyableAmount();
                })
                .flatMap(amount -> {
                    log.info("매수 가능 수량: {}", amount);
                    return placeBuyOrder();
                })
                .doOnSuccess(unused -> log.info("모든 매수 작업 완료"))
                .doOnError(error -> log.error("매수 작업 중 오류 발생: {}", error.getMessage()));
    }

    /**
     * Step 1: 트레이딩 상태 확인.
     */
    private Mono<String> queryTradingStatus() {
        return overseasStockService.getOverseasStockTimeRangeQuery()
                .doOnNext(status -> log.info("트레이딩 상태: {}", status));
    }

    /**
     * Step 2: 현재 체결가 조회.
     */
    private Mono<String> getCurrentPrice() {
        return overseasStockService.getOverseasStockPrice()
                .doOnNext(price -> log.info("현재 체결가: {}", price));
    }

    /**
     * Step 3: 매수 가능 수량 조회 (1초 대기 후 실행).
     */
    private Mono<String> getBuyableAmount() {
        return Mono.delay(Duration.ofSeconds(1))
                .then(overseasStockService.getOverseasStockBuyableAmount()
                        .doOnNext(amount -> log.info("매수 가능 수량: {}", amount)));
    }

    /**
     * Step 4: 매수 주문 실행.
     */
    private Mono<Void> placeBuyOrder() {
        return overseasStockService.overseasStockTradingOrder("")
                .doOnSuccess(orderResponse -> log.info("매수 주문 성공: {}", orderResponse))
                .doOnError(error -> log.error("매수 주문 실패: {}", error.getMessage()))
                .then();
    }

    // 주식시장 장 시작 시간에 대한 스케줄 메서드
    //@Scheduled(cron = "#{@summerTimeCheckSchedule.getStartCron()}", zone = "Asia/Seoul")  // 동적으로 크론식 설정
    public void marketOpen() {
        System.out.println("주식시장 시작");
        // 여기에 장 시작에 대한 처리 로직을 작성합니다.
    }

    // 주식시장 장 종료 시간에 대한 스케줄 메서드
    //@Scheduled(cron = "#{@summerTimeCheckSchedule.getEndCron()}", zone = "Asia/Seoul")  // 동적으로 크론식 설정
    public void marketClose() {
        System.out.println("주식시장 종료");
        // 여기에 장 종료에 대한 처리 로직을 작성합니다.
    }
}
