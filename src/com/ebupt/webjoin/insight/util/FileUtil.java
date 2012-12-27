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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.ebupt.webjoin.insight.intercept.util.KeyValPair;

public abstract class FileUtil {
	private static volatile File	TMPDIR;
	public static final String	JAR_FILE_SUFFIX=".jar";
    /**
     * Prefix used in URL(s) that reference a resource inside a JAR
     */
    public static final String	JAR_URL_PREFIX="jar:";
    /**
     * Separator used in URL(s) that reference a resource inside a JAR
     * to denote the sub-path inside the JAR
     */
    public static final char	RESOURCE_SUBPATH_SEPARATOR='!';

	public static final boolean isJarFileName (File f) {
		return (f != null) && isJarFileName(f.getName());
	}

	public static final boolean isJarFileName (String name) {
		if (StringUtil.isEmpty(name) || (name.length() <= JAR_FILE_SUFFIX.length())) {
			return false;
		} else {
			return name.endsWith(JAR_FILE_SUFFIX);
		}
	}

	/**
	 * @param relPath A relative path - may be <code>null</code>/empty
	 * @return A {@link File} representing a relative temporary location
	 * @see #getTmpDir()
	 */
	public static File getTmpLocation (String relPath) {
		File	tmpDir=getTmpDir();
		if (StringUtil.isEmpty(relPath)) {
			return tmpDir;
		} else {
			return new File(tmpDir, relPath);
		}
	}

	/**
	 * @return The current temporary location {@link File}
	 */
	public static File getTmpDir () {
		if (TMPDIR == null) {
			TMPDIR = new File(System.getProperty("java.io.tmpdir"));
		}

		return TMPDIR;
	}
    /**
     * Creates the given directory if it does not exist.
     * On error, throws a RuntimeException
     * @param dir The directory to create
     * @return <tt>true</tt> if directory did not exist and was created
     * @throws RuntimeException if file exists but is not a directory,
     * or it cannot be created
     */
    public static boolean createDir(File dir) throws RuntimeException {
        if (dir.exists()) {
            if (!dir.isDirectory() || !dir.canWrite()) {
                throw new RuntimeException("Not a folder or cannot write to [" + dir.getAbsolutePath() + "]");
            }
            
            return false;
        } else {
            if (!dir.mkdirs())
                throw new RuntimeException("Unable to create or access [" + dir.getAbsolutePath() + "]");
            
            return true;
        }
    }
    
    /**
     * @param f1 The 1st {@link File} to compare
     * @param f2 The 2nd {@link File} to compare
     * @return The result of comparing the {@link File#getAbsolutePath()} result.
     * <B>Note:</B> For Windows, the comparison is case <U>insensitive</U>
     */
    public static int compareByAbsolutePath (File f1, File f2) {
        if (f1 == f2)
            return 0;
        else if (f1 == null)
            return (+1);
        else if (f2 == null)
            return (-1);
 
        String  p1=f1.getAbsolutePath(), p2=f2.getAbsolutePath();
        if (File.separatorChar == '/')
            return p1.compareTo(p2);
        else
            return p1.compareToIgnoreCase(p2);
    }

    public static int compareByCanonicalPath (File f1, File f2) throws IOException {
        if (f1 == f2)
            return 0;
        else if (f1 == null)
            return (+1);
        else if (f2 == null)
            return (-1);
 
        String  p1=f1.getCanonicalPath(), p2=f2.getCanonicalPath();
        if (File.separatorChar == '/')
            return p1.compareTo(p2);
        else
            return p1.compareToIgnoreCase(p2);
    }

    /**
     * @param root The root {@link File} to delete from (inclusive)
     * @return Number of deleted entities (files and/or folders)
     */
    public static int deleteAll (File root) {
        if (!root.exists()) {
            return 0;
        }

        int numDeleted=0;
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if ((files != null) && (files.length > 0)) {
                for (File f : files) {
                    numDeleted += deleteAll(f);
                }
            }
        } else if (!root.isFile()) { 
            throw new UnsupportedOperationException("delete(" + root.getAbsolutePath() + ") neither a directory nor a file");
        }

