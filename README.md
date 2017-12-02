Some extension to Spring!

# Schedule

## 用途

在某些场景下，我们需要将@Scheduled放在抽象类父类上作为统一入口，同时又希望
抽象类的实现类的调度策略各不相同，这个时候使用该扩展包就可以达到此目的。

## 使用

1. 定义抽象类

    ```
package spring;

import com.lpmoon.spring.schedule.annotation.EnableExtentScheduled;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractSchedule {

    abstract void doTask();

    @EnableExtentScheduled
    @Scheduled(cron = "${cron}" )
    public void task() {
        System.out.println(this.getClass());
        doTask();
    }
}

```

  除了常规的@Scheduled，还需要添加@EnableExtentScheduled，启动扩展功能。

2. 实现类
    ```
package spring;

import org.springframework.stereotype.Component;

@Component
public class Schedule1 extends AbstractSchedule{
    @Override
    void doTask() {
        System.out.println("schedule1");
    }
}
```
    ```
package spring;

import org.springframework.stereotype.Component;

@Component
public class Schedule2 extends AbstractSchedule {
    @Override
    void doTask() {
        System.out.println("schedule2");
    }
}
```

3. properties
    ```
spring.Schedule1.cron=0/5 * *  * * ?
spring.Schedule2.cron=0/15 * *  * * ?
```

    properties文件中配置了定时任务的调度策略。在抽象类中定义的调度策略为${cron}，正常情况下会在properties中加载配置cron，
    由于使用了扩展功能，配置cron将不再起作用，你需要在properties根据不同的调度任务配置不同的策略，策略对应的配置为cron加上
    类名，比如上面的Schedule1，对应的配置是spring.Schedule1.cron。

4. xml
    ```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:task="http://www.springframework.org/schema/task"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:schedule="http://www.springframework.org/schema/schedule"
       xsi:schemaLocation="
http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
http://www.springframework.org/schema/task
http://www.springframework.org/schema/task/spring-task-3.1.xsd
http://www.springframework.org/schema/context
http://www.springframework.org/schema/context/spring-context-3.1.xsd
http://www.springframework.org/schema/schedule
http://www.springframework.org/schema/schedule/spring-schedule-3.1.xsd
">

    <context:property-placeholder location="classpath:application.properties"/>
    <schedule:cron-attach-class/>
    <task:annotation-driven/>
    <context:component-scan base-package="spring"/>

</beans>
```

   除了使用常规的<task>标签启动@Scheduled，你还需要添加额外的\<schedule:cron-attach-class/\>标签用于启动扩展功能。

5. 运行

  加载上面的配置文件运行Spring可以看到如下的输出，
  >execute in father class;
   class Schedule1Child
   schedule1
   execute in father class;
   class Schedule1Child
   schedule1
   execute in father class;
   class Schedule2Child
   schedule2
   execute in father class;
   class Schedule1Child

   从上面的输出可以看出，Schedule1和Schedule2错开运行了，执行一次Schedule2之前都会执行3次Schedule1，效果和上面的
   配置一致。


# Vm

## 用途

关于vm的一些扩展