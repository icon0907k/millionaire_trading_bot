package afterwork.millionaire.trading.domestic;

import afterwork.millionaire.config.ApiProperties;
import afterwork.millionaire.trading.dto.BaseInput;
import afterwork.millionaire.util.Input;
import afterwork.millionaire.util.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DomesticStockTimeTraderBotService {

    @Autowired
    private ApiProperties apiProperties;

    /**
     * 국내 주식의 매수 가능한 수량을 조회하는 메서드
     * @return 매수 가능한 수량
     */
    public Mono<String> getDomesticStockBuyableAmount() {

        // API 요청에 필요한 파라미터 설정
        Input input = createInput("VTTC8908R");

        // WebClient를 사용하여 GET 요청을 보내고 응답을 처리
        return WebClientUtils.sendGetRequest(apiProperties.getDomesticTradingInquirePsamount(),
                        input.out("headers"), input.out("request"))
                .flatMap(response -> {
                    // 응답에서 "output" 객체 추출
                    Map<String, Object> responseBody = response.getBody();
                    Map<String, Object> output = (Map<String, Object>) responseBody.get("output");

                    // "output"에서 max_buy_qty 값을 추출하여 반환
                    return Mono.just((String) output.get("nrcvb_buy_qty"));
                });
    }

    /**
     * 국내 주식 시간 범위 조회 메서드
     * @return 조회 상태
     */
    public Mono<String> getDomesticStockTimeRangeQuery() {

        // API 요청에 필요한 파라미터 설정
        Input input = createInput("FHKST03010200");

        // WebClient를 사용하여 GET 요청을 보내고 응답을 처리
        return WebClientUtils.sendGetRequest(apiProperties.getDomesticInquireTimeItemchartpriceTimerangQuery(),
                        input.out("headers"), input.out("request"))
                .flatMap(response -> {
                    // 응답에서 상태(status) 값 추출
                    Map<String, Object> responseBody = response.getBody();
                    return Mono.just((String) responseBody.get("status"));
                });
    }

    /**
     * 국내 주식 거래 주문을 처리하는 메서드
     * @param orderType 주문 타입 ("1" 또는 "2")
     * @return 처리된 결과 코드
     */
    public Mono<String> domesticStockTradingOrder(String orderType) {

        // 주문 타입에 맞는 tr_id와 PDNO 설정
        String trId = orderType.equals("1") ? "VTTC0802U" : "VTTC0801U";
        String pdno = getPdnoByStatus(BaseInput.dStatus);

        // PDNO가 null일 경우 예외 처리
        if (pdno == null) {
            Map<String, String> output = new HashMap<>();
            output.put("rt_cd", "1");
            return Mono.just(output.get("rt_cd"));
        }

        // API 요청에 필요한 파라미터 설정
        Input input = createInput(trId);
        input.put("request", "PDNO", pdno);
        input.put("request", "ORD_QTY", BaseInput.dAmount);

        // WebClient를 사용하여 POST 요청을 보내고 응답을 처리
        return WebClientUtils.sendPostRequest(apiProperties.getDomesticTradingOrder(),
                        input.out("headers"), input.out("request"))
                .flatMap(response -> {
                    // 응답에서 결과 코드(rt_cd) 추출
                    Map<String, Object> output = response.getBody();
                    return Mono.just((String) output.get("rt_cd"));
                });
    }

    /**
     * dStatus 값에 따라 pdno 값을 반환하는 메서드
     * @param dStatus 상태 값 ("1" 또는 "2")
     * @return pdno 값 또는 null
     */
    public String getPdnoByStatus(String dStatus) {
        // 상태 값에 맞는 PDNO 반환
        if ("1".equals(dStatus)) {
            return BaseInput.CO233740;
        } else if ("2".equals(dStatus)) {
            return BaseInput.COQ520057;
        } else {
            return null;
        }
    }

    /**
     * 공통된 Input 객체 생성 메서드
     * @param trId 요청 ID
     * @return 설정된 Input 객체
     */
    private Input createInput(String trId) {
        Input input = new Input();
        input.put("headers", "content-type", BaseInput.contentType);
        input.put("headers", "authorization", BaseInput.authorization);
        input.put("headers", "appkey", BaseInput.appkey);
        input.put("headers", "appsecret", BaseInput.appsecret);
        input.put("headers", "tr_id", trId);
        input.put("request", "CANO", BaseInput.CANO);
        input.put("request", "ACNT_PRDT_CD", BaseInput.ACNT_PRDT_CD);
        return input;
    }
}
