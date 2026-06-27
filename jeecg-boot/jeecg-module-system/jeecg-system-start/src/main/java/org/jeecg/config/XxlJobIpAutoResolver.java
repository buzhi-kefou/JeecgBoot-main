package org.jeecg.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.function.Function;

/**
 * XXL-Job执行器IP自动解析器
 * <p>
 * 解决多网卡设备上，未配置 jeecg.xxljob.ip 时执行器IP选择错误的问题。
 * <p>
 * 原理：当未配置IP时，通过向XXL-Job调度中心发起Socket探测连接，
 * 利用操作系统路由表自动选择正确的网卡IP。
 * 调度中心回调执行器时使用的就是这个IP，因此能保证连通性。
 * <p>
 * 生命周期：BeanPostProcessor 在每个bean初始化前执行，
 * 而 XxlJobSpringExecutor 的 start() 在 SmartInitializingSingleton.afterSingletonsInstantiated() 中调用，
 * 因此本处理器设置的IP会在 start() 之前生效。
 *
 * @author jeecg
 */
@Slf4j
@Component
public class XxlJobIpAutoResolver implements BeanPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof XxlJobSpringExecutor executor) {
            String ip = environment.getProperty("jeecg.xxljob.ip", "");
            if (StringUtils.hasText(ip)) {
                return bean;
            }
            String adminAddresses = environment.getProperty("jeecg.xxljob.adminAddresses", "");
            String resolvedIp = resolveIp(adminAddresses, this::resolveIpByUdpRoute, this::resolveIpByTcpConnect);
            if (resolvedIp != null) {
                executor.setIp(resolvedIp);
                log.info(">>>>>>>>>>> xxl-job auto-resolved executor ip: {}", resolvedIp);
            }
        }
        return bean;
    }

    /**
     * 解析执行器对调度中心可见的本地IP。
     * <p>
     * 优先使用UDP路由探测，避免JVM SOCKS代理让TCP探测连接到本机代理端口后返回127.0.0.1。
     */
    String resolveIp(String adminAddresses,
                     Function<InetSocketAddress, String> udpRouteProbe,
                     Function<InetSocketAddress, String> tcpConnectProbe) {
        if (!StringUtils.hasText(adminAddresses)) {
            log.warn(">>>>>>>>>>> xxl-job adminAddresses is empty, skip auto-resolve ip.");
            return null;
        }
        String firstAddress = adminAddresses.split(",")[0].trim();
        try {
            URI uri = new URI(firstAddress);
            String host = uri.getHost();
            int port = uri.getPort();
            if (port == -1) {
                port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
            }
            if (host == null) {
                log.warn(">>>>>>>>>>> xxl-job cannot parse host from adminAddress: {}", firstAddress);
                return null;
            }
            InetSocketAddress adminAddress = new InetSocketAddress(host, port);
            String udpRouteIp = udpRouteProbe.apply(adminAddress);
            if (isUsableResolvedIp(udpRouteIp)) {
                log.info(">>>>>>>>>>> xxl-job ip found by udp route: {}", udpRouteIp);
                return udpRouteIp;
            }

            String tcpConnectIp = tcpConnectProbe.apply(adminAddress);
            if (isUsableResolvedIp(tcpConnectIp)) {
                log.info(">>>>>>>>>>> xxl-job ip found by tcp connect: {}", tcpConnectIp);
                return tcpConnectIp;
            }
        } catch (Exception e) {
            log.warn(">>>>>>>>>>> xxl-job failed to auto-resolve ip by probing adminAddress: {}", firstAddress, e);
        }
        return null;
    }

    /**
     * 使用UDP connect让操作系统根据路由表选择本地出口IP；不会实际发送UDP数据包。
     */
    private String resolveIpByUdpRoute(InetSocketAddress adminAddress) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(adminAddress.getHostString()), adminAddress.getPort());
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            log.warn(">>>>>>>>>>> xxl-job failed to resolve ip by udp route: {}", adminAddress, e);
            return null;
        }
    }

    /**
     * 兼容原有逻辑：向调度中心发起TCP连接，获取本地实际使用的IP。
     */
    private String resolveIpByTcpConnect(InetSocketAddress adminAddress) {
        try (Socket socket = new Socket()) {
            socket.connect(adminAddress, 3000);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            log.warn(">>>>>>>>>>> xxl-job failed to resolve ip by tcp connect: {}", adminAddress, e);
            return null;
        }
    }

    private boolean isUsableResolvedIp(String ip) {
        return StringUtils.hasText(ip) && !"0.0.0.0".equals(ip) && !"127.0.0.1".equals(ip)
                && !"::1".equals(ip) && !"0:0:0:0:0:0:0:1".equals(ip);
    }
}
