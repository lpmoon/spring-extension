package com.lpmoon.spring.vm.info;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;
import static org.springframework.beans.factory.support.AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.registerBeanDefinition;

/**
 * Created by lpmoon on 17/11/18.
 */
public class JavaInfoBeanDefinitionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(JavaInfoTool.class);
        bdb.setDependencyCheck(DEPENDENCY_CHECK_SIMPLE);
        bdb.setScope(SCOPE_SINGLETON);

        BeanDefinition definition = bdb.getRawBeanDefinition();
        BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, "vm-java-info", null);
        registerBeanDefinition(holder, parserContext.getRegistry());

        return definition;
    }
}
