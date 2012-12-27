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

import java.util.List;

import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.util.ObjectUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

/**
 * Describes an external resource. Designed for use by plugins implementing the
 * ExternalResourceAnalyzer interface
 */
public class ExternalResourceDescriptor {
	private final Frame frame;
	private final String name;
	private final String label;
	private final String type;
	private final String vendor;
	private final String host;
	private final int port;
	private final int hashValue;
	private final String color;
	private final boolean isIncoming; // false by default

	private final String destinationApplicationName;
	private final String destinationServerName;
	private final String destinationEndpointName;
	private ExternalResourceDescriptor child;

	private final String parentResourceName;
	private final boolean isParent;

	public ExternalResourceDescriptor(Frame resFrame, String resName,
			String resLabel, String resType, String resVendor, String resColor,
			boolean incoming) {
		this(resFrame, resName, resLabel, resType, resVendor, null, -1,
				resColor, incoming, null, null, null, null, false);
	}

	public ExternalResourceDescriptor(Frame resFrame, String resName,
			String resLabel, String resType, String resVendor, String resHost,
			int resPort, String resColor, boolean incoming, String parentResName) {
		this(resFrame, resName, resLabel, resType, resVendor, resHost, resPort,
				resColor, incoming, null, null, null, parentResName, false);
	}

	public ExternalResourceDescriptor(Frame resFrame, String resName,
			String resLabel, String resType, String resVendor, String resHost,
			int resPort, String resColor, boolean incoming) {
		this(resFrame, resName, resLabel, resType, resVendor, resHost, resPort,
				resColor, incoming, null, null, null, null, false);
	}

	public ExternalResourceDescriptor(Frame resFrame, String resName,
			String resLabel, String resType, String resVendor, String resHost,
			int resPort, String resColor, boolean incoming, boolean parentFlag) {
		this(resFrame, resName, resLabel, resType, resVendor, resHost, resPort,
				resColor, incoming, null, null, null, null, parentFlag);
	}

	public ExternalResourceDescriptor(Frame resFrame, String resName,
			String resLabel, String resType, String resVendor, String resHost,
			int resPort, String resColor, boolean incoming, String destAppName,
			String destServerName, String destEndPointName, String parentResName) {
		this(resFrame, resName, resLabel, resType, resVendor, resHost, resPort,
				resColor, incoming, null, null, null, null, false);
	}

	public ExternalResourceDescriptor(Frame resFrame, String resName,
			String resLabel, String resType, String resVendor, String resHost,
			int resPort, String resColor, boolean incoming, String destAppName,
			String destServerName, String destEndPointName,
			String parentResName, boolean parentFlag) {
		this.frame = resFrame;
		this.name = resName;
		this.label = resLabel;
		this.host = resHost;
		this.port = resPort;
		this.type = resType;
		this.vendor = resVendor;
		this.color = resColor;
		this.isIncoming = incoming;
		this.destinationApplicationName = destAppName;
		this.destinationEndpointName = destEndPointName;
		this.destinationServerName = destServerName;
		this.parentResourceName = parentResName;
		this.isParent = parentFlag;

		// we can calculate the hash code since all members are final
		this.hashValue = _calcHashCode();
	}

	public Frame getFrame() {
		return frame;
	}

	public String getLabel() {
		return label;
	}

	public String getVendor() {
		return vendor;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getColor() {
		return color;
	}

	public boolean isIncoming() {
		return isIncoming;
	}

	public String getParent() {
		return parentResourceName;
	}

	public boolean isParent() {
		return isParent;
	}

	public String getDestinationApplicationName() {
		return destinationApplicationName;
	}

	public String getDestinationEndpointName() {
		return destinationEndpointName;
	}

	public String getDestinationServerName() {
		return destinationServerName;
	}

	public ExternalResourceDescriptor getChild() {
		return child;
	}

	@Override
	public int hashCode() {
		return hashValue;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;

		ExternalResourceDescriptor other = (ExternalResourceDescriptor) obj;
		if ((StringUtil.safeCompare(this.getName(), other.getName()) != 0)
				|| (StringUtil.safeCompare(this.getType(), other.getType()) != 0)
				|| (StringUtil.safeCompare(this.getHost(), other.getHost()) != 0)
				|| (StringUtil.safeCompare(this.getVendor(), other.getVendor()) != 0)
				|| (this.getPort() != other.getPort())
				|| (this.isIncoming() != other.isIncoming())
				|| (StringUtil.safeCompare(
						this.getDestinationApplicationName(),
						other.getDestinationApplicationName()) != 0)
				|| (StringUtil.safeCompare(this.getDestinationEndpointName(),
						other.getDestinationEndpointName()) != 0)
				|| (StringUtil.safeCompare(this.getDestinationServerName(),
						other.getDestinationServerName()) != 0)
				|| (StringUtil.safeCompare(this.getParent(), other.getParent()) != 0)
				|| (this.isParent() != other.isParent()))
			return false;
		else
			return true;
	}

	@Override
	public String toString() {
		return getName() + "[" + getType() + "] ," + getVendor() + " - "
				+ getLabel()
				+ (isIncoming() ? " incoming to " : " outgoing from ")
				+ getHost() + ":" + getPort() + " parent: " + getParent();
	}

	private int _calcHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ObjectUtil.hashCode(getName());
		result = prime * result + ObjectUtil.hashCode(getType());
		result = prime * result + ObjectUtil.hashCode(getHost());
		result = prime * result + ObjectUtil.hashCode(getVendor());
		result = prime * result + getPort();
		result = prime * result + (isIncoming() ? 1231 : 1237);
		result = prime * result
				+ ObjectUtil.hashCode(getDestinationApplicationName());
		result = prime * result
				+ ObjectUtil.hashCode(getDestinationEndpointName());
		result = prime * result
				+ ObjectUtil.hashCode(getDestinationServerName());
		result = prime * result + ObjectUtil.hashCode(getParent());
		result = prime * result + (isParent() ? 1231 : 1237);
		return result;
	}

	public void setChildren(List<ExternalResourceDescriptor> descriptorList) {
		if (descriptorList == null || descriptorList.size() <= 0)
			return;
		this.child = descriptorList.get(0);
	}
}
