package org.mian.gitnex.helpers;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import org.mian.gitnex.R;

/**
 * Author M M Arif
 */

public class SnackBar {

	public static void info(Context context, View createRepository, String message) {

		Snackbar snackBar = Snackbar.make(createRepository, message, Snackbar.LENGTH_LONG);

		View sbView = snackBar.getView();
		TextView textView = sbView.findViewById(R.id.snackbar_text);
		textView.setTextColor(context.getResources().getColor(R.color.lightBlue));

		snackBar.show();

	}

	public static void success(Context context, View createRepository, String message) {

		Snackbar snackBar = Snackbar.make(createRepository, message, Snackbar.LENGTH_LONG);

		View sbView = snackBar.getView();
		TextView textView = sbView.findViewById(R.id.snackbar_text);
		textView.setTextColor(context.getResources().getColor(R.color.colorWhite));

		snackBar.show();

	}

	public static void warning(Context context, View createRepository, String message) {

		Snackbar snackBar = Snackbar.make(createRepository, message, Snackbar.LENGTH_LONG);

		View sbView = snackBar.getView();
		TextView textView = sbView.findViewById(R.id.snackbar_text);
		textView.setTextColor(context.getResources().getColor(R.color.lightYellow));

		snackBar.show();

	}

	public static void error(Context context, View createRepository, String message) {

		Snackbar snackBar = Snackbar.make(createRepository, message, Snackbar.LENGTH_LONG);

		View sbView = snackBar.getView();
		TextView textView = sbView.findViewById(R.id.snackbar_text);
		textView.setTextColor(context.getResources().getColor(R.color.darkRed));

		snackBar.show();

	}

}
