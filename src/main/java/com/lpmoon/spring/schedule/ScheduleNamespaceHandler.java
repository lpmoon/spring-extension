package com.lpmoon.spring.schedule;

import com.lpmoon.spring.vm.JavaInfoBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by lpmoon on 17/11/25.
 */
public class ScheduleNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("cron-attach-class", new JavaInfoBeanDefinitionParser());
    }
}
