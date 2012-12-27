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

import com.ebupt.webjoin.insight.util.props.PropertySource;

 
public final class DeveditionUtils {
    public static final String DEV_EDITION_OVERRIDE = "insight.devedition.override";

    private DeveditionUtils () {
        throw new UnsupportedOperationException("No intance");
    }

    public static void setDeveditionOverride(boolean override) {
        System.setProperty(DEV_EDITION_OVERRIDE, String.valueOf(override));
    }
    
    public static boolean isDeveditionOverride() {
        return isDeveditionOverride(System.getProperty(DEV_EDITION_OVERRIDE));
    }
    
    public static boolean isDeveditionOverride(PropertySource source) {
        return isDeveditionOverride(source.getProperty(DEV_EDITION_OVERRIDE));
    }
    
    private static boolean isDeveditionOverride (String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        } else {
            return Boolean.parseBoolean(value);
        }
    }
}
