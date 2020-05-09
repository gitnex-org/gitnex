package org.mian.gitnex.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;

/**
 * Author opyale
 */

public class DeprecationDialog extends AlertDialog.Builder {

	private Context context;

	private String title;
	private String message;

	public DeprecationDialog(@NonNull Context context) {

		super(context);
		this.context = context;
		setup();

	}

	public DeprecationDialog(@NonNull Context context, int themeResId) {

		super(context, themeResId);
		this.context = context;
		setup();

	}

	@NonNull
	@SuppressLint("InflateParams")
	@Override
	public AlertDialog create() {

		setCancelable(false);
		setPositiveButton(context.getResources().getString(R.string.okButton), (dialog, which) -> dialog.dismiss());

		View view = LayoutInflater.from(context).inflate(R.layout.layout_deprecation_dialog, null);

		TextView customTitle = view.findViewById(R.id.customTitle);
		TextView customMessage = view.findViewById(R.id.customMessage);

		customTitle.setText(title);
		customMessage.setText(message);

		setView(view);
		return super.create();

	}

	private void setup() {

		this.message = "";
		this.title = context.getResources().getString(R.string.featureDeprecated);

	}

	public void setMessage(String message) {

		this.message = message;
	}

	public void setTitle(String title) {

		this.title = title;
	}

}
