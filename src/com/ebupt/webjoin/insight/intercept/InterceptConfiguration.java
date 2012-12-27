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

package com.ebupt.webjoin.insight.intercept;

import static java.lang.Boolean.TRUE;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.Insight;
import com.ebupt.webjoin.insight.InsightAgentClassloadingHelper;
import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.intercept.ltw.ClassLoaderUtils;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilderCallback;
import com.ebupt.webjoin.insight.intercept.trace.ThreadLocalFrameBuilder;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.server.ServerName;
import com.ebupt.webjoin.insight.util.ClassUtil;
import com.ebupt.webjoin.insight.util.DeveditionUtils;
/**
 * The central class for access to the Intercept subsystem.   The InterceptConfiguration 
 * can be used to add {@link Trace} {@link Frame}s to the current thread's {@link Trace}.
 */
public class InterceptConfiguration {
    /* Delay the actual initialization as late as possible
     * so as to allow the dispatch core (if any) to have best
     * chance of using a registered InsightAgentPluginsHelper
     */
    private static final class LazyFieldHolder {
        private static final String DISPATCHER_CONFIGURATION_CLASS="com.ebupt.webjoin.insight.intercept.CoreInterceptConfiguration";
        private static final InterceptConfiguration INSTANCE=resolveInstance();
        private static InterceptConfiguration resolveInstance () {
            try {
                ClassLoader cl=ClassUtil.getDefaultClassLoader(InterceptConfiguration.class);
                if (ClassUtil.isPresent(DISPATCHER_CONFIGURATION_CLASS, cl)) {
                    Class<?>    clazz=ClassUtil.loadClassByName(cl, DISPATCHER_CONFIGURATION_CLASS);
                    return (InterceptConfiguration) clazz.newInstance();
                }
            } catch(Throwable t) {
                Logger  LOG=Logger.getLogger(LazyFieldHolder.class.getName());
                LOG.log(Level.SEVERE,
                        "Failed (" + t.getClass().getSimpleName() + ")"
                      + " to instantiate: " + t.getMessage(),
                       t);
            }
            
            return new InterceptConfiguration();
        }
    }

    public static final ServerName  PLACEHOLDER_SERVER=ServerName.valueOf("defaultJvmServer");
    // Package private to enable testing
    DelegatingFrameBuilderCallbacks frameCallbacks = new DelegatingFrameBuilderCallbacks(new TraceDispatchCallback(this));
    FrameBuilder threadBuilder = new ThreadLocalFrameBuilder(frameCallbacks);
    InterceptDispatcher dispatcher = resolveInterceptDispatcher();
/*    volatile TraceIdGenerator traceIdGenerator = resolveTraceIdGenerator();
*/    private final AtomicReference<ServerName> server=new AtomicReference<ServerName>(PLACEHOLDER_SERVER);
    private volatile boolean devEdition;
    private Insight insight = Insight.getInstance();
    private InsightAgentClassloadingHelper helper = InsightAgentClassloadingHelper.getInstance();
    InterceptConfiguration() {
        devEdition = Boolean.parseBoolean(System.getProperty("insight.devedition", "true"));
        this.dispatcher.register(new TraceInterceptListenerImpl());
        this.helper.addPluginURLs(Thread.currentThread().getContextClassLoader());
    }

    @SuppressWarnings("synthetic-access")
    public static final InterceptConfiguration getInstance() {
        return LazyFieldHolder.INSTANCE;
    }
    
    /**
     * Get a handle to a {@link FrameBuilder} which can be used to create a {@link Frame} stack.  
     * The builder provides thread-local access, so callers do not need to worry about sharing
     * the same object.
     */
    public FrameBuilder getFrameBuilder() {
        return threadBuilder;
    }

    public void addFrameBuilderCallbacks(FrameBuilderCallback callbacks) {
        frameCallbacks.addDelegate(callbacks);
    }
    
    /**
     * Get the server that this InterceptConfiguration is associated with
     */
    public ServerName getServer() {
        return server.get();
    }
    
    /**
     * Get a handle to the {@link InterceptDispatcher}, which routes {@link Trace}s to the
     * repository.
     */
    public InterceptDispatcher getDispatcher() {
        return dispatcher;
    }

 /*   public TraceIdGenerator getTraceIdGenerator() {
        return traceIdGenerator;
    }

    public void setTraceIdGenerator(TraceIdGenerator generator) {
        traceIdGenerator = generator;
    }

    public TraceId generateNextTraceId() {
        return traceIdGenerator.generateNextId();
    }*/

    public boolean isDevEdition() {
        return this.devEdition || isDevEditionOverride();
    }

    protected boolean isDevEditionOverride() {
        return DeveditionUtils.isDeveditionOverride();
    }

    public void setDevEdition(boolean devEditionValue) {
        this.devEdition = devEditionValue;
    }

    /**
     * Dispatch a trace to the {@link InterceptDispatcher}
     */
    public void dispatchTrace(Trace trace) {
        ServerName  traceServer=getServer();  
        trace.setServer(traceServer); // possibly redundant
        dispatcher.dispatchTrace(trace);
    }

    public void setServer(ServerName serverName) {
        server.getAndSet(serverName);
    }

    public Insight getInsight() {
        return insight;
    }

    public ApplicationName resolveCurrentApplicationName () {
    	return resolveCurrentApplicationName(getClass());
    }

    public ApplicationName resolveCurrentApplicationName (Class<?> anchorClass) {
    	return resolveCurrentApplicationName(getFrameBuilder(), anchorClass);
    }

