package com.ebupt.webjoin.insight.collection.errorhandling;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.collection.strategies.CollectionAspectProperties;
import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.ClassUtil;
import com.ebupt.webjoin.insight.collection.strategies.*;


/**
 * Facilities for collecting and notifying Insight about plugin errors
 */
public class CollectionErrors {
    private static final Logger log = Logger.getLogger(CollectionErrors.class.getName());
    public static String ERROR_NAME = "plugin-error";

    public static CollectionSettingName getPluginErrorSetting(String plugin) {
        return new CollectionSettingName(CollectionErrors.ERROR_NAME, plugin, "Errors detected", true);
    }
    
    public static void disableAspect(Class<? extends CollectionAspectProperties> aspectClazz, String reason) {
        AspectManagementCollectionStrategy.disableAspect(aspectClazz, reason);
    }
    
    public static void markCollectionError(Class<?> aspectClazz, Throwable throwable) {
        // Abort this trace
        InterceptConfiguration  config=InterceptConfiguration.getInstance();
        FrameBuilder            builder=config.getFrameBuilder();
        builder.setHint(FrameBuilder.HINT_ABORTED, Boolean.TRUE);
        if (aspectClazz != null) {
            AspectManagementCollectionStrategy.enable();
            log.log(Level.SEVERE, "Disabling aspect: " + aspectClazz);
            AspectManagementCollectionStrategy.disableAspect(aspectClazz, throwable.getMessage());
        }
    }

    public static void markCollectionError(Throwable throwable) {
        // Examine stack to determine the aspect which is responsible and disable plugin
        StackTraceElement[] stack = throwable.getStackTrace();
        if (ArrayUtil.length(stack) <= 0) {
            return;
        }

        String errantClazz = stack[0].getClassName();
        try {
        	ClassLoader	cl=ClassUtil.getDefaultClassLoader(CollectionErrors.class);
            Class<?> aspectClazz = ClassUtil.loadClassByName(cl, errantClazz);
            if (CollectionAspectProperties.class.isAssignableFrom(aspectClazz)) {
                markCollectionError(aspectClazz, throwable);
            }
        } catch (Exception e) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("markCollectionError(" + errantClazz + ")"
                      + "[" + throwable.getClass().getSimpleName() + "]"
                      + " cannot (" + e.getClass().getSimpleName() + ")"
                      + " locate errant class: " + e.getMessage());
            }
        }

    }
}