        if (root.delete()) {
            return numDeleted + 1;
        } else {
            return numDeleted;
        }
    }
    
    /**
     * @param parent A parent {@link File} to be used if the specified
     * <code>path</code> denotes a relative location
     * @param path A path element to be checked - if absolute, then it is
     * the return value, otherwise it is assumed to be relative to the
     * provided <code>parent</code> parameter
     * @return A {@link File} result
     * @see #isRelativePath(CharSequence)
     */
    public static File resolveAbsoluteFile (File parent, String path) {
        if (isRelativePath(path)) {
            return new File(parent, path);
        } else {
            return new File(path);
        }
    }
    
    /**
     * @param path A file path
     * @return <P><code>true</code> if the path is <U>relative</U> - i.e., does
     * not specify an absolute location:</P></BR>
     * <UL>
     *      <LI>
     *      if <code>null</code>/empty then &quot;relative&quot;
     *      </LI>
     *      
     *      <LI>
     *      if starts with dot (.) then relative - e.g., &quot;./foo&quot,
     *      &quot;../../bar&quot;
     *      </LI>
     *      
     *      <LI>
     *      If starts with {@link File#separatorChar} then absolute
     *      </LI>
     *      
     *      <LI>
     *      <B>Note:</B> for <U>Windows</U> systems, it checks if the path
     *      starts with a drive letter followed by a colon - if so, then
     *      considered to be an absolute path
     *      </LI>
     * </UL>
     */
    public static boolean isRelativePath (CharSequence path) {
        if (StringUtil.isEmpty(path)) {
            return true;
        }

        final char  ch0=path.charAt(0);
        if (ch0 == '.') {
            return true;
        }
        else if (ch0 == File.separatorChar) {
            return false;    // this is also true for Windows where "\\" is a network path...
        }

        if ((File.separatorChar == '\\')  // special handling for windows paths with drive letter
         && (path.length() >= 2) // at least drive letter + colon
         && (path.charAt(1) == ':')) {
            // make sure a valid drive letter
            if ((('a' <= ch0) && (ch0 <= 'z'))
              || (('A' <= ch0) && (ch0 <= 'Z'))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Attempts to return the most detailed path for the provided file
     * @param file The {@link File} instance - ignored if <code>null</code>
     * @return The {@link File#getCanonicalPath()} value or {@link File#getAbsolutePath()}
     * if canonical path resolution failed
     */
    public static final String resolveBestPath (File file) {
        if (file == null) {
            return null;
        }

        try {
            return file.getCanonicalPath();
        } catch(IOException e) {
            return file.getAbsolutePath();
        }
    }

    /**
     * Scans the <U><code>main</code></U> section of a JAR file manifest for
     * the 1st non-empty attribute value
     * @param file The JAR {@link File} to be examined
     * @param names A {@link Collection} of {@link Attributes.Name}-s to be checked
     * for non-empty value. <B>Note:</B> the <U>order</U> of the names in the
     * collection dictates the 1st non-empty attribute scan
     * @return The name,value &quot;pair&quot; of the 1st non-empty main attribute
     * from the collection - <code>null</code> if no non-empty match found
     * @throws IOException If failed to access the JAR file for extracting its
     * manifest. <B>Note:</B> if the file does not exist or is not a file or no
     * names specified, then no access is attempted to begin with
     * @see #findFirstManifestAttributeValue(Manifest, Collection)
     */
    public static final KeyValPair<Attributes.Name,String> findFirstManifestAttributeValue (
    			File file, Collection<? extends Attributes.Name> names)
    		throws IOException {
    	if (file.canRead() && file.isFile() && ListUtil.size(names) > 0) {
    		JarFile	jarFile=new JarFile(file);
    		try {
    			return findFirstManifestAttributeValue(jarFile.getManifest(), names);
    		} finally {
    			jarFile.close();
    		}
    	}

    	return null;
    }

    /**
     * Scans the <U><code>main</code></U> section of a JAR file manifest for
     * the 1st non-empty attribute value
     * @param manifest The {@link Manifest} to be scanned - ignored if <code>null</code>
     * @param names A {@link Collection} of {@link Attributes.Name}-s to be checked
     * for non-empty value. <B>Note:</B> the <U>order</U> of the names in the
     * collection dictates the 1st non-empty attribute scan
     * @return  The key,value &quot;pair&quot; of the 1st non-empty main attribute
     * from the collection - <code>null</code> if no non-empty match found
     * @see #findFirstManifestAttributeValue(Attributes, Collection)
     */
    public static final KeyValPair<Attributes.Name,String> findFirstManifestAttributeValue (Manifest manifest, Collection<? extends Attributes.Name> names) {
    	if ((manifest == null) || (ListUtil.size(names) <= 0)) {
    		return null;
    	} else {
    		return findFirstManifestAttributeValue(manifest.getMainAttributes(), names);
    	}
    }
    
    /**
     * Scans the an attributes section of a JAR file manifest for the 1st
     * non-empty attribute value
     * @param attrs The section's {@link Attributes} - ignored if <code>null</code>
     * or empty
     * @param names A {@link Collection} of {@link Attributes.Name}-s to be checked
     * for non-empty value. <B>Note:</B> the <U>order</U> of the names in the
     * collection dictates the 1st non-empty attribute scan
     * @return  The key,value &quot;pair&quot; of the 1st non-empty attribute from
     * the collection - <code>null</code> if no non-empty match found
     */
    public static final KeyValPair<Attributes.Name,String> findFirstManifestAttributeValue (Attributes attrs, Collection<? extends Attributes.Name> names) {
    	if ((MapUtil.size(attrs) <= 0) || (ListUtil.size(names) <= 0)) {
    		return null;
    	}

    	for (Attributes.Name n : names) {
    		String	value=attrs.getValue(n);
    		if (StringUtil.isEmpty(value)) {
    			continue;	// debug breakpoint
    		} else {
    			return new KeyValPair<Attributes.Name,String>(n,value);
    		}
    	}

    	return null;	// no match found
    }

    /**
     * Creates a {@link File} instance referencing the path created from the
     * given sub-components
     * @param pathComponents The path components in the <U>order</U> that they
     * should be used to build the path - ignored if <code>null</code>/empty
     * @return Result {@link File} - <code>null</code> if no components provided
     * @throws IllegalArgumentException If one of the components is <code>null</code>
     * or empty
     * @see #buildFile(Collection)
     */
    public static File buildFile (CharSequence ... pathComponents) throws IllegalArgumentException {
    	if (ArrayUtil.length(pathComponents) <= 0) {
    		return null;
    	} else {
    		return buildFile(Arrays.asList(pathComponents));
    	}
    }

    /**
     * Creates a {@link File} instance referencing the path created from the
     * given sub-components
     * @param pathComponents The path components in the <U>order</U> that they
     * should be used to build the path - ignored if <code>null</code>/empty
     * @return Result {@link File} - <code>null</code> if no components provided
     * @throws IllegalArgumentException If one of the components is <code>null</code>
     * or empty
     * @see #buildFilePath(Collection)
     */
    public static final File buildFile (Collection<? extends CharSequence> pathComponents) throws IllegalArgumentException {
    	String	path=buildFilePath(pathComponents);
    	if (StringUtil.isEmpty(path)) {
    		return null;
    	} else {
    		return new File(path);
    	}
    }

    /**
     * Creates a new {@link File} from a root folder and some path components
     * @param rootFolder The root folder
     * @param pathComponents The sub-path components - can be <code>null</code>/empty,
     * in which case the root folder is returned as the result
     * @return The {@link File} instance representing the <U>relative</U> location
     * from the root folder with the sub-path appended to it (if exists)
     * @throws IllegalArgumentException If no root folder specified
     * @see #buildRelativeFile(File, Collection)
     */
    public static File buildRelativeFile (File rootFolder, CharSequence ... pathComponents) throws IllegalArgumentException {
    	return buildRelativeFile(rootFolder,
    			(ArrayUtil.length(pathComponents) <= 0) ? Collections.<CharSequence>emptyList() : Arrays.asList(pathComponents));
    }

    /**
     * Creates a new {@link File} from a root folder and some path components
     * @param rootFolder The root folder
     * @param pathComponents The sub-path components - can be <code>null</code>/empty,
     * in which case the root folder is returned as the result
     * @return The {@link File} instance representing the <U>relative</U> location
     * from the root folder with the sub-path appended to it (if exists)
     * @throws IllegalArgumentException If no root folder specified
     * @see #buildFilePath(Collection)
     */
    public static File buildRelativeFile (File rootFolder, Collection<? extends CharSequence> pathComponents) throws IllegalArgumentException {
    	if (rootFolder == null) {
    		throw new IllegalArgumentException("No root folder provided");
    	}

    	String	subPath=buildFilePath(pathComponents);
    	if (StringUtil.isEmpty(subPath)) {
    		return rootFolder;
    	} else {
    		return new File(rootFolder, subPath);
    	}
    }
    /**
     * Builds a full file path given sub-components using the {@link File#separatorChar}
     * between them
     * @param pathComponents The path components in the <U>order</U> that they
     * should be used to build the path - ignored if <code>null</code>/empty
     * @return Result path - <code>null</code> if no components provided
     * @throws IllegalArgumentException If one of the components is <code>null</code>
     * or empty
     * @see #buildFilePath(Collection)
     */
    public static final String buildFilePath (CharSequence ... pathComponents) throws IllegalArgumentException {
    	if (ArrayUtil.length(pathComponents) <= 0) {
    		return null;
    	} else {
    		return buildFilePath(Arrays.asList(pathComponents));
    	}
    }
    
    /**
     * Builds a full file path given sub-components using the {@link File#separatorChar}
     * between them
     * @param pathComponents The path components in the <U>order</U> that they
     * should be used to build the path - ignored if <code>null</code>/empty
     * @return Result path - <code>null</code> if no components provided
     * @throws IllegalArgumentException If one of the components is <code>null</code>
     * or empty
     */
    public static final String buildFilePath (Collection<? extends CharSequence> pathComponents) throws IllegalArgumentException {
    	if (ListUtil.size(pathComponents) <= 0) {
    		return null;
    	}

    	StringBuilder	sb=new StringBuilder(pathComponents.size() * 16);
    	for (CharSequence c : pathComponents) {
    		if (StringUtil.isEmpty(c)) {
    			throw new IllegalArgumentException("buildFilePath(" + pathComponents + ") null/empty component");
    		}

			int	sbLen=sb.length();
    		if (c.charAt(0) != File.separatorChar) {
        		/* 
        		 * if previous component does not end in a file separator and the
        		 * new component does not start with one then add separator
        		 */
    			if ((sbLen > 0) && (sb.charAt(sbLen-1) != File.separatorChar)) {
    				sb.append(File.separatorChar);
    			}
    		} else {
    			/*
    			 * If new component starts with file separator AND previous
    			 * one ended with separator, remove the previous ending separator
    			 */
    			if ((sbLen > 0) && (sb.charAt(sbLen-1) == File.separatorChar)) {
    				sb.setLength(sbLen - 1);
    			}
    		}

    		sb.append(c);
    	}

    	return sb.toString();
    }

    public static final String findLongestFileNameCommonSuffix (String folderPath, FileFilter filter) {
    	if (StringUtil.isEmpty(folderPath)) {
    		return null;
    	} else {
    		return findLongestFileNameCommonSuffix(new File(folderPath), filter);
    	}
    }

    public static final String findLongestFileNameCommonSuffix (File file, FileFilter filter) {
    	if (file.isFile()) {
    		if ((filter != null) && (!filter.accept(file))) {
    			return null;
    		} else {	// if a single file, then its name is the "longest" common suffix
    			return file.getName();
    		}
    	} else {
    		return findLongestFileNameCommonSuffix(filter, (filter == null) ? file.listFiles() : file.listFiles(filter));
    	}
    }

    public static final String findLongestFileNameCommonSuffix (FileFilter filter, File ... files) {
    	if (ArrayUtil.length(files) <= 0) {
    		return null;
    	} else {
    		return findLongestFileNameCommonSuffix(filter, Arrays.asList(files));
    	}
    }
     
    public static final String findLongestFileNameCommonSuffix (FileFilter filter, Collection<? extends File> files) {
    	if (ListUtil.size(files) <= 0) {
    		return null;
    	}

    	String	curMatch=null;
    	for (File f : files) {
    		String	name=f.getName();
    		if (StringUtil.isEmpty(name)) {
    			continue;	// ignore empty names
    		}
    		
    		if ((filter != null) && (!filter.accept(f))) {
    			continue;
    		}

    		// if no previous match assume this is the best we can do
    		if (StringUtil.isEmpty(curMatch)) {
    			curMatch = name;
    			continue;
    		}

    		int	curLen=curMatch.length(), nameLen=name.length();
    		int curPos=curLen-1, namePos=nameLen-1;
    		for (	; (curPos >= 0) && (namePos >= 0); curPos--, namePos--) {
    			char	chMatch=curMatch.charAt(curPos), chName=name.charAt(namePos);
    			if (chMatch != chName) {
    				break;
    			}
    		}

    		/*
    		 * At this point, curPos and namePos point to the 1st non-matching location,
    		 * or have (-1) value(s) if one is a prefix of the other
    		 */
    		curPos++;	// advance to 1st matching character

    		/*
    		 * If 1st matching character is beyond the match length, then
    		 * obviously no common suffix
    		 */
    		if (curPos >= curLen) {
    			return null;
    		}

    		/*
    		 * If matching character is not 1st one, then reduce the match
    		 */
    		if (curPos > 0) {
    			curMatch = curMatch.substring(curPos);
    		}
    	}

    	return curMatch;
    }

    /**
     * @param anchor An anchor {@link Class} whose container we want to use
     * as the starting point for the &quot;target&quot; folder lookup up the
     * hierarchy
     * @return The &quot;target&quot; <U>folder</U> - <code>null</code> if not found
     * @see #detectTargetFolder(File)
     */
    public static final File detectTargetFolder (Class<?> anchor) {
    	return detectTargetFolder(ClassUtil.getClassContainerLocationFile(anchor));
    }
    
    /**
     * @param anchorFile An anchor {@link File) we want to use
     * as the starting point for the &quot;target&quot; folder lookup up the
     * hierarchy
     * @return The &quot;target&quot; <U>folder</U> - <code>null</code> if not found
     */
    public static final File detectTargetFolder (File anchorFile) {
    	for (File	file=anchorFile; file != null; file=file.getParentFile()) {
    		if ("target".equals(file.getName()) && file.isDirectory()) {
    			return file;
    		}
    	}

    	return null;
    }

    public static final String getURLSource (File file) {
    	return getURLSource((file == null) ? null : file.toURI());
    }

    public static final String getURLSource (URI uri) {
    	return getURLSource((uri == null) ? null : uri.toString());
    }

    /**
     * @param url The {@link URL} value - ignored if <code>null</code>
     * @return The URL(s) source path where {@link #JAR_URL_PREFIX} and
     * any sub-resource are stripped
     * @see #getURLSource(String)
     */
    public static final String getURLSource (URL url) {
    	return getURLSource((url == null) ? null : url.toExternalForm());
    }
    
    /**
     * @param externalForm The {@link URL#toExternalForm()} string - ignored if
     * <code>null</code>/empty
     * @return The URL(s) source path where {@link #JAR_URL_PREFIX} and
     * any sub-resource are stripped
     */
    public static final String getURLSource (String externalForm) {
		String	url=externalForm;
    	if (StringUtil.isEmpty(url)) {
    		return url;
    	}

    	url = stripJarURLPrefix(externalForm);
    	if (StringUtil.isEmpty(url)){
    		return url;
    	}
    	
    	int	sepPos=url.indexOf(RESOURCE_SUBPATH_SEPARATOR);
    	if (sepPos < 0) {
    		return StringUtil.adjustURLPathValue(url);
    	} else {
    		return StringUtil.adjustURLPathValue(url.substring(0, sepPos));
    	}
    }

	public static String stripJarURLPrefix(String externalForm) {
		String	url=externalForm;
    	if (StringUtil.isEmpty(url)) {
    		return url;
    	}

    	if (url.startsWith(JAR_URL_PREFIX)) {
    		return url.substring(JAR_URL_PREFIX.length());
    	}    	
    	
    	return url;
	}
	
	public static String getRelativeResourcePath(String url){
		if (StringUtil.isEmpty(url)){
			return url;
		}

		int	sepPos=url.indexOf(FileUtil.RESOURCE_SUBPATH_SEPARATOR);
		String  prefix=(sepPos > 0) ? url.substring(0, sepPos) : url;
		String  remainder=(sepPos > 0) ? url.substring(sepPos) : null;
		int     lastPos=prefix.lastIndexOf('/');
		if (lastPos < 0) {
			return url;
		}

		prefix = prefix.substring(lastPos + 1);
		if (StringUtil.isEmpty(remainder)) {
			return prefix;
		} else {
			return prefix + remainder;
		}
	}
	
}
