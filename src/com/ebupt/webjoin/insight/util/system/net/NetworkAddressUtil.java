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

package com.ebupt.webjoin.insight.util.system.net;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.ReflectionUtils;
import com.ebupt.webjoin.insight.util.StringFormatterUtils;
import com.ebupt.webjoin.insight.util.StringUtil;

 

/**
 * Utility to retrieve NetworkAddress information
 */
public final class NetworkAddressUtil {
    private static final Method	macAddressMethod;
    static {
    	// exists only in JDK 6
    	macAddressMethod = ReflectionUtils.findMethod(NetworkInterface.class, "getHardwareAddress");
    }

	private NetworkAddressUtil () {
        throw new UnsupportedOperationException("No instance");
    }

    /**
     * Returns the most accessible address, without regards to
     * link local or loopback address restrictions.
     */
    public static InetAddress getDefaultNetworkAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) { // swallow, but warn since this is strange
            Logger  logger=Logger.getLogger(NetworkAddressUtil.class.getName());
            logger.warning("getDefaultNetworkAddress(" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Uses TCP to connect to the given host and retrieve the local address used for the
     * connection.
     */
    public static InetAddress getExternalAddressThroughTCPConnect(String target, int port) {
        try {
            InetAddress remote = InetAddress.getByName(target);
            Socket ds = new Socket(remote, port);
            try {
                return ds.getLocalAddress();
            } catch (Exception e) {
                // ignored
            } finally {
                ds.close();
            }
        } catch (IOException e) {
            // ignored
        }

        return null;
    }

    /**
     * Uses UDP to connect to the given host and retrieve the local address used for the
     * connection.
     */
    public static InetAddress getExternalAddressThroughUDPConnect(String target, int port) {
        try {
            DatagramSocket ds = new DatagramSocket();
            try {
                InetAddress remote = InetAddress.getByName(target);
                ds.connect(remote, port);
                return ds.getLocalAddress();
            } catch (UnknownHostException e ) {
                // ignored
            } finally {
                ds.close();
            }
        } catch (SocketException e) {
            // ignored
        }

        return null;
    }

    /**
     * Returns the first external network address assigned to this
     * machine or null if one is not found.
     * @return Inet4Address associated with an external interface
     * DevNote:  We actually return InetAddress here, as Inet4Addresses are final and cannot be mocked.
     */
    public static InetAddress getFirstExternalNetwork4Address() {
        List<? extends InetAddress> addresses = getExternalNetwork4Addresses();
        if (ListUtil.size(addresses) > 0)
            return addresses.get(0);
        else
            return null;
    }

    /**
     * Returns a list of local network addresses which are not multicast or localhost
     */
    public static List<InetAddress> getExternalNetwork4Addresses() {
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        try {
            for (Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                 (nets != null) && nets.hasMoreElements(); ) {
                NetworkInterface netint=nets.nextElement();
                for (Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                     (inetAddresses != null) && inetAddresses.hasMoreElements(); ) {
                    InetAddress inetAddress=inetAddresses.nextElement();
                    if (inetAddress.isLinkLocalAddress()) {
                        continue;
                    }
                    if (inetAddress.isMulticastAddress()) {
                        continue;
                    }
                    if (inetAddress.isLoopbackAddress()) {
                        continue;
                    }
                    if (inetAddress instanceof Inet4Address) {
                        addresses.add(inetAddress);
                    }
                }
            }
        } catch (SocketException e) {
            // swallow
        }

        return addresses;
    }

    public static boolean isLoopback(String ip) {
        try {
            return InetAddress.getByName(ip).isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * Extract the MAC address of an IP and write it in the following format: 12-34-AA-BB-CC-DD
     * @param ip IP address - ignored if <code>null</code>/empty
     * @return The formatted MAC address - <code>null</code> if unable to extract
     * it - e.g., {@link NetworkInterface} <code>getHardwareAddress</code> was
     * was introduced in JDK 1.6, so if we are running on a JVM below that
     * version, then we cannot extract the MAC address (though this is <U>not</U>
     * the only reason why a MAC might not be available)
     */
	public static String getMac(String ip) {
    	// since NetworkInterface.getHardwareAddress was introduced in JDK 1.6, we need to call it by reflection
		if ((macAddressMethod == null) || StringUtil.isEmpty(ip)) {
			return null;
		}

		try {
			NetworkInterface nif=NetworkInterface.getByInetAddress(InetAddress.getByName(ip));
			byte[] 			 mac=getMac(nif);
			return getMac(mac);
		} catch (Exception ex) {
			return null;
		}
	}

	public static byte[] getMac (NetworkInterface nif) {
    	// since NetworkInterface.getHardwareAddress was introduced in JDK 1.6, we need to call it by reflection
		if ((macAddressMethod == null) || (nif == null)) {
			return null;
		}

		try {
			return (byte[]) macAddressMethod.invoke(nif);
		} catch(Exception e) {
			return null;
		}
	}

	/**
	 * Formats a MAC address
	 * @param mac The MAC address bytes - if <code>null</code>/empty then ignored
	 * @return The formatted address - <code>null</code> if no original data
	 */
	public static String getMac (byte ... mac) {
		/*
		 * NOTE: even though the javadoc says that null should be returned,
		 * for localhost sometimes we get empty array
		 */
		if (ArrayUtil.length(mac) <= 0) {
			return null;
		}

		StringBuilder address=new StringBuilder(mac.length * 3);
		for (byte macValue : mac) {  
			if (address.length() > 0) {
				address.append('-');   // separate from previous value
			}

			try {
				StringFormatterUtils.appendHex(address, macValue, true);
			} catch(IOException e) {	// unexpected since StringBuilder does not throw IOException(s)
				throw new RuntimeException(e);
			}
		}

		return address.toString();
	}
}
