package afterwork.millionaire.trading.domestic;

import afterwork.millionaire.trading.dto.BaseInput;
import afterwork.millionaire.trading.oauth.OauthService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * DomesticStockTimeTraderBot
 * 국내 주식 거래 봇을 관리하는 클래스입니다.
 * 주요 기능:
 * - 정해진 시간에 OAuth 토큰 발급
 * - 정해진 시간에 매수 및 매도 작업 수행
 * - 스케줄 작업을 실행하기 위한 기능 제공
 */
@Slf4j
@Component
public class DomesticStockTimeTraderBot {

    // 작업을 예약할 수 있는 스레드 풀 생성
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // 서비스 및 OAuth 관련 의존성 주입
    @Autowired
    private DomesticStockTimeTraderBotService domesticStockTimeTraderBotService;

    @Autowired
    private OauthService oauthService;

    /**
     * 애플리케이션 초기화 시 실행되는 메서드
     * - 필요한 초기화 작업을 추가할 수 있습니다.
     */
    @PostConstruct
    public void initialize() {
        log.info("DomesticStockTimeTraderBot 초기화 완료");
    }

    /**
     * 특정 서울 시간에 작업을 예약하는 메서드
     * @param scheduledTime 예약할 시간 (LocalTime 형식)
     * @param task 실행할 작업 (Runnable 형식)
     */
    public void scheduleTaskAtSeoulTime(LocalTime scheduledTime, Runnable task) {
        ZoneId seoulZone = ZoneId.of("Asia/Seoul"); // 서울 시간대 설정
        ZonedDateTime now = ZonedDateTime.now(seoulZone); // 현재 서울 시간 가져오기
        ZonedDateTime target = now.withHour(scheduledTime.getHour())
                .withMinute(scheduledTime.getMinute())
                .withSecond(0);

        // 예약 시간이 현재 시간보다 이전이면 다음 날로 예약
        if (target.isBefore(now)) {
            target = target.plusDays(1);
        }

        long delay = Duration.between(now, target).getSeconds(); // 예약 시간까지 남은 초 계산

        log.info("트레이딩 작업이 서울 시간 {}에 실행되도록 예약되었습니다.", target);

        // 작업 예약
        scheduler.schedule(() -> {
            log.info("작업 실행: {}", ZonedDateTime.now(seoulZone));
            task.run(); // 전달된 작업 실행
        }, delay, TimeUnit.SECONDS);
    }

    /**
     * 매일 오전 8시 35분에 OAuth 토큰 발급 작업을 실행하는 메서드
     */
    @Scheduled(cron = "0 0 11 * * *", zone = "Asia/Seoul")
    public void issueToken() {
        log.info("토큰 발급 작업 수행 중...");
        oauthService.getAccessToken()
                .doOnNext(token -> log.info("토큰 발급 완료."))
                .subscribe(); // 비동기로 토큰 발급 수행
    }

    /**
     * 매일 오전 11시 29분 57초에 매수 작업을 실행하는 메서드
     */
    @Scheduled(cron = "57 29 11 * * *", zone = "Asia/Seoul")
    public void performTradingTask() {
        log.info("트레이딩 국내주식 매수 작업 수행 중...");
        step1(); // 매수 작업의 첫 번째 단계 실행
    }

    /**
     * 매수 작업 Step 1: 상태 조회 및 다음 단계 호출
     */
    public void step1() {
        domesticStockTimeTraderBotService.getDomesticStockTimeRangeQuery()
                .flatMap(status -> {
                    log.info("상태 조회 완료: {}", status);
                    return step2(); // Step 2로 이동
                }).subscribe(); // 비동기로 실행
    }

    /**
     * 매수 작업 Step 2: 구매 가능 수량 조회 및 매수 실행
     * @return Mono<Void> 비동기 작업 결과
     */
    public Mono<String> step2() {
        return Mono.delay(Duration.ofSeconds(1)) // 대기 시간 추가
                .then(domesticStockTimeTraderBotService.getDomesticStockBuyableAmount()) // 구매 가능 수량 조회
                .flatMap(amount -> {
                    log.info("국내주식 구매가능 수량 : {}", amount);
                    return Mono.delay(Duration.ofSeconds(1))
                            .then(domesticStockTimeTraderBotService.domesticStockTradingOrder("1")); // 매수 주문 실행
                })
                .doOnNext(orderResponse -> log.info("국내주식 매수 완료: {}", orderResponse));
    }

    /**
     * 매일 오후 1시 59분 59초에 매도 작업을 실행하는 메서드
     */
    @Scheduled(cron = "59 59 13 * * *", zone = "Asia/Seoul")
    public void performSellTask() {
        log.info("트레이딩 매도 작업 수행 중...");
        domesticStockTimeTraderBotService.domesticStockTradingOrder("2") // 매도 주문 실행
                .doOnNext(orderResponse -> log.info("국내주식 매도 완료: {}", orderResponse))
                .subscribe(); // 비동기로 실행
    }

    /**
     * 애플리케이션 종료 시 스케줄러를 종료하는 메서드
     */
    @PreDestroy
    public void shutdownScheduler() {
        scheduler.shutdown(); // 스케줄러 종료
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) { // 최대 60초 대기
                scheduler.shutdownNow(); // 강제 종료
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow(); // 예외 발생 시 강제 종료
        }
        log.info("스케줄러가 종료되었습니다.");
    }
}
