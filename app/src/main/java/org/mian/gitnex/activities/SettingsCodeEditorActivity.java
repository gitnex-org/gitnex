package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsCodeEditorBinding;
import org.mian.gitnex.fragments.SettingsFragment;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author M M Arif
 */
public class SettingsCodeEditorActivity extends BaseActivity {

	private static String[] colorList;
	private static int colorSelectedChoice = 0;
	private View.OnClickListener onClickListener;
	private static String[] indentationList;
	private static int indentationSelectedChoice = 0;
	private static String[] indentationTabsList;
	private static int indentationTabsSelectedChoice = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsCodeEditorBinding activitySettingsCodeEditorBinding =
			ActivitySettingsCodeEditorBinding.inflate(getLayoutInflater());
		setContentView(activitySettingsCodeEditorBinding.getRoot());

		initCloseListener();
		activitySettingsCodeEditorBinding.close.setOnClickListener(onClickListener);

		// color selector dialog
		colorList = getResources().getStringArray(R.array.ceColors);
		activitySettingsCodeEditorBinding.ceColorSelected.setText(colorList[colorSelectedChoice]);

		activitySettingsCodeEditorBinding.ceColorSelectionFrame.setOnClickListener(
			view -> {
				MaterialAlertDialogBuilder materialAlertDialogBuilder =
					new MaterialAlertDialogBuilder(ctx)
						.setTitle(R.string.ceSyntaxHighlightColor)
						.setSingleChoiceItems(
							colorList,
							colorSelectedChoice,
							(dialogInterfaceColor, i) -> {
								colorSelectedChoice = i;
								activitySettingsCodeEditorBinding.ceColorSelected
									.setText(colorList[i]);
								tinyDB.putInt("ceColorId", i);

								SettingsFragment.refreshParent = true;
								this.recreate();
								this.overridePendingTransition(0, 0);
								dialogInterfaceColor.dismiss();
								Toasty.success(
									appCtx,
									getResources()
										.getString(R.string.settingsSave));
							});

				materialAlertDialogBuilder.create().show();
			});

		// indentation selector dialog
		indentationList = getResources().getStringArray(R.array.ceIndentation);
		activitySettingsCodeEditorBinding.indentationSelected.setText(indentationList[indentationSelectedChoice]);

		activitySettingsCodeEditorBinding.indentationSelectionFrame.setOnClickListener(
			view -> {
				MaterialAlertDialogBuilder materialAlertDialogBuilder =
					new MaterialAlertDialogBuilder(ctx)
						.setTitle(R.string.ceIndentation)
						.setSingleChoiceItems(
							indentationList,
							indentationSelectedChoice,
							(dialogInterfaceColor, i) -> {
								indentationSelectedChoice = i;
								activitySettingsCodeEditorBinding.indentationSelected
									.setText(indentationList[i]);
								tinyDB.putInt("ceIndentationId", i);

								SettingsFragment.refreshParent = true;
								this.recreate();
								this.overridePendingTransition(0, 0);
								dialogInterfaceColor.dismiss();
								Toasty.success(
									appCtx,
									getResources()
										.getString(R.string.settingsSave));
							});

				materialAlertDialogBuilder.create().show();
			});

		// indentation tabs selector dialog
		if (indentationList[indentationSelectedChoice].startsWith("Tabs")) {
			activitySettingsCodeEditorBinding.indentationTabsSelectionFrame.setVisibility(View.VISIBLE);
		} else {
			activitySettingsCodeEditorBinding.indentationTabsSelectionFrame.setVisibility(View.GONE);
		}

		indentationTabsList = getResources().getStringArray(R.array.ceIndentationTabsWidth);
		activitySettingsCodeEditorBinding.indentationTabsSelected.setText(indentationTabsList[indentationTabsSelectedChoice]);

		activitySettingsCodeEditorBinding.indentationTabsSelectionFrame.setOnClickListener(
			view -> {
				MaterialAlertDialogBuilder materialAlertDialogBuilder =
					new MaterialAlertDialogBuilder(ctx)
						.setTitle(R.string.ceIndentationTabsWidth)
						.setSingleChoiceItems(
							indentationTabsList,
							indentationTabsSelectedChoice,
							(dialogInterfaceColor, i) -> {
								indentationTabsSelectedChoice = i;
								activitySettingsCodeEditorBinding.indentationTabsSelected
									.setText(indentationTabsList[i]);
								tinyDB.putInt("ceIndentationTabsId", i);

								SettingsFragment.refreshParent = true;
								this.recreate();
								this.overridePendingTransition(0, 0);
								dialogInterfaceColor.dismiss();
								Toasty.success(
									appCtx,
									getResources()
										.getString(R.string.settingsSave));
							});

				materialAlertDialogBuilder.create().show();
			});
	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}
}
