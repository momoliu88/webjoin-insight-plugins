package com.ebupt.webjoin.insight.intercept.application;

import java.util.HashMap;
import java.util.Map;

import com.ebupt.webjoin.insight.application.ApplicationName;

public class ApplicationMetadataCache {
	private static final ApplicationMetadataCache instance = new ApplicationMetadataCache();
	private Map<ApplicationName, ApplicationMetadata> cache;

	private ApplicationMetadataCache() {
		this.cache = new HashMap<ApplicationName, ApplicationMetadata>();
	}

	public static ApplicationMetadataCache getInstance() {
		return instance;
	}

	public void clear() {
		synchronized (this.cache) {
			for (ApplicationName name : this.cache.keySet())
				this.cache.remove(name);
		}
	}

	public String getLabel(ApplicationName name) {
		synchronized (this.cache) {
			ApplicationMetadata metadata = (ApplicationMetadata) this.cache
					.get(name);
			if (metadata != null) {
				String label = metadata.getLabel();
				if (label != null) {
					return label;
				}
			}

			return name.getFormatted();
		}
	}

	public void setLabel(ApplicationName name, String label) {
		synchronized (this.cache) {
			ApplicationMetadata metadata = (ApplicationMetadata) this.cache
					.get(name);
			if (metadata == null) {
				metadata = new ApplicationMetadata();
				this.cache.put(name, metadata);
			}
			metadata.setLabel(label);
		}
	}

	public ApplicationName getApplicationName(ApplicationName name) {
		synchronized (this.cache) {
			ApplicationMetadata metadata = (ApplicationMetadata) this.cache
					.get(name);
			if (metadata != null) {
				ApplicationName nameOverride = metadata.getApplicationName();
				if (nameOverride != null) {
					return nameOverride;
				}
			}

			return name;
		}
	}

	public void setApplicationName(ApplicationName name,
			ApplicationName nameOverride) {
		synchronized (this.cache) {
			ApplicationMetadata metadata = (ApplicationMetadata) this.cache
					.get(name);
			if (metadata == null) {
				metadata = new ApplicationMetadata();
				this.cache.put(name, metadata);
			}
			metadata.setApplicationName(nameOverride);
		}
	}

	private class ApplicationMetadata {
		private String label;
		private ApplicationName name;

		private ApplicationMetadata() {
		}

		public String getLabel() {
			return this.label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public ApplicationName getApplicationName() {
			return this.name;
		}

		public void setApplicationName(ApplicationName name) {
			this.name = name;
		}
	}
}