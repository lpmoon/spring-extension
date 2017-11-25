package schedule;

import com.lpmoon.spring.util.ObjectUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by lpmoon on 17/11/25.
 */
public class ObjectUtilTest {

    @Test
    public void objectUtilShouldWork() {
        schedule.Test test = new schedule.Test();
        test.setA("cccc");

        try {
            Object test2 = ObjectUtil.copy(test, Test2.class);
            Assert.assertEquals(((Test2)test2).getA(), "cccc");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
