package org.mian.gitnex.helpers;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import org.mian.gitnex.R;

/**
 * @author M M Arif
 */

public class SnackBar {

	public static void info(Context context, View view, String message) {
		Snackbar snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
		View sbView = snackBar.getView();
		TextView textView = sbView.findViewById(R.id.snackbar_text);
		textView.setTextColor(context.getColor(R.color.colorWhite));
		snackBar.show();
	}

	public static void success(Context context, View view, String message) {
		Snackbar snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
		View sbView = snackBar.getView();
		TextView textView = sbView.findViewById(R.id.snackbar_text);
		textView.setTextColor(context.getColor(R.color.colorLightGreen));
		snackBar.show();
	}

	public static void warning(Context context, View view, String message) {
		Snackbar snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
		View sbView = snackBar.getView();
		TextView textView = sbView.findViewById(R.id.snackbar_text);
		textView.setTextColor(context.getColor(R.color.lightYellow));
		snackBar.show();
	}

	public static void error(Context context, View view, String message) {
		Snackbar snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
		View sbView = snackBar.getView();
		TextView textView = sbView.findViewById(R.id.snackbar_text);
		textView.setTextColor(context.getColor(R.color.darkRed));
		snackBar.show();
	}
}
