package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public abstract class AbstractCapabilities {
    protected static final Logger LOGGER = Logger.getLogger(AbstractCapabilities.class);

    private static DesiredCapabilities staticCapabilities;

    public abstract DesiredCapabilities getCapability(String testName);

    protected DesiredCapabilities initBaseCapabilities(DesiredCapabilities capabilities, String browser, String testName) {
    	
        String platform = Configuration.get(Configuration.Parameter.PLATFORM);
        if (!platform.equals("*")) {
            capabilities.setPlatform(Platform.extractFromSysProperty(platform));
        }
        
        capabilities.setBrowserName(browser);
        capabilities.setVersion(Configuration.get(Parameter.BROWSER_VERSION));
        capabilities.setCapability("name", testName);
        
		Proxy proxy = setupProxy();
		if (proxy != null) {
			capabilities.setCapability(CapabilityType.PROXY, proxy);
		}
		
		//handle variant with extra capabilities from external property file
    	DesiredCapabilities extraCapabilities = getExtraCapabilities(); 	
    			
    	if (extraCapabilities != null) {
    		capabilities.merge(extraCapabilities);
    	}

		if (staticCapabilities != null)
		{
			capabilities.merge(staticCapabilities);
		}
		
        return capabilities;
    }
    
    protected DesiredCapabilities getExtraCapabilities() {
		//handle variant with extra capabilities from external property file
    	String extraCapabilitiesFile = Configuration.get(Parameter.EXTRA_CAPABILITIES);
    	DesiredCapabilities extraCapabilities = null;
    	if (!extraCapabilitiesFile.isEmpty()) {
    		LOGGER.info("Append extra Capabilities from '" + extraCapabilitiesFile + "' to desired capabilities");
    		extraCapabilities = new CapabilitiesLoder().loadCapabilities(extraCapabilitiesFile);
    	}
    	
    	return extraCapabilities;
    }
    
	public static void addStaticCapability(String name, Object value)
	{
		if (staticCapabilities == null)
		{
			staticCapabilities = new DesiredCapabilities();
		}
		staticCapabilities.setCapability(name, value);
	}

    protected Proxy setupProxy() {
		String proxyHost = Configuration.get(Parameter.PROXY_HOST);
		String proxyPort = Configuration.get(Parameter.PROXY_PORT);
		List<String> protocols = Arrays.asList(Configuration.get(Parameter.PROXY_PROTOCOLS).split("[\\s,]+"));
		
		if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {
			
			org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
			String proxyAddress = String.format("%s:%s", proxyHost, proxyPort);
			
			if (protocols.contains("http")) {
				LOGGER.info(String.format("Http proxy will be set: %s:%s", proxyHost, proxyPort));

				System.setProperty("http.proxyHost", proxyHost);
				System.setProperty("http.proxyPort", proxyPort);
				
				proxy.setHttpProxy(proxyAddress);
			}

			if (protocols.contains("https")) {
				LOGGER.info(String.format("Https proxy will be set: %s:%s", proxyHost, proxyPort));

				System.setProperty("https.proxyHost", proxyHost);
				System.setProperty("https.proxyPort", proxyPort);
				
				proxy.setSslProxy(proxyAddress);
			}

			if (protocols.contains("ftp")) {
				LOGGER.info(String.format("FTP proxy will be set: %s:%s", proxyHost, proxyPort));

				System.setProperty("ftp.proxyHost", proxyHost);
				System.setProperty("ftp.proxyPort", proxyPort);
				
				proxy.setFtpProxy(proxyAddress);
			}

			if (protocols.contains("socks")) {
				LOGGER.info(String.format("Socks proxy will be set: %s:%s", proxyHost, proxyPort));
				System.setProperty("socksProxyHost", proxyHost);
				System.setProperty("socksProxyPort", proxyPort);
				
				proxy.setSocksProxy(proxyAddress);
			}
			
			return proxy;
		}
		
		return null;
    }
    
}
