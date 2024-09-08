package xyz.rpc.framework;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Set;

/**
 * 在spring初始化bean前，通过动态代理生成rpc-client对应的bean
 * 供后续业务bean注入
 *
 * @author xin.xu
 */
@Slf4j
public class RpcClientBeanRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    /**
     * Modify the application context's internal bean definition registry after its
     * standard initialization. All regular bean definitions will have been loaded,
     * but no beans will have been instantiated yet. This allows for adding further
     * bean definitions before the next post-processing phase kicks in.
     *
     * @param registry the bean definition registry used by the application context
     * @throws BeansException in case of errors
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        log.info("RpcClientBeanRegistryPostProcessor_start");

        // load properties
        RpcClientProperties properties = RpcClientUtils.loadProperties();
        log.info("properties={}", properties);

        // scan package
        Iterator<String> iterator = properties.getPackageName().iterator();
        while (iterator.hasNext()) {
            String packageName = iterator.next();
            boolean scan = scanPackage(registry, packageName);
            if (!scan) {
                iterator.remove();
            }
        }

        // if not empty, then start job
        if (!properties.getPackageName().isEmpty()) {
            RpcClientSubscribeServerJob.start(properties);
        }
    }

    private boolean scanPackage(BeanDefinitionRegistry registry, String packageName) {
        if (StringUtils.hasText(packageName)) {
            Set<Class<?>> interfaceSet = ToolUtils.scanInterfaces(packageName);

            // registry
            for (Class<?> interfaceClass : interfaceSet) {
                RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
                rootBeanDefinition.setBeanClass(interfaceClass);
                rootBeanDefinition.setInstanceSupplier(() -> RpcClientUtils.getClientProxy(interfaceClass));

                registry.registerBeanDefinition(
                        ToolUtils.firstCharToLowercase(interfaceClass.getSimpleName()),
                        rootBeanDefinition
                );
            }

            log.info("rpc san package [{}] and create bean count {}", packageName, interfaceSet.size());

            return !interfaceSet.isEmpty();
        } else {
            log.warn("rpc scan package [{}] is blank", packageName);
            return false;
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // do nothing
    }
}
