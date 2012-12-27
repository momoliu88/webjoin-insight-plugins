/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ebupt.webjoin.insight.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ebupt.webjoin.insight.intercept.util.KeyValPair;
import com.ebupt.webjoin.insight.intercept.util.time.TimeRange;

 
public class StringFormatterUtils {
    public static final String NO_VALUE_STRING = "void";
    public static final String NULL_VALUE_STRING = "null";
    public static final int MAX_PARAM_LENGTH = 40;
    public static final String  LOWERCASE_HEX="0123456789abcdef";
    public static final String  UPPERCASE_HEX="0123456789ABCDEF";
    public static final String  EOL=System.getProperty("line.separator", "\n");

    public static String formatObject(Object obj) {
        if (obj == null) {
            return NULL_VALUE_STRING;
        }

        if (isToStringable(obj)) {
            return obj.toString();
        }

        return obj.getClass().getName();
    }

    public static String formatObjectAndTrim(Object obj) {
    	return formatObject(obj, MAX_PARAM_LENGTH);
    }
    
    public static String formatObject(Object obj, int length) {
        return StringUtil.trimWithEllipsis(formatObject(obj), length);
    }

    public static boolean isToStringable(Object obj) {
        return isPrimitiveWrapper(obj)
            || obj instanceof String
            || obj instanceof StringBuilder
            || obj instanceof StringBuffer
            || obj instanceof Date
            || obj instanceof Throwable
            || obj instanceof TimeRange;
    }

    
    public static String formatStackTrace(Throwable t) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            t.printStackTrace(new PrintStream(out, true, "UTF-8"));
            return new String(out.toByteArray(), "UTF-8");   
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    public static Map<String, String> formatMap(Map<?, ?> map) {
        if (map == null) {
            return Collections.emptyMap();
        }
        Map<String, String> formatted = new HashMap<String, String>();
        for (Object key : map.keySet()) {
            Object val = map.get(key);
            formatted.put(formatObject(key), formatObject(val));
        }
        return formatted;
    }

    /**
     * Format all key/value in the input pairs into string keyval pairs.  The
     * resulting list is sorted in default string sorting order.
     */
    public static List<KeyValPair<String, String>> formatMapAsSorted(Map<?, ?> map) {
        Map<String, String> formatted = formatMap(map);
        List<String> sortedKeys = new ArrayList<String>(formatted.keySet());
        Collections.sort(sortedKeys);

        List<KeyValPair<String, String>> res = new ArrayList<KeyValPair<String, String>>();        
        for (String key : sortedKeys) {
            res.add(new KeyValPair<String, String>(key, formatted.get(key)));
        }
        return res;
    }
    
    /**
     * @param argument An {@link Object}
     * @return TRUE if it is a wrapper for one of the primitive types
     */
    public static boolean isPrimitiveWrapper(Object argument) {
        // Could check for Number and exclude BigInteger and BigDecimal,
        // but that will be a complex check
        return (argument == null) ? false : ClassUtil.isPrimitiveWrapperClass(argument.getClass());
    }

    public static String formatAsHexString (boolean useUppercase, byte ... bytes) {
        if ((bytes == null) || (bytes.length <= 0)) {
            return "";
        }

        CharSequence    hexChars=useUppercase ? UPPERCASE_HEX : LOWERCASE_HEX;
        char[]          chars=new char[bytes.length * 2];
        for (int bIndex=0, cIndex=0; bIndex < bytes.length; bIndex++, cIndex += 2) {
            byte    bValue=bytes[bIndex];
            chars[cIndex] = hexChars.charAt((bValue >> 4) & 0x0F);
            chars[cIndex + 1] = hexChars.charAt(bValue & 0x0F);
        }

        return new String(chars);
    }

    public static <A extends Appendable> A appendHex (A sb, byte bValue, boolean useUppercase)
            throws IOException {
        CharSequence    hexChars=useUppercase ? UPPERCASE_HEX : LOWERCASE_HEX;
        sb.append(hexChars.charAt((bValue >> 4) & 0x0F));
        sb.append(hexChars.charAt(bValue & 0x0F));
        return sb;
    }

    public static <A extends Appendable> A appendEOL (A sb) throws IOException {
        sb.append(EOL);
        return sb;
    }

    public static byte[] formatAsHexBytes (boolean useUppercase, byte bValue) {
        byte[]  buf=new byte[2];
        formatAsHexBytes(useUppercase, bValue, buf);
        return buf;
    }

    public static int formatAsHexBytes (boolean useUppercase, byte bValue, byte[] buf) {
        return formatAsHexBytes(useUppercase, bValue, buf, 0, buf.length);
    }

