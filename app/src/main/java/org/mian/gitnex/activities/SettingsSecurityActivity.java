package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import org.apache.commons.io.FileUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import org.mian.gitnex.notifications.NotificationsMaster;
import java.io.File;
import java.io.IOException;

/**
 * Author M M Arif
 */

public class SettingsSecurityActivity extends BaseActivity {

	private Context appCtx;
	private Context ctx = this;

	private View.OnClickListener onClickListener;

	private static String[] cacheSizeDataList = {"50 MB", "100 MB", "250 MB", "500 MB", "1 GB"};
	private static int cacheSizeDataSelectedChoice = 0;

	private static String[] cacheSizeImagesList = {"50 MB", "100 MB", "250 MB", "500 MB", "1 GB"};
	private static int cacheSizeImagesSelectedChoice = 0;

	private static int MINIMUM_POLLING_DELAY = 1;
	private static int DEFAULT_POLLING_DELAY = 20;
	private static int MAXIMUM_POLLING_DELAY = 720;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_security;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		TinyDB tinyDb = new TinyDB(appCtx);
		String currentVersion = tinyDb.getString("giteaVersion");

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		TextView cacheSizeDataSelected = findViewById(R.id.cacheSizeDataSelected); // setter for data cache size
		TextView cacheSizeImagesSelected = findViewById(R.id.cacheSizeImagesSelected); // setter for images cache size
		TextView clearCacheSelected = findViewById(R.id.clearCacheSelected); // setter for clear cache
		TextView pollingDelaySelected = findViewById(R.id.pollingDelaySelected);

		LinearLayout certsFrame = findViewById(R.id.certsFrame);
		LinearLayout pollingDelayFrame = findViewById(R.id.pollingDelayFrame);
		LinearLayout cacheSizeDataFrame = findViewById(R.id.cacheSizeDataSelectionFrame);
		LinearLayout cacheSizeImagesFrame = findViewById(R.id.cacheSizeImagesSelectionFrame);
		LinearLayout clearCacheFrame = findViewById(R.id.clearCacheSelectionFrame);

		if(!tinyDb.getString("cacheSizeStr").isEmpty()) {

			cacheSizeDataSelected.setText(tinyDb.getString("cacheSizeStr"));
		}

		if(!tinyDb.getString("cacheSizeImagesStr").isEmpty()) {

			cacheSizeImagesSelected.setText(tinyDb.getString("cacheSizeImagesStr"));
		}

		if(cacheSizeDataSelectedChoice == 0) {

			cacheSizeDataSelectedChoice = tinyDb.getInt("cacheSizeId");
		}

		if(cacheSizeImagesSelectedChoice == 0) {

			cacheSizeImagesSelectedChoice = tinyDb.getInt("cacheSizeImagesId");
		}

		if(new Version(currentVersion).less("1.12.3")) {

			pollingDelayFrame.setVisibility(View.GONE);
		}

		pollingDelaySelected.setText(String.format(getString(R.string.pollingDelaySelectedText), tinyDb.getInt("pollingDelayMinutes", DEFAULT_POLLING_DELAY)));

		// clear cache setter
		File cacheDir = appCtx.getCacheDir();
		clearCacheSelected.setText(FileUtils.byteCountToDisplaySize((int) FileUtils.sizeOfDirectory(cacheDir)));

