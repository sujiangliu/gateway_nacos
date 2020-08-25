package com.countrygarden.bpc.gateway.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
@Component
@NacosPropertySource(dataId = "${nacos.config.data-id}", autoRefreshed = true, groupId = "${nacos.config.group}")
public class NacosGatewayDefineConfig implements CommandLineRunner {

    @NacosInjected
    private ConfigService configService;

    @Value("${nacos.config.data-id}")
    private String dataId;

    @Value("${nacos.config.group}")
    private String group;

    @Autowired
    NacosDynamicRouteService nacosDynamicRouteService;
    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        addRouteNacosListen();
    }

    /**
     * 添加动态路由监听器
     */
    private void addRouteNacosListen() {
        try {
            String configInfo = configService.getConfig(dataId, group, 5000);
            log.info("从Nacos返回的配置：" + configInfo);
            getNacosDataRoutes(configInfo);
            //注册Nacos配置更新监听器，用于监听触发
            configService.addListener(dataId, group, new Listener()  {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("Nacos更新了！");
                    log.info("接收到数据:"+configInfo);
                    getNacosDataRoutes(configInfo);
                }
                @Override
                public Executor getExecutor() {
                    return null;
                }
            });

        } catch (NacosException e) {
            log.error("nacos-addListener-error", e);
            e.printStackTrace();
        }
    }

    /**
     * 从Nacos返回的配置
     * @param configInfo
     */
    private void getNacosDataRoutes(String configInfo) {
        List<RouteDefinition> list = JSON.parseArray(configInfo, RouteDefinition.class);
        list.stream().forEach(definition -> {
            log.info(""+JSON.toJSONString(definition));
            nacosDynamicRouteService.update(definition);
        });
    }
}
