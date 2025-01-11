package afterwork.millionaire.trading.overseas;

import afterwork.millionaire.trading.oauth.OauthService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class OverseasStockTimeTraderBot {

    // 해외 주식 트레이딩 서비스
    @Autowired
    private OverseasStockTimeTraderBotService overseasStockService;

    // OAuth 인증 서비스
    @Autowired
    private OauthService oauthService;

    /**
     * 애플리케이션 초기화 메서드
     * - 애플리케이션 시작 시 필요한 초기 작업 수행
     */
    @PostConstruct
    public void initialize() {
        log.info("OverseasStockTimeTraderBot 초기화 완료");
    }

    /**
     * 매수 작업 스케줄러
     * - 매일 새벽 1시 59분 58초에 매수 작업 실행
     */
    @Scheduled(cron = "58 59 1 * * *", zone = "Asia/Seoul")
    public void performBuyTask() {
        log.info("해외 주식 매수 작업 시작...");
        executeTradingSteps()
                .subscribe(
                        unused -> log.info("매수 작업 완료"),
                        error -> log.error("매수 작업 중 오류 발생: {}", error.getMessage())
                );
    }

    /**
     * 매도 작업 스케줄러
     * - 매일 오전 5시 59분 59초에 매도 작업 실행
     */
    @Scheduled(cron = "59 59 5 * * *", zone = "Asia/Seoul")
    public void performSellTask() {
        log.info("해외 주식 매도 작업 시작...");
        overseasStockService.overseasStockTradingOrder("00") // "00"은 매도 주문 코드
                .doOnNext(orderResponse -> log.info("매도 주문 성공: {}", orderResponse))
                .doOnError(error -> log.error("매도 주문 실패: {}", error.getMessage()))
                .subscribe();
    }

    /**
     * 매수 작업을 단계별로 실행
     * - 각 단계는 비동기로 실행되며 순차적으로 다음 단계로 이동
     */
    private Mono<Void> executeTradingSteps() {
        return queryTradingStatus()
                .then(getCurrentPrice())
                .then(getBuyableAmount())
                .then(placeBuyOrder());
    }

    /**
     * Step 1: 트레이딩 상태 확인
     * - 해외 주식 트레이딩이 가능한 상태인지 조회
     */
    private Mono<Void> queryTradingStatus() {
        return overseasStockService.getOverseasStockTimeRangeQuery()
                .doOnNext(status -> log.info("트레이딩 상태 조회 완료: {}", status))
                .then();
    }

    /**
     * Step 2: 현재 체결가 조회
     * - 주식의 현재 체결가를 확인
     */
    private Mono<Void> getCurrentPrice() {
        return overseasStockService.getOverseasStockPrice()
                .doOnNext(price -> log.info("현재 체결가: {}", price))
                .then();
    }

    /**
     * Step 3: 매수 가능 수량 조회
     * - 매수 가능한 주식 수량을 확인
     * - 1초 대기 후 작업 실행
     */
    private Mono<Void> getBuyableAmount() {
        return Mono.delay(Duration.ofSeconds(1))
                .then(overseasStockService.getOverseasStockBuyableAmount()
                        .doOnNext(amount -> log.info("매수 가능 수량: {}", amount))
                        .then());
    }

    /**
     * Step 4: 매수 주문 실행
     * - 주어진 조건에 따라 매수 주문 수행
     */
    private Mono<Void> placeBuyOrder() {
        return overseasStockService.overseasStockTradingOrder("")
                .doOnSuccess(orderResponse -> log.info("매수 주문 성공: {}", orderResponse))
                .doOnError(error -> log.error("매수 주문 실패: {}", error.getMessage()))
                .then();
    }
}
