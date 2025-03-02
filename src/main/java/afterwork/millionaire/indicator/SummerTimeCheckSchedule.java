package afterwork.millionaire.indicator;

import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class SummerTimeCheckSchedule {

    /**
     * 장 시간 조건 확인 메서드.
     * 썸머타임 여부에 따라 시작 및 종료 시간을 비교.
     */
    public boolean isMarketOpen() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime start = getMarketStartTime();
        ZonedDateTime end = getMarketEndTime();

        return now.isAfter(start) && now.isBefore(end);
    }

    /**
     * 장 시작 시간 반환 메서드.
     */
    public ZonedDateTime getMarketStartTime() {
        boolean isDaylightSavingTime = isDaylightSavingTime();
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .withHour(isDaylightSavingTime ? 22 : 23)
                .withMinute(30)
                .withSecond(0);
    }

    /**
     * 장 종료 시간 반환 메서드.
     */
    public ZonedDateTime getMarketEndTime() {
        boolean isDaylightSavingTime = isDaylightSavingTime();
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .withHour(isDaylightSavingTime ? 5 : 6)
                .withMinute(0)
                .withSecond(0)
                .plusDays(1); // 종료 시간이 다음 날일 수 있음
    }

    /**
     * 썸머타임 적용 여부 확인 메서드.
     */
    public boolean isDaylightSavingTime() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
        return now.getZone().getRules().isDaylightSavings(now.toInstant());
    }

}
