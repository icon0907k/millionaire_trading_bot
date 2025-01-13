package afterwork.millionaire.trading.domestic;

import afterwork.millionaire.config.ApiProperties;
import afterwork.millionaire.trading.dto.BaseInput;
import afterwork.millionaire.util.Input;
import afterwork.millionaire.util.WebClientUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class DomesticStockTimeTraderBotService {

    @Autowired
    private ApiProperties apiProperties;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 국내 주식의 매수 가능한 수량을 조회하는 메서드
     * @return 매수 가능한 수량
     */
    public Mono<String> getDomesticStockBuyableAmount() {

        Input input = createInput("VTTC8908R");
        input.put("request", "CANO", BaseInput.CANO);
        input.put("request", "ACNT_PRDT_CD", BaseInput.ACNT_PRDT_CD);
        input.put("request", "PDNO", getPdnoByStatus(BaseInput.dStatus));
        input.put("request", "ORD_UNPR", "0");
        input.put("request", "ORD_DVSN", "01");
        input.put("request", "OVRS_ICLD_YN", "N");
        input.put("request", "CMA_EVLU_AMT_ICLD_YN", "N");

        return WebClientUtils.sendGetRequestObtype(apiProperties.getDomesticTradingInquirePsamount(),
                    input.out("headers"), input.out("request"))
                    .flatMap(response -> {
                        Map<String, Object> responseBody = response.getBody();
                        Map<String, Object> output = (Map<String, Object>) responseBody.get("output");
                        BaseInput.dAmount = output != null ? output.getOrDefault("nrcvb_buy_qty", "0").toString() : "0";
                    return Mono.just(BaseInput.dAmount);
                });
    }

    /**
     * 국내 주식 시간 범위 조회 메서드
     * @return 조회 상태
     */
    public Mono<String> getDomesticStockTimeRangeQuery() {
        // API 요청에 필요한 파라미터 설정
        Input input = createInput("FHKST03010200");
        input.put("request","FID_ETC_CLS_CODE","");
        input.put("request","FID_COND_MRKT_DIV_CODE","J");
        input.put("request","FID_INPUT_ISCD", BaseInput.CO233740);
        input.put("request","FID_INPUT_HOUR_1","112900");
        input.put("request","FID_PW_DATA_INCU_YN","N");
        input.put("request","TIME1","090000");
        input.put("request","TIME2","112900");
        // WebClient를 사용하여 GET 요청을 보내고 응답을 처리
        return WebClientUtils.sendGetRequest(
                        apiProperties.getDomesticInquireTimeItemchartpriceTimerangQuery(),
                        input.out("headers"), input.out("request"))
                .flatMap(response -> {
                    // 응답에서 상태(status) 값 추출
                    BaseInput.dStatus = Objects.requireNonNull(response.getBody()).get("status"); // 상태 저장

                    // status 값을 반환하는 Mono<String>으로 변환
                    return Mono.just(BaseInput.dStatus);
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

        // API 요청에 필요한 파라미터 설정
        Input input = createInput(trId);
        input.put("request", "CANO", BaseInput.CANO);
        input.put("request", "ACNT_PRDT_CD", BaseInput.ACNT_PRDT_CD);
        input.put("request", "PDNO",  getPdnoByStatus(BaseInput.dStatus));
        input.put("request", "ORD_DVSN", "01");
        input.put("request", "ORD_QTY", BaseInput.dAmount);
        input.put("request", "ORD_UNPR", "0");

        return WebClientUtils.sendPostRequestObType(
                apiProperties.getDomesticTradingOrder(),
                input.out("headers"),
                input.out("request")
        ).flatMap(response -> {
            // 응답 데이터에서 결과 코드 추출 및 반환
            Map<String, Object> responseBody = response.getBody();
            Object rtCd = responseBody.get("rt_cd");
            return Mono.just((String) rtCd);
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
            return BaseInput.CO252670;
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
        return input;
    }
}
