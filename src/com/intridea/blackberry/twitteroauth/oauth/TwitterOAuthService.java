/*
 * Copyright 2010, Intridea, Inc.
 */
package com.intridea.blackberry.twitteroauth.oauth;

import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;

import com.intridea.blackberry.twitteroauth.logger.Logger;
import com.intridea.blackberry.twitteroauth.network.HttpConnectionFactory;
import com.intridea.blackberry.twitteroauth.util.StringUtils;

import net.rim.device.api.crypto.HMAC;
import net.rim.device.api.crypto.HMACKey;
import net.rim.device.api.crypto.RandomSource;
import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.Comparator;

/**
 * We are using PIN-based oauth process. Please register the app as desktop client app.
 * 
 * @author yincan
 */
public class TwitterOAuthService {
    public static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    public static final String AUTHORIZE_WEBSITE_URL = "https://api.twitter.com/oauth/authorize?oauth_token=";
    public static final String ACCESS_TOKEN_ENDPOINT_URL = "https://api.twitter.com/oauth/access_token";
    private static final String TWITTER_SHARE_API_URL = "http://api.twitter.com/1/statuses/update.json";
    public static final String HEADER = "Authorization";
    public static final String SIGNATURE_METHOD = "HMAC-SHA1";
    public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    public static final String OAUTH_SIGNATURE = "oauth_signature";
    public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    public static final String OAUTH_TOKEN = "oauth_token";
    public static final String OAUTH_NONCE = "oauth_nonce";
    public static final String OAUTH_VERSION = "oauth_version";
    public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
    public static final String CALLBACK = "oauth_callback";
    public static final String VERIFIER = "oauth_verifier";
    public static final String OUT_OF_BAND = "oob";
    /**
     * Please change your consumer key/secret.
     */
    public static final String consumerKey = "eY6r9KZ9XCpnQolFUjGCew";
    public static final String consumerSecret = "e6P3Z3XanxZFN4EFEkjSDoGLrzGzuf2tiXU2TpIFDM";

    public String token = null;
    public String tokenSecret = null;
    public String verifier = null;

    private HttpConnectionFactory factory = null;
    public boolean accessTokenSucess = false;

    public TwitterOAuthService() {
        this(null, null);
    }

    public TwitterOAuthService(String token, String tokenSecret) {
        this.token = token;
        this.tokenSecret = tokenSecret;
        factory = new HttpConnectionFactory(HttpConnectionFactory.TRANSPORTS_ANY);
    }

    public void clean() {
        token = null;
        tokenSecret = null;
        verifier = null;
    }

    public boolean hasToken() {
        return StringUtils.present(token) && StringUtils.present(tokenSecret);
    }

