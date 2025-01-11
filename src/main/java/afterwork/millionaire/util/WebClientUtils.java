package afterwork.millionaire.util;

import afterwork.millionaire.config.ApiProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Map;

/**
 * WebClient를 활용하여 외부 API와 통신하는 유틸리티 클래스입니다.
 * 이 클래스는 외부 API와의 통신을 담당하며, HTTP 요청을 보내고 응답을 처리합니다.
 */
@Component
@Slf4j
public class WebClientUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static WebClient webClient;  // WebClient 인스턴스

    /**
     * WebClientUtils 생성자.
     * Spring에서 WebClient와 관련된 설정을 주입받아 WebClient 인스턴스를 초기화합니다.
     *
     * @param webClientBuilder WebClient를 생성하는 빌더 객체
     * @param apiProperties API와 관련된 설정 정보를 포함한 객체
     */
    @Autowired
    public WebClientUtils(WebClient.Builder webClientBuilder, ApiProperties apiProperties ) {
        // 주입받은 baseUrl을 사용하여 WebClient를 초기화
        webClient = webClientBuilder.baseUrl(apiProperties.getBaseUrl()).build();
    }

    /**
     * 외부 API에 POST 요청을 보내는 메서드입니다.
     * 주어진 URL, 헤더, 본문 데이터를 사용하여 POST 요청을 비동기적으로 처리합니다.
     *
     * @param url 요청을 보낼 URL (Base URL 이후 경로)
     * @param hInput 요청 헤더에 추가할 키-값 쌍
     * @param bInput 요청 본문에 추가할 키-값 쌍
     * @return 응답을 포함한 Mono 객체 (비동기 응답 처리)
     */
    public static Mono<ResponseEntity<Map<String, String>>> sendPostRequest(
            String url,
            Map<String, String> hInput, // 요청 헤더에 추가할 키-값 쌍
            Map<String, String> bInput  // 요청 본문에 추가할 키-값 쌍
    ) {
        // WebClient를 사용하여 POST 요청을 보내고 응답을 처리
        return webClient.post()
                .uri(url) // 요청할 URL 설정
                .headers(httpHeaders -> httpHeaders.addAll(convertToHttpHeaders(hInput))) // 헤더 설정
                .bodyValue(bInput) // 본문 설정
                .retrieve() // 요청 실행 및 응답 처리
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {}) // 응답 데이터를 Map 형태로 변환
                .flatMap(responseBody ->
                        Mono.just(ResponseEntity.ok(responseBody)) // 응답 데이터를 ResponseEntity로 감싸서 반환
                )
                .onErrorResume(e -> {
                    // 외부 호출 중 발생한 에러를 로그로 기록하고, ErrorUtils를 통해 에러 응답 생성
                    log.error("외부 호출 에러: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "외부 호출 에러입니다.")));
                });
    }

    /**
     * 외부 API에 POST 요청을 보내는 메서드입니다.
     * 주어진 URL, 헤더, 본문 데이터를 사용하여 POST 요청을 비동기적으로 처리합니다.
     *
     * @param url 요청을 보낼 URL (Base URL 이후 경로)
     * @param hInput 요청 헤더에 추가할 키-값 쌍
     * @param bInput 요청 본문에 추가할 키-값 쌍
     * @return 응답을 포함한 Mono 객체 (비동기 응답 처리)
     */
    public static Mono<ResponseEntity<Map<String,Object>>> sendPostRequestObType(
            String url,
            Map<String, String> hInput, // 요청 헤더에 추가할 키-값 쌍
            Map<String, String> bInput  // 요청 본문에 추가할 키-값 쌍
    ) {
        // WebClient를 사용하여 POST 요청을 보내고 응답을 처리
        return webClient.post()
                .uri(url) // 요청할 URL 설정
                .headers(httpHeaders -> httpHeaders.addAll(convertToHttpHeaders(hInput))) // 헤더 설정
                .bodyValue(bInput) // 본문 설정
                .retrieve() // 요청 실행 및 응답 처리
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}) // 응답 데이터를 Map 형태로 변환
                .flatMap(responseBody ->
                        Mono.just(ResponseEntity.ok(responseBody)) // 응답 데이터를 ResponseEntity로 감싸서 반환
                )
                .onErrorResume(e -> {
                    // 외부 호출 중 발생한 에러를 로그로 기록하고, ErrorUtils를 통해 에러 응답 생성
                    log.error("외부 호출 에러: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "외부 호출 에러입니다.")));
                });
    }

    /**
     * 외부 API에 GET 요청을 보내는 메서드입니다.
     * 요청 URL, 헤더, 쿼리 파라미터 데이터를 사용하여 GET 요청을 비동기적으로 처리합니다.
     *
     * @param url 요청을 보낼 URL (Base URL 이후 경로)
     * @param hInput 요청 헤더에 추가할 키-값 쌍
     * @param bInput 요청 본문에 추가할 쿼리 파라미터 키-값 쌍
     * @return 응답을 포함한 Mono 객체 (비동기 응답 처리)
     */
    public static Mono<ResponseEntity<Map<String, String>>> sendGetRequest(
            String url,
            Map<String, String> hInput, // 요청 헤더에 추가할 키-값 쌍
            Map<String, String> bInput  // 요청 본문에 추가할 쿼리 파라미터 키-값 쌍
    ) {
        // WebClient를 사용하여 GET 요청을 보내고 응답을 처리
        return webClient.get()
                .uri(uriBuilder -> {
                    var uriBuilderWithParams = uriBuilder.path(url);
                    // 쿼리 파라미터 추가
                    bInput.forEach(uriBuilderWithParams::queryParam);
                    return uriBuilderWithParams.build();
                })
                .headers(httpHeaders -> httpHeaders.addAll(convertToHttpHeaders(hInput))) // 헤더 설정
                .retrieve() // 요청 실행 및 응답 처리
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {}) // 응답 데이터를 Map 형태로 변환
                .flatMap(responseBody ->
                        Mono.just(ResponseEntity.ok(responseBody)) // 응답 데이터를 ResponseEntity로 감싸서 반환
                )
                .onErrorResume(e -> {
                    // 외부 호출 중 발생한 에러를 로그로 기록하고, ErrorUtils를 통해 에러 응답 생성
                    log.error("외부 호출 에러: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "외부 호출 에러입니다.")));
                });
    }
    /**
     * 외부 API에 GET 요청을 보내는 메서드입니다.
     * 요청 URL, 헤더, 쿼리 파라미터 데이터를 사용하여 GET 요청을 비동기적으로 처리합니다.
     *
     * @param url 요청을 보낼 URL (Base URL 이후 경로)
     * @param hInput 요청 헤더에 추가할 키-값 쌍
     * @param bInput 요청 본문에 추가할 쿼리 파라미터 키-값 쌍
     * @return 응답을 포함한 Mono 객체 (비동기 응답 처리)
     */
    public static Mono<ResponseEntity<Map<String, Object>>> sendGetRequestObtype(
            String url,
            Map<String, String> hInput, // 요청 헤더에 추가할 키-값 쌍
            Map<String, String> bInput  // 요청 본문에 추가할 쿼리 파라미터 키-값 쌍
    ) {
        // WebClient를 사용하여 GET 요청을 보내고 응답을 처리
        return webClient.get()
                .uri(uriBuilder -> {
                    var uriBuilderWithParams = uriBuilder.path(url);
                    // 쿼리 파라미터 추가
                    bInput.forEach(uriBuilderWithParams::queryParam);
                    return uriBuilderWithParams.build();
                })
                .headers(httpHeaders -> httpHeaders.addAll(convertToHttpHeaders(hInput))) // 헤더 설정
                .retrieve() // 요청 실행 및 응답 처리
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}) // 응답 데이터를 Map 형태로 변환
                .flatMap(responseBody ->
                        Mono.just(ResponseEntity.ok(responseBody)) // 응답 데이터를 ResponseEntity로 감싸서 반환
                )
                .onErrorResume(e -> {
                    // 외부 호출 중 발생한 에러를 로그로 기록하고, ErrorUtils를 통해 에러 응답 생성
                    log.error("외부 호출 에러: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "외부 호출 에러입니다.")));
                });
    }
    /**
     * 주어진 경로에서 JSON 파일을 읽고, 이를 Map으로 변환하는 메서드입니다.
     * 파일 경로를 통해 JSON 파일을 읽고, ObjectMapper를 사용하여 이를 Map 형태로 변환합니다.
     *
     * @param jsonFilePath JSON 파일 경로
     * @return JSON 파일을 Map으로 변환한 데이터
     * @throws Exception 파일 읽기 또는 JSON 파싱 중 발생할 수 있는 예외
     */
    private static Map<String, String> readJsonFile(String jsonFilePath) throws Exception {
        File jsonFile = new File(jsonFilePath);
        // ObjectMapper로 JSON 파일을 Map으로 변환
        return objectMapper.readValue(jsonFile, new TypeReference<>() {});
    }

    /**
     * Map을 HttpHeaders로 변환하는 메서드입니다.
     * Map의 각 항목을 HttpHeaders 객체로 변환하여 반환합니다.
     *
     * @param map 변환할 Map 객체
     * @return HttpHeaders로 변환된 헤더 객체
     */
    private static HttpHeaders convertToHttpHeaders(Map<String, String> map) {
        HttpHeaders headers = new HttpHeaders();
        map.forEach(headers::add); // Map의 각 항목을 HttpHeaders로 변환
        return headers;
    }
}
