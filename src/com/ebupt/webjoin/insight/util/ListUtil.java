package com.ebupt.webjoin.insight.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.ebupt.webjoin.insight.intercept.util.GroupingClosure;

public class ListUtil {
	public static <T> List<T> pickSample(List<T> population, int samplesNeeded,
			Random r) {
		if (samplesNeeded > population.size()) {
			throw new IllegalArgumentException(
					"Requested more samples than in the population");
		}
		List<T> res = new ArrayList<T>(samplesNeeded);
		int i = 0;
		int nLeft = population.size();
		while (samplesNeeded > 0) {
			int rand = r.nextInt(nLeft);
			if (rand < samplesNeeded) {
				res.add(population.get(i));
				samplesNeeded--;
			}
			nLeft--;
			i++;
		}
		return res;
	}

	public static <T> Set<T> asSet(T... items) {
		Set<T> res = new HashSet<T>();
		res.addAll(Arrays.asList(items));
		return res;
	}

	public static <T> Set<T> asSet(Collection<T> items) {
		Set<T> res = new HashSet<T>();
		res.addAll(items);
		return res;
	}

	public static double[] toArrayDouble(Collection<Double> vals) {
		double[] res = new double[vals.size()];
		int idx = 0;
		for (Double d : vals) {
			res[(idx++)] = d.doubleValue();
		}
		return res;
	}

	public static long[] toArrayLong(Collection<Long> vals) {
		long[] res = new long[vals.size()];
		int idx = 0;
		for (Long l : vals) {
			res[(idx++)] = l.longValue();
		}
		return res;
	}

	public static <T> List<List<T>> partition(List<T> population,
			int numSublists) {
		if (numSublists <= 0) {
			throw new IllegalArgumentException("numSublists <= 0");
		}

		int popSize = population.size();
		if (numSublists > popSize) {
			numSublists = popSize;
		}

		int basePerSubList = popSize / numSublists;
		int[] subListSize = new int[numSublists];
		for (int i = 0; i < numSublists; i++) {
			subListSize[i] = basePerSubList;
		}

		List<Integer> subListsToBeefUp = makeCountingList(numSublists);
		Collections.shuffle(subListsToBeefUp, new Random(numSublists));

		int used = basePerSubList * numSublists;
		int beefyIndex = 0;
		while (used != popSize) {
			int chosenSub = ((Integer) subListsToBeefUp.get(beefyIndex++))
					.intValue();
			subListSize[chosenSub] += 1;
			used++;
		}

		List<List<T>> res = new ArrayList<List<T>>(numSublists);
		int popIdx = 0;
		for (int i = 0; i < numSublists; i++) {
			List<T> subList = new ArrayList<T>(subListSize[i]);
			for (int j = 0; j < subListSize[i]; j++) {
				subList.add(population.get(popIdx++));
			}
			res.add(subList);
		}

		if (popIdx != popSize) {
			throw new IllegalStateException("Ended unbalanced");
		}
		return res;
	}

	public static List<Integer> makeCountingList(int length) {
		List<Integer> res = new ArrayList<Integer>(length);
		for (int i = 0; i < length; i++) {
			res.add(Integer.valueOf(i));
		}
		return res;
	}

	public static <K, V> Map<K, Collection<V>> groupBy(Collection<V> vals,
			GroupingClosure<K, V> grouper) {
		Map<K, Collection<V>> res = new HashMap<K, Collection<V>>();
		for (Iterator<V> itr = vals.iterator(); itr.hasNext();) {
			V v = itr.next();
			K group = grouper.getGroupFor(v);
			Collection<V> inGroup = res.get(group);
			if (inGroup == null) {
				inGroup = new ArrayList<V>();
				res.put(group, inGroup);
			}
			inGroup.add(v);
		}
		return res;
	}

	public static final SortedSet<URL> createURLsSet(URL[] urls) {
		return createURLsSet((urls == null) || (urls.length <= 0) ? null
				: Arrays.asList(urls));
	}

