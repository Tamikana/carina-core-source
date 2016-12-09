package com.qaprosoft.carina.core.foundation.grid;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.FluentWait;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.gson.Gson;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.qaprosoft.carina.core.foundation.grid.GridRequest.Operation;
import com.qaprosoft.carina.core.foundation.report.zafira.ZafiraIntegrator;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.zafira.client.model.EventType;
import com.qaprosoft.zafira.client.model.EventType.Type;

/**
 * DeviceGrid communicates over PubNub with grid queue and provides connect/diconnect device functionality.
 * 
 * @author Alex Khursevich
 */
public class DeviceGrid {
	
	private static final Logger LOGGER = Logger.getLogger(DeviceGrid.class);
	
	private static final String  GRID_SESSION_ID = Configuration.get(Parameter.CI_RUN_ID) != null ? Configuration.get(Parameter.CI_RUN_ID) :  UUID.randomUUID().toString();
	private static final Integer GRID_DEVICE_TIMEOUT = Configuration.getInt(Parameter.ZAFIRA_GIRD_TIMEOUT);
	private static final Integer GRID_HB_TIMEOUT = 120;
	private static final Integer GRID_HB_INTERVAL = 59;
	
	private static final String PN_PKEY = Configuration.get(Parameter.ZAFIRA_GRID_PKEY);
	private static final String PN_SKEY = Configuration.get(Parameter.ZAFIRA_GRID_SKEY);
	private static final String PN_CHANNEL = Configuration.get(Parameter.ZAFIRA_GRID_CHANNEL);
	
	
	private static PubNub heartbeat;
	
	/**
	 * Connect to remote mobile device.
	 * @param testId - unique test id generated bu UUID
	 * @param deviceModels - list of possible devices to get from STF
	 * @return device udid
	 */
	public static String connectDevice(String testId, List<String> deviceModels) 
	{
		PubNub pubnub = new PubNub(getGeneralPNConfig());
		GridCallback gridCallback = new GridCallback(testId);
		GridRequest rq = new GridRequest(GRID_SESSION_ID, testId, deviceModels, Operation.CONNECT);
		try {
			startHeartBeat();
			pubnub.subscribe().channels(Arrays.asList(PN_CHANNEL)).execute();
			pubnub.addListener(gridCallback);
			publishMessage(pubnub, rq);
			ZafiraIntegrator.logEvent(new EventType(Type.REQUEST_DEVICE_CONNECT, GRID_SESSION_ID, testId, new Gson().toJson(rq)));
			
			new FluentWait<GridCallback>(gridCallback)
				.withTimeout(GRID_DEVICE_TIMEOUT, TimeUnit.SECONDS)
				.pollingEvery(10, TimeUnit.SECONDS)
				.until(new Function<GridCallback, Boolean>() 
				{
					@Override
					public Boolean apply(GridCallback callback) 
					{
						return !StringUtils.isEmpty(callback.getUdid());
					}
				});
		} 
		catch (TimeoutException e) {
			ZafiraIntegrator.logEvent(new EventType(Type.DEVICE_WAIT_TIMEOUT, GRID_SESSION_ID, testId, new Gson().toJson(rq)));
			rq.setOperation(Operation.DISCONNECT);
			publishMessage(pubnub, rq);
			LOGGER.error(e.getMessage(), e);
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			pubnub.unsubscribeAll();
		}
		
		return gridCallback.getUdid();
	}
	
	/**
	 * Disconnects from remote device.
	 * @param testId - unique test id generated bu UUID
	 * @param udid - device udid to disconnect from
	 */
	public static void disconnectDevice(final String testId, String udid) 
	{
		try
		{
			GridRequest rq = new GridRequest(GRID_SESSION_ID, testId, udid, Operation.DISCONNECT);
			publishMessage(new PubNub(getGeneralPNConfig()), rq);
			ZafiraIntegrator.logEvent(new EventType(Type.REQUEST_DEVICE_DISCONNECT, GRID_SESSION_ID, testId, new Gson().toJson(rq)));
		} catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	public static void startHeartBeat()
	{
		if(heartbeat == null)
		{
			PNConfiguration pnConfiguration = getGeneralPNConfig();
			pnConfiguration.setPresenceTimeoutWithCustomInterval(GRID_HB_TIMEOUT, GRID_HB_INTERVAL);
			pnConfiguration.setUuid(GRID_SESSION_ID);
			heartbeat = new PubNub(pnConfiguration);
			heartbeat.subscribe().channels(Arrays.asList(PN_CHANNEL)).execute();
		}
	}
	
	public static void stopHeartBeat()
	{
		if(heartbeat != null)
		{
			heartbeat.unsubscribeAll();
		}
	}
	
	public static class GridCallback extends SubscribeCallback
	{
		private String udid;
		private String testId;
		
		public GridCallback(String testId)
		{
			this.testId = testId;
		}

		public String getUdid()
		{
			return udid;
		}

		@Override
		public void message(PubNub pubnub, PNMessageResult message) {
			JsonNode msg = message.getMessage(); 
			if (msg != null && msg.has("testId") && msg.has("connected"))
			{
				try
				{
					GridResponse rs = new ObjectMapper().treeToValue(msg, GridResponse.class);
					if (rs.isConnected())
					{
						udid = rs.getSerial();
						ZafiraIntegrator.markEventReceived(new EventType(Type.CONNECT_DEVICE, GRID_SESSION_ID, testId));
						LOGGER.info("Device found in grid by UDID: " + udid);
					}
				} catch (Exception e)
				{
					LOGGER.error(e.getMessage(), e);
				}
			}
		}

		@Override
		public void presence(PubNub pubnub, PNPresenceEventResult presence) {
			// Do nothing
		}

		@Override
		public void status(PubNub pubnub, PNStatus status) {
			if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory || status.getCategory() == PNStatusCategory.PNTimeoutCategory) 
			{
				pubnub.reconnect();
			}
		}
	}
	
	private static void publishMessage(PubNub pubnub, Object msg)
	{
		pubnub.publish().message(msg).channel(PN_CHANNEL)
			.async(new PNCallback<PNPublishResult>() {
		        @Override
		        public void onResponse(PNPublishResult result, PNStatus status) {
		            if (status.isError()) 
		            {
		            	LOGGER.error("Unable to publush PubNub messsage: " + status.getStatusCode());
		            }
		        }
		    });
	}
	
	private static PNConfiguration getGeneralPNConfig()
	{
		PNConfiguration pnConfiguration = new PNConfiguration();
		pnConfiguration.setPublishKey(PN_PKEY);
		pnConfiguration.setSubscribeKey(PN_SKEY);
		return pnConfiguration;
	}
}
