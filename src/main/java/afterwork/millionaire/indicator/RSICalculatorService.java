package afterwork.millionaire.indicator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.text.DecimalFormat;
import java.util.List;

@Service
@Slf4j
public class RSICalculatorService {

    public static double calculateRSI(List<Double> closePrices, int period) {
        if (closePrices == null || closePrices.size() < period + 1) {
            log.info("RSI 계산을 위한 데이터가 부족합니다. 최소 " + (period + 1) + "개의 가격이 필요합니다.");
            return -1; // 오류 표시 값
        }
        log.info("입력 데이터: " + closePrices);
        double gainSum = 0;
        double lossSum = 0;
        double lastRSI = 0;

        // 초기 평균 상승/하락 계산 (첫 번째 변화값부터 계산, period-1개만 포함)
        for (int i = 1; i < period; i++) { // i < period로 수정
            double change = closePrices.get(i) - closePrices.get(i - 1);
            if (change > 0) {
                gainSum += change;
            } else {
                lossSum += Math.abs(change);
            }
        }

        // 초기 평균 상승/하락 계산
        double avgGain = gainSum / (period - 1);  // period - 1로 수정
        double avgLoss = lossSum / (period - 1);  // period - 1로 수정

        // RSI 계산
        for (int i = period; i < closePrices.size(); i++) { // i < closePrices.size()로 수정
            double change = closePrices.get(i) - closePrices.get(i - 1);
            double gain = Math.max(change, 0);
            double loss = Math.abs(Math.min(change, 0));

            avgGain = ((avgGain * (period - 1)) + gain) / period;
            avgLoss = ((avgLoss * (period - 1)) + loss) / period;

            double rs = (avgLoss == 0) ? 100 : avgGain / avgLoss;
            double rsi = 100 - (100 / (1 + rs));

            lastRSI = rsi; // 마지막 RSI 값 저장
        }

        // 마지막 RSI 값은 소수점 두 자리로 반올림
        // 소수점 두 자리까지 반올림
        return Math.round(lastRSI * 100.0) / 100.0;
    }


}
