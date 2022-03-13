package org.mian.gitnex.helpers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.LoginActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.core.MainApplication;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author M M Arif
 */

public class AppUtil {

	public static void logout(Context ctx) {
		TinyDB tinyDB = TinyDB.getInstance(ctx);

		UserAccountsApi api = BaseApi.getInstance(ctx, UserAccountsApi.class);
		assert api != null;

		api.logout(tinyDB.getInt("currentActiveAccountId"));
		if (api.getCount() >= 1) {
			switchToAccount(ctx, api.loggedInUserAccounts().get(0));
			if(ctx instanceof MainActivity) {
				((Activity) ctx).recreate();
			} else { // if it's not a MainActivity, open MainActivity instead of current one
				((Activity) ctx).finish();
				Intent intent = new Intent(ctx, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				ctx.startActivity(intent);
			}
		} else {
			tinyDB.putInt("currentActiveAccountId", -2);
			((Activity) ctx).finish();
			Intent intent = new Intent(ctx, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			ctx.startActivity(intent);
		}
	}

	public enum FileType { IMAGE, AUDIO, VIDEO, DOCUMENT, TEXT, EXECUTABLE, FONT, UNKNOWN }

	private static final HashMap<String[], FileType> extensions = new HashMap<>();

	// AppUtil should not be instantiated.
	private AppUtil() {}

	static {

		extensions.put(new String[]{"jpg", "jpeg", "gif", "png", "ico", "tif", "tiff", "bmp"}, FileType.IMAGE);
		extensions.put(new String[]{"mp3", "wav", "opus", "flac", "wma", "aac", "m4a", "oga", "mpc", "ogg"}, FileType.AUDIO);
		extensions.put(new String[]{"mp4", "mkv", "avi", "mov", "wmv", "qt", "mts", "m2ts", "webm", "flv", "ogv", "amv", "mpg", "mpeg", "mpv", "m4v", "3gp", "wmv"}, FileType.VIDEO);
		extensions.put(new String[]{"doc", "docx", "ppt", "pptx", "xls", "xlsx", "xlsm", "odt", "ott", "odf", "ods", "ots", "odg", "otg", "odp", "otp", "bin", "psd", "xcf", "pdf"}, FileType.DOCUMENT);
		extensions.put(new String[]{"exe", "msi", "jar", "dmg", "deb", "apk"}, FileType.EXECUTABLE);
		extensions.put(new String[]{"txt", "md", "json", "java", "go", "php", "c", "cc", "cpp", "h", "cxx", "cyc", "m", "cs", "bash", "sh", "bsh", "cv", "python", "perl", "pm", "rb", "ruby", "javascript", "coffee", "rc", "rs", "rust", "basic", "clj", "css", "dart", "lisp", "erl", "hs", "lsp", "rkt", "ss", "llvm", "ll", "lua", "matlab", "pascal", "r", "scala", "sql", "latex", "tex", "vb", "vbs", "vhd", "tcl", "wiki.meta", "yaml", "yml", "markdown", "xml", "proto", "regex", "py", "pl", "js", "html", "htm", "volt", "ini", "htaccess", "conf", "gitignore", "gradle", "txt", "properties", "bat", "twig", "cvs", "cmake", "in", "info", "spec", "m4", "am", "dist", "pam", "hx", "ts", "kt", "kts"}, FileType.TEXT);
		extensions.put(new String[]{"ttf", "otf", "woff", "woff2", "ttc", "eot"}, FileType.FONT);
	}

	public static FileType getFileType(String extension) {

		if(extension != null && !extension.isEmpty()) {
			for(String[] testExtensions : extensions.keySet()) {
				for(String testExtension : testExtensions) {

					if(testExtension.equalsIgnoreCase(extension))
						return extensions.get(testExtensions);
				}
			}
		}

		return FileType.UNKNOWN;

	}

	public static boolean hasNetworkConnection(Context context) {
		return NetworkStatusObserver.getInstance(context).hasNetworkConnection();
	}

	public static void copyProgress(InputStream inputStream, OutputStream outputStream, long totalSize, ProgressListener progressListener) throws IOException {

		byte[] buffer = new byte[4096];
		int read;

		long totalSteps = (long) Math.ceil((double) totalSize / buffer.length);
		long stepsPerPercent = (long) Math.floor((double) totalSteps / 100);

		short percent = 0;
		long stepCount = 0;

		progressListener.onActionStarted();

		while((read = inputStream.read(buffer)) != -1) {

			outputStream.write(buffer, 0, read);
			stepCount++;

			if(stepCount == stepsPerPercent) {
				percent++;
				if(percent <= 100) progressListener.onProgressChanged(percent);
				stepCount = 0;
			}
		}

		if(percent < 100) {
			progressListener.onProgressChanged((short) 100);
		}

		progressListener.onActionFinished();
	}

	public interface ProgressListener {
		default void onActionStarted() {}
		default void onActionFinished() {}

		void onProgressChanged(short progress);
	}

	public static int getAppBuildNo(Context context) {

		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return (int) PackageInfoCompat.getLongVersionCode(packageInfo);
		}
		catch(PackageManager.NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static String getAppVersion(Context context) {

		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		}
		catch(PackageManager.NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static boolean isPro(Context context) {
		return context.getPackageName().equals("org.mian.gitnex.pro");
	}

	public static Boolean checkStringsWithAlphaNumeric(String str) { // [a-zA-Z0-9]
		return str.matches("^[\\w]+$");
	}

	public static Boolean checkStrings(String str) { // [a-zA-Z0-9-_. ]
		return str.matches("^[\\w .-]+$");
	}

	public static Boolean checkStringsWithAlphaNumericDashDotUnderscore(String str) { // [a-zA-Z0-9-_]
		return str.matches("^[\\w.-]+$");
	}

	public static Boolean checkStringsWithDash(String str) { // [a-zA-Z0-9-_. ]
		return str.matches("^[\\w-]+$");
	}

	public static Boolean checkIntegers(String str) {

		return str.matches("\\d+");
	}

	public int getResponseStatusCode(String u) throws Exception {

		URL url = new URL(u);
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		return (http.getResponseCode());

	}

	public static void setAppLocale(Resources resource, String locCode) {

		DisplayMetrics dm = resource.getDisplayMetrics();
		Configuration config = resource.getConfiguration();
		config.setLocale(new Locale(locCode.toLowerCase()));
		resource.updateConfiguration(config, dm);

	}

	public static String getTimestampFromDate(Context context, Date date) {

		TinyDB tinyDB = TinyDB.getInstance(context);
		Locale locale = new Locale(tinyDB.getString("locale"));

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", locale).format(date);
		} else {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale).format(date);
		}

	}

	@ColorInt
	public static int getColorFromAttribute(Context context, int resid) {

		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(resid, typedValue, true);

		return typedValue.data;

	}

	public static String customDateFormat(String customDate) {

		String[] parts = customDate.split("-");
		final String year = parts[0];
		final String month = parts[1];
		final String day = parts[2];

		String sMonth;
		if(Integer.parseInt(month) < 10) {
			sMonth = "0" + month;
		}
		else {
			sMonth = month;
		}

		String sDay;
		if(Integer.parseInt(day) < 10) {
			sDay = "0" + day;
		}
		else {
			sDay = day;
		}

		return year + "-" + sMonth + "-" + sDay;

	}

	public static String customDateCombine(String customDate) {

		final Calendar c = Calendar.getInstance();
		int mHour = c.get(Calendar.HOUR_OF_DAY);
		int mMinute = c.get(Calendar.MINUTE);
		int mSeconds = c.get(Calendar.SECOND);

		String sMin;
		if((mMinute) < 10) {
			sMin = "0" + mMinute;
		}
		else {
			sMin = String.valueOf(mMinute);
		}

		String sSec;
		if((mSeconds) < 10) {
			sSec = "0" + mSeconds;
		}
		else {
			sSec = String.valueOf(mSeconds);
		}

		return (customDate + "T" + mHour + ":" + sMin + ":" + sSec + "Z");

	}

	public static String encodeBase64(String str) {

		String base64Str = str;
		if(!str.equals("")) {
			byte[] data = str.getBytes(StandardCharsets.UTF_8);
			base64Str = Base64.encodeToString(data, Base64.DEFAULT);
		}

		return base64Str;

	}

	public static String decodeBase64(String str) {

		String base64Str = str;
		if(!str.equals("")) {
			byte[] data = Base64.decode(base64Str, Base64.DEFAULT);
			base64Str = new String(data, StandardCharsets.UTF_8);
		}

		return base64Str;

	}

	public static String getLastCharactersOfWord(String str, int count) {
		return str.substring(str.length() - count);

	}

	public static void setMultiVisibility(int visibility, View... views) {
		for(View view : views) {
			view.setVisibility(visibility);
		}
	}

	public static int getPixelsFromDensity(Context context, int dp) {
		return (int) (context.getResources().getDisplayMetrics().density * dp);
	}

	public static int getPixelsFromScaledDensity(Context context, int sp) {
		return (int) (context.getResources().getDisplayMetrics().scaledDensity * sp);
	}

	public static long getLineCount(String s) {

		long lines = 0;

		Pattern pattern = Pattern.compile("(\r\n|\n)");
		Matcher matcher = pattern.matcher(s);

		while(matcher.find())
			lines++;

		// Sometimes there may be text, but no line breaks.
		// This should still count as one line.
		if(s.length() > 0 && lines == 0)
			return 1;

		return lines;

	}

	public static void copyToClipboard(Context ctx, CharSequence data, String message) {

		ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
		assert clipboard != null;

		ClipData clip = ClipData.newPlainText(data, data);
		clipboard.setPrimaryClip(clip);

		Toasty.info(ctx, message);

	}

	public static boolean switchToAccount(Context context, UserAccount userAccount) {
		return ((MainApplication) context.getApplicationContext()).switchToAccount(userAccount, false);
	}

	public static boolean switchToAccount(Context context, UserAccount userAccount, boolean tmp) {
		return ((MainApplication) context.getApplicationContext()).switchToAccount(userAccount, tmp);
	}

	public static void openUrlInBrowser(Context context, String url) {
		TinyDB tinyDB = TinyDB.getInstance(context);

		try {
			if(tinyDB.getBoolean("useCustomTabs")) {
				new CustomTabsIntent
					.Builder()
					.setDefaultColorSchemeParams(
						new CustomTabColorSchemeParams.Builder()
							.setToolbarColor(getColorFromAttribute(context, R.attr.primaryBackgroundColor))
							.setNavigationBarColor(getColorFromAttribute(context, R.attr.primaryBackgroundColor))
							.setSecondaryToolbarColor(R.attr.primaryTextColor)
							.build()
					)
					.build()
					.launchUrl(context, Uri.parse(url));
			} else {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				i.addCategory(Intent.CATEGORY_BROWSABLE);
				context.startActivity(i);
			}
		} catch(ActivityNotFoundException e) {
			Toasty.error(context, context.getString(R.string.browserOpenFailed));
		} catch (Exception e) {
			Toasty.error(context, context.getString(R.string.genericError));
		}
	}

	public static Uri getUriFromGitUrl(String url) {
		Uri uri = Uri.parse(url);
		String host = uri.getHost();
		if(host != null) {
			return uri;
		}
		// must be a Git SSH URL now (old rcp standard)
		return Uri.parse(getUriFromSSHUrl(url));
	}

	public static String getUriFromSSHUrl(String url) {
		String[] urlParts = url.split("://");
		if (urlParts.length > 1) {
			url = urlParts[1];
		}
		return "https://" + url.replace(":", "/");
	}

	public static Uri changeScheme(Uri origin, String scheme) {
		String raw = origin.toString();
		int schemeIndex = raw.indexOf("://");
		if (schemeIndex >= 0) {
			raw = raw.substring(schemeIndex);
		}
		return Uri.parse(scheme+raw);
	}
}
