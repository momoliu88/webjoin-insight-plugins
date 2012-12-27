package test.com.ebupt.webjoin.insight.intercept.plugin;

import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;

import junit.framework.TestCase;

public class CollectionSettingNameTest extends TestCase {
	CollectionSettingName test = new CollectionSettingName("test","plugintest");

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testGetName() {
		assertEquals("test",test.getName());
	}

	public void testGetPlugin() {
		assertEquals("plugintest",test.getPlugin());
	}

	public void testGetDescription() {
		assertEquals("",test.getDescription());
	}

	public void testGetCollectionSettingsKey() {
		assertEquals("insight.collection.plugintest.test",test.getCollectionSettingsKey());
	}

	public static void testGetCollectionSettings() {
		assertEquals(new CollectionSettingName("test","plugintest"),CollectionSettingName.getCollectionSettings("insight.collection.plugintest.test"));
}

}
