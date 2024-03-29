package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.mobile;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

public class MobileWebCapabilities extends MobileCapabilies {


    public DesiredCapabilities getCapability(String testName) {
        return getCapability(false);
    }

    public DesiredCapabilities getCapability(boolean isGrid) {
        DesiredCapabilities capabilities = getMobileCapabilities(isGrid, Configuration.get(Configuration.Parameter.MOBILE_PLATFORM_NAME),
                Configuration.get(Configuration.Parameter.MOBILE_PLATFORM_VERSION), Configuration.get(Configuration.Parameter.MOBILE_DEVICE_NAME),
                Configuration.get(Configuration.Parameter.MOBILE_AUTOMATION_NAME), Configuration.get(Configuration.Parameter.MOBILE_NEW_COMMAND_TIMEOUT),
                Configuration.get(Configuration.Parameter.BROWSER), null, null, null);
        return capabilities;
    }
}