		// clear cache
		clearCacheFrame.setOnClickListener(v1 -> {

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsSecurityActivity.this);

			builder.setTitle(getResources().getString(R.string.clearCacheDialogHeader));
			builder.setMessage(getResources().getString(R.string.clearCacheDialogMessage));
			builder.setPositiveButton(R.string.menuDeleteText, (dialog, which) -> {

				try {

					FileUtils.deleteDirectory(cacheDir);
					FileUtils.mkdir(cacheDir.getAbsolutePath());
					this.recreate();
					this.overridePendingTransition(0, 0);
				}
				catch (IOException e) {

					Log.e("SettingsSecurity", e.toString());
				}
			});

			builder.setNeutralButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss());
			builder.create().show();

		});

		// cache size images selection dialog
		cacheSizeImagesFrame.setOnClickListener(view -> {

			AlertDialog.Builder tsBuilder = new AlertDialog.Builder(SettingsSecurityActivity.this);

			tsBuilder.setTitle(getResources().getString(R.string.cacheSizeImagesDialogHeader));
			tsBuilder.setCancelable(cacheSizeImagesSelectedChoice != -1);

			tsBuilder.setSingleChoiceItems(cacheSizeImagesList, cacheSizeImagesSelectedChoice, (dialogInterfaceTheme, i) -> {

				cacheSizeImagesSelectedChoice = i;
				cacheSizeImagesSelected.setText(cacheSizeImagesList[i]);
				tinyDb.putString("cacheSizeImagesStr", cacheSizeImagesList[i]);
				tinyDb.putInt("cacheSizeImagesId", i);

				dialogInterfaceTheme.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			AlertDialog cfDialog = tsBuilder.create();
			cfDialog.show();
		});

		// cache size data selection dialog
		cacheSizeDataFrame.setOnClickListener(view -> {

			AlertDialog.Builder tsBuilder = new AlertDialog.Builder(SettingsSecurityActivity.this);

			tsBuilder.setTitle(getResources().getString(R.string.cacheSizeDataDialogHeader));
			tsBuilder.setCancelable(cacheSizeDataSelectedChoice != -1);

			tsBuilder.setSingleChoiceItems(cacheSizeDataList, cacheSizeDataSelectedChoice, (dialogInterfaceTheme, i) -> {

				cacheSizeDataSelectedChoice = i;
				cacheSizeDataSelected.setText(cacheSizeDataList[i]);
				tinyDb.putString("cacheSizeStr", cacheSizeDataList[i]);
				tinyDb.putInt("cacheSizeId", i);

				dialogInterfaceTheme.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			AlertDialog cfDialog = tsBuilder.create();
			cfDialog.show();
		});

		// certs deletion
		certsFrame.setOnClickListener(v1 -> {

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsSecurityActivity.this);

			builder.setTitle(getResources().getString(R.string.settingsCertsPopupTitle));
			builder.setMessage(getResources().getString(R.string.settingsCertsPopupMessage));
			builder.setPositiveButton(R.string.menuDeleteText, (dialog, which) -> {

				appCtx.getSharedPreferences(MemorizingTrustManager.KEYSTORE_NAME, Context.MODE_PRIVATE).edit().remove(MemorizingTrustManager.KEYSTORE_KEY).apply();

				tinyDb.putBoolean("loggedInMode", false);
				tinyDb.remove("basicAuthPassword");
				tinyDb.putBoolean("basicAuthFlag", false);

				Intent loginActivityIntent = new Intent().setClass(appCtx, LoginActivity.class);
				loginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				appCtx.startActivity(loginActivityIntent);
			});

			builder.setNeutralButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss());
			builder.create().show();
		});

		// polling delay
		pollingDelayFrame.setOnClickListener(v -> {

			NumberPicker numberPicker = new NumberPicker(ctx);
			numberPicker.setMinValue(MINIMUM_POLLING_DELAY);
			numberPicker.setMaxValue(MAXIMUM_POLLING_DELAY);
			numberPicker.setValue(tinyDb.getInt("pollingDelayMinutes", DEFAULT_POLLING_DELAY));
			numberPicker.setWrapSelectorWheel(true);

			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setTitle(getString(R.string.pollingDelayDialogHeaderText));
			builder.setMessage(getString(R.string.pollingDelayDialogDescriptionText));

			builder.setCancelable(true);
			builder.setPositiveButton(getString(R.string.okButton), (dialog, which) -> {

				tinyDb.putInt("pollingDelayMinutes", numberPicker.getValue());

				NotificationsMaster.fireWorker(ctx);
				NotificationsMaster.hireWorker(ctx);

				pollingDelaySelected.setText(String.format(getString(R.string.pollingDelaySelectedText), numberPicker.getValue()));
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			builder.setNeutralButton(R.string.cancelButton, null);
			builder.setView(numberPicker);
			builder.create().show();
		});
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}
}
