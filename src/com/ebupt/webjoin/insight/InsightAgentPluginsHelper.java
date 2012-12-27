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

package com.ebupt.webjoin.insight;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.intercept.ConfigUtils;
import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.ltw.ClassLoaderUtils;
import com.ebupt.webjoin.insight.intercept.ltw.InsightClassLoader;
import com.ebupt.webjoin.insight.intercept.ltw.WeavingSupport;
import com.ebupt.webjoin.insight.intercept.plugin.PluginRuntimeDescriptorsRegistery;
import com.ebupt.webjoin.insight.server.ContainerInformationProvider;
import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.ClassUtil;
import com.ebupt.webjoin.insight.util.ExceptionUtils;
import com.ebupt.webjoin.insight.util.FileUtil;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.MapUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

 

/**
 * A helper class that encapsulates the common logic for retrieving locations
 * of the various Insight runtime artifacts - e.g., plugins, properties file
 */
public abstract class InsightAgentPluginsHelper implements FileFilter {
    /**
     * Property used to indicate where the Insight distribution root folder
     * resides 
     */
    public static final String INSIGHT_BASE = ConfigUtils.INSIGHT_BASE;
    /**
     * Property used to indicate where insight logs should reside
     */
    public static final String INSIGHT_LOGS = "insight.logs";
    /**
     * Name of properties file used to load and read from
     */
    public static final String INSIGHT_PROPERTIES = ConfigUtils.INSIGHT_PROPERTIES;
    /**
     * Sub-folder under {@link #INSIGHT_BASE} where collection plugins reside
     */
    public static final String COLLECTION_PLUGINS = "collection-plugins";
    /**
     * Folder underneath collection-plugins for the manual configuration of
     * aop.xml
     */
    public static final String COLLECTION_AOP = "custom-aop";

    protected final Logger  log=Logger.getLogger(getClass().getName());
    private final File insightBase;
    private final File insightConfig;
    private final File collectionPluginDir;
    private final String collectionPluginSource;
    private final File collectionAopDir;
    private final List<File> pluginFiles = new ArrayList<File>();
    private final List<URL> pluginURLs;
    private final List<URI> pluginURIs;
    private final List<String>  pluginURIValues;
	/*
	 * NOTE: we are using a case INSENSITIVE set since it is very bad
	 * practice to have 2 plugin JAR(s) that differ only by their case
	 */
    private final Map<String,URL> pluginURLSources=new TreeMap<String,URL>(String.CASE_INSENSITIVE_ORDER);
    private final FileFilter  pluginsFilter;
    private final ConfigUtils	config;

    private static volatile boolean insightInitialized;
    private static final AtomicReference<InsightAgentPluginsHelper> registeredHelper=
            new AtomicReference<InsightAgentPluginsHelper>(null);
    
    public static final InsightAgentPluginsHelper getRegisteredInsightAgentPluginsHelper () {
        return registeredHelper.get();
    }

    public static final void registerInsightAgentPluginsHelper (InsightAgentPluginsHelper helper) {
        if (helper == null) {
            throw new IllegalArgumentException("No helper provided");
        }

        InsightAgentPluginsHelper   prev=registeredHelper.getAndSet(helper);
        if ((prev != null) && (helper != prev)) {
            throw new IllegalStateException("Multiple helpers installed");
        }
    }

