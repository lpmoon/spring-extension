package schedule;

import com.lpmoon.spring.schedule.JavassistChildClassGenerator;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by lpmoon on 17/11/25.
 */
public class JavassistChildClassGeneratorTest {

    @Test
    public void javassistChildClassFactoryShouldWork() throws IllegalAccessException, InstantiationException, NoSuchMethodException, ClassNotFoundException, CannotCompileException, NotFoundException, IOException {

        Test2 test2 = new Test2();
        Class t2 = JavassistChildClassGenerator.generate(test2);
        Object to2 = t2.newInstance();
        Method m2 = to2.getClass().getMethod("test", String.class);
        Scheduled a2 = m2.getAnnotation(Scheduled.class);
        Assert.assertNotNull(a2);
        Assert.assertEquals("${schedule.Test2.cron}", a2.cron());

        Test3 test3 = new Test3();
        Class t3 = JavassistChildClassGenerator.generate(test3);
        Object to3 = t3.newInstance();
        Method m3 = to3.getClass().getMethod("test", String.class);
        Scheduled a3 = m3.getAnnotation(Scheduled.class);
        Assert.assertNotNull(a3);
        Assert.assertEquals("${schedule.Test3.cron}", a3.cron());

        m2 = to2.getClass().getMethod("test", String.class);
        a2 = m2.getAnnotation(Scheduled.class);
        Assert.assertNotNull(a2);
        Assert.assertEquals("${schedule.Test2.cron}", a2.cron());
    }

}
