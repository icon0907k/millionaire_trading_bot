package afterwork.millionaire.trading.oauth;

import afterwork.millionaire.config.ApiProperties;
import afterwork.millionaire.trading.dto.BaseInput;
import afterwork.millionaire.util.Input;
import afterwork.millionaire.util.WebClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

@Service
public class OauthService {

    @Autowired
    private ApiProperties apiProperties;

    /**
     * 액세스 토큰을 비동기적으로 발급받고, 발급된 토큰을 처리하여 반환하는 메서드.
     * - 요청 헤더와 파라미터를 설정하고 외부 API로 POST 요청을 보냄.
     * - 응답에서 액세스 토큰을 추출하고, 이를 BaseInput 클래스의 authorization 필드에 추가.
     *
     * @return 액세스 토큰 발급 결과를 나타내는 Mono<String> (성공 시 "ok" 반환)
     */
    public Mono<String> getAccessToken() {

        // 액세스 토큰 요청을 위한 파라미터 설정
        Input input = new Input();
        input.put("headers", "content-type", BaseInput.contentType);  // 요청 헤더에 content-type 추가
        input.put("request", "grant_type", "client_credentials");  // 요청 파라미터에 grant_type 설정
        input.put("request", "appkey", BaseInput.appkey);  // 요청 파라미터에 appkey 설정
        input.put("request", "appsecret", BaseInput.appsecret);  // 요청 파라미터에 appsecret 설정

        // WebClient를 사용하여 POST 요청을 보내고, 그 결과를 비동기적으로 처리
        return WebClientUtils.sendPostRequest(apiProperties.getToken(), input.out("headers"), input.out("request"))
                .flatMap(response -> {
                    // 응답에서 액세스 토큰을 추출하여 BaseInput.authorization에 추가
                    BaseInput.authorization += Objects.requireNonNull(response.getBody()).get("access_token");
                    // 성공적으로 토큰을 처리한 후 "ok" 반환
                    return Mono.just("ok");
                });
    }
}
