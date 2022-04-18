package org.main.gitnex.activities;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mian.gitnex.activities.MainActivity;

/**
 * @author qwerty287
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

	@Test
	public void activityTest() {

		ActivityScenario<MainActivity> a = ActivityScenario.launch(MainActivity.class);
		a.close();
	}

}
