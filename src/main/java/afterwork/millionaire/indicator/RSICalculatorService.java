package afterwork.millionaire.indicator;

import org.springframework.stereotype.Service;
import java.text.DecimalFormat;
import java.util.List;

@Service
public class RSICalculatorService {

    private static final int PERIOD = 14; // 14일 기준 RSI

    public double calculateRSI(List<Double> closingPrices) {
        if (closingPrices == null || closingPrices.size() < PERIOD) {
            throw new IllegalArgumentException("주어진 데이터가 충분하지 않습니다.");
        }

        // 1. 가격 변화 계산 (Gain, Loss)
        double gain = 0.0;
        double loss = 0.0;

        for (int i = 1; i <= PERIOD; i++) {
            double priceChange = closingPrices.get(i) - closingPrices.get(i - 1);
            if (priceChange > 0) {
                gain += priceChange;
            } else {
                loss -= priceChange; // Loss는 음수이므로 - 값을 더합니다.
            }
        }

        // 2. 평균 Gain, 평균 Loss 계산
        double averageGain = gain / PERIOD;
        double averageLoss = loss / PERIOD;

        // 3. RS (Relative Strength) 계산
        double rs = averageGain / averageLoss;

        // 4. RSI 계산
        double rsi = 100 - (100 / (1 + rs));

        // 5. 소수점 두 자리로 포맷팅
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(rsi));
    }
}