    public static int formatAsHexBytes (boolean useUppercase, byte bValue, byte[] buf, int off, int len) {
        if (len < 2) {
            throw new IndexOutOfBoundsException("Not enough space for HEX data: " + len);
        }
        
        CharSequence    hexChars=useUppercase ? UPPERCASE_HEX : LOWERCASE_HEX;
        buf[off] = (byte) hexChars.charAt((bValue >> 4) & 0x0F);
        buf[off + 1] = (byte) hexChars.charAt(bValue & 0x0F);
        return 2;
    }

    public static byte decodeHexBytes (byte[] buf, int off, int len) {
        if (len < 2) {
            throw new IndexOutOfBoundsException("Not enough space for HEX data: " + len);
        }
        
        return decodeHexBytes(buf[off], buf[off+1]);
    }
    
    public static byte decodeHexBytes (byte hi, byte lo) {
        byte    hiValue=getHexNibbleValue((char) (hi & 0x00FF)),
                loValue=getHexNibbleValue((char) (lo & 0x00FF));
        if ((hiValue < 0) || (loValue < 0)) {
            throw new NumberFormatException("decodeHexBytes() invalid value: 0x"
                        + Integer.toHexString(hi) + Integer.toHexString(lo));
        }
        
        return (byte) (((hiValue << 4) & 0xF0) + (loValue & 0x0F));
    }

    public static byte[] decodeHexString (CharSequence cs) throws NumberFormatException {
        return decodeHexString(cs, 0, cs.length());
    }

    public static byte[] decodeHexString (CharSequence cs, int startPos, int length)
            throws NumberFormatException {
        if (length <= 0) {
            return new byte[0];
        }
        
        if ((length & 0x01) != 0) {
            throw new NumberFormatException("decodeHexString(" + cs + ") invalid length: " + length);
        }

        byte[]  result=new byte[length / 2];
        for (int index=0, pos=startPos, maxPos=startPos + length; pos < maxPos; pos += 2, index++) {
            char    hi=cs.charAt(pos), lo=cs.charAt(pos + 1);
            byte    hiValue=getHexNibbleValue(hi), loValue=getHexNibbleValue(lo);
            if ((hiValue < 0) || (loValue < 0)) {
                throw new NumberFormatException("decodeHexString(" + cs + ") invalid value: " + new String(new char[] { hi, lo }));
            }
            
            result[index] = (byte) (((hiValue << 4) & 0xF0) + (loValue & 0x0F));
        }
        
        return result;
    }

    private static byte getHexNibbleValue (char ch) {
        int index=UPPERCASE_HEX.indexOf(ch);
        if (index < 0) {
            index = LOWERCASE_HEX.indexOf(ch);
        }
        
        if (index < 0) {
            return (-1);
        }
        
        return (byte) (index & 0x00FF);
    }

    public static final String  DEFAULT_DELIMITER=",";
    public static String arrayToDelimitedString (Object[] values) {
        return arrayToDelimitedString(DEFAULT_DELIMITER, values);
    }

    public static String arrayToDelimitedString (String delim, Object[] values) {
        return arrayToDelimitedString("", "", delim, values);
    }

    public static String arrayToDelimitedString (String prefix, String suffix, String delim, Object[] values) {
        int                 numValues=(values == null) ? 0 : values.length;
        Collection<Object>  coll=(numValues <= 0) ? Collections.<Object>emptyList() : Arrays.asList(values);
        return collectionToDelimitedString(coll, delim, prefix, suffix);
    }

    public static String collectionToDelimitedString (Collection<?> coll) {
        return collectionToDelimitedString(coll, DEFAULT_DELIMITER);
    }

    public static String collectionToDelimitedString (Collection<?> coll, String delim) {
        return collectionToDelimitedString(coll, delim, "", "");
    }
    
    public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {
        int             numItems=ListUtil.size(coll),
                        prefixLen=StringUtil.getSafeLength(prefix),
                        delimLen=StringUtil.getSafeLength(delim),
                        suffixLen=StringUtil.getSafeLength(suffix),
                        initialSize=prefixLen + numItems * (16 + delimLen) + suffixLen;
        if (initialSize <= 0) {
            return "";
        }

        StringBuilder   sb=new StringBuilder(initialSize);
        if (prefixLen > 0) {
            sb.append(prefix);
        }

        if (numItems > 0) {
            for (Object o : coll) {
                // check if already have some listed items
                if ((delimLen > 0) && (sb.length() > prefixLen)) {
                    sb.append(delim);
                }
                
                sb.append(o);
            }
        }

        if (suffixLen > 0) {
            sb.append(suffix);
        }
        
        if (sb.length() > 0) {
            return sb.toString();
        }
        
        return "";
    }
}
