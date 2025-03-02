package afterwork.millionaire.trading.overseas;

import afterwork.millionaire.indicator.RSICalculatorService;
import afterwork.millionaire.indicator.SummerTimeCheckSchedule;
import afterwork.millionaire.trading.oauth.OauthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class OverseasStockTimeTraderBot {
/*  추후 반복 작업 트레이딩 전략이 있을시 작성 예정
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    private final SummerTimeCheckSchedule summerTimeCheckSchedule;

    private final OverseasStockTimeTraderBotService overseasStockTimeTraderBotService;

    private final OauthService oauthService;

    *//**
     * 해외주식시장 스케줄 시작 메서드.
     * 썸머타임 여부에 따라 반복 실행 조건을 확인하며 실행.
     *//*
    @Scheduled(cron = "0 29 22 * * *", zone = "Asia/Seoul")
    public void startMarketSchedule() {
        scheduler.scheduleAtFixedRate(() -> {
            if (summerTimeCheckSchedule.isMarketOpen()) {
                executeMarketLogic();
            }
        }, 0, 1, TimeUnit.SECONDS); // 1초마다 반복 실행
    }

    *//**
     * 해외주식시장 스케줄 종료 메서드.
     *//*
    @Scheduled(cron = "0 1 6 * * *", zone = "Asia/Seoul")
    public void stopMarketSchedule() {
    }

    *//**
     * 장 시간에 반복 실행될 작업.
     *//*
    @Scheduled(cron = "10 29 20 * * *", zone = "Asia/Seoul")
    public void executeMarketLogic() {
    }*/
}
