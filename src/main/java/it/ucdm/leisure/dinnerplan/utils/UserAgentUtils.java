package it.ucdm.leisure.dinnerplan.utils;

import org.springframework.stereotype.Component;

@Component
public class UserAgentUtils {

    public boolean isMobile(String userAgent) {
        if (userAgent == null) {
            return false;
        }
        String ua = userAgent.toLowerCase();
        return ua.contains("mobile") || ua.contains("android") || ua.contains("iphone");
    }
}
