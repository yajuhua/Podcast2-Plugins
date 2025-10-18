package io.github.yajuhua.youtube.utils;

import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.utils.SettingUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

@Slf4j
public class NetworkUtil {
    /**
     * 根据设置的代理字符串，生成Proxy对象
     *
     * @return 代理对象
     */
    public static Proxy getProxy(Params params) {
        try {
            if (params != null
                    && params.getSettings() != null
                    && !params.getSettings().isEmpty()) {
                Map<String, String> map = SettingUtils.SettingListToMap(params.getSettings());
                String proxyStr = null;
                //提取代理字符串
                if (map.containsKey("http代理")) {
                    proxyStr = "http://" + map.get("http代理");
                } else if (map.containsKey("socks代理")) {
                    proxyStr = "socks://" + map.get("socks代理");
                }
                //转换成Proxy对象
                if (proxyStr != null && !proxyStr.isEmpty()) {
                    URL proxyUrl = new URL(proxyStr);
                    String protocol = proxyUrl.getProtocol().toUpperCase();
                    int port = proxyUrl.getPort();
                    String host = proxyUrl.getHost();
                    return new Proxy(Proxy.Type.valueOf(protocol), new InetSocketAddress(host, port));
                }
            }
            return Proxy.NO_PROXY;
        } catch (Exception e) {
            log.error("设置代理错误", e);
            return Proxy.NO_PROXY;
        }
    }

    /**
     * 获取参数中的代理url链接
     * @param params
     * @return
     */
    public static String getProxyUrl(Params params) {
        String proxyStr = null;
        if (params != null && params.getSettings() != null && !params.getSettings().isEmpty()) {
            Map<String, String> map = SettingUtils.SettingListToMap(params.getSettings());
            //提取代理字符串
            if (map.containsKey("http代理")) {
                proxyStr = "http://" + map.get("http代理");
            } else if (map.containsKey("socks代理")) {
                proxyStr = "socks://" + map.get("socks代理");
            }
        }
        return proxyStr;
    }
}
