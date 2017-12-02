package com.lpmoon.spring.property.zookeeper;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringValueResolver;

import java.util.Properties;

/**
 * Created by lpmoon on 17/12/2.
 */
public class ZookeeperPropertyResourceConfigurer extends PlaceholderConfigurerSupport {

    // zk address
    private String zookeeperAddress;

    // session timeout
    private int sessionTimeout;

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        StringValueResolver valueResolver = new ZookeeperPropertyResourceConfigurer.ZookeeperPlaceholderResolvingStringValueResolver(zookeeperAddress, sessionTimeout);
        // doProcessProperties will parse property in all BeanDefinitions, and register ZookeeperPlaceholderResolvingStringValueResolver
        // as addEmbeddedValueResolver
        doProcessProperties(beanFactoryToProcess, valueResolver);
    }

    private class ZookeeperPlaceholderResolvingStringValueResolver implements StringValueResolver {

        private final PropertyPlaceholderHelper helper;

        private final PropertyPlaceholderHelper.PlaceholderResolver resolver;

        public ZookeeperPlaceholderResolvingStringValueResolver(String zookeeperAddress, int sessionTimeout) {
            this.helper = new PropertyPlaceholderHelper(
                    placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
            this.resolver = new ZookeeperPropertyPlaceholderConfigurerResolver(zookeeperAddress, sessionTimeout);
        }

        @Override
        public String resolveStringValue(String strVal) throws BeansException {
            // use PropertyPlaceholderHelper to resolve the property
            String resolved = this.helper.replacePlaceholders(strVal, this.resolver);
            if (trimValues) {
                resolved = resolved.trim();
            }
            return (resolved.equals(nullValue) ? null : resolved);
        }
    }


    private class ZookeeperPropertyPlaceholderConfigurerResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

        private ZKClient zkClient;

        private ZookeeperPropertyPlaceholderConfigurerResolver(String zookeeperAddress, int sessionTimeout) {
            this.zkClient = new ZKClient(zookeeperAddress, sessionTimeout);
            this.zkClient.start();
        }

        @Override
        public String resolvePlaceholder(String placeholderName) {
            return this.zkClient.get(changePlaceHolderNameToZkPath(placeholderName));
        }

        private String changePlaceHolderNameToZkPath(String placeholderName) {
            return "/" + placeholderName.replaceAll("\\.", "/");
        }

    }

    public String getZookeeperAddress() {
        return zookeeperAddress;
    }

    public void setZookeeperAddress(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}

