package schedule;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by lpmoon on 17/11/22.
 */

public class Test {
    private String a;

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
