package org.mian.gitnex.helpers;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Author 6543
 */

public class VersionTest {

	@Test
	public void equal() {

		assertTrue(new Version("1.12.0").equal("1.12.0"));
		assertTrue(new Version("1.12.0").equal(new Version("1.12.0")));
		assertTrue(new Version("1.12.0").equal("1.12"));
		assertTrue(new Version("1.12.0").equal("1.12.0+dev-211-g316db0fe7"));
		assertTrue(new Version("1.12.0").equal("v1.12"));
		assertTrue(new Version("v1.12.0").equal("1.12.0"));
		assertTrue(new Version("0").equal("0"));

		assertFalse(new Version("1.12.1").equal("1.12.0+dev-211-g316db0fe7"));
		assertFalse(new Version("v1.12.0").equal("1.10.0"));
		assertFalse(new Version("2.12.0").equal("v1.12"));
		assertFalse(new Version("1.12").equal("2"));
		assertFalse(new Version("2").equal("1"));
		assertFalse(new Version("1.2").equal("2.1"));
		assertFalse(new Version("2.2").equal("2.1.120"));
		assertFalse(new Version("1.12.3").equal("1.13.0+dev-307-g633f52c22"));

	}

	@Test
	public void less() {

		assertTrue(new Version("1.11.0").less("1.12"));
		assertTrue(new Version("v1.11").less("1.12.0+dev-211-g316db0fe7"));
		assertTrue(new Version("1.12.0").less("v2"));
		assertTrue(new Version("v1.12.0").less("1.12.1-wowowow"));
		assertTrue(new Version("1.2.3").less("1.2.4"));
		assertTrue(new Version("1.2.4").less("1.3.1"));
		assertTrue(new Version("1.2").less("2.1"));
		assertTrue(new Version("1.12.3").less("1.13.0+dev-307-g633f52c22"));

		assertFalse(new Version("1").less("1.1.10"));
		assertFalse(new Version("1.12.1").less("1.12.0+dev-211-g316db0fe7"));
		assertFalse(new Version("1.12.0").less("1.12.0"));
		assertFalse(new Version("v1.12.0").less("1.10.0"));
		assertFalse(new Version("2.12.0").less("v1.12"));
		assertFalse(new Version("2").less("1"));
		assertFalse(new Version("2.2").less("2.1.120"));

	}

	@Test
	public void lessOrEqual() {

		assertTrue(new Version("1.11.0").lessOrEqual("1.12"));
		assertTrue(new Version("v1.11").lessOrEqual("1.12.0+dev-211-g316db0fe7"));
		assertTrue(new Version("1.12.0").lessOrEqual("v2"));
		assertTrue(new Version("v1.12.0").lessOrEqual("1.12.1-wowowow"));
		assertTrue(new Version("1.2.3").lessOrEqual("1.2.4"));
		assertTrue(new Version("1").lessOrEqual("1.1.10"));
		assertTrue(new Version("1.12.0").lessOrEqual("1.12.0"));
		assertTrue(new Version("1.12.3").lessOrEqual("1.13.0+dev-307-g633f52c22"));

		assertFalse(new Version("1.12.1").lessOrEqual("1.12.0+dev-211-g316db0fe7"));
		assertFalse(new Version("v1.12.0").lessOrEqual("1.10.0"));
		assertFalse(new Version("2.12.0").lessOrEqual("v1.12"));
		assertFalse(new Version("2").lessOrEqual("1"));
		assertFalse(new Version("2.1").lessOrEqual("1.2"));
		assertFalse(new Version("2.2").lessOrEqual("2.1.120"));

	}


	@Test
	public void higher() {

		assertTrue(new Version("1.12").higher("1.11.0"));
		assertTrue(new Version("1.12.0+dev-211-g316db0fe7").higher("v1.11"));
		assertTrue(new Version("v2").higher("1.12.0"));
		assertTrue(new Version("1.12.1-wowowow").higher("v1.12.0"));
		assertTrue(new Version("1.2.4").higher("1.2.3"));
		assertTrue(new Version("1.13.0+dev-30-gb02d2c377").higher("1.11.4"));
		assertTrue(new Version("2.1").higher("1.2"));
		assertTrue(new Version("1.13.0+dev-307-g633f52c22").higher("1.12.3"));

		assertFalse(new Version("1").higher("1.1.10"));
		assertFalse(new Version("1.12.0+dev-211-g316db0fe7").higher("1.12.1"));
		assertFalse(new Version("1.12.0").higher("1.12.0"));
		assertFalse(new Version("1.10.0").higher("v1.12.0"));
		assertFalse(new Version("v1.12").higher("2.12.0"));
		assertFalse(new Version("1").higher("2"));
		assertFalse(new Version("2.1.120").higher("2.2"));

	}

	@Test
	public void higherOrEqual() {

		assertTrue(new Version("1.12").higherOrEqual("1.11.0"));
		assertTrue(new Version("1.12.0+dev-211-g316db0fe7").higherOrEqual("v1.11"));
		assertTrue(new Version("v2").higherOrEqual("1.12.0"));
		assertTrue(new Version("1.12.1-wowowow").higherOrEqual("v1.12.0"));
		assertTrue(new Version("1.2.4").higherOrEqual("1.2.3"));
		assertTrue(new Version("1").higherOrEqual("1.1.10"));
		assertTrue(new Version("1.12.0").higherOrEqual("1.12.0"));
		assertTrue(new Version("1.13.0+dev-307-g633f52c22").higherOrEqual("1.12.3"));

		assertFalse(new Version("1.12.0+dev-211-g316db0fe7").higherOrEqual("1.12.1"));
		assertFalse(new Version("1.10.0").higherOrEqual("v1.12.0"));
		assertFalse(new Version("v1.12").higherOrEqual("2.12.0"));
		assertFalse(new Version("1").higherOrEqual("2"));
		assertFalse(new Version("1.2").higherOrEqual("2.1"));
		assertFalse(new Version("2.1.120").higherOrEqual("2.2"));

	}

	@Test
	public void valid() {

		assertTrue(Version.valid("1.12"));
		assertTrue(Version.valid("1.12.0+dev-211-g316db0fe7"));
		assertTrue(Version.valid("v2"));
		assertTrue(Version.valid("1.12.1-wowowow"));
		assertTrue(Version.valid("0.2.4"));
		assertTrue(Version.valid("1"));
		assertTrue(Version.valid("1.12.0"));

		assertFalse(Version.valid("fdsa21.22.-"));
		assertFalse(Version.valid("weo2.2.2"));
		assertFalse(Version.valid(""));
		assertFalse(Version.valid(" "));
		assertFalse(Version.valid("\t"));
		assertFalse(Version.valid("abc"));
		assertFalse(Version.valid("version1"));
		assertFalse(Version.valid(null));
	}

}
