/**
 * Copyright (c) E.Y. Baskoro, Research In Motion Limited.
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without 
 * restriction, including without limitation the rights to use, 
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * This License shall be included in all copies or substantial 
 * portions of the Software.
 * 
 * The name(s) of the above copyright holders shall not be used 
 * in advertising or otherwise to promote the sale, use or other 
 * dealings in this Software without prior written authorization.
 * 
 */
package com.intridea.blackberry.twitteroauth.util;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

public class StringUtils {

    public static String[] split(String str, char sep, int maxNum) {
        if ((str == null) || (str.length() == 0)) {
            return new String[0];
        }

        Vector results = maxNum == 0 ? new Vector() : new Vector(maxNum);

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == sep) {
                if (maxNum != 0) {
                    if ((results.size() + 1) == maxNum) {
                        for (; i < str.length(); i++) {
                            buf.append(str.charAt(i));
                        }
                        break;
                    }
                }

                results.addElement(buf.toString());
                buf.setLength(0);
            } else {
                buf.append(c);
            }
        }

        if (buf.length() > 0) {
            results.addElement(buf.toString());
        }

        return toStringArray(results);
    }

    public static String[] toStringArray(Vector strings) {
        String[] result = new String[strings.size()];
        for (int i = 0; i < strings.size(); i++) {
            result[i] = strings.elementAt(i).toString();
        }
        return result;
    }

    public static String chomp(String inStr) {
        if ((inStr == null) || "".equals(inStr)) {
            return inStr;
        }

        char lastChar = inStr.charAt(inStr.length() - 1);
        if (lastChar == 13) {
            return inStr.substring(0, inStr.length() - 1);
        } else if (lastChar == 10) {
            String tmp = inStr.substring(0, inStr.length() - 1);
            if ("".equals(tmp)) {
                return tmp;
            }
            lastChar = tmp.charAt(tmp.length() - 1);
            if (lastChar == 13) {
                return tmp.substring(0, tmp.length() - 1);
            } else {
                return tmp;
            }
        } else {
            return inStr;
        }
    }

    public static String parentOf(String inStr) {
        String result = null;

        if ((inStr != null) && !inStr.trim().equals("")) {
            inStr = inStr.trim();
            int index = inStr.lastIndexOf('.');
            if (index != -1) {
                result = inStr.substring(0, index);
            }
        }

        return result;
    }

    public static boolean isBlank(String string) {
        return (string == null) || "".equals(string.trim());
    }
    public static boolean present(String value) {
        return !isBlank(value);
    }

    public static String fixHttpsUrlPrefix(String url) {
        String result = "";
        if ((url == null) || url.trim().equals("")) {
            result = url;
        } else {
            if (url.startsWith("http://") && (url.indexOf(":443") != -1)) {
                // fix it
                result = "https://" + url.substring(7);
            } else {
                // return the original url for all other cases
                result = url;
            }
        }
        return result;
    }

    /**
     * <p>
     * Encode a given string. UTF-8 is considered.
     * </p>
     * 
     * @param s String to encode.
     * @return Encoded string.
     * @throws IllegalArgumentException If string is empty or null.
     */
    public static String encode(String s) {
        return encode(s, null);
    }

    /**
     * <p>
     * Encode a given string. If encode type is not informed, UTF-8 is considered.
     * </p>
     * 
     * @param s String to encode.
     * @param enc Encode.
     * @return Encoded string.
     * @throws IllegalArgumentException If string is empty or null.
     */
    public static String encode(String s, String enc) {
        if (s == null) {
            throw new IllegalArgumentException("String must not be null");
        }
        if (enc == null) {
            enc = "UTF-8";
        }
        //
        ByteArrayInputStream bIn;
        //
        try {
            bIn = new ByteArrayInputStream(s.getBytes(enc));
        } catch (UnsupportedEncodingException e) {
            bIn = new ByteArrayInputStream(s.getBytes());
        }
        //
        int c = bIn.read();
        StringBuffer ret = new StringBuffer();
        //
        while (c >= 0) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '.' || c == '-'
            /* || c == '*' */
            || c == '_') {
                ret.append((char) c);
            } else if (c == ' ') {
                ret.append("%20");
            } else {
                if (c < 128) {
                    ret.append(getHexChar(c));
                } else if (c < 224) {
                    ret.append(getHexChar(c));
                    ret.append(getHexChar(bIn.read()));
                } else if (c < 240) {
                    ret.append(getHexChar(c));
                    ret.append(getHexChar(bIn.read()));
                    ret.append(getHexChar(bIn.read()));
                }
            }
            //
            c = bIn.read();
        }
        //
        return ret.toString();
    }

    /**
     * <p>
     * Get a hex value of a char.
     * </p>
     * 
     * @param c Char.
     */
    private static String getHexChar(int c) {
        return (c < 16 ? "%0" : "%") + Integer.toHexString(c).toUpperCase();
    }
}