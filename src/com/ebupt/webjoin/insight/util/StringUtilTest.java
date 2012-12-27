package com.ebupt.webjoin.insight.util;


import junit.framework.TestCase;

public class StringUtilTest extends TestCase {
	protected void setUp() throws Exception {
		
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testIsEmptyStringString() {
	}
	public void testRemoveAllCharacterOccurrences()
	{
		String s = "abc/djflj//121/df";
		String result = StringUtil.removeAllCharacterOccurrences(s,'/');
		assertEquals(result,"abcdjflj121df");
		
	}

}
