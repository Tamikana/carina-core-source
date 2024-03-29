package com.qaprosoft.carina.core.foundation.grid;

import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

public class DeviceGridTest
{
	@Test(enabled=false)
	public void testDeviceGrid() throws InterruptedException
	{
		DevicePool.addDevices();
		DevicePool.registerDevice();
		Thread.sleep(5 * 1000);
		DevicePool.deregisterDevice();
	}
}
