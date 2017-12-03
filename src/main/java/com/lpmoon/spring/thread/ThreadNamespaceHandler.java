package com.lpmoon.spring.thread;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class ThreadNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("dead-lock-check", new DeadCheckerBeanDefinitionParser());
    }
}
