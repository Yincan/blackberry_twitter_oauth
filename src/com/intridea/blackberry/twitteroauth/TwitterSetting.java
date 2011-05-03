/*
 * Copyright 2010, Intridea, Inc.
 */
package com.intridea.blackberry.twitteroauth;

import net.rim.device.api.util.Persistable;

/**
 * Will be persisted for further access.
 * @author yincan
 */
public class TwitterSetting implements Persistable {

	public String token = null;
	public String tokenSecret = null;

	public TwitterSetting(String token, String tokenSecret) {
	    this.token = token;
	    this.tokenSecret = tokenSecret;
	}
}
