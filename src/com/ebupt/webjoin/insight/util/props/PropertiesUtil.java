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

package com.ebupt.webjoin.insight.util.props;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ebupt.webjoin.insight.intercept.util.KeyValPair;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.MapUtil;
import com.ebupt.webjoin.insight.util.StringFormatterUtils;
import com.ebupt.webjoin.insight.util.StringUtil;

 


/**
 * Provides useful utilities for accessing {@link Properties} or {@link PropertySource}-s
 */
public final class PropertiesUtil {
	/**
	 * Default separator used to generate property pair in <code>properties</code> file
	 */
	public static final char	DEFAULT_PROPERTY_SEPARATOR=':';
	/**
	 * An alternative separator that the parser understands
	 */
	public static final char	ALTERNATIVE_PROPERTY_SEPARATOR='=';
	/**
	 * All allowed separators for a property name
	 */
	public static final String	PROPERTY_NAME_SEPARATORS=new String(new char[] { DEFAULT_PROPERTY_SEPARATOR, ALTERNATIVE_PROPERTY_SEPARATOR });
	/**
	 * Character used to denote the start of a property comment
	 */
	public static final char	PROPERTY_COMMENT_CHAR='#';

    private PropertiesUtil() {
        throw new UnsupportedOperationException("No instance");
    }