    protected InsightAgentPluginsHelper() {
        EnvironmentDetector detector=EnvironmentDetector.getInstance();
        Map<String,?>		fixResult=detector.fixEnv();
        if (MapUtil.size(fixResult) > 0) {
        	for (Map.Entry<String,?> e : fixResult.entrySet()) {
        		log.warning("Environment fixed " + e.getKey() + "=" + e.getValue());
        	}
        }

    	config = ConfigUtils.getInstance();
    	ContainerInformationProvider	provider=config.getContainerInformationProvider();
        if ((insightBase=requireInsightBase(config)) == null) {
            throw new IllegalStateException("No Insight base file provided");
        }
        defaultInsightLogs(provider);

        collectionPluginDir = new File(insightBase, COLLECTION_PLUGINS);
        /*
         * The reason we map the collection plugins folder as a source
         * is to enable easy check if an URL is prefixed by it
         */
        collectionPluginSource = mapPluginSource(collectionPluginDir);
        collectionAopDir = new File(collectionPluginDir, COLLECTION_AOP);
        insightConfig = config.getPropsFile();
        pluginsFilter = resolvePluginsFilter();

        pluginFiles.addAll(getPluginJars(collectionPluginDir));
        if (accept(collectionAopDir)) {
            pluginFiles.add(collectionAopDir);
        } else {
            log.info("Filtered out " + collectionAopDir);
        }

        try {
            pluginURLs = ListUtil.toURL(pluginFiles);
        } catch (RuntimeException e) {
            log.log(Level.SEVERE, e.getClass().getSimpleName() + ": " + e.getMessage(), e.getCause());
            throw e;
        }

        for (URL url : pluginURLs) {
        	mapPluginSource(url);
        }

        pluginURIs = ListUtil.toURI(pluginURLs);
        pluginURIValues = ListUtil.toURIString(pluginURIs);
    }

    /**
     * Adds <U>all</U> the known plugins {@link URL}-s to the specified loader
     * @param loader The loader instance
     * @throws RuntimeException if failed to add a URL - the {@link Throwable#getCause()}
     * will contain the actual reason for the failure
     * @see ClassLoaderUtils#addUrl(Object, URL)
     */
    public void addPluginURLs(Object loader) {
    	Collection<? extends URL>	urls=getPluginURLs();
    	for (URL url : urls) {
    		try {
    			System.out.println("add plugin "+url);
    			ClassLoaderUtils.addUrl(loader, url);
    		} catch(Exception e) {
    			Throwable t=ExceptionUtils.peelThrowable(e);
    			RuntimeException	re=new RuntimeException(
    							"Failed (" + t.getClass().getSimpleName() + ")"
   							  + " to add URL=" + url.toExternalForm()
   							  + ": " + t.getMessage(), t);
    			log.warning(re.getMessage());
    			throw re;
    		}
    	}
    }

    public final File getInsightBase() {
        return insightBase;
    }

    public final File getInsightConfigFile() {
        return insightConfig;
    }

    public final File getCollectionPluginDir() {
        return collectionPluginDir;
    }

    public final List<File> getPluginFiles() {
        return pluginFiles;
    }

    public final List<URL> getPluginURLs() {
        return pluginURLs;
    }

    public final List<URI> getPluginURIs () {
        return pluginURIs;
    }

    public final List<String> getPluginURIValues () {
        return pluginURIValues;
    }

    public final Insight getInsight() {
        final InterceptConfiguration intConfig = InterceptConfiguration.getInstance();
        final Insight insight = intConfig.getInsight();
        if (!insightInitialized) {
            insight.readConfigurationIfNotConfigured(getInsightConfigFile());
            insightInitialized = true;
        }

        return insight;
    }

    protected String mapPluginSource (File file) {
    	return mapPluginSource((file == null) ? null : file.toURI());
    }

    protected String mapPluginSource (URI uri) {
    	try {
    		return mapPluginSource((uri == null) ? null : uri.toURL());
    	} catch(MalformedURLException e) {
    		throw new IllegalArgumentException("mapPluginSource(" + uri.toString() + ") failed to convert to URL: " + e.getMessage(), e);
    	}
    }
    /**
     * @param url The {@link URL} representing a plugin source
     * @return The &quot;canonical&quot; used as a key for the mapping
     * @throws IllegalArgumentException if <code>null</code> URL
     * @throws IllegalStateException If the canonical form already mapped
     * @see {@link #getCanonicalURLValue(URL)}
     */
    protected String mapPluginSource (URL url) {
    	if (url == null) {
    		throw new IllegalArgumentException("No URL provided");
    	}

    	String	src=getCanonicalURLValue(url);
    	URL	prev=pluginURLSources.put(src, url);
    	if (prev != null) {
    		String	msg="Multiple plugin sources for " + url.toExternalForm() + " - also " + prev.toExternalForm();
    		log.severe(msg);
    		throw new IllegalStateException(msg);
    	}

    	return src;
    }

