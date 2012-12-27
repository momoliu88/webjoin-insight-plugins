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

package com.ebupt.webjoin.insight.intercept.topology;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.ebupt.webjoin.insight.util.StringFormatterUtils;
import com.ebupt.webjoin.insight.util.StringUtil;

 

public final class MD5NameGenerator {
	private MD5NameGenerator () {
		throw new UnsupportedOperationException("No instance");
	}

    public static String getName(URI uri) {
        return getName(uri.toASCIIString());
    }

    public static String getName(String stringToHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[]        stringBytes=stringToHash.getBytes();
            byte[]        bytes = md.digest(stringBytes);
            /*
             * In order to remain backward compatible with the former code
             * (new BigInteger(1, bytes).toString(16)) we have to remove
             * leading zeroes
             */
            String digest = trimLeadingZeroes(StringFormatterUtils.formatAsHexString(false, bytes));
            if (StringUtil.isEmpty(digest)) {
                throw new IllegalStateException("No non-zero bytes found in digest of " + stringToHash);
            }
            
            return digest;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 Algorithm Not Available");
        }
    }

    static String trimLeadingZeroes (String digest) {
        for (int index=0; index < digest.length(); index++) {
            if (digest.charAt(index) != '0') {
                if (index == 0) {
                    return digest;
                } else {
                    return digest.substring(index);
                }
            }
        }

        return "";  // all zeroes
    }
}
