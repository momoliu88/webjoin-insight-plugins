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

import java.io.IOException;

/**
 * Used to mark classes that can output their contents as <code>properties</code>
 * files format
 */
public interface PropertiesAppender {
	/**
	 * @param sb The {@link Appendable} instance to append properties to
	 * @return Same as input
	 * @throws IOException If failed to append the properties
	 */
	<A extends Appendable> A appendProperties (A sb) throws IOException;
}
