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

package com.ebupt.webjoin.insight.intercept.trace;

import java.util.Collections;
import java.util.Set;

public class EmptyObscuredValueRegistry implements ObscuredValueRegistry {
    private static final EmptyObscuredValueRegistry INSTANCE = new EmptyObscuredValueRegistry();
    public EmptyObscuredValueRegistry () {
        super();
    }

    public static final EmptyObscuredValueRegistry getInstance () {
        return INSTANCE;
    }

    public void markObscured(Object o) {
        throw new IllegalArgumentException("Unable to mark anything sensitive in an empty registry");
    }

    public boolean isObscured(Object obj) {
        return false;
    }

    public Set<Object> getValues() {
        return Collections.emptySet();
    }
}
