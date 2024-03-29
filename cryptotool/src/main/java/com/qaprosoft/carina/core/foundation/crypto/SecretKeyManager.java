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
package com.qaprosoft.carina.core.foundation.crypto;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class SecretKeyManager
{
	private static final Logger LOGGER = Logger.getLogger(SecretKeyManager.class);
	
	public static SecretKey generateKey(String keyType, int size) throws NoSuchAlgorithmException 
	{
		LOGGER.debug("generating key use algorithm: '" + keyType + "'; size: " + size);
		KeyGenerator keyGenerator = KeyGenerator.getInstance(keyType);
	    keyGenerator.init(size);
	    SecretKey key = keyGenerator.generateKey();
	    return key; 
	}
	
	
	public static void saveKey(SecretKey key, File file) throws IOException {
	    byte[] encoded = key.getEncoded();
	    FileUtils.writeByteArrayToFile(file, Base64.encodeBase64(encoded));
	}
	
	public static SecretKey loadKey(File file, String cryptoKeyType) throws IOException {
	    SecretKey key = new SecretKeySpec(Base64.decodeBase64(FileUtils.readFileToByteArray(file)), cryptoKeyType);
	    return key;
	}
	
	public static SecretKey getKey(String keyAsString, String cryptoKeyType) {
	    return new SecretKeySpec(Base64.decodeBase64(keyAsString), cryptoKeyType);
	}
	
}
