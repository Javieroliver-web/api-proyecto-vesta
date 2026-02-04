package com.vesta.api.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtils {

    public static String getClientIp(HttpServletRequest request) {
        if (request == null)
            return "unknown";

        // Headers commonly used by proxies to pass the real client IP
        String[] headers = {
                "X-Forwarded-For",
                "X-FORWARDED-FOR",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // If there are multiple IPs (comma-separated), take the first one
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}
