package com.ebupt.webjoin.insight.intercept.plugin;

import java.io.Serializable;

public abstract interface CollectionSettingsUpdateListener {
	public abstract void incrementalUpdate(
			CollectionSettingName paramCollectionSettingName,
			Serializable paramSerializable);
}