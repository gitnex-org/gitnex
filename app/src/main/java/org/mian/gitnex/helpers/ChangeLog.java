package org.mian.gitnex.helpers;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import androidx.core.text.HtmlCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mian.gitnex.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.util.Objects;

/**
 * @author M M Arif
 */

public class ChangeLog {

    static final private String TAG = "ChangeLog";
    static final private String CHANGELOG_XML_NODE = "changelog";

    private final Activity changelogActivity;

	public ChangeLog(Activity context) {
		changelogActivity = context;
    }

	private String ParseReleaseTag(XmlResourceParser aXml) throws XmlPullParserException, IOException {

		StringBuilder strBuilder = new StringBuilder(aXml.getAttributeValue(null, "version") + "<br>");
		int eventType = aXml.getEventType();

		while ((eventType != XmlPullParser.END_TAG) || (aXml.getName().equals("change"))) {

			if ((eventType == XmlPullParser.START_TAG) && (aXml.getName().equals("change"))) {
				eventType = aXml.next();
				strBuilder.append(aXml.getText()).append("<br>");
			}
			eventType = aXml.next();

		}
		strBuilder.append("<br>");

		return strBuilder.toString();

	}

	private String getChangelog(int resId, Resources res) {

		StringBuilder strBuilder = new StringBuilder();
		try (XmlResourceParser xml = res.getXml(resId)) {

			int eventType = xml.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {

				if ((eventType == XmlPullParser.START_TAG) && (xml.getName().equals("release"))) {
					strBuilder.append(ParseReleaseTag(xml));

				}
				eventType = xml.next();

			}

		}
		catch (XmlPullParserException | IOException e) {
			Log.e(TAG, Objects.requireNonNull(e.getMessage()));
		}

		return strBuilder.toString();

	}

	public void showDialog() {

		String packageName = changelogActivity.getPackageName();
		Resources res = null;

		try {
			res = changelogActivity.getPackageManager().getResourcesForApplication(packageName);
		}
		catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, Objects.requireNonNull(e.getMessage()));
		}

		assert res != null;
		int resId = res.getIdentifier(CHANGELOG_XML_NODE, "xml", packageName);

		String changelogMessage = getChangelog(resId, res);

		MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(changelogActivity)
			.setTitle(R.string.changelogTitle)
			.setMessage(HtmlCompat.fromHtml("<small>" + changelogMessage + "</small>", HtmlCompat.FROM_HTML_MODE_LEGACY))
			.setCancelable(false)
			.setNeutralButton(R.string.close, null);

		materialAlertDialogBuilder.create().show();
	}

}
