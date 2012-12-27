package com.ebupt.webjoin.insight.collection.strategies;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsUpdateListener;


/**
 * Collection Strategy which allows for arbitrarily disabling the collection of frames
 * based upon the matching code being instrumented.
 */
public class PrefixExcludeCollectionStrategy extends CollectionStrategyBase implements CollectionSettingsUpdateListener {
    private static final String STRATEGY_TYPE = "instrument";
    private static final String STRATEGY_GROUP = "prefix";
    private static final String STRATEGY_KEY = STRATEGY_GROUP + "." + STRATEGY_TYPE;
    private static final String METHOD_KEY_PREFIX = STRATEGY_KEY + ".";
    private static final CollectionSettingName PREFIX_EXCLUDE_COLLECTION_STRATEGY
            = new CollectionSettingName(STRATEGY_TYPE, STRATEGY_GROUP,
            "Include/Exclude frames from pointcut targets which match a list of prefixes" +
                    "The list of prefixes can be configured with keys of the form " +
                    new CollectionSettingName(STRATEGY_KEY + "[class].[method]", "[insight plugin name]").getCollectionSettingsKey() +
    " - Only methods which are targeted by plugins are recognized");
    private final ConcurrentHashMap<String, Boolean> knownTargets = new ConcurrentHashMap<String, Boolean>();
    private final CollectionSettingsRegistry registry;


    public PrefixExcludeCollectionStrategy() {
        this(CollectionSettingsRegistry.getInstance());
    }
    public PrefixExcludeCollectionStrategy(CollectionSettingsRegistry reg) {
        registry = reg;
        registry.register(PREFIX_EXCLUDE_COLLECTION_STRATEGY, Boolean.TRUE);
        registry.addListener(this);
    }

    public static CollectionSettingName getMethodSettingName(Class<?> clazz, String plugin, String method) {
        
         return new CollectionSettingName(METHOD_KEY_PREFIX + clazz.getCanonicalName() + "." + method, plugin, "Enable/Disable this method from being instrumented");
    }

    public CollectionSettingName getStrategyName() {
        return PREFIX_EXCLUDE_COLLECTION_STRATEGY;
    }

    public boolean enabled() { 
        return registry.getBooleanSetting(PREFIX_EXCLUDE_COLLECTION_STRATEGY).booleanValue();
       }

    public final boolean collect(CollectionAspectProperties aspect, JoinPoint.StaticPart target) {
        Signature sig =  target.getSignature();
        Class<?> clazz = sig.getDeclaringType();
        String methodName = sig.getName();
        String classMethod = clazz.getCanonicalName() + "." + methodName;
        Boolean keepFullMethod = knownTargets.get(classMethod);
        if (keepFullMethod == null) {
            String pluginName = aspect.getPluginName();
            if (pluginName == null) pluginName = "unknown";
            knownTargets.put(classMethod, Boolean.TRUE);
            registry.register(getMethodSettingName(clazz, pluginName,  methodName), Boolean.TRUE);
            return true;
        }
        return keepFullMethod.booleanValue();
    }

    public void incrementalUpdate(CollectionSettingName name, Serializable value) {
        // Only pay attention to specific settings
        String key = name.getKey();
        if (!key.contains(METHOD_KEY_PREFIX)) {
            return;
        }
        if (!(value instanceof Boolean)) {
            return;
        }
        Boolean enabled = (Boolean) value;
        key = key.replaceFirst(".*" + METHOD_KEY_PREFIX, "");
        if (key.length() <= 0) { return; }
        // Run through all the classMethods we have seen thus far... if any of them
        // start with the specified key update the setting
        for (String target : knownTargets.keySet()) {
            if (target.startsWith(key)) {
                knownTargets.put(target, enabled);
            }
        }

    }
}