    public void requestToken() {
        HttpConnection httpConn = null;
        InputStream input = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.addProperty("WWW-Authenticate", "OAuth realm=http://api.twitter.com/");
            String oauth_header = getOauthRequestHeader(REQUEST_TOKEN_URL, HttpProtocolConstants.HTTP_METHOD_POST, null, true);
            headers.addProperty(HEADER, "OAuth " + oauth_header);

            httpConn = factory.getHttpConnectionForTwitter(REQUEST_TOKEN_URL, headers, null);
            input = httpConn.openDataInputStream();
            int resp = httpConn.getResponseCode();
            if (resp == HttpConnection.HTTP_OK) {
                StringBuffer buffer = new StringBuffer();
                int ch;
                while ((ch = input.read()) != -1) {
                    buffer.append((char) ch);
                }
                String content = buffer.toString();
                token = content.substring(content.indexOf((OAUTH_TOKEN + "=")) + (OAUTH_TOKEN + "=").length(),
                        content.indexOf('&'));
                int indexOfSecret = content.indexOf((OAUTH_TOKEN_SECRET + "=")) + (OAUTH_TOKEN_SECRET + "=").length();
                int indexOfEnd = content.indexOf("&", indexOfSecret);
                tokenSecret = content.substring(indexOfSecret, indexOfEnd == -1 ? content.length() : indexOfEnd);
            }
        } catch (Exception nc) {
            Logger.error("Error occurs during request token." + nc.toString());
        } finally {
            close(input);
            close(httpConn);
        }
    }

    public void accessToken() {
        InputStream input = null;
        HttpConnection httpConn = null;
        String header = getOauthRequestHeader(ACCESS_TOKEN_ENDPOINT_URL, HttpProtocolConstants.HTTP_METHOD_POST, null, false);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.addProperty("WWW-Authenticate", "OAuth realm=http://api.twitter.com/");
            headers.addProperty("Authorization", "OAuth " + header);

            httpConn = factory.getHttpConnectionForTwitter(ACCESS_TOKEN_ENDPOINT_URL, headers, null);

            input = httpConn.openDataInputStream();
            int resp = httpConn.getResponseCode();
            if (resp == HttpConnection.HTTP_OK) {
                StringBuffer buffer = new StringBuffer();
                int ch;
                while ((ch = input.read()) != -1) {
                    buffer.append((char) ch);
                }
                String content = buffer.toString();

                int startIndex = content.indexOf(OAUTH_TOKEN + "=");
                if (startIndex != -1) {
                    int endOfSecret = content.indexOf("&", startIndex + (OAUTH_TOKEN + "=").length());
                    if (endOfSecret == -1) {
                        endOfSecret = content.length();
                    }
                    token = content.substring(startIndex + (OAUTH_TOKEN + "=").length(), endOfSecret);
                    Logger.debug("The token " + token);
                }

                startIndex = content.indexOf((OAUTH_TOKEN_SECRET + "="));
                if (startIndex != -1) {
                    int endOfSecret = content.indexOf("&", startIndex + (OAUTH_TOKEN_SECRET + "=").length());
                    if (endOfSecret == -1) {
                        endOfSecret = content.length();
                    }
                    tokenSecret = content.substring(startIndex + (OAUTH_TOKEN_SECRET + "=").length(), endOfSecret);
                    Logger.debug("The token secret " + tokenSecret);
                }
                verifier = null;
                accessTokenSucess = true;
            }
        } catch (Exception e) {
            Logger.error("error in getting the access token" + e.toString());
        } finally {
            close(input);
            close(httpConn);
        }
    }

    public int updateStatus(String msg) {
        InputStream input = null;
        HttpConnection httpConn = null;
        String header = getOauthRequestHeader(TWITTER_SHARE_API_URL, HttpProtocolConstants.HTTP_METHOD_POST, msg, false);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.addProperty(HEADER, "OAuth " + header);
            headers.addProperty("WWW-Authenticate", "OAuth realm=http://twitter.com/");
            httpConn = factory.getHttpConnectionForTwitter(TWITTER_SHARE_API_URL, headers, msg.getBytes());
            input = httpConn.openDataInputStream();
            int resp = httpConn.getResponseCode();
            Logger.debug("The share resp code = " + resp);
            return resp;
        } catch (Exception e) {
            Logger.error("Sharing to LinkedIn " + e.toString());
        } finally {
            close(input);
            close(httpConn);
        }
        return 401;
    }

    private String getOauthRequestHeader(String url, String method, String postBody, boolean isRequestToken) {
        long timestamp = timestamp();
        String nonce = nonce();
        Hashtable pairs = new Hashtable();

        if (isRequestToken) {
            pairs.put(CALLBACK, OUT_OF_BAND);
        }
        pairs.put(OAUTH_CONSUMER_KEY, consumerKey);
        pairs.put(OAUTH_NONCE, nonce);
        pairs.put(OAUTH_SIGNATURE_METHOD, SIGNATURE_METHOD);
        pairs.put(OAUTH_TIMESTAMP, Long.toString(timestamp));
        pairs.put(OAUTH_VERSION, "1.0");
        if (token != null && token.trim().length() > 0) {
            pairs.put(OAUTH_TOKEN, token);
        }
        if (verifier != null && verifier.trim().length() > 0) {
            pairs.put(VERIFIER, verifier);
        }
        String signature = encode(signature(method.toUpperCase(), url, pairs, postBody));
        pairs.put(OAUTH_SIGNATURE, signature);

        StringBuffer header_sb = new StringBuffer();
        for (Enumeration it = pairs.keys(); it.hasMoreElements();) {
            String key = (String) it.nextElement();
            String value = (String) pairs.get(key);
            if (header_sb.length() > 0) {
                if (method.equalsIgnoreCase("get")) {
                    header_sb.append("&");
                } else {
                    header_sb.append(", ");
                }
            }
            if (method.equalsIgnoreCase("get")) {
                header_sb.append(key).append("=").append(value);
            } else {
                header_sb.append(key).append("=\"").append(value).append("\"");
            }
        }
        return header_sb.toString();
    }

    private String signature(String method, String requestURL, Hashtable pairs, String postBody) {
        StringBuffer sb = new StringBuffer();
        String[] keys = new String[pairs.size()];
        Enumeration e = pairs.keys();
        int i = 0;

        while (e.hasMoreElements()) {
            String k = (String) e.nextElement();
            keys[i++] = k + "%3D" + encode((String) pairs.get(k));
        }
        Arrays.sort(keys, new Comparator() {
            public int compare(Object arg0, Object arg1) {
                return ((String) arg0).compareTo((String) arg1);
            }
        });
        for (i = 0; i < keys.length; i++) {
            sb.append(keys[i]).append("%26");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        //the sign base string
        String baseUrl = method.toUpperCase() + "&" + encode(requestURL) + "&" + sb.toString();
        if (postBody != null) {
            baseUrl += "%26" + encode(postBody);
        }
        Logger.debug("percentEncode(postBody) " + encode(postBody));
        Logger.debug("The base url = " + baseUrl);

        //the key
        StringBuffer key = new StringBuffer();
        key.append(encode(consumerSecret)).append("&");
        if (tokenSecret != null) {
            key.append(encode(tokenSecret));
        }

        try {
            return hmacsha1(key.toString(), baseUrl);
        } catch (Exception ex) {
            return null;
        }
    }

    private static void close(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private static void close(HttpConnection httpConn) {
        try {
            if (httpConn != null) {
                httpConn.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public String getAuthorizationUrl() {
        return AUTHORIZE_WEBSITE_URL + token;
    }

    private String nonce() {
        return Long.toString(Math.abs(RandomSource.getLong()));
    }

    static long timestamp() {
        return new Date().getTime() / 1000;
    }

    private static String encode(String string) {
        return StringUtils.encode(string);
    }

    private static String hmacsha1(String key, String message) throws Exception {
        HMACKey k = new HMACKey(key.getBytes());
        HMAC hmac = new HMAC(k, new SHA1Digest());
        hmac.update(message.getBytes());
        byte[] mac = hmac.getMAC();
        return Base64OutputStream.encodeAsString(mac, 0, mac.length, false, false);
    }
}
