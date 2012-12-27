package com.ebupt.webjoin.insight.resource;

import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.intercept.endpoint.EndPointName;
import com.ebupt.webjoin.insight.intercept.remote.ExternalResourceName;
import com.ebupt.webjoin.insight.server.ServerName;
import com.ebupt.webjoin.insight.util.StringUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.management.ObjectName;

public class ResourceKey implements Serializable {
	private static final long serialVersionUID = 6025653747739588637L;
	private String objectName;
	private transient String nameProperty;
	private static final Map<String, ObjectName> cachedObjectNames = Collections
			.synchronizedMap(new WeakHashMap<String, ObjectName>());

	private static final Map<CacheKey, ResourceKey> cachedResourceKeys = Collections
			.synchronizedMap(new WeakHashMap<CacheKey, ResourceKey>());

	private static final Map<String, ResourceKey> cachedObjectNameKeys = Collections
			.synchronizedMap(new WeakHashMap<String, ResourceKey>());
	public static final String NAME_KEY = "name";
	public static final String TYPE_KEY = "type";
	public static final String SUB_TYPE_KEY = "sub-type";
	private static final String INSIGHT_DOMAIN = "insight";
	public static final String OPERATION_KEY = "ResourceKey";

	private ResourceKey(ObjectName mbeanName) {
		this.objectName = mbeanName.getCanonicalName();
		this.nameProperty = getKeyProperty(NAME_KEY);
	}

	public String getName() {
		if (this.nameProperty == null) {
			return getKeyProperty(NAME_KEY);
		}
		return this.nameProperty;
	}

	private ResourceKey() {
	}

	/*
	 * private void setObjectName(String objectName) { ObjectName mbeanName =
	 * getOrCreateObjectName(objectName); this.objectName =
	 * mbeanName.getCanonicalName(); cachedObjectNameKeys.put(this.objectName,
	 * this); }
	 * 
	 * private void setNameProperty(String nameProperty) { this.nameProperty =
	 * nameProperty; }
	 */

	public ApplicationName getApplicationName() {
		ObjectName name = getObjectName();
		String type = name.getKeyProperty(TYPE_KEY);
		String appKey;
		if (type.equals("Application"))
			appKey = getName();
		else {
			appKey = getKeyProperty("Application");
		}

		if (appKey == null) {
			return null;
		}

		return ApplicationName.valueOf(appKey);
	}

	public ServerName getServerName() {
		ObjectName name = getObjectName();
		String type = name.getKeyProperty(TYPE_KEY);
		String server;
		if (type.equals("Server"))
			server = getName();
		else {
			server = getKeyProperty("Server");
		}

		if (server == null) {
			return null;
		}

		return ServerName.valueOf(server);
	}

	public EndPointName getEndPointName() {
		ObjectName name = getObjectName();
		String type = name.getKeyProperty(TYPE_KEY);
		String endpoint;
		if ((type.equals("Application.EndPoint"))
				|| (type.equals("Application.Server.EndPoint"))
				|| (type.equals("EndPoint"))) {
			endpoint = getName();
		} else {
			if (type.equals("Application.Server.EndPoint.External"))
				endpoint = getKeyProperty("EndPoint");
			else {
				endpoint = getKeyProperty("Application.EndPoint");
			}
		}
		if (endpoint == null) {
			return null;
		}

		return EndPointName.valueOf(endpoint);
	}

	public ExternalResourceName getExternalResourceName() {
		ObjectName name = getObjectName();
		String type = name.getKeyProperty(TYPE_KEY);
		String extResource;
		if (type.equals("Application.Server.EndPoint.External"))
			extResource = getName();
		else {
			extResource = getKeyProperty("Application.Server.EndPoint.External");
		}

		if (extResource == null) {
			return null;
		}

		return ExternalResourceName.valueOf(extResource);
	}

	public String getType() {
		return getObjectName().getKeyProperty(TYPE_KEY);
	}

	public String getSubType() {
		return getObjectName().getKeyProperty(SUB_TYPE_KEY);
	}

	public String getKeyProperty(String key) {
		ObjectName oName = getObjectName();
		String res = oName.getKeyProperty(key);
		if (res == null) {
			return null;
		}
		return ObjectName.unquote(res);
	}