    public static boolean getBooleanProperty (PropertySource props, String key, boolean defaultValue) {
        String  value=props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value);
    }

    public static boolean getBooleanProperty (Properties props, String key, boolean defaultValue) {
        String  value=props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value);
    }

    public static long getLongProperty (PropertySource props, String key, long defaultValue)
            throws NumberFormatException {
        String  value=props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        return Long.parseLong(value);
    }

    public static long getLongProperty (Properties props, String key, long defaultValue)
            throws NumberFormatException {
        String  value=props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return Long.parseLong(value);
    }

    public static int getIntProperty (PropertySource props, String key, int defaultValue)
            throws NumberFormatException {
        String  value=props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        return Integer.parseInt(value);
    }

    public static int getIntProperty (Properties props, String key, int defaultValue)
            throws NumberFormatException {
        String  value=props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        return Integer.parseInt(value);
    }

    public static NamedPropertySource loadSourceFromFilePath (String path) throws IOException {
        InputStream in=new FileInputStream(path);
        try {
            return loadSourceFromInputStream(in);
        } finally {
            in.close();
        }
    }

    public static NamedPropertySource loadSourceFromFile (File f) throws IOException {
        InputStream in=new FileInputStream(f);
        try {
            return loadSourceFromInputStream(in);
        } finally {
            in.close();
        }
    }

    public static NamedPropertySource loadSourceFromURL (URL url) throws IOException {
        return toPropertySource(loadFromURL(url));
    }
    
    public static NamedPropertySource loadSourceFromInputStream (InputStream in) throws IOException {
        return toPropertySource(loadFromInputStream(in));
    }

    public static NamedPropertySource toPropertySource (final Map<String,String> props) {
        return new NamedPropertySource() {
            public String getProperty(String name) {
                return getProperty(name, null);
            }
            
            public String getProperty(String name, String defaultValue) {
                String  value=props.get(name);
                if (value == null) {
                    return defaultValue;
                }
                
                return value;
            }

            public Collection<String> getPropertyNames() {
                // avoid concurrent modifications
                return new TreeSet<String>(props.keySet());
            }

            @Override
            public String toString () {
                return String.valueOf(props);
            }
        };
    }

    public static NamedPropertySource toPropertySource (final Properties props) {
        return new NamedPropertySource() {
            public String getProperty(String name) {
                return getProperty(name, null);
            }
            
            public String getProperty(String name, String defaultValue) {
                return props.getProperty(name, defaultValue);
            }

            public Collection<String> getPropertyNames() {
                return propertiesNames(props);
            }

            @Override
            public String toString () {
                return String.valueOf(props);
            }
        };
    }

    public static Properties toProperties (NamedPropertySource source) {
        return addAllProperties(new Properties(), source);
    }
    
    public static <P extends Properties> P addAllProperties (P props, NamedPropertySource source) {
        for (String name : source.getPropertyNames()) {
            String  value=source.getProperty(name);
            props.setProperty(name, value);
        }

        return props;
    }

    /**
     * Reverses the functionality of properties names and values
     * @param src The source {@link NamedPropertySource}
     * @return A {@link NamedPropertySource} whose keys are the values of the
     * source and vice versa.
     */
    public static NamedPropertySource flip (NamedPropertySource src) {
    	Map<String, String>	propsMap=toPropertiesMap(src);
    	Map<String, String>	flippedMap=MapUtil.flip(propsMap, new TreeMap<String,String>());
    	return toPropertySource(flippedMap);
    }

    /**
     * Reverses the functionality of properties names and values
     * @param src The source {@link Properties)
     * @return A new instance where the values from the source are the keys
     * and vice versa. <B>Note:</B> <code>null</code>/empty values are skipped
     * @see #flip(Properties, Properties)
     */
    public static Properties flip (Properties src) {
    	return flip(src, new Properties());
    }

    /**
     * Reverses the functionality of properties names and values from the
     * source into the destination
     * @param src The source {@link Properties)
     * @param dst The destination {@link Properties)
     * @return Same as destination instance where the values from the source
     * are the keys and vice versa. <B>Note:</B> <code>null</code>/empty
     * values are skipped
     */
    public static Properties flip (Properties src, Properties dst) {
    	if (MapUtil.size(src) <= 0) {
    		return dst;
    	}

    	Collection<String>	names=propertiesNames(src);
    	for (String key : names) {
    		String value=src.getProperty(key);
    		if (StringUtil.isEmpty(value)) {
    			continue;
    		}

    		dst.setProperty(value, key);
    	}

    	return dst;
    }

    public static Map<String, String> toPropertiesMap (NamedPropertySource source) {
        return addAllMapProperties(new TreeMap<String,String>(), source);
    }
    
    public static <M extends Map<String,String>> M addAllMapProperties (M map, NamedPropertySource source) {
        for (String name : source.getPropertyNames()) {
            String  value=source.getProperty(name);
            map.put(name, value);
        }
        
        return map;
    }

    public static Properties loadFromFilePath (String path) throws IOException {
        InputStream in=new FileInputStream(path);
        try {
            return loadFromInputStream(in);
        } finally {
            in.close();
        }
    }

    public static Properties loadFromFile (File f) throws IOException {
        InputStream in=new FileInputStream(f);
        try {
            return loadFromInputStream(in);
        } finally {
            in.close();
        }
    }

    public static Properties loadFromURL (URL url) throws IOException {
        InputStream in=url.openStream();
        try {
            return loadFromInputStream(in);
        } finally {
            in.close();
        }
    }

    public static <P extends Properties> P loadFromURL (P props, URL url) throws IOException {
        InputStream in=url.openStream();
        try {
            return loadFromInputStream(props, in);
        } finally {
            in.close();
        }
    }

    public static Properties loadFromInputStream (InputStream in) throws IOException {
        return loadFromInputStream(new Properties(), in);
    }

    public static <P extends Properties> P loadFromInputStream (P props, InputStream in) throws IOException {
        props.load(in);
        return props;
    }

    /**
     * Appends all properties available from a {@link NamedPropertySource}
     * using the <code>properties</code> file format - including the
     * (E)nd-(O)f-(L)ine mark for the current machine
     * @param sb The {@link Appendable} instance to append the property line to
     * @param props The {@link NamedPropertySource} from which to get the
     * properties names and values
     * @return Same as input appender instance
     * @throws IOException If failed to append the properties lines
     * @see {@link #appendProperty(Appendable, String, String)}
     */
    public static <A extends Appendable> A appendProperties (A sb, NamedPropertySource props) throws IOException {
    	if (sb == null) {
    		throw new IOException("No appender instance");
    	}

    	Collection<String>	names=props.getPropertyNames();
    	if (ListUtil.size(names) <= 0) {
    		return sb;
    	}

    	for (String propName : names) {
    		appendProperty(sb, propName, props.getProperty(propName));
    	}

    	return sb;
    }

    /**
     * Appends a property name+value pair using the <code>properties</code> file
     * format - including the (E)nd-(O)f-(L)ine mark for the current machine
     * @param sb The {@link Appendable} instance to append the property line to
     * @param propName Property name - may not be <code>null</code>/empty
     * @param propValue Property value - if <code>null</code>/empty then nothing
     * generated
     * @return Same as input appender instance
     * @throws IOException If failed to append the property line
     */
    public static <A extends Appendable> A appendProperty (A sb, String propName, String propValue) throws IOException {
    	if (sb == null) {
    		throw new IOException("No appender instance");
    	}

    	if (StringUtil.isEmpty(propName)) {
    		throw new StreamCorruptedException("appendProperty(" + propName + ")[" + propValue + "] missing name");
    	}

    	if (StringUtil.isEmpty(propValue)) {
    		return sb;
    	}

    	sb.append(propName)
    	  .append(DEFAULT_PROPERTY_SEPARATOR)
    	  .append(' ')
    	  .append(propValue)
    	  ;
    	return StringFormatterUtils.appendEOL(sb);
    }

	/**
	 * Parses an input line assumed to contain a property pair using the
	 * <code>properties</code> file format. The method <U>ignores</U>
	 * empty lines, comments and/or malformed data. <B>Note:</B> the code
	 * looks for <U>both</U> property name separators
	 * @param data The input line - may be <code>null</code>/empty
	 * @return A {@link KeyValPair} whose key=property name (trimmed), and
	 * value=property value (trimmed) - <code>null</code> if cannot parse the
	 * input data (or ignored content)
	 * @see #DEFAULT_PROPERTY_SEPARATOR
	 * @see #ALTERNATIVE_PROPERTY_SEPARATOR
	 * @see #PROPERTY_COMMENT_CHAR
	 */
	public static KeyValPair<String,String> parsePropertyLine (String data) {
		String	line=StringUtil.isEmpty(data) ? "" : data.trim();
		if (StringUtil.isEmpty(line) || (line.charAt(0) == PROPERTY_COMMENT_CHAR)) {
			return null;
		}

		// find "earliest" separator
		int	sepPos=(-1);
    	for (int	index=0; index < PropertiesUtil.PROPERTY_NAME_SEPARATORS.length(); index++) {
    		char	sepChar=PropertiesUtil.PROPERTY_NAME_SEPARATORS.charAt(index);
    		int		chPos=line.indexOf(sepChar);
    		if (chPos < 0) {
    			continue;
    		}

    		if ((sepPos >= 0) && (sepPos < chPos)) {
    			continue;	// already have an earlier
    		}
    		
    		sepPos = chPos;
    	}

		if ((sepPos <= 0) || (sepPos >= (line.length() - 1))) {
			return null;	// ignore malformed lines where separator is 1st or no value
		}

		char	sepChar=line.charAt(sepPos);
		switch(sepChar) {
			case DEFAULT_PROPERTY_SEPARATOR	: 
				if (line.charAt(sepPos+1) != ' ') {
					return null;	// a ':' must be followed by a space
				}
				break;

			case ALTERNATIVE_PROPERTY_SEPARATOR	:
				if (line.charAt(sepPos-1) == ' ') {
					return null;	// a '=' cannot be preceded by a space
				}
				break;

			default	:	// do nothing
		}

		String	propName=line.substring(0, sepPos).trim();
		String	propValue=line.substring(sepPos+1).trim();
		if (StringUtil.isEmpty(propName) || StringUtil.isEmpty(propValue)) {
			return null;	// ignore malformed lines
		}

		return new KeyValPair<String, String>(propName, propValue);
	}

    public static Set<String> propertiesNames (Properties props) {
        if (MapUtil.size(props) <= 0) {
            return Collections.emptySet();
        }
        
        Set<String> names=new TreeSet<String>();
        for (Object key : props.keySet()) {
            names.add(String.valueOf(key));
        }
        
        return names;
    }

    public static String format (final String s, final Map<String,String> props) {
        return (MapUtil.size(props) <= 0) ? s : format(s, toPropertySource(props));
    }

    /**
     * Traverses the input {@link String} and looks for property patterns
     * encoded as <code>${propname}</code>. Once such a pattern is encountered
     * it is replaced with its value from the associated {@link PropertySource}
     * instance. If no value is found then the pattern is echoed to the output
     * as-is.
     * @param s Input string - may be null/empty (in which case nothing is
     * translated)
     * @param props The {@link PropertySource} to use to resolve referenced
     * properties - may be null (in which case nothing is translated)
     * @return Translation result - same as input if no translation occurred
     */
    public static String format (final String s, final PropertySource props)
    {
        final int   sLen=StringUtil.getSafeLength(s);
        if ((sLen <= 0) || (props == null)) {
            return s;
        }

        StringBuilder   sb=null;
        int             curPos=0;
        for (int    nextPos=s.indexOf('$'); (nextPos >= curPos) && (nextPos < sLen); ) {
            if (nextPos >= (sLen-1)) {
                break;  // if '$' at end then nothing can follow it anyway
            }

            if (s.charAt(nextPos+1) != '{') {
                nextPos = s.indexOf('$', nextPos + 1);
                continue;   // if not followed by '{' then assume not start of a property
            }

            final int   endPos=s.indexOf('}', nextPos + 2);
            if (endPos <= nextPos) {
                break;  // if no ending '}' then no more properties can exist
            }

            if (endPos <= (nextPos+2)) {
                if (endPos >= (sLen-1))
                    break;

                nextPos = s.indexOf('$', endPos + 1);
                continue;   // if empty property name assume clear text
            }

            final String    propName=s.substring(nextPos+2, endPos),
                            propVal=props.getProperty(propName);
            if (propVal == null) {
                nextPos = s.indexOf('$', endPos + 1);
                continue;   // if empty property value assume clear text
            }

            final String    repVal=format(propVal, props);    // do recursive resolution
            if (sb == null) {
                sb = new StringBuilder(sLen + repVal.length());
            }

            // append clear text
            if (nextPos > curPos) {
                final String    t=s.substring(curPos, nextPos);
                sb.append(t);
            }
            sb.append(repVal);

            if ((curPos=(endPos+1)) >= sLen) {
                break;  // stop if gone beyond string length
            }

            nextPos = s.indexOf('$', curPos);   // keep looking
        }

        // check if any leftovers
        if ((curPos > 0) && (curPos < sLen)) {
            final String    t=s.substring(curPos);
            sb.append(t);   // NOTE: sb cannot be null since we appended something to it
        }

        if (sb == null) { // means no replacement took place
            return s;
        }

        return sb.toString();
    }
}
