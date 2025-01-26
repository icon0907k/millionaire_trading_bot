package afterwork.millionaire.indicator;

import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class SummerTimeCheckSchedule {

    // 장 시작 시간을 위한 크론식을 반환하는 메서드
    public String getStartCron() {
        boolean isDaylightSavingTime = isDaylightSavingTime();  // 썸머타임 여부 확인
        // 썸머타임 적용 시 한국 시간으로 10:30 PM (미국 동부 시간 9:30 AM, EDT)
        // 썸머타임이 아닐 경우 한국 시간으로 11:30 PM (미국 동부 시간 9:30 AM, EST)
        return isDaylightSavingTime ? "0 30 22 * * *" : "0 30 23 * * *";
    }

    // 장 종료 시간을 위한 크론식을 반환하는 메서드
    public String getEndCron() {
        boolean isDaylightSavingTime = isDaylightSavingTime();  // 썸머타임 여부 확인
        // 썸머타임 적용 시 한국 시간으로 5:00 AM (미국 동부 시간 4:00 PM, EDT)
        // 썸머타임이 아닐 경우 한국 시간으로 6:00 AM (미국 동부 시간 4:00 PM, EST)
        return isDaylightSavingTime ? "0 0 5 * * *" : "0 0 6 * * *";
    }

    // 현재 시간이 썸머타임(일광 절약 시간제) 적용 여부를 확인하는 메서드
    private boolean isDaylightSavingTime() {
        // "America/New_York" 시간대를 기준으로 현재 시간이 썸머타임 적용 여부를 확인
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
        // ZonedDateTime 객체의 getZone().getRules().isDaylightSavings() 메서드를 사용하여 현재 시간이 썸머타임인지 확인
        return now.getZone().getRules().isDaylightSavings(now.toInstant());
    }


}
