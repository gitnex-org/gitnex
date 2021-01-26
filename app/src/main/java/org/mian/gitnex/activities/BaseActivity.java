package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.acra.ACRA;
import org.acra.BuildConfig;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraNotification;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.LimiterConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.data.StringFormat;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.FontsOverride;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.notifications.NotificationsMaster;
import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.STACK_TRACE;

/**
 * Author M M Arif
 */

@SuppressLint("NonConstantResourceId")
@AcraNotification(resIcon = R.drawable.gitnex_transparent,
		resTitle = R.string.crashTitle,
		resChannelName = R.string.setCrashReports,
		resText = R.string.crashMessage)
@AcraCore(reportContent = { ANDROID_VERSION, PHONE_MODEL, STACK_TRACE })

public abstract class BaseActivity extends AppCompatActivity {

	protected TinyDB tinyDB;

	protected Context ctx = this;
	protected Context appCtx;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		this.appCtx = getApplicationContext();
		this.tinyDB = TinyDB.getInstance(appCtx);

		switch(tinyDB.getInt("themeId")) {

			case 1:

				tinyDB.putString("currentTheme", "light");
				setTheme(R.style.AppThemeLight);
				break;
			case 2:

				if(TimeHelper.timeBetweenHours(18, 6)) { // 6pm to 6am

					tinyDB.putString("currentTheme", "dark");
					setTheme(R.style.AppTheme);
				}
				else {

					tinyDB.putString("currentTheme", "light");
					setTheme(R.style.AppThemeLight);
				}
				break;
			case 3:

				tinyDB.putString("currentTheme", "light");
				setTheme(R.style.AppThemeRetro);
				break;
			case 4:
				if(TimeHelper.timeBetweenHours(18, 6)) { // 6pm to 6am

					tinyDB.putString("currentTheme", "dark");
					setTheme(R.style.AppTheme);
				}
				else {

					tinyDB.putString("currentTheme", "light");
					setTheme(R.style.AppThemeRetro);
				}
				break;
			case 5:

				tinyDB.putString("currentTheme", "dark");
				setTheme(R.style.AppThemePitchBlack);
				break;
			default:

				tinyDB.putString("currentTheme", "dark");
				setTheme(R.style.AppTheme);

		}

		String appLocale = tinyDB.getString("locale");
		AppUtil.setAppLocale(getResources(), appLocale);

		super.onCreate(savedInstanceState);

		// FIXME Performance nightmare
		switch(tinyDB.getInt("customFontId", -1)) {

			case 0:

				FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/roboto.ttf");
				FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/roboto.ttf");
				FontsOverride.setDefaultFont(this, "SERIF", "fonts/roboto.ttf");
				FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/roboto.ttf");
				break;
			case 2:

				FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/sourcecodeproregular.ttf");
				FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/sourcecodeproregular.ttf");
				FontsOverride.setDefaultFont(this, "SERIF", "fonts/sourcecodeproregular.ttf");
				FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/sourcecodeproregular.ttf");
				break;
			default:

				FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/manroperegular.ttf");
				FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/manroperegular.ttf");
				FontsOverride.setDefaultFont(this, "SERIF", "fonts/manroperegular.ttf");
				FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/manroperegular.ttf");
		}

		if(tinyDB.getInt("pollingDelayMinutes", 0) <= 0) {

			tinyDB.putInt("pollingDelayMinutes", StaticGlobalVariables.defaultPollingDelay);
        }

		// FIXME Performance nightmare
        NotificationsMaster.hireWorker(appCtx);

        // enabling counter badges by default
        if(tinyDB.getString("enableCounterBadgesInit").isEmpty()) {

            tinyDB.putBoolean("enableCounterBadges", true);
            tinyDB.putString("enableCounterBadgesInit", "yes");
        }

		// enable crash reports by default
		if(tinyDB.getString("crashReportingEnabledInit").isEmpty()) {

			tinyDB.putBoolean("crashReportingEnabled", true);
			tinyDB.putString("crashReportingEnabledInit", "yes");
		}

		// default cache setter
		if(tinyDB.getString("cacheSizeStr").isEmpty()) {

			tinyDB.putString("cacheSizeStr", getResources().getString(R.string.cacheSizeDataSelectionSelectedText));
		}
		if(tinyDB.getString("cacheSizeImagesStr").isEmpty()) {

			tinyDB.putString("cacheSizeImagesStr", getResources().getString(R.string.cacheSizeImagesSelectionSelectedText));
		}

		// enable comment drafts by default
		if(tinyDB.getString("draftsCommentsDeletionEnabledInit").isEmpty()) {

			tinyDB.putBoolean("draftsCommentsDeletionEnabled", true);
			tinyDB.putString("draftsCommentsDeletionEnabledInit", "yes");
		}

		// FIXME Performance nightmare
		if (tinyDB.getBoolean("crashReportingEnabled")) {

			CoreConfigurationBuilder ACRABuilder = new CoreConfigurationBuilder(this);
			ACRABuilder.setBuildConfigClass(BuildConfig.class).setReportFormat(StringFormat.KEY_VALUE_LIST);
			ACRABuilder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class).setReportAsFile(true).setMailTo(getResources().getString(R.string.appEmail)).setSubject(getResources().getString(R.string.crashReportEmailSubject, AppUtil.getAppBuildNo(getApplicationContext()))).setEnabled(true);
			ACRABuilder.getPluginConfigurationBuilder(LimiterConfigurationBuilder.class).setEnabled(true);
			ACRA.init(getApplication(), ACRABuilder);
		}
	}

}


