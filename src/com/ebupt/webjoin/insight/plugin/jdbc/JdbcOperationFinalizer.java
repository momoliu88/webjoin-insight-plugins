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
package com.ebupt.webjoin.insight.plugin.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFinalizer;
import com.ebupt.webjoin.insight.intercept.operation.OperationList;
import com.ebupt.webjoin.insight.intercept.operation.OperationMap;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.StringFormatterUtils;
import com.ebupt.webjoin.insight.util.StringUtil;

 

public class JdbcOperationFinalizer implements OperationFinalizer {
    private static final JdbcOperationFinalizer INSTANCE = new JdbcOperationFinalizer();
    
    /**
     * The keys in these maps should be strongly referenced in the frame stack; so they should not
     * be removed until the frame leaves the station.
     */
    private static final WeakKeyHashMap<Operation, Map<String, Object>> mappedParamStorage = new WeakKeyHashMap<Operation, Map<String, Object>>();
    private static final WeakKeyHashMap<Operation, List<Object>> indexedParamStorage = new WeakKeyHashMap<Operation, List<Object>>();

    public JdbcOperationFinalizer () {
    	super();
    }

    public static void register(Operation operation) {
        operation.addFinalizer(INSTANCE);
    }
    
    public static void addParam(Operation operation, String key, Object param) {
        synchronized (operation) {
            Map<String, Object> params = mappedParamStorage.get(operation);
            if (params == null) {
                params = new HashMap<String, Object>();
                mappedParamStorage.put(operation, params);
            }
            params.put(key, param);
        }
    }
    
    public static void addParam(Operation operation, int paramIndex, Object param) {
        // JDBC indexes are 1-based, so let's adjust it to the modern world first!
        int index=paramIndex - 1;
        synchronized (operation) {
            List<Object> params = indexedParamStorage.get(operation);
            if (params == null) {
                params = new ArrayList<Object>();
                indexedParamStorage.put(operation, params);
            }
            // grow array if needed
            while (index >= params.size()) {
                params.add(null);
            }
            params.set(index, param);
        }
    }
    
    public void finalize(Operation operation, Map<String, Object> richObjects) {
        operation.label(createLabel(operation.get("sql", String.class)));
        if (mappedParamStorage.get(operation) != null) {
            OperationMap params = operation.createMap("params");
            for (Entry<String, Object> entry : mappedParamStorage.get(operation).entrySet()) {
                params.put(entry.getKey(), StringFormatterUtils.formatObjectAndTrim(entry.getValue()));
            }
        } else if (indexedParamStorage.get(operation) != null) {
            OperationList params = operation.createList("params");
            for (Object param : indexedParamStorage.get(operation)) {
                params.add(StringFormatterUtils.formatObjectAndTrim(param));
            }
        }
        /**
         * We know we will never need these SoftKeyEntries again, so let's
         * remove it explicitly so they are cleaned up using traditional
         * garbage collection.
         */
        mappedParamStorage.remove(operation);
        indexedParamStorage.remove(operation);
    }

    private static final Collection<Map.Entry<String,String>>	stmtsList=
    		Collections.unmodifiableMap(new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER) {
				private static final long serialVersionUID = 1L;

				{
    				put("SELECT", " FROM ");
    				put("INSERT", " INTO ");
    				put("DELETE", " FROM ");
    				put("UPDATE", "UPDATE ");
    				put("CREATE TABLE", " TABLE ");
    				put("ALTER TABLE", " TABLE ");
    				put("DROP TABLE", " TABLE ");
    				put("CREATE INDEX", " INDEX ");
    				put("CREATE UNIQUE INDEX", " INDEX ");
    				put("DROP INDEX", " INDEX ");
    				put("CALL", "CALL ");
    			}
    		}).entrySet();
    public static String createLabel(String sql) {
        if (StringUtil.isEmpty(sql)) {
            return "JDBC";
        }

        String upperSql = sql.toUpperCase().trim();
        for (Map.Entry<String,String> stmt : stmtsList) {
        	String	kwd=stmt.getKey();
        	if (!upperSql.startsWith(kwd)) {
        		continue;
        	}

        	String	argPos=stmt.getValue();
        	return appendArgumentValue("JDBC " + kwd, captureWordAfter(upperSql, argPos));
        }

        // some special extra statements 
        if (upperSql.startsWith("CREATE")) {
            return "JDBC DML";
        } else if (upperSql.startsWith("CHECKPOINT")) {
            return "JDBC CHECKPOINT";
        } else {
            return "JDBC STATEMENT"; // could be any number of unhandled JDBC statements
        }
    }

    private static String appendArgumentValue (String prefix, String agrValue) {
    	if (StringUtil.isEmpty(agrValue)) {
    		return prefix;
    	} else {
    		return prefix + " (" + agrValue + ")";
    	}
    }

    private static final Set<Character> WORD_DELIMS=
        Collections.unmodifiableSet(ListUtil.asSet(Character.valueOf(' '), Character.valueOf('(')));
    private static String captureWordAfter(String source, String delim) {
        if (delim.charAt(delim.length() - 1) != ' ') {
            throw new IllegalArgumentException("Last char must be a ' '");
        }

        int fromIdx = source.indexOf(delim);
        if (fromIdx < 0) {
            return null;
        }

        String strAfterDelim = source.substring(fromIdx + delim.length() - 1).trim();
        int wordIdx = StringUtil.indexOfNotIn(strAfterDelim, WORD_DELIMS);
        if (wordIdx < 0) {
            return null;
        } else if (wordIdx > 0) {
        	strAfterDelim = strAfterDelim.substring(wordIdx);
        }

        int wordEndIdx = StringUtil.indexOfIn(strAfterDelim, WORD_DELIMS);
        if (wordEndIdx < 0) {
            return strAfterDelim;
        } else {
            return strAfterDelim.substring(0, wordEndIdx);
        }
    }
}
