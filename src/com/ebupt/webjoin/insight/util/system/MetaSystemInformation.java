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

import java.util.HashMap;
import java.util.Map;

import com.ebupt.webjoin.insight.util.StringUtil;
import com.ebupt.webjoin.insight.util.props.PropertiesUtil;
import com.ebupt.webjoin.insight.util.props.PropertySource;
import com.ebupt.webjoin.insight.util.system.net.NetworkAddressUtil;
 

/**
 * Returns properties based on other properties.
 */
public class MetaSystemInformation extends AbstractSystemInformation {
    public MetaSystemInformation () {
        super();
    }

    public Map<String, String> getProperties(Map<String, String> existingProperties) {
        Map<String, String> props = new HashMap<String, String>();
        String preferredIp = getPreferredIp(existingProperties);
        props.put(NETWORK_IP_KEY, preferredIp);
        
        String macAddress = NetworkAddressUtil.getMac(preferredIp);
        if (!StringUtil.isEmpty(macAddress)) {
        	props.put(NETWORK_MAC_KEY, macAddress);
        }

        return props;
    }

	/**
     * Use existing properties from different sources to determine the
     * best value for the {@link #NETWORK_IP_KEY} or {@link #NETWORK_MAC_KEY}.
     * @param props {@link Map} of properties to be used for value resolution
     * @return The resolved value - may be <code>null</code>/empty
     * @see #getPreferredIp(PropertySource)
     */
    public static final String getPreferredIp(Map<String, String> props) {
        return getPreferredIp(PropertiesUtil.toPropertySource(props));
    }
    
    /**
     * Use existing properties from different sources to determine the
     * best value for the {@link #NETWORK_IP_KEY} or {@link #NETWORK_MAC_KEY}.
     * @param props The {@link PropertySource} to use for value resolution
     * @return The resolved value - may be <code>null</code>/empty
     */
    public static final String getPreferredIp(PropertySource props) {
        String connectingIp = props.getProperty(NETWORK_CONNECTING_IP_KEY);
        String defaultRouteIp = props.getProperty(NETWORK_DEFAULT_ROUTE_IP_KEY);
        String fallbackIp = props.getProperty(NETWORK_FALLBACK_IP_KEY);
        fallbackIp = StringUtil.isEmpty(fallbackIp) ? "127.0.0.1" : fallbackIp;
        if ((!StringUtil.isEmpty(connectingIp)) && (!NetworkAddressUtil.isLoopback(connectingIp))) {
            return connectingIp;
        } else if (!StringUtil.isEmpty(defaultRouteIp)) {
            return defaultRouteIp;
        }
        return fallbackIp;
    }
}
