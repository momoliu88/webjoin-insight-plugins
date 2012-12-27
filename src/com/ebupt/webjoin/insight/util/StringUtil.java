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

package com.ebupt.webjoin.insight.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.management.ObjectName;

public final class StringUtil {
	public static final String ELLIPSIS = "...";

	private StringUtil() {
		throw new UnsupportedOperationException("Instance N/A");
	}

	/**
	 * Strips any enclosing quotes or double-quotes (if existing)
	 * 
	 * @param value
	 *            The original {@link CharSequence}
	 * @return The value without any enclosing quotes or double-quotes - same as
	 *         input if not quotes to begin with
	 * @throws IllegalArgumentException
	 *             If imbalanced quotes
	 */
	public static CharSequence stripQuotes(CharSequence value)
			throws IllegalArgumentException {
		int vLen = getSafeLength(value);
		if (vLen <= 0) {
			return value;
		}

		char delim = value.charAt(0);
		if ((delim != '\'') && (delim != '"')) {
			delim = value.charAt(vLen - 1);

			if ((delim == '\'') || (delim == '"')) {
				throw new IllegalArgumentException("Imbalanced end quote: "
						+ value);
			} else {
				return value;
			}
		}

		if (vLen < 2) {
			throw new IllegalArgumentException("String too short for quoting: "
					+ value);
		}

		if (value.charAt(vLen - 1) != delim) {
			throw new IllegalArgumentException("Imbalanced start quote: "
					+ value);
		}

		return value.subSequence(1, vLen - 1);
	}

	/**
	 * Checks if a value is quoted, and if so, then un-quotes it using
	 * {@link ObjectName#unquote(String)}
	 * 
	 * @param value
	 *            The input {@link String} value - if <code>null</code> then
	 *            nothing is done
	 * @return The un-quoted result - same as input if already un-quoted
	 * @throws IllegalArgumentException
	 *             If the value is an &quot;imbalanced&quot; quoted value -
	 *             i.e., starts with a quote but does not end in one or vice
	 *             versa
	 */
	public static String smartUnquoteObjectName(String value)
			throws IllegalArgumentException {
		int vLen = getSafeLength(value);
		if (vLen > 0) {
			char startChar = value.charAt(0), endChar = value.charAt(vLen - 1);

			if ((startChar == '"') || (endChar == '"')) {
				if (vLen < 2)
					throw new IllegalArgumentException(
							"Imbalanced quotes[string too small]: " + value);
				if (startChar != '"')
					throw new IllegalArgumentException(
							"Imbalanced quotes[no start quote]: " + value);
				if (endChar != '"')
					throw new IllegalArgumentException(
							"Imbalanced quotes[no end quote]: " + value);

				return ObjectName.unquote(value);
			}
		}

		return value; // no quotes
	}

	/**
	 * Checks is a value is already quoted - if so, then does nothing, else
	 * invokes {@link ObjectName#quote(String)} on it
	 * 
	 * @param value
	 *            The input {@link String} value - if <code>null</code> then
	 *            nothing is done
	 * @return The quoted result - same as input if already quoted
	 * @throws IllegalArgumentException
	 *             If the value is an &quot;imbalanced&quot; quoted value -
	 *             i.e., starts with a quote but does not end in one or vice
	 *             versa
	 */
	public static String smartQuoteObjectName(String value)
			throws IllegalArgumentException {
		if (value == null) {
			return null;
		}

		int vLen = value.length();
		if (vLen > 0) {
			char startChar = value.charAt(0), endChar = value.charAt(vLen - 1);
			if ((startChar == '"') || (endChar == '"')) {
				if (vLen < 2)
					throw new IllegalArgumentException(
							"Imbalanced quotes[string too small]: " + value);
				if (startChar != '"')
					throw new IllegalArgumentException(
							"Imbalanced quotes[no start quote]: " + value);
				if (endChar != '"')
					throw new IllegalArgumentException(
							"Imbalanced quotes[no end quote]: " + value);

				return value; // already quoted
			}
		}

		return ObjectName.quote(value);
	}

	/**
	 * Checks if a String is empty ("") or null.
	 * 
	 * @param str
	 *            the String to check, may be null
	 * @return
	 */
	public static boolean isEmpty(CharSequence str) {
		return (str == null) || (str.length() <= 0);
	}

