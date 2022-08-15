package org.mian.gitnex.helpers;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author qwerty287
 */
public class AppUtilTest {

	@Test
	public void getFileType() {
		assertEquals(AppUtil.FileType.AUDIO, AppUtil.getFileType("mp3"));
		assertEquals(AppUtil.FileType.IMAGE, AppUtil.getFileType("png"));
		assertEquals(AppUtil.FileType.EXECUTABLE, AppUtil.getFileType("deb"));
		assertEquals(AppUtil.FileType.TEXT, AppUtil.getFileType("JSON"));
		assertEquals(AppUtil.FileType.DOCUMENT, AppUtil.getFileType("PDF"));
		assertEquals(AppUtil.FileType.FONT, AppUtil.getFileType("woff2"));
	}

	@Test
	public void checkStringsWithAlphaNumeric() {
		assertTrue(AppUtil.checkStringsWithAlphaNumeric("string"));
		assertTrue(AppUtil.checkStringsWithAlphaNumeric("123"));
		assertFalse(AppUtil.checkStringsWithAlphaNumeric("123 with string"));
		assertFalse(AppUtil.checkStringsWithAlphaNumeric("string 123"));
		assertFalse(AppUtil.checkStringsWithAlphaNumeric("string-123"));
	}

	@Test
	public void checkIntegers() {
		assertFalse(AppUtil.checkIntegers("string"));
		assertTrue(AppUtil.checkIntegers("123"));
		assertFalse(AppUtil.checkIntegers("123 with string"));
		assertFalse(AppUtil.checkIntegers("string 123"));
	}

	@Test
	public void parseSSHUrl() {
		assertEquals("https://git@codeberg.org/gitnex/GitNex", AppUtil.getUriFromSSHUrl("ssh://git@codeberg.org:gitnex/GitNex"));
		assertEquals("https://codeberg.org/gitnex/GitNex", AppUtil.getUriFromSSHUrl("codeberg.org:gitnex/GitNex"));
		assertEquals("https://git@codeberg.org/gitnex/GitNex", AppUtil.getUriFromSSHUrl("ssh://git@codeberg.org/gitnex/GitNex"));
		assertEquals("https://git@codeberg.org/gitnex/GitNex.git", AppUtil.getUriFromSSHUrl("ssh://git@codeberg.org:gitnex/GitNex.git"));
		assertEquals("https://codeberg.org/gitnex/GitNex.git", AppUtil.getUriFromSSHUrl("codeberg.org:gitnex/GitNex.git"));
	}

}
