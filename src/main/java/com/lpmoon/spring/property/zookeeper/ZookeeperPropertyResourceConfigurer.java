package com.lpmoon.spring.property.zookeeper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringValueResolver;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by lpmoon on 17/12/2.
 */
public class ZookeeperPropertyResourceConfigurer extends PlaceholderConfigurerSupport {

    // zk address
    private String zookeeperAddress;

    // session timeout
    private int sessionTimeout;

    // enable cache
    private boolean enableCache;

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        // NOTE: we just ignore props because it is useless。。

        StringValueResolver valueResolver = new ZookeeperPropertyResourceConfigurer.ZookeeperPlaceholderResolvingStringValueResolver(zookeeperAddress, sessionTimeout, enableCache);
        // doProcessProperties will parse property in all BeanDefinitions, and register ZookeeperPlaceholderResolvingStringValueResolver
        // as addEmbeddedValueResolver
        doProcessProperties(beanFactoryToProcess, valueResolver);
    }

    private class ZookeeperPlaceholderResolvingStringValueResolver implements StringValueResolver {

        private final PropertyPlaceholderHelper helper;

        private final PropertyPlaceholderHelper.PlaceholderResolver resolver;

        public ZookeeperPlaceholderResolvingStringValueResolver(String zookeeperAddress, int sessionTimeout, boolean enableCache) {
            this.helper = new PropertyPlaceholderHelper(
                    placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
            this.resolver = new ZookeeperPropertyPlaceholderConfigurerResolver(zookeeperAddress, sessionTimeout, enableCache);
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

        private Cache<String, String> propertiesCache;

        private ZookeeperPropertyPlaceholderConfigurerResolver(String zookeeperAddress, int sessionTimeout, boolean enableCache) {
            this.zkClient = new ZKClient(zookeeperAddress, sessionTimeout);
            this.zkClient.start();
            if (enableCache) {
                propertiesCache = CacheBuilder.newBuilder() //
                        .concurrencyLevel(10) //
                        .initialCapacity(50) //
                        .expireAfterWrite(10, TimeUnit.SECONDS) //
                        .maximumSize(100).build();
            }
        }

        @Override
        public String resolvePlaceholder(String placeholderName) {
            String data;

            if (enableCache) {
                if ((data = propertiesCache.getIfPresent(placeholderName)) == null) {
                    data = this.zkClient.get(changePlaceHolderNameToZkPath(placeholderName));
                    if (data != null) {
                        propertiesCache.put(placeholderName, data);
                    }
                }
            } else {
                data = this.zkClient.get(changePlaceHolderNameToZkPath(placeholderName));
            }

            if (data == null) {
                logger.error("property: " + placeholderName + "not exist!!");
            }

            return data;
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

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }
}

