/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.http;


import org.apache.log4j.Logger;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

/*
 * HttpClient - sends HTTP request with specified parameters and returns response.
 * 
 * @author Alex Khursevich
 */
public class HttpClient
{
	protected static final Logger LOGGER = Logger.getLogger(HttpClient.class);
	
	public static Response send(RequestSpecification request, String methodPath, HttpMethodType methodType)
	{
		Response response = null;
		setupProxy();
		switch (methodType)
		{
		case HEAD:
			response = request.head(methodPath);
			break;
		case GET:
			response = request.get(methodPath);
			break;
		case PUT:
			response = request.put(methodPath);
			break;
		case POST:
			response = request.post(methodPath);
			break;
		case DELETE:
			response = request.delete(methodPath);
			break;
		case PATCH:
			response = request.patch(methodPath);
			break;
		default:
			throw new RuntimeException("MethodType is not specified for the API method: " + methodPath);
		}

		return response;
	}
	
	private static void setupProxy()
	{
		String proxyHost = Configuration.get(Parameter.PROXY_HOST);
		String proxyPort = Configuration.get(Parameter.PROXY_PORT);
		
		if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {
			System.setProperty("http.proxyHost", proxyHost);
			System.setProperty("http.proxyPort", proxyPort);

			System.setProperty("https.proxyHost", proxyHost);
			System.setProperty("https.proxyPort", proxyPort);

			System.setProperty("ftp.proxyHost", proxyHost);
			System.setProperty("ftp.proxyPort", proxyPort);
			
			System.setProperty("socksProxyHost", proxyHost);
			System.setProperty("socksProxyPort", proxyPort);

			LOGGER.info(String.format("HTTP client will use http/https/ftp proxies: %s:%s", proxyHost, proxyPort));
		}
	}
}
