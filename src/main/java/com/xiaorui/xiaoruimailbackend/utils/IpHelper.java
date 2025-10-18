package com.xiaorui.xiaoruimailbackend.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @description: IP 工具类
 * @author: xiaorui
 * @date: 2025-10-18 21:04
 **/
public class IpHelper {

    private static final String UNKNOWN = "unknown";

    /**
     * 得到用户的真实地址,如果有多个就取第一个
     */
    public static String getIpAddr() {
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        if (request == null) {
            return null;
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        String[] ips = ip.split(",");
        return ips[0].trim();
    }

}
