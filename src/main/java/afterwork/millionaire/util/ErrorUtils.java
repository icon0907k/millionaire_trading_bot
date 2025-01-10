package afterwork.millionaire.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ErrorUtils {

    // ObjectMapper 인스턴스를 사용하여 JSON 데이터를 처리합니다.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * WebClientResponseException을 받아서 에러 메시지와 세부 정보를 포함하는 응답을 생성합니다.
     *
     * - WebClient에서 발생한 예외를 처리하고, 응답 본문에서 JSON 데이터를 추출하여 파싱합니다.
     * - 이 메서드는 에러 응답을 클라이언트에게 반환하는데 사용됩니다.
     *
     * @param message 사용자 지정 에러 메시지
     * @param errorDetails WebClientResponseException 예외 객체
     * @return 에러 메시지와 세부 정보가 포함된 ResponseEntity 객체
     */
    public static ResponseEntity<Map<String, Object>> createErrorResponse(String message, Throwable errorDetails) {
        try {
            // 응답에 포함될 에러 정보를 담을 HashMap 생성
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");  // 상태는 항상 'error'
            errorResponse.put("message", message);  // 사용자 지정 메시지 추가

            // WebClientResponseException 예외를 캐스팅하여 세부 정보를 추출
            WebClientResponseException webClientError = (WebClientResponseException) errorDetails;
            String errorMessage = webClientError.getResponseBodyAsString();  // 응답 본문을 문자열로 가져옴

            // 응답 본문에서 JSON 형식의 데이터를 추출하여 파싱
            String detailsJson = extractJsonFromErrorDetails(errorMessage);  // JSON 부분 추출
            Map<String, Object> parsedDetails = objectMapper.readValue(detailsJson, new TypeReference<>() {});

            // 파싱된 세부 정보를 에러 응답에 추가
            errorResponse.put("errorDetails", parsedDetails);

            // 서버 오류를 나타내는 HTTP 상태 코드 500과 함께 응답 반환
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            // JSON 파싱에 실패한 경우 예외 처리
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");  // 상태는 항상 'error'
            errorResponse.put("message", message);  // 사용자 지정 메시지 추가
            errorResponse.put("errorDetails", e.getMessage());  // 예외 메시지를 에러 세부 정보로 추가

            // 서버 오류를 나타내는 HTTP 상태 코드 500과 함께 응답 반환
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 에러 메시지에서 JSON 형식의 데이터를 추출하는 메서드.
     *
     * - WebClientResponseException에서 응답 본문이 JSON 형식일 때,
     *   그 부분만 추출하여 반환합니다.
     *
     * @param errorDetails 에러 메시지 내에서 JSON 부분을 포함하는 문자열
     * @return JSON 부분만 추출한 문자열
     */
    private static String extractJsonFromErrorDetails(String errorDetails) {
        // errorDetails가 문자열로 되어있을 때, JSON 형식의 부분을 추출하는 로직
        int start = errorDetails.indexOf("{");  // JSON 시작 위치
        int end = errorDetails.lastIndexOf("}") + 1;  // JSON 끝 위치

        // JSON 시작과 끝을 기준으로 부분 문자열을 추출하여 반환
        return errorDetails.substring(start, end);
    }
}
