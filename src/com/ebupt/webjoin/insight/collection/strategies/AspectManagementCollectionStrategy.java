package com.ebupt.webjoin.insight.collection.strategies;

import com.ebupt.webjoin.insight.collection.errorhandling.CollectionErrors;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsUpdateListener;
import org.aspectj.lang.JoinPoint;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enables/Disables Specific Plugin Aspects
 * This differs from the PrefixExcludeCollectionStrategy in that it operates on aspects as
 * opposed to specific instrumented classes and methods.
 *
 * This strategy interacts with the AdviceErrorHandlingAspect to automatically disable plugins which
 * create detectable problems.
 */
public class AspectManagementCollectionStrategy extends CollectionStrategyBase implements CollectionSettingsUpdateListener {
    private static final String UNKNOWN_PLUGIN = "unknown";
    private static final String STRATEGY_TYPE = "enabled";
    private static final String STRATEGY_GROUP = "aspect";
    private static final String STRATEGY_KEY = STRATEGY_GROUP + "." + STRATEGY_TYPE;
    private static final CollectionSettingName ASPECT_MANAGEMENT_COLLECTION_STRATEGY
            = new CollectionSettingName(STRATEGY_TYPE, STRATEGY_GROUP,
            "Enable/Disable specific plugin aspects using keys of the form: " +
                    new CollectionSettingName("[aspect class simplename]", STRATEGY_KEY).getCollectionSettingsKey());
    private final ConcurrentHashMap<String, Boolean> knownTargets = new ConcurrentHashMap<String, Boolean>();
    private final CollectionSettingsRegistry registry;

    public AspectManagementCollectionStrategy() {
        this(CollectionSettingsRegistry.getInstance());
    }

    public AspectManagementCollectionStrategy(CollectionSettingsRegistry reg) {
        registry = reg;
        // Initially disabled, since this is only used by the aspect error handling
        registry.register(ASPECT_MANAGEMENT_COLLECTION_STRATEGY, Boolean.FALSE);
        registry.addListener(this);
    }

    public static void enable() {
        CollectionSettingsRegistry.getInstance().set(ASPECT_MANAGEMENT_COLLECTION_STRATEGY, Boolean.TRUE);
    }

    public static void enableAspect(Class<?> aspect) {
        CollectionSettingName name = getAspectSettingName(aspect);
        CollectionSettingsRegistry.getInstance().set(name, Boolean.TRUE);
    }

    public static void disableAspect(Class<?> aspect, String error) {
        CollectionSettingName name = getAspectSettingName(aspect);
        CollectionSettingsRegistry  registry = CollectionSettingsRegistry.getInstance();
        registry.set(name, Boolean.FALSE);

        String  pluginName=name.getPlugin();
        if ((error != null) && (!UNKNOWN_PLUGIN.equals(pluginName))) {
            CollectionSettingName pluginErrorName = CollectionErrors.getPluginErrorSetting(pluginName);
            registry.set(pluginErrorName, error);
        }
    }

    /**
     * Determine the CollectionSettingsName which should be associated with this aspect
     */
    public static CollectionSettingName getAspectSettingName(Class<?> aspect) {
        /**
         * Attempt to find the plugin associated with this aspect. This only works if the
         * aspect has a default public constructor, is not an inner class, and overrides
         * the getPlugin() method.
         */
        if (CollectionAspectProperties.class.isAssignableFrom(aspect)) {
            try {
                CollectionAspectProperties aspectInst = (CollectionAspectProperties)aspect.newInstance();
                String plugin = aspectInst.getPluginName();
                if (plugin != null) {
                    return getAspectSettingName(aspect, plugin);
                }
            } catch (Throwable t) {
                // swallow
            }
        }
        return getAspectSettingName(aspect, UNKNOWN_PLUGIN);
    }
    
    private static CollectionSettingName getAspectSettingName(Class<?> aspect, String plugin) {
        return new CollectionSettingName(aspect.getSimpleName() + "." + STRATEGY_KEY, plugin, "Enable or Disable this Aspect");
    }

    /**
     * Determine the class of the aspect assoicated with a CollectionSettingName
     *
     * MyAspect.class.getSimpleName() == getAspectClassName(getAspectSettingName(MyAspect.class))
     */
    public static String getAspectClassName(CollectionSettingName name) {
        String setting = name.getName();
        return setting.replaceAll("." + STRATEGY_KEY, "");
    }
    
    public void incrementalUpdate(CollectionSettingName name, Serializable value) {
        if (name.getName().endsWith(STRATEGY_KEY)
                && value instanceof Boolean) {
            knownTargets.put(getAspectClassName(name), (Boolean)value);
        }                                        
    }

    public CollectionSettingName getStrategyName() {
        return ASPECT_MANAGEMENT_COLLECTION_STRATEGY;
    }

    public boolean collect(CollectionAspectProperties aspect, JoinPoint.StaticPart staticPoint) {
        Boolean enabled = knownTargets.get(aspect.getClass().getSimpleName());
        if ((enabled == null) || enabled.booleanValue()) {
            return true;
        }
        return false;
    }
}
