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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * Returns the <U>first</U> successful look according to the provided order
 * of {@link PropertySource}-s
 */
public class AggregatePropertySource implements PropertySource {
    protected final Collection<? extends PropertySource> sources;
    public AggregatePropertySource (PropertySource ... srcList) {
        this((srcList == null) ? null : Arrays.asList(srcList));
    }

    public AggregatePropertySource (final Collection<? extends PropertySource> srcList) {
        if ((srcList == null) || srcList.isEmpty()) {
            throw new IllegalStateException("No sources provided");
        }

        sources = Collections.unmodifiableList(new ArrayList<PropertySource>(srcList));
    }

    public String getProperty(String name) {
        return getProperty(name, null);
    }

    public String getProperty(String name, String defaultValue) {
        for (PropertySource src : sources) {
            String  value=src.getProperty(name);
            if (value != null) {
                return value;
            }
        }

        // this point is reached if no source returned a valid value
        return defaultValue;
    }

}