	private static ObjectName getOrCreateObjectName(String objectName) {
		ObjectName objName = (ObjectName) cachedObjectNames.get(objectName);
		if (objName != null)
			return objName;
		try {
			objName = new ObjectName(objectName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		cachedObjectNames.put(objectName, objName);

		String canonicalName = objName.getCanonicalName();
		if (!objectName.equals(canonicalName)) {
			cachedObjectNames.put(canonicalName, objName);
		}
		return objName;
	}

	public ObjectName getObjectName() {
		return getOrCreateObjectName(this.objectName);
	}

	public String getKey() {
		return this.objectName;
	}

	public int hashCode() {
		return getObjectName().getCanonicalName().hashCode();
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass()) {
			return false;
		}
		ResourceKey other = (ResourceKey) obj;
		ObjectName otherName = other.getObjectName();
		ObjectName thisName = getObjectName();
		String otherCanonical = otherName.getCanonicalName();
		String thisCanonical = thisName.getCanonicalName();
		if (!thisCanonical.equals(otherCanonical)) {
			return false;
		}

		return true;
	}

	public String toString() {
		return this.objectName;
	}

	public void assertIsOfType(String resourceTypeName) {
		if (!getType().equals(resourceTypeName))
			throw new RuntimeException("Resource key [" + getKey()
					+ "] is not type=" + resourceTypeName);
	}

	public static ResourceKey valueOf(String type, String name) {
		return valueOf(type, name, null);
	}

	public static ResourceKey valueOf(String type, String name, String params) {
		return valueOf(type, null, name, params);
	}

	public static ResourceKey valueOf(String type, String subType, String name,
			String params) {
		return valueOf(new CacheKey(type, subType, name, params));
	}

	public static ResourceKey valueOf(String objectName) {
		return valueOf(getOrCreateObjectName(objectName));
	}

	public static ResourceKey valueOf(ObjectName mbeanName) {
		String key = mbeanName.getCanonicalName();
		ResourceKey res = (ResourceKey) cachedObjectNameKeys.get(key);
		if (res == null) {
			res = new ResourceKey(mbeanName);
			cachedObjectNameKeys.put(key, res);
		}

		return res;
	}

	static ResourceKey valueOf(CacheKey key) {
		ResourceKey res = (ResourceKey) cachedResourceKeys.get(key);
		if (res == null) {
			res = createFromCacheKey(key);
			cachedResourceKeys.put(key, res);
		}

		return res;
	}

	private static ResourceKey createFromCacheKey(CacheKey key) {
		String objectName = INSIGHT_DOMAIN + ":name="
				+ ObjectName.quote(key.name) + "," + "type" + "=" + key.type;

		if (StringUtil.getSafeLength(key.subType) > 0) {
			objectName = objectName + ",sub-type=" + key.subType;
		}

		if (StringUtil.getSafeLength(key.params) > 0) {
			objectName = objectName + "," + key.params;
		}

		return valueOf(objectName);
	}

	static class CacheKey {
		protected final String type;
		protected final String subType;
		protected final String name;
		protected final String params;
		private final int hashCodeValue;

		CacheKey(String type, String subType, String name, String params) {
			this.name = name;
			this.type = type;
			this.subType = subType;
			this.params = params;
			this.hashCodeValue = ((name == null ? 0 : name.hashCode())
					+ (type == null ? 0 : type.hashCode())
					+ (subType == null ? 0 : subType.hashCode()) + (params == null ? 0
					: params.hashCode()));
		}

		public int hashCode() {
			return this.hashCodeValue;
		}

		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (this == obj) {
				return true;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}

			CacheKey key = (CacheKey) obj;
			if ((StringUtil.safeCompare(this.name, key.name) != 0)
					|| (StringUtil.safeCompare(this.type, key.type) != 0)
					|| (StringUtil.safeCompare(this.subType, key.subType) != 0)
					|| (StringUtil.safeCompare(this.params, key.params) != 0)) {
				return false;
			}

			return true;
		}

		public String toString() {
			return "name=" + this.name + ", type=" + this.type + ", sub-type="
					+ this.subType + ", params=" + this.params;
		}
	}
}