package org.mian.gitnex.helpers;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vdurmont.emoji.EmojiParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author M M Arif
 */
public class ChangeLog {

	private final Activity changelogActivity;

	public ChangeLog(Activity context) {
		this.changelogActivity = context;
	}

	private static class Release {
		String version;
		int versioncode;
		List<Type> types;

		Release(String version, int versioncode) {
			this.version = version;
			this.versioncode = versioncode;
			this.types = new ArrayList<>();
		}
	}

	private static class Type {
		String name;
		List<String> changes;

		Type(String name) {
			this.name = name;
			this.changes = new ArrayList<>();
		}
	}

	private Release parseLatestRelease(@NonNull XmlResourceParser xml) {

		List<Release> releases = new ArrayList<>();
		Release currentRelease = null;
		Type currentType = null;

		try {
			int eventType = xml.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {

				if (eventType == XmlPullParser.START_TAG) {

					String tagName = xml.getName();
					if (tagName.equals("release")) {
						String version = xml.getAttributeValue(null, "version");
						int versioncode = xml.getAttributeIntValue(null, "versioncode", -1);
						currentRelease = new Release(version, versioncode);
						releases.add(currentRelease);
					} else if (tagName.equals("type") && currentRelease != null) {
						String typeName = xml.getAttributeValue(null, "name");
						currentType = new Type(typeName);
						currentRelease.types.add(currentType);
					} else if (tagName.equals("change") && currentType != null) {
						eventType = xml.next();
						if (eventType == XmlPullParser.TEXT) {
							String changeText = xml.getText().trim();
							if (!changeText.isEmpty()) {
								currentType.changes.add(changeText);
							}
						}
					}
				}
				eventType = xml.next();
			}
		} catch (XmlPullParserException | IOException e) {
			return new Release("Error", -1);
		}

		Release latest = null;
		for (Release r : releases) {
			if (latest == null
					|| (r.versioncode > latest.versioncode && r.versioncode != -1)
					|| (r.versioncode == -1 && latest.versioncode == -1)) {
				latest = r;
			}
		}
		return latest != null ? latest : new Release("No releases found", -1);
	}

	private LinearLayout buildChangelogView(Release release) {

		LinearLayout layout = new LinearLayout(changelogActivity);
		layout.setOrientation(LinearLayout.VERTICAL);
		int paddingHorizontal =
				(int)
						TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP,
								24,
								changelogActivity.getResources().getDisplayMetrics());
		int paddingVertical =
				(int)
						TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP,
								16,
								changelogActivity.getResources().getDisplayMetrics());
		layout.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);

		TextView versionView = new TextView(changelogActivity);
		versionView.setText(
				HtmlCompat.fromHtml(
						"<b>" + release.version + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY));
		versionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		LinearLayout.LayoutParams versionParams =
				new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		versionParams.setMargins(0, 0, 0, 16);
		versionView.setLayoutParams(versionParams);
		layout.addView(versionView);

		for (Type type : release.types) {

			TextView typeView = new TextView(changelogActivity);
			typeView.setText(
					HtmlCompat.fromHtml(
							"<b>" + EmojiParser.parseToUnicode(type.name) + "</b>",
							HtmlCompat.FROM_HTML_MODE_LEGACY));
			typeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			LinearLayout.LayoutParams typeParams =
					new LinearLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
			typeParams.setMargins(0, 48, 0, 16);
			typeView.setLayoutParams(typeParams);
			layout.addView(typeView);

			for (String change : type.changes) {
				if (!change.isEmpty()) {
					TextView changeView = new TextView(changelogActivity);
					changeView.setText(
							HtmlCompat.fromHtml("• " + change, HtmlCompat.FROM_HTML_MODE_LEGACY));
					changeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					LinearLayout.LayoutParams changeParams =
							new LinearLayout.LayoutParams(
									ViewGroup.LayoutParams.WRAP_CONTENT,
									ViewGroup.LayoutParams.WRAP_CONTENT);
					changeParams.setMargins(32, 0, 0, 8);
					changeView.setLayoutParams(changeParams);
					layout.addView(changeView);
				}
			}
		}

		return layout;
	}

	public void showDialog() {

		Resources res = changelogActivity.getResources();
		Release latestRelease;

		try (XmlResourceParser xml = res.getXml(R.xml.changelog)) {
			latestRelease = parseLatestRelease(xml);
		} catch (Exception e) {
			latestRelease = new Release("Error loading changelog", -1);
		}

		ScrollView scrollView = new ScrollView(changelogActivity);
		scrollView.addView(buildChangelogView(latestRelease));

		MaterialAlertDialogBuilder dialogBuilder =
				new MaterialAlertDialogBuilder(changelogActivity)
						.setTitle(R.string.changelogTitle)
						.setView(scrollView)
						.setCancelable(false)
						.setNeutralButton(R.string.close, null);

		AlertDialog dialog = dialogBuilder.create();
		dialog.show();
		TextView titleView = dialog.findViewById(com.google.android.material.R.id.alertTitle);
		if (titleView != null) {
			int extraTopPadding =
					(int)
							TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_DIP,
									16,
									changelogActivity.getResources().getDisplayMetrics());
			titleView.setPadding(
					titleView.getPaddingLeft(),
					titleView.getPaddingTop() + extraTopPadding,
					titleView.getPaddingRight(),
					titleView.getPaddingBottom());
		}
	}
}
