package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsCodeEditorBinding;
import org.mian.gitnex.fragments.SettingsFragment;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.SnackBar;

/**
 * @author M M Arif
 */
public class SettingsCodeEditorActivity extends BaseActivity {

	private static String[] colorList;
	private static int colorSelectedChoice;
	private static String[] indentationList;
	private static int indentationSelectedChoice;
	private static String[] indentationTabsList;
	private static int indentationTabsSelectedChoice;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsCodeEditorBinding activitySettingsCodeEditorBinding =
				ActivitySettingsCodeEditorBinding.inflate(getLayoutInflater());
		setContentView(activitySettingsCodeEditorBinding.getRoot());

		activitySettingsCodeEditorBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		// color selector dialog
		colorList = getResources().getStringArray(R.array.ceColors);
		colorSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_CE_SYNTAX_HIGHLIGHT_KEY));
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
												AppDatabaseSettings.updateSettingsValue(
														ctx,
														String.valueOf(i),
														AppDatabaseSettings
																.APP_CE_SYNTAX_HIGHLIGHT_KEY);

												SettingsFragment.refreshParent = true;
												this.recreate();
												this.overridePendingTransition(0, 0);
												dialogInterfaceColor.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});

		// indentation selector dialog
		indentationList = getResources().getStringArray(R.array.ceIndentation);
		indentationSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_CE_INDENTATION_KEY));
		activitySettingsCodeEditorBinding.indentationSelected.setText(
				indentationList[indentationSelectedChoice]);

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
												activitySettingsCodeEditorBinding
														.indentationSelected.setText(
														indentationList[i]);
												AppDatabaseSettings.updateSettingsValue(
														ctx,
														String.valueOf(i),
														AppDatabaseSettings.APP_CE_INDENTATION_KEY);

												SettingsFragment.refreshParent = true;
												this.recreate();
												this.overridePendingTransition(0, 0);
												dialogInterfaceColor.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});

		// indentation tabs selector dialog
		if (indentationList[indentationSelectedChoice].startsWith("Tabs")) {
			activitySettingsCodeEditorBinding.indentationTabsSelectionFrame.setVisibility(
					View.VISIBLE);
		} else {
			activitySettingsCodeEditorBinding.indentationTabsSelectionFrame.setVisibility(
					View.GONE);
		}

		indentationTabsList = getResources().getStringArray(R.array.ceIndentationTabsWidth);
		indentationTabsSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_CE_TABS_WIDTH_KEY));
		activitySettingsCodeEditorBinding.indentationTabsSelected.setText(
				indentationTabsList[indentationTabsSelectedChoice]);

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
												activitySettingsCodeEditorBinding
														.indentationTabsSelected.setText(
														indentationTabsList[i]);
												AppDatabaseSettings.updateSettingsValue(
														ctx,
														String.valueOf(i),
														AppDatabaseSettings.APP_CE_TABS_WIDTH_KEY);

												SettingsFragment.refreshParent = true;
												this.recreate();
												this.overridePendingTransition(0, 0);
												dialogInterfaceColor.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});
	}
}
