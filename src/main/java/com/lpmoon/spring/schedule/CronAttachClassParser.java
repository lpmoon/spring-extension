package com.lpmoon.spring.schedule;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Created by lpmoon on 17/11/25.
 */
public class CronAttachClassParser implements BeanDefinitionParser {

    public static final String CRON_ATTACH_CLASS_PROCESSOR_BEAN_NAME = "com.lpmoon.spring.schedule.cronAttachClassBeanPostProcessor";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        Object source = parserContext.extractSource(element);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                "com.lpmoon.spring.schedule.CronAttachClassBeanPostProcessor");
        builder.getRawBeanDefinition().setSource(source);

        registerPostProcessor(parserContext, builder, CRON_ATTACH_CLASS_PROCESSOR_BEAN_NAME);

        return null;
    }

    private static void registerPostProcessor(
            ParserContext parserContext, BeanDefinitionBuilder builder, String beanName) {

        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        parserContext.getRegistry().registerBeanDefinition(beanName, builder.getBeanDefinition());
        BeanDefinitionHolder holder = new BeanDefinitionHolder(builder.getBeanDefinition(), beanName);
        parserContext.registerComponent(new BeanComponentDefinition(holder));
    }
}
