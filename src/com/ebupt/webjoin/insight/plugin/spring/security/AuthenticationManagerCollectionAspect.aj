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

package com.ebupt.webjoin.insight.plugin.spring.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

/**
 * 
 */
public aspect AuthenticationManagerCollectionAspect extends AbstractAuthenticationCollectionAspect {
    public AuthenticationManagerCollectionAspect () {
        super();
    }

    public pointcut collectionPoint() : execution(* AuthenticationManager+.authenticate(Authentication));
}
