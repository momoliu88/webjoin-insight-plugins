package com.ebupt.webjoin.insight;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class PropertiesReader {
	private static Properties props = null;
	private static final Logger log = Logger.getLogger(PropertiesReader.class.getName());

	private static void loadProps()
	{
		try {
			props  =new Properties();
			props.load(new FileInputStream(new StringBuffer(System.getProperty("catalina.base")
					+ File.separator + "conf" + File.separator +"insight.properties").toString()));
		} catch (FileNotFoundException e) {
			log.warning("insight.properties is missing!");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static Properties getProps()
	{
		if(null == props)
			loadProps();
		return props;
	}
	
}
