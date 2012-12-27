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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.ExceptionUtils;
import com.ebupt.webjoin.insight.util.StringFormatterUtils;
import com.ebupt.webjoin.insight.util.StringUtil;


 

/**
 * Provides manipulations of a <code>properties</code> file while at the same
 * time preserving the file's internal structure - including comments
 */
public class LayoutPreservingProperties implements NamedPropertySource {
    private static final Pattern COMMENT_PATTERN  = Pattern.compile("^\\s*[#!].*");
    private static final Pattern EMPTY_PATTERN    = Pattern.compile("\\s+");
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("[:=]");
    
    private final List<Item> linesList=new ArrayList<Item>();
    private final Map<String, KeyValuePair> keyToPairMap=new HashMap<String, KeyValuePair>();
    private final Map<String, Comment> commentsMap=new HashMap<String, Comment>();
    private final Properties props=new Properties();
    private final File file;
    
    public LayoutPreservingProperties(File propsFile) {
        file = propsFile;
        
        if (propsFile.exists()) {
            readFile(propsFile);
        }
    }

    public File getPropsFile () {
    	return file;
    }

    private void readFile(File fileToRead) {
        try {
            FileInputStream in = new FileInputStream(fileToRead);
            try {
            	props.load(in);
            } finally {
                in.close();
            }
        } catch (Exception e) {
            throw ExceptionUtils.toRuntimeException(e);
        }

        try {
        	BufferedReader reader = new BufferedReader(new FileReader(fileToRead));
        	try {
	            boolean multValue = false;
	            KeyValuePair pair = null;
	            Comment	lastComment = null;
	            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
	                if (isEmpty(line)) {
	                    linesList.add(EmptyLine.getInstance());
	                    multValue = false;
	                    continue;
	                }
                
	                if (multValue) {
	                	if (pair == null) {
	                		throw new StreamCorruptedException("No current key for multi-line: " + line);
	                	}
	                    pair.append(StringFormatterUtils.EOL).append(line);
	                    multValue = isMultValue(line);
	                    if (lastComment != null) {
	                    	lastComment = null;
	                    }
	                    continue;
	                }
                
	                if (isComment(line)) {
	                	lastComment = new Comment(line);
	                    linesList.add(lastComment);
	                    continue;
	                }
                
	                String[] parts = PROPERTY_PATTERN.split(line, 2);
	                //this is not our job to validate the file structure
	                if (ArrayUtil.length(parts) < 2) {
	                	lastComment = new Comment(line);
	                    linesList.add(lastComment);
	                    continue;
	                }
                
	                String key = parts[0].trim();
	                if (StringUtil.isEmpty(key)) {
	                	throw new StreamCorruptedException("Empty key for line: " + line);
	                }

	                String value = parts[1];
	                pair = new KeyValuePair(key, value);
	                keyToPairMap.put(key, pair);
	                linesList.add(pair);

                    if (lastComment != null) {
                    	commentsMap.put(key, lastComment);
                    	lastComment = null;
                    }
                
	                multValue = isMultValue(line);
	            }
        	} finally {
        		reader.close();
        	}
        } catch (Exception e) {
            throw ExceptionUtils.toRuntimeException(e);
        }
    }
    
    private static boolean isMultValue(String line) {
        return line.endsWith("\\");
    }
    
    private static boolean isEmpty(String line) {
        return StringUtil.isEmpty(line) || EMPTY_PATTERN.matcher(line).matches();
    }
    
    private static boolean isComment(String line) {
        return COMMENT_PATTERN.matcher(line).matches();
    }
    
    public String getProperty(String name) {
        return this.props.getProperty(name);
    }
    
    public String getProperty(String name, String defaultValue) {
        return this.props.getProperty(name, defaultValue);
    }
    
    public Collection<String> getPropertyNames() {
        return this.keyToPairMap.keySet();
    }

    public String setProperty(String key, Object value) {
    	return setProperty(key, value, null);
    }

    public String setProperty(String key, Object value, String comment) {
        Object	oldValue=put(key, value, comment);
        return StringUtil.safeToString(oldValue);
    }

    public Object put(String key, Object value) {
    	return put(key, value, null);
    }

    public Object put(String key, Object value, String comment) {
    	if (StringUtil.isEmpty(key) || (value == null)) {
    		throw new IllegalArgumentException("put(" + key + ")[" + value + "] null/empty key or null value");
    	}

        KeyValuePair pair = getKeyValuePair(key);
        if (pair != null) {
            pair.setValue(StringUtil.safeToString(value));

            if (!StringUtil.isEmpty(comment)) {
            	Comment	commentLine=getComment(key);
            	if (commentLine != null) {
            		commentLine.setComment("#" + comment);
            	}
            }
        } else {
            pair = new KeyValuePair(key, StringUtil.safeToString(value));
            keyToPairMap.put(key, pair);

            if (!StringUtil.isEmpty(comment)) {
            	Comment	commentLine=new Comment("#" + comment);
            	commentsMap.put(key, commentLine);
            	linesList.add(commentLine);
            }

            linesList.add(pair);
        }
        
        return props.put(key, value);
    }

    public Object removeProperty(String key) {
    	KeyValuePair pair=keyToPairMap.remove(key);
    	if (pair == null) {
    		return null;
    	}

    	if (!linesList.remove(pair)) {
    		throw new IllegalStateException("Line not found: " + pair);
    	}

    	Comment	comment=commentsMap.remove(key);
    	if (comment != null) {
    		linesList.remove(comment);
    	}

    	Object	value=props.remove(key);
    	return value;
    }

    public void writeToFile() {
        writeToFile(getPropsFile());
    }

    KeyValuePair getKeyValuePair (String key) {
    	return keyToPairMap.get(key);
    }

    Comment getComment (String key) {
    	return commentsMap.get(key);
    }

    private void writeToFile(File fileToWrite) {
        if (fileToWrite == null) {
            throw new IllegalArgumentException("file is null");
        }
        
        try {
            BufferedWriter writer=new BufferedWriter(new FileWriter(fileToWrite));
            try {
	            for(int i=0; i<linesList.size(); i++) {
	            	Item	item=linesList.get(i);
	                if (i>0) {
	                    writer.write(StringFormatterUtils.EOL);
	                }

	                writer.write(item.toString());
	            }
            } finally {
            	writer.close();
            }
        } catch (Exception e) {
            throw ExceptionUtils.toRuntimeException(e);
        }
    }

    static interface Item {
    	// marker interface
    }

    static final class EmptyLine implements Item {
    	private static final EmptyLine	INSTANCE=new EmptyLine();	
    	private EmptyLine () {
    		super();
    	}
    	
    	static EmptyLine getInstance () {
    		return INSTANCE;
    	}

    	@Override
    	public String toString () {
    		return "";
    	}
    }

    static final class Comment implements Item {
        private String value;
        
        public Comment(String data) {
            this.value = data;
        }

        public String getComment () {
        	return value;
        }

        public void setComment (String data) {
        	value = data;
        }

        @Override
        public String toString() {
            return getComment();
        }
    }

    static final class KeyValuePair implements Item {
        public static final String SEPARATOR = ": "; 
        private String key;
        private StringBuilder value;
        
        public KeyValuePair(String keyData, String valueData) {
            this.key = keyData;
            setValue(valueData);
        }
        
        public void setValue(String valueData) {
        	String	effectiveValue=StringUtil.trimLeadingWhitespace(valueData);
        	if (value == null) {
        		value = StringUtil.isEmpty(effectiveValue)
        				? new StringBuilder()
        				: new StringBuilder(effectiveValue)
        				;
        	} else {
        		value.setLength(0);
        		value.append(effectiveValue);
        	}
        }

        public StringBuilder append(CharSequence data) {
        	return this.value.append(data);
        }

        @Override
        public String toString() {
            return new StringBuilder(key.length() + SEPARATOR.length() + value.length())
            			   .append(key)
                           .append(SEPARATOR)
                           .append(value)
                           .toString()
                           ;
        }
    }
}
