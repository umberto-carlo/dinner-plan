package it.ucdm.leisure.dinnerplan.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class UserAgentUtilsTest {

    private final UserAgentUtils userAgentUtils = new UserAgentUtils();

    @Test
    void isMobile_MobileUserAgent_ReturnsTrue() {
        assertTrue(userAgentUtils.isMobile(
                "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1"));
        assertTrue(userAgentUtils.isMobile(
                "Mozilla/5.0 (Linux; Android 7.0; SM-G930V Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36"));
    }

    @Test
    void isMobile_DesktopUserAgent_ReturnsFalse() {
        assertFalse(userAgentUtils.isMobile(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36"));
    }

    @Test
    void isMobile_Null_ReturnsFalse() {
        assertFalse(userAgentUtils.isMobile(null));
    }
}
