package org.mian.gitnex.helpers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.ColorInt;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class AppUtil {

	public static String strReplace(String str, String original, String replace) {

		return str.replace(original, replace);
	}

	public static boolean hasNetworkConnection(Context context) {

		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		assert cm != null;
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for(NetworkInfo ni : netInfo) {
			if(ni.getTypeName().equalsIgnoreCase("WIFI")) {
				if(ni.isConnected()) {
					haveConnectedWifi = true;
				}
			}
			if(ni.getTypeName().equalsIgnoreCase("MOBILE")) {
				if(ni.isConnected()) {
					haveConnectedMobile = true;
				}
			}
		}
		return haveConnectedWifi || haveConnectedMobile;
	}

	public static int getAppBuildNo(Context context) {

		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
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

	public int charactersLength(String str) {

		return str.length();
	}

	public Boolean checkStringsWithAlphaNumeric(String str) { // [a-zA-Z0-9]
		return str.matches("^[\\w]+$");
	}

	public Boolean checkStrings(String str) { // [a-zA-Z0-9-_. ]
		return str.matches("^[\\w .-]+$");
	}

	public Boolean checkStringsWithAlphaNumericDashDotUnderscore(String str) { // [a-zA-Z0-9-_]
		return str.matches("^[\\w.-]+$");
	}

	public Boolean checkStringsWithDash(String str) { // [a-zA-Z0-9-_. ]
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

		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", locale).format(date);

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

	public String encodeBase64(String str) {

		String base64Str = str;
		if(!str.equals("")) {
			byte[] data = str.getBytes(StandardCharsets.UTF_8);
			base64Str = Base64.encodeToString(data, Base64.DEFAULT);
		}

		return base64Str;

	}

	public String decodeBase64(String str) {

		String base64Str = str;
		if(!str.equals("")) {
			byte[] data = Base64.decode(base64Str, Base64.DEFAULT);
			base64Str = new String(data, StandardCharsets.UTF_8);
		}

		return base64Str;

	}

	public Boolean sourceCodeExtension(String ext) {

		String[] extValues = new String[]{"md", "json", "java", "go", "php", "c", "cc", "cpp", "h", "cxx", "cyc", "m", "cs", "bash", "sh", "bsh", "cv", "python", "perl", "pm", "rb", "ruby", "javascript", "coffee", "rc", "rs", "rust", "basic", "clj", "css", "dart", "lisp", "erl", "hs", "lsp", "rkt", "ss", "llvm", "ll", "lua", "matlab", "pascal", "r", "scala", "sql", "latex", "tex", "vb", "vbs", "vhd", "tcl", "wiki.meta", "yaml", "yml", "markdown", "xml", "proto", "regex", "py", "pl", "js", "html", "htm", "volt", "ini", "htaccess", "conf", "gitignore", "gradle", "txt", "properties", "bat", "twig", "cvs", "cmake", "in", "info", "spec", "m4", "am", "dist", "pam"};

		return Arrays.asList(extValues).contains(ext);

	}

	public Boolean pdfExtension(String ext) {

		String[] extValues = new String[]{"pdf"};

		return Arrays.asList(extValues).contains(ext);

	}

	public Boolean imageExtension(String ext) {

		String[] extValues = new String[]{"jpg", "jpeg", "gif", "png", "ico"};

		return Arrays.asList(extValues).contains(ext);

	}

	public Boolean excludeFilesInFileViewerExtension(String ext) {

		String[] extValues = new String[]{"doc", "docx", "ppt", "pptx", "xls", "xlsx", "xlsm", "odt", "ott", "odf", "ods", "ots", "exe", "jar", "odg", "otg", "odp", "otp", "bin", "dmg", "psd", "xcf"};

		return Arrays.asList(extValues).contains(ext);

	}

	public String getLastCharactersOfWord(String str, int count) {

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

}
