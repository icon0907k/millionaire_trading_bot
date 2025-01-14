package afterwork.millionaire.trading.overseas;

import afterwork.millionaire.config.ApiProperties;
import afterwork.millionaire.trading.dto.BaseInput;
import afterwork.millionaire.util.Input;
import afterwork.millionaire.util.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class OverseasStockTimeTraderBotService {

    @Autowired
    private ApiProperties apiProperties;

    /**
     * 해외 주식 현재가를 조회하는 메서드
     * @return Mono<String> 현재가를 반환
     */
    public Mono<String> getOverseasStockPrice() {

        // API 요청에 필요한 파라미터 설정
        Input input = createInput("HHDFS00000300");
        input.put("request", "SYMB", getSymbByStatus(BaseInput.oStatus)); // 종목 코드
        input.put("request", "AUTH", "");
        input.put("request", "EXCD", BaseInput.EXCD); // 거래소 코드

        return WebClientUtils.sendGetRequestObtype(
                apiProperties.getOverseasPrice(),
                input.out("headers"),
                input.out("request")
        ).flatMap(response -> {
            // 응답 데이터를 처리하여 마지막 가격을 저장하고 반환
            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> output = (Map<String, Object>) responseBody.get("output");
            BaseInput.OVRS_ORD_UNPR = (String) output.get("last");
            return Mono.just(BaseInput.OVRS_ORD_UNPR);
        });
    }

    /**
     * 해외 주식 시간대별 가격 조회
     * @return Mono<String> 상태 값 반환
     */
    public Mono<String> getOverseasStockTimeRangeQuery() {

        // API 요청에 필요한 파라미터 설정
        Input input = createInput("HHDFS76950200");
        input.put("headers", "custtype", "P");
        input.put("request", "AUTH", "");
        input.put("request", "EXCD", BaseInput.EXCD);
        input.put("request", "SYMB", "TQQQ"); // 종목 코드
        input.put("request", "PINC", "1"); // 가격 간격
        input.put("request", "NEXT", ""); // 가격 간격
        input.put("request", "NREC", "120"); // 요청 데이터 수
        input.put("request", "FILL", "");
        input.put("request", "KEYB", "");
        input.put("request", "TIME1", "233000"); // 시작 시간
        input.put("request", "TIME2", "020000"); // 종료 시간

        return WebClientUtils.sendGetRequest(
                apiProperties.getOverseasInquireTimeItemchartpriceTimerangQuery(),
                input.out("headers"),
                input.out("request")
        ).flatMap(response -> {
            // 응답에서 상태 값 추출 및 반환
            BaseInput.oStatus = Objects.requireNonNull(response.getBody()).get("status");
            return Mono.just(BaseInput.oStatus);
        });
    }

    /**
     * 해외 주식 매수 가능 수량 조회
     * @return Mono<String> 매수 가능 수량 반환
     */
    public Mono<String> getOverseasStockBuyableAmount() {

        // API 요청에 필요한 파라미터 설정
        Input input = createInput("VTTS3007R");
        input.put("headers", "tr_cont", "");
        input.put("headers", "custtype", "P");
        input.put("headers", "seq_no", "");
        input.put("request", "CANO", BaseInput.CANO); // 계좌 번호
        input.put("request", "ACNT_PRDT_CD", BaseInput.ACNT_PRDT_CD); // 계좌 상품 코드
        input.put("request", "OVRS_EXCG_CD", BaseInput.OVRS_EXCG_CD); // 해외 거래소 코드
        input.put("request", "OVRS_ORD_UNPR", BaseInput.OVRS_ORD_UNPR); // 주문 가격
        input.put("request", "ITEM_CD", getSymbByStatus(BaseInput.oStatus)); // 종목 코드

        return WebClientUtils.sendGetRequestObtype(
                apiProperties.getOverseasTradingInquirePsamount(),
                input.out("headers"),
                input.out("request")
        ).flatMap(response -> {
            // 응답 데이터를 처리하여 매수 가능 수량 반환
            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> output = (Map<String, Object>) responseBody.get("output");
            BaseInput.ORD_QTY = (String) output.get("ovrs_max_ord_psbl_qty");
            return Mono.just(BaseInput.ORD_QTY);
        });
    }

    /**
     * 해외 주식 매매 주문 실행
     * @param orderType 주문 타입 ("" 매수, "00" 매도)
     * @return Mono<String> 결과 코드 반환
     */
    public Mono<String> overseasStockTradingOrder(String orderType) {

        // 주문 타입에 따른 요청 ID 설정
        String trId = orderType.equals("00") ? "VTTT1001U" : "VTTT1002U";

        // API 요청에 필요한 파라미터 설정
        Input input = createInput(trId);
        input.put("request", "CANO", BaseInput.CANO);
        input.put("request", "ACNT_PRDT_CD", BaseInput.ACNT_PRDT_CD);
        input.put("request", "OVRS_EXCG_CD", BaseInput.OVRS_EXCG_CD);
        input.put("request", "PDNO", getSymbByStatus(BaseInput.oStatus)); // 종목 코드
        input.put("request", "ORD_QTY", BaseInput.ORD_QTY); // 주문 수량
        input.put("request", "OVRS_ORD_UNPR", BaseInput.OVRS_ORD_UNPR); // 주문 가격
        input.put("request", "SLL_TYPE", orderType); // 매수/매도 타입
        input.put("request", "ORD_SVR_DVSN_CD", "0");
        input.put("request", "ORD_DVSN", "00");

        return WebClientUtils.sendPostRequestObType(
                apiProperties.getOverseasTradingOrder(),
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
     * 공통 Input 객체 생성
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

    /**
     * 상태 값에 따라 종목 코드 반환
     * @param oStatus 상태 값 ("1" 또는 "2")
     * @return 종목 코드 또는 null
     */
    public String getSymbByStatus(String oStatus) {
        if ("1".equals(oStatus)) {
            return BaseInput.COTQQQ; // 매수 코드
        } else if ("2".equals(oStatus)) {
            return BaseInput.COSQQQ; // 매도 코드
        } else {
            return null;
        }
    }
}