	public static final SortedSet<URL> createURLsSet(
			Collection<? extends URL> urls) {
		SortedSet<URL> retSet = createURLsSet();
		if ((urls != null) && (urls.size() > 0)) {
			retSet.addAll(urls);
		}

		return retSet;
	}

	public static final SortedSet<URL> createURLsSet() {
		return new TreeSet<URL>(StringUtil.BY_EXTERNAL_FORM_COMPARATOR);
	}

	public static final SortedSet<URL> createByPathOnlyURLSet(URL[] urls) {
		return createByPathOnlyURLSet((urls == null) || (urls.length <= 0) ? null
				: Arrays.asList(urls));
	}

	public static final SortedSet<URL> createByPathOnlyURLSet(
			Collection<? extends URL> urls) {
		SortedSet<URL> urlSet = createByPathOnlyURLSet();
		if ((urls != null) && (urls.size() > 0)) {
			urlSet.addAll(urls);
		}
		return urlSet;
	}

	public static final SortedSet<URL> createByPathOnlyURLSet() {
		return new TreeSet<URL>(StringUtil.BY_PATH_ONLY_COMPARATOR);
	}

	public static final List<URI> toURI(Collection<? extends URL> urls) {
		int numURLs = urls == null ? 0 : urls.size();
		if (numURLs <= 0) {
			return Collections.emptyList();
		}

		List<URI> uris = new ArrayList<URI>(numURLs);
		for (URL url : urls) {
			if (url != null) {
				try {
					if (!uris.add(url.toURI()))
						;
				} catch (URISyntaxException e) {
					throw new RuntimeException("toURI(" + url.toExternalForm()
							+ "): " + e.getMessage(), e);
				}
			}
		}
		return uris;
	}

	public static final List<String> toURIString(Collection<? extends URI> uris) {
		int numURIs = uris == null ? 0 : uris.size();
		if (numURIs <= 0) {
			return Collections.emptyList();
		}

		List<String> strs = new ArrayList<String>(numURIs);
		for (URI uri : uris) {
			String strValue = uri == null ? null : uri.toString();
			if ((strValue == null) || (strValue.length() <= 0)
					|| (!strs.add(strValue)))
				;
		}

		return strs;
	}

	public static final List<URL> toURL(Collection<? extends File> files) {
		int numFiles = files == null ? 0 : files.size();
		if (numFiles <= 0) {
			return Collections.emptyList();
		}

		List<URL> urls = new ArrayList<URL>(numFiles);
		for (File f : files) {
			try {
				urls.add(f.toURI().toURL());
			} catch (MalformedURLException e) {
				throw new RuntimeException("Could not get URL access to file: "
						+ f, e);
			}
		}

		return urls;
	}

	public static final <E> Enumeration<E> eliminateDuplicates(
			Enumeration<E> values, Comparator<E> comp) {
		Collection<E> filtered = new TreeSet<E>(comp);
		while ((values != null) && (values.hasMoreElements())) {
			E member = values.nextElement();
			if (!filtered.add(member))
				;
		}

		return Collections.enumeration(filtered);
	}

	public static<T> int size(Collection<T> nameSet) {
		if(nameSet == null) return -1;
		return nameSet.size();
	}

	public static<T> boolean compareCollections(
			List<T> list1,
			List<T> list2) {
		if(null == list1 || list2 == null) return false;
		if(list1.size() != list2.size()) return false;
		Iterator<T> itr1=list1.iterator();
		Iterator<T> itr2= list2.iterator();
		for(;itr1.hasNext()&&itr2.hasNext();)
			if(!itr1.next().equals(itr2.next())) return false;
		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFirstMember(Collection<T> frames) {
		if(null == frames) return null;
		if(frames.size() == 0 )return null;
		return (T) frames.toArray()[0];
	}

	public static String combine(List<?> values, char sep) {
		if(values == null) return null;
		StringBuffer buffer = new StringBuffer();
		for(Object value:values)
			buffer.append(value.toString()).append(sep);
		if(buffer.length() <= 0) return "";
		int lastIndex = buffer.length()-1;
		if(sep == buffer.charAt(lastIndex))
			buffer.deleteCharAt(lastIndex);
		return buffer.toString();
	}

 
}