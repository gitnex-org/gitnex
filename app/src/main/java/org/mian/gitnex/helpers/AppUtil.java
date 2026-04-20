package org.mian.gitnex.helpers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.core.content.res.ResourcesCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.LoginActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.core.MainApplication;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;

/**
 * @author mmarif
 */
public class AppUtil {

	private static final Map<String[], FileType> extensions = new HashMap<>();
	public static Typeface typeface;

	public enum FileType {
		IMAGE,
		AUDIO,
		VIDEO,
		DOCUMENT,
		EXECUTABLE,
		TEXT,
		FONT,
		UNKNOWN,
		KEYSTORE
	}

	static {
		extensions.put(
				new String[] {"jpg", "jpeg", "gif", "png", "ico", "tif", "tiff", "bmp"},
				FileType.IMAGE);
		extensions.put(
				new String[] {
					"mp3", "wav", "opus", "flac", "wma", "aac", "m4a", "oga", "mpc", "ogg"
				},
				FileType.AUDIO);
		extensions.put(
				new String[] {
					"mp4", "mkv", "avi", "mov", "wmv", "qt", "mts", "m2ts", "webm", "flv", "ogv",
					"amv", "mpg", "mpeg", "mpv", "m4v", "3gp", "wmv"
				},
				FileType.VIDEO);
		extensions.put(
				new String[] {
					"doc", "docx", "ppt", "pptx", "xls", "xlsx", "xlsm", "odt", "ott", "odf", "ods",
					"ots", "odg", "otg", "odp", "otp", "bin", "psd", "xcf", "pdf", "csv"
				},
				FileType.DOCUMENT);
		extensions.put(
				new String[] {"exe", "msi", "jar", "dmg", "deb", "apk"}, FileType.EXECUTABLE);
		extensions.put(
				new String[] {
					"txt",
					"md",
					"json",
					"java",
					"go",
					"php",
					"c",
					"cc",
					"cpp",
					"d",
					"h",
					"cxx",
					"cyc",
					"m",
					"cs",
					"bash",
					"sh",
					"bsh",
					"cv",
					"python",
					"perl",
					"pm",
					"rb",
					"ruby",
					"javascript",
					"coffee",
					"rc",
					"rs",
					"rust",
					"basic",
					"clj",
					"css",
					"dart",
					"lisp",
					"erl",
					"hs",
					"lsp",
					"rkt",
					"ss",
					"llvm",
					"ll",
					"lua",
					"matlab",
					"pascal",
					"r",
					"scala",
					"sql",
					"latex",
					"tex",
					"vb",
					"vbs",
					"vhd",
					"tcl",
					"wiki.meta",
					"yaml",
					"yml",
					"markdown",
					"xml",
					"proto",
					"regex",
					"py",
					"pl",
					"js",
					"html",
					"htm",
					"volt",
					"ini",
					"htaccess",
					"conf",
					"gitignore",
					"gradle",
					"txt",
					"properties",
					"bat",
					"twig",
					"cvs",
					"cmake",
					"in",
					"info",
					"spec",
					"m4",
					"am",
					"dist",
					"pam",
					"hx",
					"ts",
					"kt",
					"kts",
					"el",
					"cjs",
					"jenkinsfile",
					"toml",
					"pro",
					"gitattributes",
					"gitmodules",
					"editorconfig",
					"gradlew",
					"mjs",
					"next",
					"nvmrc",
					"lock",
					"vue",
					"zig",
					"zon"
				},
				FileType.TEXT);
		extensions.put(new String[] {"ttf", "otf", "woff", "woff2", "ttc", "eot"}, FileType.FONT);
		extensions.put(new String[] {"jks"}, FileType.KEYSTORE);
	}

	public static Map<String[], FileType> getExtensions() {
		return extensions;
	}

	// AppUtil should not be instantiated.
	private AppUtil() {}

	public static FileType getFileTypeFromFileName(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return FileType.UNKNOWN;
		}

