package schedule;

import com.lpmoon.spring.schedule.ExtentScheduledTaskFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;

/**
 * Created by lpmoon on 17/11/25.
 */
public class ExtentScheduledTaskFactoryTest {

    @Test
    public void extentScheduledTaskFactoryTestShouldWork() {

        Test2 test2 = new Test2();
        Object t2 = null;
        try {
            t2 = ExtentScheduledTaskFactory.generate(test2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Method m2 = null;
        try {
            m2 = t2.getClass().getMethod("test", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Scheduled a2 = m2.getAnnotation(Scheduled.class);
        Assert.assertNotNull(a2);
        Assert.assertEquals("${schedule.Test2.cron}", a2.cron());

        Test3 test3 = new Test3();
        Object t3 = null;
        try {
            t3 = ExtentScheduledTaskFactory.generate(test3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Method m3 = null;
        try {
            m3 = t3.getClass().getMethod("test", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Scheduled a3 = m3.getAnnotation(Scheduled.class);
        Assert.assertNotNull(a3);
        Assert.assertEquals("${schedule.Test3.cron}", a3.cron());

        try {
            m2 = t2.getClass().getMethod("test", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        a2 = m2.getAnnotation(Scheduled.class);
        Assert.assertNotNull(a2);
        Assert.assertEquals("${schedule.Test2.cron}", a2.cron());
    }

}