    /**
     * @param curPath The generated path - using the <U>local</U> {@link File#separator}
     * @return The actual value to set to as a path property - default
     * is to return the input parameter as-is. This method was added due to
     * the JBoss 6.0 peculiarity in handling (or rather mis-handling)
     * Windows-like paths that contain a backslash
     */
    public String adjustInsightBasePath (final String curPath) {
        return curPath;
    }
    
    public InsightClassLoader findInsightClassLoader() {
        Thread      curThread=Thread.currentThread();
        return findInsightClassLoader(curThread.getContextClassLoader());
    }

    protected InsightClassLoader findInsightClassLoader(ClassLoader rootLoader) {
        return ClassLoaderUtils.findInsightWeavingClassLoader(rootLoader);
    }

    private final File requireInsightBase(ConfigUtils configInstance) {
        String basePropValue = configInstance.resolveInsightBase();
        if (StringUtil.isEmpty(basePropValue)) {
        	throw new IllegalStateException("System property '"
                        + INSIGHT_BASE
                        + "' must be defined - no alternative provided");
        }

        String	sysValue=System.getProperty(INSIGHT_BASE);
        if (StringUtil.isEmpty(sysValue)) {
        	basePropValue = adjustInsightBasePath(basePropValue);
            System.setProperty(INSIGHT_BASE, basePropValue);
            log.warning("Using default " + INSIGHT_BASE + "=" + basePropValue);
        }

        return new File(basePropValue);
    }

    /**
     * Checks if the {@link #INSIGHT_LOGS} property has been set or
     * an alternative has been provided by {@link ContainerInformationProvider#getDefaultInsightLogsFolder()}.
     * If not, then it sets it to <code>{@link #INSIGHT_BASE}/logs</code>
     * @param provider the current detected {@link ContainerInformationProvider}
     */
    void defaultInsightLogs(ContainerInformationProvider provider) {
    	String	propValue=System.getProperty(INSIGHT_LOGS);
        if (StringUtil.isEmpty(propValue)) {
            propValue = provider.getDefaultInsightLogsRoot();
            if (StringUtil.isEmpty(propValue)) {
                propValue = insightBase.getAbsolutePath() + File.separator + ContainerInformationProvider.LOGS_FOLDER;
            }

            propValue = adjustInsightBasePath(propValue);
            System.setProperty(INSIGHT_LOGS, propValue);
            log.info("Using default " + INSIGHT_LOGS + "=" + propValue);
        }
    }

    protected List<File> getPluginJars(final File pluginDir) {
        final File[] files = pluginDir.listFiles();
        if (ArrayUtil.length(files) <= 0) {
            return Collections.emptyList();
        }

        final List<File> res=new ArrayList<File>(files.length);
        PluginRuntimeDescriptorsRegistery   registry=PluginRuntimeDescriptorsRegistery.getInstance();
        for (final File f : files) {
            if (fileIsValidPluginJar(f)) {
                res.add(f);
                registry.register(f);
                
                if (log.isLoggable(Level.FINER)) {
                    log.finer("getPluginJars(" + f.getAbsolutePath() + ")");
                }
            }
        }
        return res;
    }

    protected boolean fileIsValidPluginJar(File f) {
        return accept(f)
            && f.isFile()
            && f.canRead()
            && FileUtil.isJarFileName(f)
            ;
    }

    private static final String DISPATCH_PLUGINS_FILTER="com.ebupt.webjoin.insight.agent.dispatch.DispatchAgentPluginsFilter";
    protected FileFilter resolvePluginsFilter () {
        ClassLoader cl=ClassUtil.getDefaultClassLoader(getClass());
        if (ClassUtil.isPresent(DISPATCH_PLUGINS_FILTER, cl)) {
            try {
                Class<?>    clazz=ClassUtil.loadClassByName(cl, DISPATCH_PLUGINS_FILTER);
                return (FileFilter) clazz.newInstance();
            } catch(Exception e) {
                log.warning("Failed (" + e.getClass().getSimpleName() + ")"
                          + " to load dispatch agent filter: " + e.getMessage());
            }
        }
        
        return null;
    }

