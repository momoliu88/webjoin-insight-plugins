package com.ebupt.webjoin.insight.intercept.ltw;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import org.apache.catalina.LifecycleException;
import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;

import com.ebupt.webjoin.insight.Insight;
import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;

public class TomcatWeavingInsightClassLoader extends TomcatInsightClassLoader {
	/*
	 * private static final Log log = LogFactory
	 * .getLog(TomcatWeavingInsightClassLoader.class);
	 */

	private Insight insight = InterceptConfiguration.getInstance().getInsight();

	public TomcatWeavingInsightClassLoader() {
	}

	public TomcatWeavingInsightClassLoader(ClassLoader parent) {
		super(parent);
	}

	public void start() throws LifecycleException {
		super.start();
		readInsightConfig();

		List<File> pluginRoots = new ArrayList<File>();
		pluginRoots.add(new File(System.getProperty("insight.base")));

		if ((this.insight.isInsightEnabled() == true)
				&& (!this.insight.isContextIgnored(getApplicationName()))) {
			addTransformer(new LoggingClassFileTransformer(
					new ClassPreProcessorAgentAdapter()));
		} /*
		 * else log.info("Context [" + getApplicationName() +
		 * "] will not be woven");
		 */
		WebappLoadingHelper helper = new WebappLoadingHelper(pluginRoots);
		addPluginsToClassPath(helper);
		//String[] resps = findRepositories();
		//System.out.println("classloader "+Thread.currentThread().getContextClassLoader());
		/*for(String resp:resps)
			System.out.println("resp: "+resp);*/
	}

	private void readInsightConfig() {
		File insightDir = new File(System.getProperty("insight.base"));
		File confDir = new File(insightDir, "conf");
		File insightConfig = new File(confDir, "insight.properties");
		this.insight.readConfigurationIfNotConfigured(insightConfig);
	}

	private void addPluginsToClassPath(WebappLoadingHelper helper) {
		for (String aspectsPathUrl : helper.getClassLoaderSearchUrls()) {
			addRepository(aspectsPathUrl);
		}
	}

	private class LoggingClassFileTransformer implements ClassFileTransformer {
		private final ClassFileTransformer delegate;

		public LoggingClassFileTransformer(ClassFileTransformer delegate) {
			this.delegate = delegate;
		}

		public byte[] transform(ClassLoader loader, String className,
				Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer)
				throws IllegalClassFormatException {
			return this.delegate.transform(loader, className,
					classBeingRedefined, protectionDomain, classfileBuffer);
		}
	}
}