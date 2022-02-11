package org.mian.gitnex.helpers;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

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
		assertEquals(AppUtil.checkStringsWithAlphaNumeric("string"), true);
		assertEquals(AppUtil.checkStringsWithAlphaNumeric("123"), true);
		assertEquals(AppUtil.checkStringsWithAlphaNumeric("123 with string"), false);
		assertEquals(AppUtil.checkStringsWithAlphaNumeric("string 123"), false);
		assertEquals(AppUtil.checkStringsWithAlphaNumeric("string-123"), false);
	}

	@Test
	public void checkIntegers() {
		assertEquals(AppUtil.checkIntegers("string"), false);
		assertEquals(AppUtil.checkIntegers("123"), true);
		assertEquals(AppUtil.checkIntegers("123 with string"), false);
		assertEquals(AppUtil.checkIntegers("string 123"), false);
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
