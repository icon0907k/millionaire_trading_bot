package afterwork.millionaire.trading.controller;

import afterwork.millionaire.trading.domestic.DomesticStockTimeTraderBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;

@RestController
public class TradingController {

    @Autowired
    private DomesticStockTimeTraderBot traderBot;

    /*
    @GetMapping("/start-trading")
    public String startTrading(int hour, int minute, int second) {
        traderBot.scheduleTaskAtSeoulTime(LocalTime.of(hour, minute, second),""); // 예: 15:30에 실행 예약
        return "트레이딩 작업이 예약되었습니다.";
    }
     */
}