package test.com.ebupt.webjoin.insight.intercept.plugin;

import java.util.Properties;
 

import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;

import junit.framework.TestCase;

public class CollectionSettingsRegistryTest extends TestCase {
	CollectionSettingsRegistry register = CollectionSettingsRegistry.getInstance();
	protected void setUp() throws Exception {
		super.setUp();
 	}
	public void testConfigure() {
		Properties props = new Properties();
		props.put("insight.collection.plugin.jdbc", "123");
		props.put("insight.collection.plugin.socket", "false");
		CollectionSettingName setting = new CollectionSettingName("jdbc","plugin");
		CollectionSettingName setting1 = new CollectionSettingName("socket","plugin");
		register.configure(props);
		 
		assertEquals(123,register.get(setting));
		assertEquals(false,register.get(setting1));

	}

	public void testGetTyped() {
		assertEquals(false,CollectionSettingsRegistry.getTyped("false"));
	} 

}
