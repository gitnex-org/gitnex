package org.main.gitnex.helpers;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mian.gitnex.helpers.AppUtil;
import static org.junit.Assert.*;

/**
 * @author qwerty287
 */
@RunWith(AndroidJUnit4.class)
public class AppUtilTest {

	@Test
	public void getAppBuildNoTest() {

		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		assertEquals(425, AppUtil.getAppBuildNo(context));
	}

	@Test
	public void getAppVersionTest() {

		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		assertEquals("4.3.0", AppUtil.getAppVersion(context));
	}

	@Test
	public void isProTest() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		assertFalse(AppUtil.isPro(context)); // tests use a custom package -> always false
	}

	@Test
	public void encodeBase64Test() {
		assertEquals("SGVsbG8gV29ybGQh\n", AppUtil.encodeBase64("Hello World!"));
		assertEquals("R2l0TmV4\n", AppUtil.encodeBase64("GitNex"));
		assertEquals("Q29kZWJlcmc=\n", AppUtil.encodeBase64("Codeberg"));
		assertEquals("R2l0ZWE=\n", AppUtil.encodeBase64("Gitea"));

		assertNotEquals("123\n", AppUtil.encodeBase64("Hello World!"));
		assertNotEquals("234\n", AppUtil.encodeBase64("GitNex"));
		assertNotEquals("345\n", AppUtil.encodeBase64("Codeberg"));
		assertNotEquals("456\n", AppUtil.encodeBase64("Gitea"));
	}

	@Test
	public void decodeBase64Test() {
		assertEquals("Hello World!", AppUtil.decodeBase64("SGVsbG8gV29ybGQh\n"));
		assertEquals("GitNex", AppUtil.decodeBase64("R2l0TmV4\n"));
		assertEquals("Codeberg", AppUtil.decodeBase64("Q29kZWJlcmc=\n"));
		assertEquals("Gitea", AppUtil.decodeBase64("R2l0ZWE=\n"));

		assertNotEquals("helloworld", AppUtil.decodeBase64("SGVsbG8gV29ybGQh\n"));
		assertNotEquals("gitnex", AppUtil.decodeBase64("R2l0TmV4\n"));
		assertNotEquals("123codeberg", AppUtil.decodeBase64("Q29kZWJlcmc=\n"));
		assertNotEquals("gitea123", AppUtil.decodeBase64("R2l0ZWE=\n"));
	}

	@Test
	public void setMultiVisibilityTest() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		View v1 = new View(context);
		View v2 = new View(context);
		View v3 = new View(context);
		View v4 = new View(context);

		AppUtil.setMultiVisibility(View.GONE, v1, v2, v3, v4);
		assertEquals(View.GONE, v1.getVisibility());
		assertEquals(View.GONE, v2.getVisibility());
		assertEquals(View.GONE, v3.getVisibility());
		assertEquals(View.GONE, v4.getVisibility());

		AppUtil.setMultiVisibility(View.VISIBLE, v2);
		assertEquals(View.GONE, v1.getVisibility());
		assertEquals(View.VISIBLE, v2.getVisibility());
		assertEquals(View.GONE, v3.getVisibility());
		assertEquals(View.GONE, v4.getVisibility());

		AppUtil.setMultiVisibility(View.INVISIBLE, v4);
		assertEquals(View.GONE, v1.getVisibility());
		assertEquals(View.VISIBLE, v2.getVisibility());
		assertEquals(View.GONE, v3.getVisibility());
		assertEquals(View.INVISIBLE, v4.getVisibility());
	}

	@Test
	public void getUriFromGitUrlTest() {
		assertEquals("https://git@codeberg.org/gitnex/GitNex", AppUtil.getUriFromGitUrl("ssh://git@codeberg.org:gitnex/GitNex").toString());
		assertEquals("https://codeberg.org/gitnex/GitNex", AppUtil.getUriFromGitUrl("codeberg.org:gitnex/GitNex").toString());
		assertEquals("ssh://git@codeberg.org/gitnex/GitNex", AppUtil.getUriFromGitUrl("ssh://git@codeberg.org/gitnex/GitNex").toString());
		assertEquals("https://git@codeberg.org/gitnex/GitNex.git", AppUtil.getUriFromGitUrl("ssh://git@codeberg.org:gitnex/GitNex.git").toString());
		assertEquals("https://codeberg.org/gitnex/GitNex.git", AppUtil.getUriFromGitUrl("codeberg.org:gitnex/GitNex.git").toString());
		assertEquals("https://codeberg.org/gitnex/GitNex.git", AppUtil.getUriFromGitUrl("https://codeberg.org/gitnex/GitNex.git").toString());
		assertEquals("https://gitnex.com", AppUtil.getUriFromGitUrl("https://gitnex.com").toString());
		assertEquals("https://gitnex.com:3000", AppUtil.getUriFromGitUrl("https://gitnex.com:3000").toString());
	}

	@Test
	public void changeSchemeTest() {
		assertEquals("https://codeberg.org/gitnex/GitNex", AppUtil.changeScheme(Uri.parse("ssh://codeberg.org/gitnex/GitNex"), "https").toString());
		assertEquals("https://gitnex.com", AppUtil.changeScheme(Uri.parse("http://gitnex.com"), "https").toString());
		assertEquals("ssh://codeberg.org/gitnex/GitNex", AppUtil.changeScheme(Uri.parse("http://codeberg.org/gitnex/GitNex"), "ssh").toString());
	}
}