		String ext = MimeTypeMap.getFileExtensionFromUrl(fileName).toLowerCase();
		if (ext.isEmpty()) {
			return FileType.UNKNOWN;
		}

		for (Map.Entry<String[], FileType> entry : extensions.entrySet()) {
			for (String validExt : entry.getKey()) {
				if (validExt.equalsIgnoreCase(ext)) {
					return entry.getValue();
				}
			}
		}

		return FileType.UNKNOWN;
	}

	public static void logout(Context ctx) {
		TinyDB tinyDB = TinyDB.getInstance(ctx);

		UserAccountsApi api = BaseApi.getInstance(ctx, UserAccountsApi.class);
		assert api != null;

		api.logout(tinyDB.getInt("currentActiveAccountId"));
		if (api.getCount() >= 1) {
			switchToAccount(ctx, api.loggedInUserAccounts().get(0));
			if (ctx instanceof MainActivity) {
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

	public static FileType getFileType(String extension) {

		if (extension != null && !extension.isEmpty()) {
			for (String[] testExtensions : extensions.keySet()) {
				for (String testExtension : testExtensions) {

					if (testExtension.equalsIgnoreCase(extension)) {
						return extensions.get(testExtensions);
					}
				}
			}
		}

		return FileType.UNKNOWN;
	}

	public static boolean hasNetworkConnection(Context context) {
		return NetworkStatusObserver.getInstance(context).hasNetworkConnection();
	}

	public static void copyProgress(
			InputStream inputStream,
			OutputStream outputStream,
			long totalSize,
			ProgressListener progressListener)
			throws IOException {

		byte[] buffer = new byte[4096];
		int read;

		long totalSteps = (long) Math.ceil((double) totalSize / buffer.length);
		long stepsPerPercent = (long) Math.floor((double) totalSteps / 100);

		short percent = 0;
		long stepCount = 0;

		progressListener.onActionStarted();

		while ((read = inputStream.read(buffer)) != -1) {

			outputStream.write(buffer, 0, read);
			stepCount++;

			if (stepCount == stepsPerPercent) {
				percent++;
				if (percent <= 100) {
					progressListener.onProgressChanged(percent);
				}
				stepCount = 0;
			}
		}

		if (percent < 100) {
			progressListener.onProgressChanged((short) 100);
		}

		progressListener.onActionFinished();
	}

	public static int getAppBuildNo(Context context) {

		try {
			PackageInfo packageInfo =
					context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return (int) PackageInfoCompat.getLongVersionCode(packageInfo);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static String getAppVersion(Context context) {

		try {
			PackageInfo packageInfo =
					context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static boolean isPro(Context context) {
		return context.getPackageName().equals("org.mian.gitnex.pro");
	}

	public static Boolean checkStringsWithAlphaNumeric(String str) { // [a-zA-Z0-9]
		return str.matches("^\\w+$");
	}

	public static Boolean checkStrings(String str) { // [a-zA-Z0-9-_. ]
		return str.matches("^[\\w .-]+$");
	}

	public static Boolean checkLabel(String str) { // [a-zA-Z0-9-_. /:]
		return str.matches("^[\\w .-/:]+$");
	}

	public static Boolean checkStringsWithAlphaNumericDashDotUnderscore(
			String str) { // [a-zA-Z0-9-_]
		return str.matches("^[\\w.-]+$");
	}

	public static Boolean checkStringsWithDash(String str) { // [a-zA-Z0-9-_. ]
		return str.matches("^[\\w-]+$");
	}

	public static Boolean isValidGitBranchName(String str) {
		if (str == null || str.isEmpty()) return false;

		if (str.contains("..")
				|| str.startsWith("/")
				|| str.endsWith("/")
				|| str.startsWith("-")
				|| str.endsWith(".")) {
			return false;
		}

		return str.matches("^[a-zA-Z0-9\\-_. /]+$");
	}

	public static Boolean checkIntegers(String str) {
		return str.matches("\\d+");
	}

	public static Context setAppLocale(Context context, String locCode) {

		String[] multiCodeLang = locCode.split("-");
		String languageCode = multiCodeLang[0];
		String countryCode = multiCodeLang.length > 1 ? multiCodeLang[1] : "";

		Locale locale = new Locale(languageCode.toLowerCase(), countryCode);
		Locale.setDefault(locale);

		Configuration config = new Configuration();
		config.setLocale(locale);

		return context.createConfigurationContext(config);
	}

	public static String getTimestampFromDate(Context context, Date date) {

		String[] locale_ =
				AppDatabaseSettings.getSettingsValue(context, AppDatabaseSettings.APP_LOCALE_KEY)
						.split("\\|");
		Locale locale = new Locale(locale_[1]);

		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", locale).format(date);
	}

	@ColorInt
	public static int getColorFromAttribute(Context context, @AttrRes int resid) {

		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(resid, typedValue, true);

		return typedValue.data;
	}

	public static String encodeBase64(String str) {

		String base64Str = str;
		if (!str.isEmpty()) {
			byte[] data = str.getBytes(StandardCharsets.UTF_8);
			base64Str = Base64.encodeToString(data, Base64.DEFAULT);
		}

		return base64Str;
	}

	public static String imageEncodeToBase64(Bitmap image) {

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
		byte[] bytes = byteArrayOutputStream.toByteArray();

		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

	public static String decodeBase64(String str) {

		String base64Str = str;
		if (!str.isEmpty()) {
			byte[] data = Base64.decode(base64Str, Base64.DEFAULT);
			base64Str = new String(data, StandardCharsets.UTF_8);
		}

		return base64Str;
	}

	public static void setMultiVisibility(int visibility, View... views) {
		for (View view : views) {
			view.setVisibility(visibility);
		}
	}

	public static int getPixelsFromDensity(Context context, int dp) {
		return (int) (context.getResources().getDisplayMetrics().density * dp);
	}

	public static long getLineCount(String s) {

		if (s.isEmpty()) {
			return 0;
		}

		long lines = 1; // we start counting at 1 because there is always at least one line

		Pattern pattern = Pattern.compile("(\r\n|\n)");
		Matcher matcher = pattern.matcher(s);

		while (matcher.find()) lines++;

		return lines;
	}

	public static void copyToClipboard(Context ctx, CharSequence data, String message) {

		ClipboardManager clipboard =
				(ClipboardManager)
						Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
		assert clipboard != null;

		ClipData clip = ClipData.newPlainText(data, data);
		clipboard.setPrimaryClip(clip);

		Toasty.show(ctx, message);
	}

	public static boolean switchToAccount(Context context, UserAccount userAccount) {
		return ((MainApplication) context.getApplicationContext())
				.switchToAccount(userAccount, false);
	}

	public static void switchToAccount(Context context, UserAccount userAccount, boolean tmp) {
		((MainApplication) context.getApplicationContext()).switchToAccount(userAccount, tmp);
	}

	public static void sharingIntent(Context ctx, String url) {

		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, url);
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, url);
		ctx.startActivity(Intent.createChooser(sharingIntent, url));
	}

	private static Intent wrapBrowserIntent(Context context, Intent intent) {

		final PackageManager pm = context.getPackageManager();
		final List<ResolveInfo> activities =
				pm.queryIntentActivities(
						new Intent(intent)
								.setData(
										Objects.requireNonNull(intent.getData())
												.buildUpon()
												.authority("example.com")
												.scheme("https")
												.build()),
						PackageManager.MATCH_ALL);
		final ArrayList<Intent> chooserIntents = new ArrayList<>();
		final String ourPackageName = context.getPackageName();

		activities.sort(new ResolveInfo.DisplayNameComparator(pm));

		for (ResolveInfo resInfo : activities) {
			ActivityInfo info = resInfo.activityInfo;
			if (!info.enabled || !info.exported) {
				continue;
			}
			if (info.packageName.equals(ourPackageName)) {
				continue;
			}

			Intent targetIntent = new Intent(intent);
			targetIntent.setPackage(info.packageName);
			targetIntent.setDataAndType(intent.getData(), intent.getType());
			chooserIntents.add(targetIntent);
		}

		if (chooserIntents.isEmpty()) {
			return null;
		}

		final Intent lastIntent = chooserIntents.remove(chooserIntents.size() - 1);
		if (chooserIntents.isEmpty()) {
			return lastIntent;
		}

		Intent chooserIntent = Intent.createChooser(lastIntent, null);
		String extraName = Intent.EXTRA_ALTERNATE_INTENTS;
		chooserIntent.putExtra(extraName, chooserIntents.toArray(new Intent[0]));
		return chooserIntent;
	}

	public static void openUrlInBrowser(Context context, String url) {

		Intent i;
		if (Boolean.parseBoolean(
				AppDatabaseSettings.getSettingsValue(
						context, AppDatabaseSettings.APP_CUSTOM_BROWSER_KEY))) {
			i =
					new CustomTabsIntent.Builder()
							.setDefaultColorSchemeParams(
									new CustomTabColorSchemeParams.Builder()
											.setToolbarColor(
													getColorFromAttribute(
															context, R.attr.primaryBackgroundColor))
											.setNavigationBarColor(
													getColorFromAttribute(
															context, R.attr.primaryBackgroundColor))
											.setSecondaryToolbarColor(R.attr.primaryTextColor)
											.build())
							.build()
							.intent;
			i.setData(Uri.parse(url));
		} else {
			i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			i.addCategory(Intent.CATEGORY_BROWSABLE);
		}
		try {
			Intent browserIntent = wrapBrowserIntent(context, i);
			if (browserIntent == null) {
				Toasty.show(context, context.getString(R.string.genericError));
			}
			context.startActivity(browserIntent);
		} catch (ActivityNotFoundException e) {
			Toasty.show(context, context.getString(R.string.browserOpenFailed));
		} catch (Exception e) {
			Toasty.show(context, context.getString(R.string.genericError));
		}
	}

	public static Uri getUriFromGitUrl(String url) {
		Uri uri = Uri.parse(url);
		String host = uri.getHost();
		if (host != null && !host.contains(":")) {
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
		return Uri.parse(scheme + raw);
	}

	public static Typeface getTypeface(Context context) {
		int fontIndex =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								context, AppDatabaseSettings.APP_FONT_KEY));
		try {
			return switch (fontIndex) {
				case 0 -> ResourcesCompat.getFont(context, R.font.roboto);
				case 2 -> ResourcesCompat.getFont(context, R.font.sourcecodeproregular);
				case 3 -> Typeface.DEFAULT;
				default -> ResourcesCompat.getFont(context, R.font.manroperegular);
			};
		} catch (Exception e) {
			return Typeface.DEFAULT;
		}
	}

	/** Pretty number format Example, 1200 = 1.2k */
	public static String numberFormatter(Number number) {

		char[] suffix = {' ', 'k', 'M', 'B', 'T'};
		long numValue = number.longValue();
		int value = (int) Math.floor(Math.log10(numValue));
		int base = value / 3;
		if (value >= 3 && base < suffix.length) {
			return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3))
					+ suffix[base];
		}
		if (base >= suffix.length) {
			return new DecimalFormat("#0").format(numValue / Math.pow(10, base * 2)) + suffix[4];
		} else {
			return new DecimalFormat("#,##0").format(numValue);
		}
	}

	/*
	 * check for ghost/restricted users/profiles
	 */
	public static Boolean checkGhostUsers(String str) {

		ArrayList<String> restrictedUsers = new ArrayList<>();
		restrictedUsers.add("Ghost");
		return restrictedUsers.contains(str);
	}

	public interface ProgressListener {

		default void onActionStarted() {}

		default void onActionFinished() {}

		void onProgressChanged(short progress);
	}

	public static boolean isNightModeThemeDynamic(Context context) {
		int nightModeFlags =
				context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

		return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
	}

	public static int dynamicColorResource(Context context) {

		int resource = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			resource = context.getResources().getColor(android.R.color.system_accent1_900, null);
		}
		return resource;
	}

	public static String inferProvider(String version) {
		if (Version.valid(version)) {
			Version v = new Version(version);
			int majorVersion = v.getValues().isEmpty() ? 0 : v.getValues().get(0);
			return majorVersion == 1 ? "gitea" : "forgejo";
		}
		return "gitea";
	}

	public static void applySheetStyle(@NonNull BottomSheetDialog dialog, boolean isDraggable) {
		View bottomSheet =
				dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
		if (bottomSheet != null) {
			BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
			behavior.setFitToContents(true);
			behavior.setSkipCollapsed(true);
			behavior.setExpandedOffset(0);
			behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
			behavior.setDraggable(isDraggable);
		}
	}

	public static void applyFullScreenSheetStyle(
			@NonNull BottomSheetDialog dialog, boolean isDraggable) {
		View bottomSheet =
				dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
		if (bottomSheet != null) {
			ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
			layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
			bottomSheet.setLayoutParams(layoutParams);

			BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
			behavior.setFitToContents(false);
			behavior.setSkipCollapsed(true);
			behavior.setExpandedOffset(0);
			behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
			behavior.setDraggable(isDraggable);
		}
	}

	public static String parseErrorMessage(String errorBody) {
		if (errorBody == null || errorBody.isEmpty()) {
			return "Unknown error occurred";
		}

		String parsedMessage;
		try {
			JSONObject jsonObject = new JSONObject(errorBody);

			if (jsonObject.has("message")) {
				parsedMessage = jsonObject.getString("message");
			} else if (jsonObject.has("errors")) {
				parsedMessage = jsonObject.get("errors").toString();
			} else {
				parsedMessage = errorBody;
			}
		} catch (JSONException e) {
			parsedMessage = errorBody.length() > 100 ? "Error parsing server response" : errorBody;
		}
		return "API error: " + parsedMessage;
	}

	public static void hideKeyboard(Activity activity) {
		if (activity != null && activity.getCurrentFocus() != null) {
			InputMethodManager imm =
					(InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
		}
	}

	public static boolean isLightColor(int color) {
		double luminance =
				(0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color))
						/ 255;
		return luminance > 0.5;
	}

	public static String getMimeTypeFromUri(Context context, Uri uri) {
		if (uri == null) return null;

		if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
			return context.getContentResolver().getType(uri);
		} else {
			String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
			return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
		}
	}

	public static String getFileNameFromUri(Context context, Uri uri) {
		if (uri == null) return null;

		String fileName = null;
		if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
			try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
				if (cursor != null && cursor.moveToFirst()) {
					int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
					if (nameIndex != -1) {
						fileName = cursor.getString(nameIndex);
					}
				}
			}
		}
		if (fileName == null) {
			fileName = uri.getPath();
			int cut = fileName != null ? fileName.lastIndexOf('/') : -1;
			if (cut != -1) {
				fileName = fileName.substring(cut + 1);
			}
		}
		return fileName;
	}

	public static long getFileSizeFromUri(Context context, Uri uri) {
		if (uri == null) return 0;

		long size = 0;
		if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
			try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
				if (cursor != null && cursor.moveToFirst()) {
					int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
					if (sizeIndex != -1) {
						size = cursor.getLong(sizeIndex);
					}
				}
			}
		}
		return size;
	}

	public static String readInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int len;
		while ((len = is.read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		is.close();
		return baos.toString(StandardCharsets.UTF_8.name());
	}

	public static String encodeUriToBase64(Context context, Uri uri) {
		if (uri == null) return "";

		try {
			InputStream is = context.getContentResolver().openInputStream(uri);
			if (is == null) return "";

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int len;
			while ((len = is.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			is.close();

			return android.util.Base64.encodeToString(
					baos.toByteArray(), android.util.Base64.NO_WRAP);
		} catch (IOException e) {
			return "";
		}
	}
}
