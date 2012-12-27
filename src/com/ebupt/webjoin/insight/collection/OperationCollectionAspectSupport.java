package com.ebupt.webjoin.insight.collection;

import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.collection.errorhandling.CollectionErrors;
import com.ebupt.webjoin.insight.collection.strategies.CollectionAspectProperties;
import com.ebupt.webjoin.insight.collection.strategies.CollectionStrategyRunner;
import com.ebupt.webjoin.insight.color.ColorManager;
import com.ebupt.webjoin.insight.color.ColorManager.ColorParams;
import com.ebupt.webjoin.insight.color.ColorManager.ExtractColorParams;
import com.ebupt.webjoin.insight.intercept.operation.SourceCodeLocation;
import com.ebupt.webjoin.insight.intercept.util.version.NoVersionAvailableException;
import com.ebupt.webjoin.insight.intercept.util.version.VersionScannerRunner;

public abstract class OperationCollectionAspectSupport implements
		CollectionAspectProperties {
	protected static final CollectionStrategyRunner strategies = CollectionStrategyRunner
			.getInstance();
	protected static final VersionScannerRunner version = new VersionScannerRunner();
	protected static final ColorManager colorManager = ColorManager
			.getInstance();

	private OperationCollector collector;

	public OperationCollectionAspectSupport() {
		this(new DefaultOperationCollector());
	}

	public OperationCollectionAspectSupport(
			OperationCollector operationCollector) {
		if ((this.collector = operationCollector) == null) {
			throw new IllegalStateException("No collector specified");
		}
	}

	public OperationCollector getCollector() {
		return collector;
	}

	public void setCollector(OperationCollector endpointValue) {
		if ((this.collector = endpointValue) == null) {
			throw new IllegalStateException("No collector specified");
		}
	}

	public SourceCodeLocation getSourceCodeLocation(JoinPoint jp) {
		return getSourceCodeLocation(jp.getStaticPart());
	}

	public SourceCodeLocation getSourceCodeLocation(JoinPoint.StaticPart part) {
		return OperationCollectionUtil.getSourceCodeLocation(part);
	}

	/**
	 * Does this aspect provide an endpoint? Aspects which do not create
	 * endpoints do not need to be collected if the EndPoint Strategy
	 * {@link com.springsource.insight.collection.strategies.EndPointOnlyCollectionStrategy}
	 * is enabled and the trace is selected to be endpoint-only. The default is
	 * "false" Aspects can override this to make sure they are always included
	 * in the frame stack.
	 */
	public boolean isEndpoint() {
		return false;
	}

	/**
	 * Plugin Name associated with this aspect Used to associate
	 * collection-level aspects with the higher-level notion of a plugin and all
	 * the associated meta-data.
	 * 
	 * There is no default.
	 */
	public abstract String getPluginName();

	/**
	 * Check an "anchor" class to determine which package or jar it is
	 * associated with, look up the version information as best we can and make
	 * sure the version is between min and max (inclusive). If there is
	 * versioning information available, and the anchor is out of range disable
	 * the aspect.
	 * 
	 * @param aspectClazz
	 *            Aspect which is to be disabled if the version is not
	 *            compatible
	 * @param anchor
	 *            Anchor class for looking up versioning information
	 * @param min
	 *            Minimum version required
	 * @param max
	 *            Maximum version required
	 * @return
	 */
	public static boolean assertCompatible(
			Class<? extends OperationCollectionAspectSupport> aspectClazz,
			String searchKey, Class<?> anchor, String min, String max) {

		try {
			if (!version.inRange(min, max, anchor, searchKey)) {
				String detected = version.getVersion(anchor, searchKey);
				if (detected == null)
					detected = "UNKNOWN_VERSION";
				CollectionErrors.disableAspect(aspectClazz, "The "
						+ aspectClazz.getSimpleName() + " does not support "
						+ searchKey + " version " + detected
						+ " expected version " + min + " to " + max);
				return false;
			}
		} catch (NoVersionAvailableException e) {
			// swallow, do not disable plugin
		}
		return true;
	}

	public void colorForward(ColorParams params) {
		colorManager.colorForward(params);
	}

	public void extractColor(ExtractColorParams params) {
		colorManager.extractColor(params);
	}

	public void markException(String prefix, Throwable t) {

	}
}