    public boolean accept(File f) {
        if (pluginsFilter == null) {
            return (f != null) && f.exists();
        }

        if (pluginsFilter.accept(f)) {
            return true;
        }

        log.info("fileIsValidPluginJar(" + f.getName() + ") filtered");
        return false;
    }

    /**
     * Decides whether a request {@link URL} for an AOP related resource should
     * be returned as-is or filtered. In addition to the considerations taken
     * by {@link #isAopResourceFilteringRequired(String, ClassLoader)} it also
     * checks if the URL originated in one of the plugins URL(s).
     * @param resourceName The requested resource name
     * @param loader The requesting {@link ClassLoader}
     * @param originalResult The original {@link URL} that the loader returned
     * @return Same as input URL if no filtering occurred or <code>null</code>
     * if result should be filtered (or original was <code>null</code> to
     * begin with)
     * @see #isAopResourceFilteringRequired(String, ClassLoader)
     * @see #isPluginSourceURL(URL)
     */
    public URL filterAopResourceURL (String resourceName, ClassLoader loader, URL originalResult) {
    	if (originalResult == null) {
    		return null;
    	}

    	if (!isAopResourceFilteringRequired(resourceName, loader)) {
        	return originalResult;
        }

    	if (isPluginSourceURL(originalResult, resourceName)) {
    		return null;
    	} else {
    		return originalResult;
    	}
    }

    /**
	 * Goes over the result of a {@link ClassLoader#getResources(String)} call
	 * and removes AOP related resource {@link URL}-s. In addition to the
	 * considerations taken by {@link #isAopResourceFilteringRequired(String, ClassLoader)}
	 * it also checks if the URL originated in one of the plugins URL(s).
	 * It also removes any plugin duplications.
	 * @param resourceName The requested resource name
	 * @param loader The requesting {@link ClassLoader}
	 * @param originalResult The original {@link Enumeration} that the loader returned
	 * @return The updated {@link Enumeration} - same as input if no filtering
	 * @see #isAopResourceFilteringRequired(String, ClassLoader)
	 * @see #isPluginSourceURL(URL)
	 */
	public Enumeration<URL> filterResourcesURLs (String resourceName, ClassLoader loader, Enumeration<URL> originalResult) {
		if (originalResult == null) {
			return null;
		}
		
		Enumeration<URL> result = originalResult;
		
		if (!WeavingSupport.isAopResource(resourceName)){
			result = eliminateDuplicatePluginUrls(resourceName, result);
		}
		
		if (!isAopResourceFilteringRequired(resourceName, loader)) {
			return result;
		}

		return filterPluginSourceURLs(resourceName, result);
	}

	/** 
	 * Leaves only ONE copy of the plugins URL(s).
	 * If the agent/dashboard are running on an application server whose application context class loader is "deep" AND
	 * (at least some of) the application's class loader ancestors are also instrumented, then we get multiple instantiations of various analyzers.
	 * This is both wasteful and erroneous - especially TraceErrorAnalyzer(s) which duplicate the error.
	 * The problem is with the automatic "scanning" of all "insight-*.xml" done by the Spring container.
	 * 
	 * @param resourceName The requested resource name
	 * @param urls The original {@link Enumeration} that the loader returned
	 * @return The updated {@link Enumeration} - same as input but with only 1 instance of any plugin resource
	 */
	protected Enumeration<URL> eliminateDuplicatePluginUrls(String resourceName, Enumeration<URL> urls) {
		List<URL> urlsToReturn = new ArrayList<URL>();

		final Collection<String> filtered = new HashSet<String>();
		while ((urls != null) && urls.hasMoreElements()) {
			URL url = urls.nextElement();
			if (isPluginSourceURL(url, resourceName)){
				String externalForm = url.toExternalForm();
				String convertedURLExternalForm = getUniqueURLPath(externalForm);
				if (filtered.add(convertedURLExternalForm)) {					
					urlsToReturn.add(url);				
				}
				else{
					if (log.isLoggable(Level.FINE)){
						log.fine("filterd a duplicate plugin resource - " + externalForm);
					}				
				}
			}
			else{
				urlsToReturn.add(url);				
			}
		}

		return Collections.enumeration(urlsToReturn);
	}

