package org.mian.gitnex.helpers;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Author opyale
 */

public class PathsHelperTest {

	@Test
	public void testJoin() {

		assertEquals("/test/test/test/test/", PathsHelper.join("test", "/test", "test/", "/test/"));
		assertEquals("/test/test/test/test/", PathsHelper.join("test", "test", "test", "test"));
		assertEquals("/test/test/test/test/", PathsHelper.join("/test", "/test", "/test", "/test"));
		assertEquals("/test/test/test/test/", PathsHelper.join("/test/", "/test/", "test/", "/test/"));
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

