package com.lpmoon.spring.thread;

import com.lpmoon.spring.vm.info.JavaInfoTool;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.util.concurrent.TimeUnit;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;
import static org.springframework.beans.factory.support.AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.registerBeanDefinition;

public class DeadCheckerBeanDefinitionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(DeadlockChecker.class);
        bdb.setDependencyCheck(DEPENDENCY_CHECK_SIMPLE);
        bdb.setScope(SCOPE_SINGLETON);

        bdb.addPropertyValue("handlerId", element.getAttribute("handlerId"));
        bdb.addPropertyValue("period", Long.parseLong(element.getAttribute("period")));

        String timeUnitStr = element.getAttribute("unit");
        TimeUnit timeUnit;
        if ("ms".equals(timeUnitStr)) {
            timeUnit = TimeUnit.MILLISECONDS;
        } else {
            timeUnit = TimeUnit.SECONDS;
        }
        bdb.addPropertyValue("unit", timeUnit);

        BeanDefinition definition = bdb.getRawBeanDefinition();
        BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, "dead-lock-check", null);
        registerBeanDefinition(holder, parserContext.getRegistry());

        return definition;
    }
}