    /**
     * @param resourceName The requested resource name - may be <code>null</code>/empty
     * @param originalResult An {@link Enumeration} of {@link URL}s that might
     * need filtering
     * @return The updated {@link Enumeration} where any URL(s) that originate
     * from one of the plugins is filtered out
     * @see #filterPluginSourceURLs(Collection)
     * @see #isPluginSourceURL(URL)
     */
    public Enumeration<URL> filterPluginSourceURLs (String resourceName, Enumeration<URL> originalResult) {
    	Collection<URL>	filtered=filterPluginSourceURLs(resourceName,
    			(originalResult == null) ? Collections.<URL>emptyList(): Collections.list(originalResult));
    	if (ListUtil.size(filtered) <= 0) {
    		return WeavingSupport.EMPTY_URL_ENUMERATION;
    	} else {
    		return Collections.enumeration(filtered);
    	}
    }

    /**
     * @param resourceName The requested resource name - may be <code>null</code>/empty
     * @param originalResult A {@link Collection} of {@link URL}s that might
     * need filtering
     * @return The updated {@link List} where any URL(s) that originate
     * from one of the plugins is filtered out
     * @see #isPluginSourceURL(URL)
     */
    public List<URL> filterPluginSourceURLs (String resourceName, Collection<? extends URL> originalResult) {
    	if (ListUtil.size(originalResult) <= 0) {
    		return Collections.emptyList();
    	}
    	
    	List<URL>	result=new ArrayList<URL>(originalResult.size());
    	for (URL url : originalResult) {
    		if (isPluginSourceURL(url, resourceName)) {
    			continue;
    		}

    		result.add(url);
    	}

    	return result;
    }

    /**
     * @param resourceName The requested resource name - may be <code>null</code>/empty
     * @param url Input {@link URL}
     * @return <code>true</code> if the URL originates from one of the plugin(s)
     * @see #getPluginSourceURL(URL)
     */
    public boolean isPluginSourceURL (URL url, String resourceName) {
    	return getPluginSourceURL(url, resourceName) != null;
    }

    /**
     * Checks if a give {@link URL} originates from one of the plugin(s)
     * @param url Input {@link URL}
     * @param resourceName The requested resource name - may be <code>null</code>/empty
     * @return The URL of the plugin from which the input URL originates, or
     * <code>null</code> if no matching plugin found
     * @see #getPluginSourceURL(String)
     */
    public URL getPluginSourceURL (URL url, String resourceName) {
    	return getPluginSourceURL((url == null) ? null : url.toExternalForm(), resourceName);
    }
    
    /**
     * Checks if a give {@link URL#toExternalForm()} originates from one of
     * the plugin(s)
     * @param url Input {@link String}
     * @param resourceName The requested resource name - may be <code>null</code>/empty
     * @return The URL of the plugin from wichh the input URL originates, or
     * <code>null</code> if no matching plugin found
     */
    public URL getPluginSourceURL (String url, String resourceName) {
    	String	src=getCanonicalURLValue(url);
    	if (StringUtil.isEmpty(src)) {
    		return null;
    	}

    	URL	plugin=pluginURLSources.get(src);
    	if (plugin != null) {
    		return plugin;
    	}

    	if (StringUtil.isEmpty(resourceName)) {
    		return null;
    	}

    	/*
    	 * Folder URL(s) do not have a '!' that is stripped, so they end in the resource name
    	 */
    	if (!src.endsWith(resourceName)) {
    		/*
    		 * Check if collection plugins folder is a prefix of it - e.g.,
    		 * the resource name contains wildcards or is just a prefix
    		 * - e.g., getResources("META-INF/") returns URL(s) of all the
    		 * files under that folder.
    		 */
    		if (src.startsWith(collectionPluginSource)) {
    			return pluginURLSources.get(collectionPluginSource);
    		}

    		return null;
    	}

    	// strip the resource sub-path
    	src = StringUtil.adjustURLPathValue(src.substring(0, src.length() - resourceName.length()));

    	if ((plugin=pluginURLSources.get(src)) != null) {
    		return plugin;
    	}

    	return null;
    }

