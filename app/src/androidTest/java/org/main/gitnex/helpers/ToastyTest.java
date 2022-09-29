package org.main.gitnex.helpers;

import android.content.Context;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mian.gitnex.helpers.Toasty;

/**
 * Class test if Toasts are working, no assertions, just crash tests.
 *
 * @author qwerty287
 */
@RunWith(AndroidJUnit4.class)
public class ToastyTest {

	@BeforeClass
	public static void prepare() {
		Looper.prepare();
	}

	@Test
	public void infoTest() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		Toasty.info(context, "GitNex info test");
	}

	@Test
	public void warningTest() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		Toasty.warning(context, "GitNex warning test");
	}

	@Test
	public void errorTest() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		Toasty.error(context, "GitNex error test");
	}

	@Test
	public void successTest() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		Toasty.success(context, "GitNex success test");
	}
}
