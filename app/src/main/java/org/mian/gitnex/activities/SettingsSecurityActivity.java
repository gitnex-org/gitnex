package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import org.apache.commons.io.FileUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsSecurityBinding;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import java.io.File;
import java.io.IOException;

/**
 * Author M M Arif
 */

public class SettingsSecurityActivity extends BaseActivity {

	private View.OnClickListener onClickListener;

	private static final String[] cacheSizeDataList = {"50 MB", "100 MB", "250 MB", "500 MB", "1 GB"};
	private static int cacheSizeDataSelectedChoice = 0;

	private static final String[] cacheSizeImagesList = {"50 MB", "100 MB", "250 MB", "500 MB", "1 GB"};
	private static int cacheSizeImagesSelectedChoice = 0;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_security;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsSecurityBinding activitySettingsSecurityBinding = ActivitySettingsSecurityBinding.inflate(getLayoutInflater());

		ImageView closeActivity = activitySettingsSecurityBinding.close;

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		TextView cacheSizeDataSelected = activitySettingsSecurityBinding.cacheSizeDataSelected; // setter for data cache size
		TextView cacheSizeImagesSelected = activitySettingsSecurityBinding.cacheSizeImagesSelected; // setter for images cache size
		TextView clearCacheSelected = activitySettingsSecurityBinding.clearCacheSelected; // setter for clear cache

		LinearLayout certsFrame = activitySettingsSecurityBinding.certsFrame;
		LinearLayout cacheSizeDataFrame = activitySettingsSecurityBinding.cacheSizeDataSelectionFrame;
		LinearLayout cacheSizeImagesFrame = activitySettingsSecurityBinding.cacheSizeImagesSelectionFrame;
		LinearLayout clearCacheFrame = activitySettingsSecurityBinding.clearCacheSelectionFrame;

		if(!tinyDB.getString("cacheSizeStr").isEmpty()) {

			cacheSizeDataSelected.setText(tinyDB.getString("cacheSizeStr"));
		}

		if(!tinyDB.getString("cacheSizeImagesStr").isEmpty()) {

			cacheSizeImagesSelected.setText(tinyDB.getString("cacheSizeImagesStr"));
		}

		if(cacheSizeDataSelectedChoice == 0) {

			cacheSizeDataSelectedChoice = tinyDB.getInt("cacheSizeId");
		}

		if(cacheSizeImagesSelectedChoice == 0) {

			cacheSizeImagesSelectedChoice = tinyDB.getInt("cacheSizeImagesId");
		}

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
				tinyDB.putString("cacheSizeImagesStr", cacheSizeImagesList[i]);
				tinyDB.putInt("cacheSizeImagesId", i);

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
				tinyDB.putString("cacheSizeStr", cacheSizeDataList[i]);
				tinyDB.putInt("cacheSizeId", i);

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

				tinyDB.putBoolean("loggedInMode", false);
				tinyDB.remove("basicAuthPassword");
				tinyDB.putBoolean("basicAuthFlag", false);

				Intent loginActivityIntent = new Intent().setClass(appCtx, LoginActivity.class);
				loginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				appCtx.startActivity(loginActivityIntent);
			});

			builder.setNeutralButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss());
			builder.create().show();
		});
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}
}
