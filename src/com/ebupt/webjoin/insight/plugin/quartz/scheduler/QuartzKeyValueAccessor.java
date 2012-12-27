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
package com.ebupt.webjoin.insight.plugin.quartz.scheduler;

import com.ebupt.webjoin.insight.util.StringUtil;


/**
 * 
 */
public final class QuartzKeyValueAccessor extends AbstractQuartzValueAccessor {
	private static class LazyFieldHolder {
		static final QuartzKeyValueAccessor	accessor=new QuartzKeyValueAccessor();
	}

	public static QuartzKeyValueAccessor getInstance () {
		return LazyFieldHolder.accessor;
	}

	QuartzKeyValueAccessor () {
		super("org.quartz.utils.Key");
	}

	public String getFullName (Object key) {
		String	group=getGroup(key), name=getName(key);
		if (StringUtil.isEmpty(group)) {
			if (StringUtil.isEmpty(name)) {
				return "";
			} else {
				return name;
			}
		} else if (StringUtil.isEmpty(name)) {
			return group;
		} else {	// both non-empty
			return group + "." + name;
		}
	}

	public String getGroup (Object key) {
		return getKeyValue(key, "group");
	}

	public String getName (Object key) {
		return getKeyValue(key, "name");
	}

    private String getKeyValue (Object key, String name) {
    	return getProperty(key, name, String.class);
    }
}