    public String getCanonicalURLValue (File file) {
    	return getCanonicalURLValue((file == null) ? null : file.toURI());
    }

    public String getCanonicalURLValue (URI uri) {
    	try {
    		return getCanonicalURLValue((uri == null) ? null : uri.toURL());
		} catch(MalformedURLException e) {
			throw new IllegalArgumentException("getCanonicalURLValue(" + uri.toString() + ") failed to convert to URL: " + e.getMessage(), e);
		}
    }
    /**
     * @param url The {@link URL} - ignored if <code>null</code>
     * @return A &quot;canonical&quot; form that can be used to compare it
     * with the plugins
     */
    public String getCanonicalURLValue (URL url) {
    	return getCanonicalURLValue((url == null) ? null : url.toExternalForm());
    }

    /**
     * @param url The url - ignored if <code>null</code>/empty
     * @return A &quot;canonical&quot; form that can be used to compare it
     * with the plugins
     * @see #getURLSource(String)
     * @see #convertURLExternalForm(String)
     */
    public String getCanonicalURLValue (String url) {
    	String	src=FileUtil.getURLSource(url);
    	return convertURLExternalForm(src);
    }
    
    public String getUniqueURLPath(String externalForm) {    	
    	String src = FileUtil.stripJarURLPrefix(externalForm);
    	return convertURLExternalForm(src);
	}

    protected ContainerInformationProvider getContainerInformationProvider () {
    	return config.getContainerInformationProvider();
    }

    protected String convertURLExternalForm(String src) {
        ContainerInformationProvider	provider=getContainerInformationProvider();
        return provider.convertURLExternalForm(src);
    }
    
    /**
     * Decides whether a request for an AOP related resource should be returned as-is or
     * filtered. Such a resource is filtered if the following conditions hold:</BR>
     * <UL>
     * 		<LI>
     * 		Insight instrumentation is enabled
     * 		</LI>
     * 
     * 		<LI>
     * 		The requested resource is one for which {@link WeavingSupport#isAopResource(String)}
     * 		returns <code>true</code> 
     * 		</LI>
     * 
     * 		<LI>
     * 		The {@link ClassLoader} that is requesting the resource is an {@link InsightClassLoader}
     * 		</LI>
     * 
     * 		<LI>
     * 		The associated {@link ApplicationName} is marked as &quot;ignored context&quot;
     * 		</LI>
     * </UL>
     * @param resourceName The requested resource name
     * @param loader The requesting {@link ClassLoader}
     * @return <code>true</code> if resource filtering is required
     * @see #isAopResourceFilteringRequired(String, ApplicationName)
     */
    public boolean isAopResourceFilteringRequired (String resourceName, ClassLoader loader) {
    	InsightClassLoader	insightLoader=ClassLoaderUtils.findInsightWeavingClassLoader(loader);
        if (insightLoader == null) {
        	return false;
        } else {
        	return isAopResourceFilteringRequired(resourceName, insightLoader.getApplicationName());
        }
    }

    /**
     * @param resourceName The requested resource name
     * @param appName The {@link ApplicationName} to be checked - ignored if <code>null</code>
     * @return <code>true</code> if resource filtering is required
     */
    public boolean isAopResourceFilteringRequired (String resourceName, ApplicationName appName) {
        Insight	insight=getInsight();
        if (!insight.isInsightEnabled()) {
        	return false;
        }

        if (!WeavingSupport.isAopResource(resourceName)) {
        	return false;
        }

        if ((appName != null) && insight.isContextIgnored(appName)) {
        	return true;	// debug breakpoint
        } else {
        	return false;	// debug breakpoint
        }
    }
}
