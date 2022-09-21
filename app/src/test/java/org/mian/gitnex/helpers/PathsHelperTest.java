package org.mian.gitnex.helpers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author opyale
 */
public class PathsHelperTest {

	@Test
	public void testJoin() {

		assertEquals("/test/test/test/test/", PathsHelper.join("test", "/test", "test/", "/test/"));
		assertEquals("/test/test/test/test/", PathsHelper.join("test", "test", "test", "test"));
		assertEquals("/test/test/test/test/", PathsHelper.join("/test", "/test", "/test", "/test"));
		assertEquals(
				"/test/test/test/test/", PathsHelper.join("/test/", "/test/", "test/", "/test/"));
		assertEquals("/test/test/test/test/", PathsHelper.join("test", "test", "/test", "/test"));
		assertEquals("/test/test/test/test/", PathsHelper.join("test/", "test", "/test", "/test"));

		assertEquals("/test/test/test/test/", PathsHelper.join("test/test/test/test"));
		assertEquals("/test/test/test/test/", PathsHelper.join("/test/test/test/test"));
		assertEquals("/test/test/test/test/", PathsHelper.join("test/test/test/test/"));

		assertEquals("/test/", PathsHelper.join("test"));
		assertEquals("/test/", PathsHelper.join("test/"));
		assertEquals("/test/", PathsHelper.join("/test/"));
		assertEquals("/test/", PathsHelper.join("/test"));
	}
}