    public ApplicationName resolveCurrentApplicationName (ClassLoader cl) {
    	return resolveCurrentApplicationName(getFrameBuilder(), cl);
    }
    /**
     * Marks {@link FrameBuilder#HINT_ABORTED} as <code>true</code>
     */
    public void abortTrace() {
        threadBuilder.setHint(FrameBuilder.HINT_ABORTED, TRUE);
    }

    public FrameBuilder.OperationCollectionLevel getCollectionLevel() {
        FrameBuilder.OperationCollectionLevel collectionLevel=
        		threadBuilder.getHint(FrameBuilder.HINT_OPERATION_COLLECT_LEVEL, FrameBuilder.OperationCollectionLevel.class);
        return collectionLevel == null ? FrameBuilder.OperationCollectionLevel.HIGH : collectionLevel;
    }

    private static InterceptDispatcher resolveInterceptDispatcher () {
        return resolveInstance("com.springsource.insight.intercept.CoreInterceptDispatcherImpl",
                               InterceptDispatcher.class,
                               new InterceptDispatcherImpl());
    }

   /* private static TraceIdGenerator resolveTraceIdGenerator () {
        return resolveInstance("com.springsource.insight.intercept.CoreInterceptDispatcherTraceIdGenerator",
                               TraceIdGenerator.class,
                               new AtomicLongTraceIdGenerator());
    }*/
    
    private static <O> O resolveInstance (String fqcn, Class<O> type, O defaultValue) {
        if (ClassUtil.isPresent(fqcn)) {
            try {
                Class<?>    clazz=ClassUtil.loadClassByName(fqcn);
                Object      instance=clazz.newInstance();
                return type.cast(instance);
            } catch(Throwable t) {
                Logger  LOG=Logger.getLogger(InterceptConfiguration.class.getSimpleName());
                LOG.log(Level.SEVERE,
                        "resolveInstance(" + type.getSimpleName() + ")"
                      + " failed (" + t.getClass().getSimpleName() + ")"
                      + " to instantiate: " + t.getMessage(),
                       t);
            }
        }
        
        return defaultValue;
    }

    /**
     * Attempts a &quot;best effort&quot; to resolve the current thread's associated
     * {@link ApplicationName} value:</BR>
     * <UL>
     * 		<LI>
     * 		Check the {@link FrameBuilder#HINT_APPNAME} hint value - if exists and not
     * 		the {@link ApplicationName#UNKNOWN_APPLICATION_NAME} then use it
     * 		</LI>
     * 
     * 		<LI>
     * 		Otherwise, check if the provided {@link ClassLoader} or any of the parents
     * 		of the provided <I>anchor</I> {@link Class} is an {@link InsightClassLoader},
     * 		and if so use its {@link InsightClassLoader#getApplicationName()}
     * 		value.
     * 		</LI>
     * 
     * 		<LI>
     * 		Otherwise return {@link ApplicationName#UNKNOWN_APPLICATION_NAME}
     * 		</LI>
     * </UL>
     * @param builder The {@link FrameBuilder} to query for hints - ignored if <code>null</code>
     * @param anchorClass The &quot;anchor&quot; class to use whose {@link ClassLoader} is to
     * be used if using the hints was not successful
     * @return The resolved application name
     * @see FrameBuilder#HINT_APPNAME
     * @see ClassUtil#getDefaultClassLoader(Class)
     * @see ClassLoaderUtils#findApplicationName(ClassLoader)
     */
    public static final ApplicationName resolveCurrentApplicationName (FrameBuilder builder, Class<?> anchorClass) {
    	ApplicationName	name=(builder == null) ? null : builder.getHint(FrameBuilder.HINT_APPNAME, ApplicationName.class);
    	if ((name == null) || ApplicationName.UNKNOWN_APPLICATION_NAME.equals(name)) {
    		ClassLoader	cl=ClassUtil.getDefaultClassLoader(anchorClass);
    		name = ClassLoaderUtils.findApplicationName(cl);
    	}

    	return name;
    }
    
    /**
     * Attempts a &quot;best effort&quot; to resolve the current thread's associated
     * {@link ApplicationName} value:</BR>
     * <UL>
     * 		<LI>
     * 		Check the {@link FrameBuilder#HINT_APPNAME} hint value - if exists and not
     * 		the {@link ApplicationName#UNKNOWN_APPLICATION_NAME} then use it
     * 		</LI>
     * 
     * 		<LI>
     * 		Otherwise, check if the provided {@link ClassLoader} or any of its parents
     * 		is an {@link InsightClassLoader}, and if so use its {@link InsightClassLoader#getApplicationName()}
     * 		value.
     * 		</LI>
     * 
     * 		<LI>
     * 		Otherwise return {@link ApplicationName#UNKNOWN_APPLICATION_NAME}
     * 		</LI>
     * </UL>
     * @param builder The {@link FrameBuilder} to query for hints - ignored if <code>null</code>
     * @param cl The class loader to query if frame builder hint no good
     * @return The resolved application name
     * @see FrameBuilder#HINT_APPNAME
     * @see ClassLoaderUtils#findApplicationName(ClassLoader)
     */
    public static final ApplicationName resolveCurrentApplicationName (FrameBuilder builder, ClassLoader cl) {
    	ApplicationName	name=(builder == null) ? null : builder.getHint(FrameBuilder.HINT_APPNAME, ApplicationName.class);
    	if ((name == null) || ApplicationName.UNKNOWN_APPLICATION_NAME.equals(name)) {
    		name = ClassLoaderUtils.findApplicationName(cl);
    	}

    	return name;
    }
}