	/**
	 * Throw an {@link IllegalArgumentException} if the text is empty ("") or
	 * null
	 * 
	 * @param text
	 *            str the String to check, may be null
	 * @param message
	 */
	public static void isEmpty(CharSequence text, String message) {
		if (isEmpty(text)) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Find the string in between 2 tokens.
	 * 
	 * e.g.: findStringBetween("foo bar baz", "FOO BAR BAZ", "foo ", " baz") ->
	 * "bar"
	 * 
	 * @param origStr
	 *            The string from which to return the result (can be mixed case)
	 * @param upperStr
	 *            A toUpper() version of the origStr, which is used to do the
	 *            string scanning
	 * @param firstToken
	 *            First token to look for (must be upper case)
	 * @param lastToken
	 *            Last token to look for (must be upper case)
	 */
	public static String findStringBetween(String origStr, String upperStr,
			String firstToken, String lastToken) {
		int firstIdx = upperStr.indexOf(firstToken);
		int lastIdx = upperStr.indexOf(lastToken);

		if (origStr.length() != upperStr.length()) {
			throw new IllegalArgumentException(
					"upperStr must be same length as origStr");
		}

		if (firstIdx == -1 || lastIdx == -1 || firstIdx >= lastIdx)
			return null;

		return origStr.substring(firstIdx + firstToken.length(), lastIdx);
	}

	/**
	 * Execute {@link #chopHead(String, int)} and add ellipses to the
	 * <U>head</U> of the string if it was chopped. The resulting string will
	 * never be &gt; maxLen in length.
	 * 
	 * @param str
	 *            The {@link String} to be chopped
	 * @param maxLen
	 *            The maximum allowed length
	 * @return Chopped result or same as input if no chopping
	 * @throws IllegalArgumentException
	 *             if negative max length
	 */
	public static String chopHeadAndEllipsify(String s, int maxLen) {
		if (maxLen < 0) {
			throw new IllegalArgumentException("chopHeadAndEllipsify(" + s
					+ ")[" + maxLen + "] negative length N/A");
		}

		if (getSafeLength(s) <= maxLen) {
			return s;
		} else if (maxLen == 0) {
			return "";
		} else if (maxLen <= ELLIPSIS.length()) {
			return ELLIPSIS.substring(0, maxLen);
		} else {
			String str = chopHead(s, maxLen - ELLIPSIS.length());
			return ELLIPSIS + str;
		}
	}

	/**
	 * Chops the head from a given string if it exceeds the specified max.
	 * length, such that its length does not exceed it.
	 * 
	 * @param str
	 *            The {@link String} to be chopped
	 * @param maxLen
	 *            The maximum allowed length
	 * @return Chopped result or same as input if no chopping
	 * @throws IllegalArgumentException
	 *             if negative max length
	 */
	public static String chopHead(String str, int maxLen)
			throws IllegalArgumentException {
		if (maxLen < 0) {
			throw new IllegalArgumentException("chopHead(" + str + ")["
					+ maxLen + "] negative length N/A");
		}

		int strLen = getSafeLength(str);
		if (strLen <= maxLen) {
			return str; // OK if below or equal to max length
		} else if (maxLen == 0) {
			return "";
		} else {
			return str.substring(strLen - maxLen, strLen);
		}
	}

	/**
	 * Chop the tailoff a string, such that its length does not exceed maxChars.
	 * 
	 * @param str
	 *            String to chop
	 * @param maxLen
	 *            Maximum # that returnValue.length() should be
	 * @return a new string, with or without a tail.
	 * @throws IllegalArgumentException
	 *             if negative max length
	 */
	public static String chopTail(String str, int maxLen) {
		if (maxLen < 0) {
			throw new IllegalArgumentException("chopTail(" + str + ")["
					+ maxLen + "] negative length N/A");
		}

		int strLen = getSafeLength(str);
		if (strLen <= maxLen) {
			return str; // OK if below or equal to max length
		} else if (maxLen == 0) {
			return "";
		} else {
			return str.substring(0, maxLen);
		}
	}

	/**
	 * Execute {@link #chopTail(String, int)} and add ellipses to the tail of
	 * the string if it was chopped. The resulting string will never be > maxLen
	 * in length.
	 */
	public static String chopTailAndEllipsify(String s, int maxLen) {
		if (maxLen < 0) {
			throw new IllegalArgumentException("chopTailAndEllipsify(" + s
					+ ")[" + maxLen + "] negative length N/A");
		}

		if (getSafeLength(s) <= maxLen) {
			return s;
		} else if (maxLen == 0) {
			return "";
		} else if (maxLen <= ELLIPSIS.length()) {
			return ELLIPSIS.substring(0, maxLen);
		} else {
			String str = chopTail(s, maxLen - ELLIPSIS.length());
			return str + ELLIPSIS;
		}
	}

	/**
	 * Get the index of the n'th char from the tail of the string. Useful to get
	 * the last # of elements in a formatted string
	 * 
	 * ex: indexOfNthCharFromTail("c.f.Book.method", ".", 2) == 3
	 * 
	 * @param str
	 *            String to look in
	 * @param ch
	 *            Character to search for within str
	 * @param n
	 *            # of 'ch's back to look from the end of the string
	 * 
	 * @return The index of the nth ch or -1 if too much n or not enough ch.
	 */
	public static int indexOfNthCharFromTail(String str, char ch, int n) {
		if (n == 0) {
			return -1;
		}

		int idx = str.length();
		for (int i = 0; i < n; i++) {
			idx = str.lastIndexOf(ch, idx - 1);
			if (idx == -1) {
				return -1;
			}
		}
		return idx;
	}

	/**
	 * Convert a throwable to a string. The resulting string contains the
	 * results of t.printStackTrace()
	 */
	public static String throwableToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

	public static String createStringOfLen(int len) {
		StringBuilder b = new StringBuilder(Math.max(len, 5));
		for (int i = 0; i < len; i++) {
			b.append('x');
		}
		return b.toString();
	}

	/**
	 * @param obj
	 *            The {@link Object} to be checked
	 * @return <code>null</code> if the object is <code>null</code>,
	 *         {@link Object#toString()} otherwise. <B>Note:</B> if the input
	 *         object is already a {@link String} then it is simply returned.
	 */
	public static String safeToString(Object obj) {
		if (obj == null) {
			return null;
		} else if (obj instanceof String) {
			return (String) obj;
		} else {
			return obj.toString();
		}
	}

	/**
	 * @param seq
	 *            Input {@link CharSequence}
	 * @return The {@link CharSequence#length()} or zero if <code>null</code>
	 */
	public static int getSafeLength(CharSequence seq) {
		if (seq == null) {
			return 0;
		} else {
			return seq.length();
		}
	}

	/**
	 * Compares 2 {@link String}-s allowing for <code>null</code>'s
	 * 
	 * @param s1
	 *            1st string
	 * @param s2
	 *            2nd string
	 * @return Same as {@link String#compareTo(String)} except that
	 *         <code>null</code> takes precedence over non-<code>null</code>
	 * @see #safeCompare(String, String, boolean)
	 */
	public static int safeCompare(String s1, String s2) {
		return safeCompare(s1, s2, true);
	}

	/**
	 * Compares 2 {@link String}-s allowing for <code>null</code>'s
	 * 
	 * @param s1
	 *            1st string
	 * @param s2
	 *            2nd string
	 * @param caseSensitive
	 * @return Same as {@link String#compareTo(String)} or
	 *         {@link String#compareToIgnoreCase(String)} (as per the
	 *         sensitivity flag) except that <code>null</code> takes precedence
	 *         over non-<code>null</code>
	 */
	public static int safeCompare(String s1, String s2, boolean caseSensitive) {
		if (s1 == s2) {
			return 0;
		} else if (s1 == null) { // s2 cannot be null or s1 == s2...
			return (-1);
		} else if (s2 == null) {
			return (+1);
		} else if (caseSensitive) {
			return s1.compareTo(s2);
		} else {
			return s1.compareToIgnoreCase(s2);
		}
	}

	public static int hashCode(String s, boolean useUppercase) {
		if (isEmpty(s)) {
			return 0;
		} else if (useUppercase) {
			return s.toUpperCase().hashCode();
		} else {
			return s.toLowerCase().hashCode();
		}
	}

	/**
	 * @param s
	 *            Original {@link String}
	 * @param c
	 *            Character to be removed from initial string
	 * @return The result of removing <U>all</U> occurrences of the specified
	 *         character from the original string. If the character does not
	 *         appear in the string then returns the original string reference.
	 */
	public static String removeAllCharacterOccurrences(String s, char c) {
		int cIndex = (s == null) ? (-1) : s.indexOf(c);
		if (cIndex < 0) { // character does not appear in the string
			return s;
		}

		int sLen = s.length(), lastPos = 0;
		StringBuilder sb = new StringBuilder(sLen - 1 /*
													 * we know we are going to
													 * delete at least one
													 * character
													 */);
		while (cIndex >= lastPos) {
			if (lastPos < cIndex) { // do we have any text since last removal ?
				String subText = s.substring(lastPos, cIndex);
				sb.append(subText);
			}

			if ((lastPos = cIndex + 1) >= sLen) // skip removed character
				return sb.toString(); // no more characters to scan

			cIndex = s.indexOf(c, lastPos);
		}

		if (lastPos < sLen) { // check if any leftovers
			String subText = s.substring(lastPos);
			sb.append(subText);
		}

		return sb.toString();
	}

	/**
	 * Returns the substring occurring after the last character found from the
	 * input list.
	 * 
	 * @param str
	 *            String to take substr of
	 * @param ch
	 *            Characters to use for indexing:
	 * 
	 * @return str or a substring of it
	 * 
	 *         example: subStringAfterLast("com.package.Class", '.') -> "Class"
	 *         subStringAfterLast("com.package.Class$SubClass", '.', '$') ->
	 *         "SubClass"
	 */
	public static String subStringAfterLast(String str, char... ch) {
		int lastIndex = -1;

		for (char c : ch) {
			int index = str.lastIndexOf(c);
			if (index > lastIndex) {
				lastIndex = index;
			}
		}

		if (lastIndex == -1 || lastIndex == str.length() - 1) {
			return str;
		}
		return str.substring(lastIndex + 1);
	}

	/**
	 * @param str
	 *            the {@link CharSequence} to scan
	 * @param chars
	 *            the {@link Collection} of {@link Character}s to scan for
	 * @return the index of the first character within 'str' that is <U>not</U>
	 *         within the given set of characters, or -1 if there are no
	 *         characters in 'str' which are not also in 'chars' s -
	 *         example:</BR>
	 * 
	 *         indexOfNotIn(&quot; bar&quot;, { ' ' }) -> 2
	 * @see #indexOf(CharSequence, Collection, boolean)
	 */
	public static int indexOfNotIn(CharSequence str, Collection<Character> chars) {
		return indexOf(str, chars, false);
	}

	/**
	 * @param str
	 *            the {@link CharSequence} to scan
	 * @param chars
	 *            the {@link Collection} of {@link Character}s to scan for
	 * @return the index of the first character within 'str' that is one of the
	 *         given set of characters, or -1 if there are no characters in
	 *         'str' which are not also in 'chars' s - example:</BR>
	 * 
	 *         indexOfNotIn(&quot; bar&quot;, { ' ' }) -> 2
	 * @see #indexOf(CharSequence, Collection, boolean)
	 * @see #indexOf(CharSequence, Collection, boolean)
	 */
	public static int indexOfIn(CharSequence str, Collection<Character> chars) {
		return indexOf(str, chars, true);
	}

	public static int indexOf(CharSequence str, Collection<Character> chars,
			boolean contained) {
		if (isEmpty(str)) {
			return (-1);
		}

		int strlen = str.length();
		for (int i = 0; i < strlen; i++) {
			char ch = str.charAt(i);
			if (chars.contains(Character.valueOf(ch)) == contained) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @param path
	 *            A URL path value
	 * @return The path after stripping any trailing '/' provided the path is
	 *         not '/' itself
	 */
	public static final String adjustURLPathValue(final String path) {
		final int pathLen = getSafeLength(path);
		if ((pathLen <= 1) || (path.charAt(pathLen - 1) != '/')) {
			return path;
		}

		return path.substring(0, pathLen - 1);
	}

	public static final String adjustURLPathValue(URL url) {
		return adjustURLPathValue((url == null) ? null : url.getPath());
	}

	/**
	 * Split a string into a list, each element straddling the given delimiter
	 */
	public static final List<String> explode(String str, String delim) {
		List<String> res = new ArrayList<String>();
		int fromIdx = 0;
		int delimLength = delim.length();
		while (fromIdx < str.length()) {
			int idx = str.indexOf(delim, fromIdx);
			if (idx == -1) {
				break;
			}

			if (idx - fromIdx != 0) {
				res.add(str.substring(fromIdx, idx));
			} else {
				res.add("");
			}
			int toIdx = idx + delimLength;
			fromIdx = toIdx;
		}

		if (fromIdx != str.length()) {
			res.add(str.substring(fromIdx));
		} else {
			res.add("");
		}
		return res;
	}

	public static final String implode(List<String> str, String delim) {
		int capacity = 0;
		int idx = 0;
		for (String s : str) {
			capacity += s.length();
			idx++;
			if (idx != str.size()) {
				capacity += delim.length();
			}
		}

		StringBuilder b = new StringBuilder(capacity);
		idx = 0;
		for (String s : str) {
			b.append(s);
			idx++;
			if (idx != str.size()) {
				b.append(delim);
			}
		}
		return b.toString();
	}

	public static final Map<String, String> parseURLQuery(URL url) {
		return parseURIQuery(url.getQuery());
	}

	public static final Map<String, String> parseURIQuery(URI uri) {
		return parseURIQuery(uri.getQuery());
	}

	/**
	 * Parses a URI/URL query part into a {@link Map} of properties and their
	 * values
	 * 
	 * @param queryString
	 *            The query {@link String} - if <code>null</code>/empty then an
	 *            empty map is returned
	 * @return A {@link Map} of properties and their values where the keys are
	 *         case <U>insensitive</U> and the values are <U>unquoted</U> and
	 *         <U>trimmed</U>
	 * @throws IllegalArgumentException
	 *             if bad format detected
	 * @throws IllegalStateException
	 *             if same property name (case <U>insensitive</U>) is re-mapped
	 */
	public static final Map<String, String> parseURIQuery(
			final String queryString) {
		if (StringUtil.isEmpty(queryString)) {
			return Collections.emptyMap();
		}

		String[] queryValues = queryString.split("&");
		if ((queryValues == null) || (queryValues.length <= 0)) {
			throw new IllegalArgumentException("Malformed query string: "
					+ queryString);
		}

		Map<String, String> result = new TreeMap<String, String>(
				String.CASE_INSENSITIVE_ORDER);
		for (String value : queryValues) {
			int pos = value.indexOf('=');
			if ((pos <= 0) || (pos >= (value.length() - 1))) {
				throw new IllegalArgumentException("Bad value format (" + value
						+ "): " + queryString);
			}

			String aName = value.substring(0, pos).trim(), aValue = value
					.substring(pos + 1).trim();
			if (StringUtil.isEmpty(aName)) {
				throw new IllegalArgumentException("Empty property name name ("
						+ value + "): " + queryString);
			}

			String prevValue = result.put(aName, StringUtil.stripQuotes(aValue)
					.toString().trim());
			if (prevValue != null) {
				throw new IllegalStateException("Multiple values for " + aName
						+ ": " + aValue + " and " + prevValue + "in "
						+ queryString);
			}
		}

		return result;
	}

	/**
	 * Checks if a given {@link CharSequence}
	 * 
	 * @param s
	 *            The {@link CharSequence} to be checked - <B>Note:</B> if
	 *            <code>null</code>/empty then result is <code>false</code>
	 * @return <code>true</code> if the input parameter represents a
	 *         positive/negative non-floating point number - e.g., <code>-73965,
	 * 3777347</code> are <code>true</code> whereas <code>3.14, abcd,
	 * 12 34</code> are <code>false</code>
	 */
	public static final boolean isIntegerNumber(CharSequence s) {
		if (isEmpty(s)) {
			return false;
		}

		for (int index = 0; index < s.length(); index++) {
			final char ch = s.charAt(index);
			if ((ch == '+') || (ch == '-')) {
				if ((index != 0) || (s.length() <= 1)) {
					return false;
				}
			} else if ((ch < '0') || (ch > '9')) {
				return false;
			}
		}

		return true;
	}

	public static String trimLeadingWhitespace(String str) {
		if (isEmpty(str)) {
			return str;
		}

		int i = 0;
		for (; i < str.length(); i++) {
			char c = str.charAt(i);
			if (!Character.isWhitespace(c)) {
				break;
			}
		}

		return str.substring(i, str.length());
	}

	/**
	 * @param s
	 *            The {@link String} to be capitalized - ignored if
	 *            <code>null</code> empty
	 * @return The string with the 1st character capitalized if not already such
	 */
	public static final String capitalize(String s) {
		int strLen = getSafeLength(s);
		if (strLen <= 0) {
			return s;
		}

		char ch0 = s.charAt(0);
		if ((ch0 >= 'A') && (ch0 <= 'Z')) {
			return s;
		} else if (strLen == 1) {
			return String.valueOf(Character.toUpperCase(ch0));
		} else {
			return String.valueOf(Character.toUpperCase(ch0)) + s.substring(1);
		}
	}

	/**
	 * Compares 2 {@link URL}-s by using their {@link URL#toExternalForm()}
	 * values <U>after</U> applying {@link #adjustURLPathValue(String)} to the
	 * them
	 */
	public static final Comparator<URL> BY_EXTERNAL_FORM_COMPARATOR = new Comparator<URL>() {
		/*
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(URL o1, URL o2) {
			final String f1 = adjustURLPathValue((o1 == null) ? null : o1
					.toExternalForm()), f2 = adjustURLPathValue((o2 == null) ? null
					: o2.toExternalForm());
			if (f1 == null) { // push null(s) to end
				return (f2 == null) ? 0 : (+1);
			} else if (f2 == null) {
				return (-1);
			} else {
				return f1.compareTo(f2);
			}
		}
	};
	/**
	 * Compares 2 {@link URL}-s by using their {@link URL#getPath()} values
	 * <U>after</U> applying {@link #adjustURLPathValue(String)} to them
	 */
	public static final Comparator<URL> BY_PATH_ONLY_COMPARATOR = new Comparator<URL>() {
		/*
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(URL u1, URL u2) {
			final String p1 = adjustURLPathValue((u1 == null) ? null : u1
					.getPath()), p2 = adjustURLPathValue((u2 == null) ? null
					: u2.getPath());
			// push null(s) to end
			if (p1 == null) {
				return (p2 == null) ? 0 : (+1);
			} else if (p2 == null) {
				return (-1);
			} else {
				return p1.compareTo(p2);
			}
		}
	};
	/**
	 * Compares <U>case insensitive</U> 2 {@link URI}-s by their
	 * {@link URI#getScheme()} values
	 */
	public static final Comparator<URI> BY_SCHEME_COMPARATOR = new Comparator<URI>() {
		public int compare(URI o1, URI o2) {
			String s1 = o1.getScheme(), s2 = o2.getScheme();
			return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
		}
	};

	public static String trimWithEllipsis(final String string,
			final int maxLength) {
		return string.length() <= maxLength + ELLIPSIS.length() ? string
				: string.substring(0, maxLength) + ELLIPSIS;
	}

	/**
	 * returns the min occurrence of a given delimiter
	 * 
	 * @param src
	 *            source string
	 * @param delimiters
	 *            set of chars
	 * @return min occurrence of a given delimiter or -1 if <code>src</code> is
	 *         <code>null</code> or empty, or if <code>delimiters</code> is
	 *         <code>null</code> or empty, or if none of the delimiters was
	 *         found in the string
	 */
	public static int findMinOccurrence(String src, CharSequence delimiters) {
		if (isEmpty(src)) {
			return -1;
		}

		if (isEmpty(delimiters)) {
			return -1;
		}

		int minIndex = -1;
		for (int i = 0; i < delimiters.length(); i++) {
			char delimiter = delimiters.charAt(i);

			int index = src.indexOf(delimiter);

			if (index < 0) {
				continue;
			}

			if (minIndex == -1) {
				minIndex = index;
			} else {
				minIndex = Math.min(index, minIndex);
			}
		}

		return minIndex;
	}

	public static boolean hasText(String str) {
		return hasText((CharSequence) str);

	}

	public static boolean hasText(CharSequence str) {

		if (getSafeLength(str) <= 0) {

			return false;

		}

		int strLen = str.length();

		for (int i = 0; i < strLen; i++) {

			if (!Character.isWhitespace(str.charAt(i))) {

				return true;

			}

		}

		return false;

	}
}
