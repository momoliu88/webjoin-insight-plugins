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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ebupt.webjoin.insight.util.StringFormatterUtils;

 
/**
 * A {@link SystemInformation} that aggregates the properties across
 * a list of sub-infos. <B>Note:</B> <U>order</U> of the provided sub-infos
 * is important if same property appears in more than one info. In such as
 * case, the <U>last</U> info prevails 
 */
public class AggregatingSystemInformation extends AbstractSystemInformation {
    private final List<SystemInformation> children;
    public AggregatingSystemInformation(SystemInformation ... sysInfos) {
        this(Arrays.asList(sysInfos));
    }

    public AggregatingSystemInformation(Collection<? extends SystemInformation> sysInfos) {
        children = Collections.unmodifiableList(new ArrayList<SystemInformation>(sysInfos));
    }

    public Map<String, String> getProperties(Map<String, String> existingProperties) {
        Map<String, String> res = new HashMap<String, String>();
        for (SystemInformation sysInfo : children) {
            res.putAll(sysInfo.getProperties(Collections.unmodifiableMap(res)));
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder   sb=new StringBuilder(children.size() * 256 + 8);
        for (SystemInformation sysInfo : children) {
            if (sb.length() > 0) {
                sb.append(StringFormatterUtils.EOL);
            }
            sb.append(sysInfo);
        }
        return sb.toString();
    }
}
