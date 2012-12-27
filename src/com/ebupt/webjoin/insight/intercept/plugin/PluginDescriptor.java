package com.ebupt.webjoin.insight.intercept.plugin;

import java.io.Serializable;

public class PluginDescriptor implements Serializable {
	private static final long serialVersionUID = 4536466551698450260L;
	private String name;
	private String version;
	private String publisher;
	private String href;

	@SuppressWarnings("unused")
	private PluginDescriptor() {
	}

	public PluginDescriptor(String name, String version, String publisher,
			String href) {
		this.name = name;
		this.version = version;
		this.publisher = publisher;
		this.href = href;
	}

	public String getName() {
		return this.name;
	}

	public String getVersion() {
		return this.version;
	}

	public String getPublisher() {
		return this.publisher;
	}

	public String getHref() {
		return this.href;
	}

	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (this.name == null ? 0 : this.name.hashCode());
		result = prime * result
				+ (this.version == null ? 0 : this.version.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PluginDescriptor other = (PluginDescriptor) obj;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		if (this.version == null) {
			if (other.version != null)
				return false;
		} else if (!this.version.equals(other.version))
			return false;
		return true;
	}
}