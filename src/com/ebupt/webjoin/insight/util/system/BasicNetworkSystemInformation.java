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

package com.ebupt.webjoin.insight.util.system;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.util.StringUtil;
import com.ebupt.webjoin.insight.util.props.NamedPropertySource;
import com.ebupt.webjoin.insight.util.props.PropertiesUtil;
import com.ebupt.webjoin.insight.util.system.net.NetworkAddressUtil;
 

/**
 * Provides basic network related properties:
 *  - {@link SystemInformation#NETWORK_IP_KEY}
 *  - {@link SystemInformation#NETWORK_NAME_KEY}
 */
public class BasicNetworkSystemInformation
        extends AbstractSystemInformation
        implements NamedPropertySource {
    private static final BasicNetworkSystemInformation   INSTANCE=new BasicNetworkSystemInformation();
    private final String testHost;
    private final int testPort;
    /**
     * Potentially available properties names (not all may return a value...)
     */
    static final Collection<String>  PROPS_NAMES=
            Collections.unmodifiableList(Arrays.asList(
                    NETWORK_CONNECTING_IP_KEY,
                    NETWORK_FALLBACK_IP_KEY,
                    NETWORK_NAME_KEY));

    public static final BasicNetworkSystemInformation getInstance () {
        return INSTANCE;
    }

    public BasicNetworkSystemInformation(String testUriString) {
        URI testUri = null;
        try {
            if (!StringUtil.isEmpty(testUriString)) {
                testUri = new URI(testUriString);
            }
        } catch (URISyntaxException e) {  // ignored
            Logger  logger=Logger.getLogger(getClass().getName());
            logger.warning("Bad (" + e.getClass().getSimpleName() + ")"
                         + " test URI string (" + testUriString + ")"
                         + ": " + e.getMessage());
        }

        if (testUri != null) {
            testHost = testUri.getHost();
            testPort = testUri.getPort();
        } else {
            testHost = null;
            testPort = -1;
        }
    }

    public BasicNetworkSystemInformation(URI testUri) {
        if (testUri != null) {
            testHost = testUri.getHost();
            testPort = testUri.getPort();
        } else {
            testHost = null;
            testPort = -1;
        }
    }

    public BasicNetworkSystemInformation() {
        this(null, -1);
    }

    public BasicNetworkSystemInformation(String host, int port) {
        this.testHost = host;
        this.testPort = port;
    }

    public Map<String, String> getProperties(Map<String, String> existingProperties) {
        Map<String,String>  result=null;
        for (String name : PROPS_NAMES) {
            String  value=getProperty(name);
            if (StringUtil.isEmpty(value)) {
                continue;
            }
            
            if (result == null) {
                result = new TreeMap<String, String>();
            }
            result.put(name, value);
        }

        if (result == null) {
            return Collections.emptyMap();
        }
        
        return result;
    }

    @Override
	public <A extends Appendable> A appendProperties(A sb) throws IOException {
		return PropertiesUtil.appendProperties(sb, this);
	}

	public String getProperty(String name, String defaultValue) {
        String  value=getProperty(name);
        if (value == null)
            return defaultValue;
        else
            return value;
    }

    public Collection<String> getPropertyNames() {
        Collection<String>  available=new LinkedList<String>();
        for (String name : PROPS_NAMES) {
            String  value=getProperty(name);
            if (StringUtil.isEmpty(value)) {
                continue;
            }
            
            available.add(name);
        }
        
        return available;
    }

    public String getProperty(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException("No property name");
        }
        
        if (NETWORK_CONNECTING_IP_KEY.equals(name)) {
            InetAddress conAddr = getConnectingIp();
            if (conAddr != null) {
                String ip = conAddr.toString();
                return ip.replaceAll(".*/", "");
            }
        } else if (NETWORK_FALLBACK_IP_KEY.equals(name)) {
            InetAddress addr = getFallbackAddress();
            if (addr != null) {
                String ip = addr.toString();
                return ip.replaceAll(".*/", "");
            }
        } else if (NETWORK_NAME_KEY.equals(name)) {
            InetAddress addr = getConnectingIp();
            
            if (addr == null) {
                addr = getFallbackAddress();
            }
            
            if (addr != null ) {
                return addr.getHostName();
            }
        }

        return null;
    }

    public String getTestHost() {
        return testHost;
    }

    public int getTestPort() {
        return testPort;
    }

    public InetAddress getFallbackAddress() {
        InetAddress addr = null;
        // Trapped in guess work
        if (addr == null) {
            addr = NetworkAddressUtil.getFirstExternalNetwork4Address();
        }
        // Giving up and using localhost.
        if (addr == null) {
            addr = NetworkAddressUtil.getDefaultNetworkAddress();
        }
        return addr;
    }

    public InetAddress getConnectingIp() {
        // Try a target system, because it more accurate, and it does not require full
        // connectivity
        if ((!StringUtil.isEmpty(testHost)) && (testPort > 0)) {
            return NetworkAddressUtil.getExternalAddressThroughTCPConnect(testHost, testPort);
        }
        return null;
    }
}