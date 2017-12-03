package com.lpmoon.spring.vm.info;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;
import java.util.Properties;

/**
 * Created by lpmoon on 17/11/18.
 */
public class JavaInfoTool implements ApplicationListener<ContextRefreshedEvent> {

    public void printJavaInfo() {
        Properties properties = System.getProperties();
        for (Map.Entry entry : properties.entrySet()) {
            System.out.printf("%20s -> %s\r\n", entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(event.getApplicationContext().getParent() == null) {
            printJavaInfo();
        }
    }
}
