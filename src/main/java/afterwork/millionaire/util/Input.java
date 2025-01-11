package afterwork.millionaire.util;

import afterwork.millionaire.trading.dto.BaseInput;

import java.util.HashMap;
import java.util.Map;

public class Input {

    // 각 인스턴스에서만 사용할 데이터를 저장할 Map
    // 각 카테고리마다 별도의 Map을 생성하여 데이터를 관리
    private Map<String, Map<String, String>> inputData = new HashMap<>();

    /**
     * 데이터를 저장하는 메서드.
     * 주어진 카테고리 내에 데이터를 저장합니다.
     *
     * @param category 데이터를 저장할 카테고리
     * @param key 데이터를 저장할 키
     * @param value 데이터를 저장할 값
     */
    public void put(String category, String key, String value) {
        // 카테고리가 없으면 새로 생성하고, 주어진 키와 값을 저장
        inputData.computeIfAbsent(category, k -> new HashMap<>()).put(key, value);
    }

    /**
     * 카테고리별 데이터를 조회하는 메서드.
     * 주어진 카테고리의 데이터를 반환합니다.
     *
     * @param category 조회할 카테고리
     * @return 해당 카테고리의 데이터 Map, 카테고리가 없으면 빈 Map 반환
     */
    public Map<String, String> out(String category) {
        return inputData.getOrDefault(category, new HashMap<>());  // 카테고리가 없으면 빈 Map 반환
    }

}
