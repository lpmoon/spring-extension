package schedule;

import com.lpmoon.spring.schedule.annotation.EnableExtentScheduled;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by lpmoon on 17/11/22.
 */

public class Test {
    private String a;

    @EnableExtentScheduled
    @Scheduled(cron = "${cron}")
    public String test(String a) {
        System.out.println(a);
        return a;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }
}
