package afterwork.millionaire.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ApiProperties
 * 애플리케이션 설정 파일(application.properties 또는 application.yml)에서
 * API 관련 속성들을 로드하는 클래스입니다.
 * `@ConfigurationProperties`를 사용하여 API 관련 속성들을 주입받습니다.
 */
@Configuration
@ConfigurationProperties(prefix = "api")  // 'api' 접두어로 시작하는 속성들을 로드
@Getter
@Setter
public class ApiProperties {

    // API 기본 URL
    private String baseUrl;
    // 토큰 발급 URI
    private String token;
    // 토큰 취소 URI
    private String revoke;
    // 해시 키 요청 URI
    private String hashkey;
    // 승인 관련 URI
    private String approval;

    // 해외 주식 관련 URI들
    private String overseasPrice; // 해외주식 현재체결가 URI
    private String overseasDailyPrice; // 해외주식 기간별시세 URI
    private String overseasInquireDailyChartprice; // 해외주식 종목/지수/환율기간별시세 URI
    private String overseasInquireSearch; // 해외주식 조건검색 URI
    private String overseasInquireTimeItemchartprice; // 해외주식 분봉조회 URI
    private String overseasInquireTimeItemchartpriceTimerangQuery; // 해외주식 특정시간 분봉조회 URI
    private String overseasTradingInquirePsamount; // 해외주식 매수가능금액조회 URI
    private String overseasTradingOrder; // 해외주식 주문 URI

    // 국내 주식 관련 URI들
    private String domesticPrice; // 국내주식 현재체결가 URI
    private String domesticInquireTimeItemchartprice; // 국내주식 분봉조회 URI
    private String domesticInquireTimeItemchartpriceTimerangQuery; // 국내주식 특정시간 분봉조회 URI
    private String domesticTradingInquirePsamount; // 국내주식 매수가능금액조회 URI
    private String domesticTradingOrder; // 국내주식 주문 URI
}
