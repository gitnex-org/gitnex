package org.mian.gitnex.core;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import org.acra.ACRA;
import org.acra.BuildConfig;
import org.acra.ReportField;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraNotification;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.LimiterConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.data.StringFormat;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.FontsOverride;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.notifications.Notifications;

/**
 * @author opyale
 */

@SuppressLint("NonConstantResourceId")
@AcraNotification(resIcon = R.drawable.gitnex_transparent,
	resTitle = R.string.crashTitle,
	resChannelName = R.string.setCrashReports,
	resText = R.string.crashMessage)
@AcraCore(reportContent = {
	ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL,
	ReportField.STACK_TRACE, ReportField.AVAILABLE_MEM_SIZE, ReportField.BRAND })

public class MainApplication extends Application {

	private Context appCtx;
	private TinyDB tinyDB;

	@Override
	public void onCreate() {

		super.onCreate();

		appCtx = getApplicationContext();
		tinyDB = TinyDB.getInstance(appCtx);

		setDefaults();

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

		Notifications.createChannels(appCtx);
	}

	@Override
	protected void attachBaseContext(Context context) {
		super.attachBaseContext(context);

		tinyDB = TinyDB.getInstance(context);

		if(tinyDB.getBoolean("crashReportingEnabled")) {

			CoreConfigurationBuilder ACRABuilder = new CoreConfigurationBuilder(this);

			ACRABuilder.setBuildConfigClass(BuildConfig.class).setReportFormat(StringFormat.KEY_VALUE_LIST);
			ACRABuilder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class).setReportAsFile(true).setMailTo(getResources().getString(R.string.appEmail)).setSubject(getResources().getString(R.string.crashReportEmailSubject, AppUtil
				.getAppBuildNo(context))).setEnabled(true);
			ACRABuilder.getPluginConfigurationBuilder(LimiterConfigurationBuilder.class).setEnabled(true);

			ACRA.init(this, ACRABuilder);
		}
	}

	private void setDefaults() {

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

		// setting default polling delay
		if(tinyDB.getInt("pollingDelayMinutes", 0) <= 0) {

			tinyDB.putInt("pollingDelayMinutes", Constants.defaultPollingDelay);
		}

		// disable biometric by default
		if(tinyDB.getString("biometricStatusInit").isEmpty()) {

			tinyDB.putBoolean("biometricStatus", false);
			tinyDB.putString("biometricStatusInit", "yes");
		}
	}
}
