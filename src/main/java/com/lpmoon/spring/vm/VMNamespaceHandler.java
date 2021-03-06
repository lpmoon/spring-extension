package com.lpmoon.spring.vm;

import com.lpmoon.spring.vm.info.JavaInfoBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by lpmoon on 17/11/18.
 */
public class VMNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("info", new JavaInfoBeanDefinitionParser());
    }
}
