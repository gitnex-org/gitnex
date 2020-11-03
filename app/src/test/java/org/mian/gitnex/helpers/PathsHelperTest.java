package org.mian.gitnex.helpers;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Author opyale
 */

public class PathsHelperTest {

	@Test
	public void testJoin() {

		assertEquals(PathsHelper.join("test", "/test", "test/", "/test/"), "/test/test/test/test/");
		assertEquals(PathsHelper.join("test", "test", "test", "test"), "/test/test/test/test/");
		assertEquals(PathsHelper.join("/test", "/test", "/test", "/test"), "/test/test/test/test/");
		assertEquals(PathsHelper.join("/test/", "/test/", "test/", "/test/"), "/test/test/test/test/");
		assertEquals(PathsHelper.join("test", "test", "/test", "/test"), "/test/test/test/test/");
		assertEquals(PathsHelper.join("test/", "test", "/test", "/test"), "/test/test/test/test/");

		assertEquals(PathsHelper.join("test/test/test/test"), "/test/test/test/test/");
		assertEquals(PathsHelper.join("/test/test/test/test"), "/test/test/test/test/");
		assertEquals(PathsHelper.join("test/test/test/test/"), "/test/test/test/test/");

		assertEquals(PathsHelper.join("test"), "/test/");
		assertEquals(PathsHelper.join("test/"), "/test/");
		assertEquals(PathsHelper.join("/test/"), "/test/");
		assertEquals(PathsHelper.join("/test"), "/test/");

	}

}

