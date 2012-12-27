package com.ebupt.webjoin.insight.collection.strategies;

/**
 * Interface which provides information on a particular collection aspect which
 * can be useful for making some determination on whether a collection strategy
 * should be used.
 */
public interface CollectionAspectProperties {
	public boolean isEndpoint();

	public String getPluginName();
}
