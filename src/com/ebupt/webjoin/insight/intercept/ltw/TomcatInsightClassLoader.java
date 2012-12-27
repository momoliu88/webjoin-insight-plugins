package com.ebupt.webjoin.insight.intercept.ltw;

import java.net.URL;
import java.util.Collection;
import org.apache.catalina.LifecycleException;
import org.apache.naming.resources.ProxyDirContext;
import org.springframework.instrument.classloading.tomcat.TomcatInstrumentableClassLoader;

import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.util.ListUtil;

public class TomcatInsightClassLoader extends TomcatInstrumentableClassLoader
		implements InsightClassLoader {
	private ApplicationName applicationName = ApplicationName.UNKOWN_APPLICATION;

	public TomcatInsightClassLoader() {
	}

	public TomcatInsightClassLoader(ClassLoader parent) {
		super(parent);
	}

	public void start() throws LifecycleException {
		super.start();
		setApplicationName(deriveApplicationName());
	}

	public void addLookupUrl(URL url) {
		addRepository(url.toExternalForm());
	}

	public Collection<URL> getLookupUrls() {
		return ListUtil.createURLsSet(getURLs());
	}

	public ApplicationName getApplicationName() {
		return this.applicationName;
	}

	public void setApplicationName(ApplicationName applicationName) {
		if (applicationName != null)
			this.applicationName = applicationName;
	}

	private ApplicationName deriveApplicationName() {
		ProxyDirContext resources = (ProxyDirContext) this.resources;
		String host = resources.getHostName();
		String context = resources.getContextName();
		return ApplicationName.valueOf(host, context);
	}
}