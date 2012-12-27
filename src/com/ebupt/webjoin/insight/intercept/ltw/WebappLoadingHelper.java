package com.ebupt.webjoin.insight.intercept.ltw;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ebupt.webjoin.insight.util.ListUtil;

public class WebappLoadingHelper {
	private Collection<File> searchRoots;
	private JarFileFilter aopFilter = new JarFileFilter("aop.xml");
	private JarFileFilter jarFilter = new JarFileFilter(null);

	public WebappLoadingHelper(Collection<File> searchRoots) {
		this.searchRoots = searchRoots;
	}

	public Collection<String> getClassLoaderSearchUrls() {
		List<String> res = new ArrayList<String>();
		List<String> aopFile = new ArrayList<String>();
		for (File root : this.searchRoots) {
			File pluginPath = makePluginPath(root);
			getAspectPathUrls(pluginPath, res, aopFile);

			String aops = ListUtil.combine(aopFile, ';');
			System.setProperty("org.aspectj.weaver.loadtime.configuration",
					aops);
		}
		return res;
	}

	private File makePluginPath(File baseDir) {
		return new File(baseDir, "collection-plugins");
	}

	private void getAspectPathUrls(File aspectspathHome, List<String> urls,
			List<String> aopfiles) {
		// List<String> urls = new ArrayList<String>();
		// urls.add(new
		// File(aspectspathHome.getAbsolutePath()).toURI().toString());
		if (aspectspathHome == null)
			return;
		if (aspectspathHome.isFile() && jarFilter.accept(aspectspathHome)) {
			urls.add(aspectspathHome.toURI().toString());
			return;
		}
		if (aspectspathHome.isFile() && aopFilter.accept(aspectspathHome)) {
			aopfiles.add(aspectspathHome.toURI().toString());
			return;
		} else if (aspectspathHome.isDirectory()) {
			File[] jars = aspectspathHome.listFiles();
			if (jars != null) {
				for (File jar : jars) {
					getAspectPathUrls(jar, urls, aopfiles);
				}
			}
		}
	}

	private static class JarFileFilter implements FileFilter {
		private String keyword;

		public JarFileFilter(String keyword) {
			this.keyword = keyword == null ? ".jar" : keyword;
		};

		public boolean accept(File pathname) {
			return pathname.getPath().endsWith(keyword);
		}
	}
}