package org.mian.gitnex.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.flag.BubbleFlag;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateLabelOption;
import org.gitnex.tea4j.v2.models.EditLabelOption;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateLabelBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.LabelsViewModel;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class CreateLabelActivity extends BaseActivity {

	public static boolean refreshLabels = false;
	private ActivityCreateLabelBinding activityCreateLabelBinding;
	private RepositoryContext repository;
	private String labelColor = "";
	private String labelColorDefault = "";
	private ColorPickerPreferenceManager colorManager;
	private int page = 1;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityCreateLabelBinding = ActivityCreateLabelBinding.inflate(getLayoutInflater());
		setContentView(activityCreateLabelBinding.getRoot());

		repository = RepositoryContext.fromIntent(getIntent());

		if (getIntent().getStringExtra("labelAction") != null
				&& Objects.requireNonNull(getIntent().getStringExtra("labelAction"))
						.equals("delete")) {

			deleteLabel(
					Integer.parseInt(
							Objects.requireNonNull(getIntent().getStringExtra("labelId"))));
			finish();
			return;
		}

		activityCreateLabelBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		resultLimit = Constants.getCurrentResultLimit(ctx);

		colorManager = ColorPickerPreferenceManager.getInstance(this);
		colorManager.clearSavedAllData();
		activityCreateLabelBinding.colorPicker.setBackgroundColor(
				colorManager.getColor("colorPickerDialogLabels", Color.RED));

		MenuItem create = activityCreateLabelBinding.topAppBar.getMenu().getItem(0);
		MenuItem update = activityCreateLabelBinding.topAppBar.getMenu().getItem(1);
		update.setVisible(false);

		activityCreateLabelBinding.colorPicker.setOnClickListener(v -> newColorPicker());

		if (getIntent().getStringExtra("labelAction") != null
				&& Objects.requireNonNull(getIntent().getStringExtra("labelAction"))
						.equals("edit")) {

			activityCreateLabelBinding.labelName.setText(getIntent().getStringExtra("labelTitle"));
			int labelColor_ = Color.parseColor("#" + getIntent().getStringExtra("labelColor"));
			activityCreateLabelBinding.colorPicker.setBackgroundColor(labelColor_);
			labelColorDefault = "#" + getIntent().getStringExtra("labelColor");

			activityCreateLabelBinding.topAppBar.setTitle(getString(R.string.pageTitleLabelUpdate));

			update.setVisible(true);
			create.setVisible(false);

			activityCreateLabelBinding.topAppBar.setOnMenuItemClickListener(
					menuItem -> {
						int id = menuItem.getItemId();

						if (id == R.id.update) {
							processUpdateLabel();
							return true;
						} else {
							return super.onOptionsItemSelected(menuItem);
						}
					});

			return;
		}

		activityCreateLabelBinding.topAppBar.setOnMenuItemClickListener(
				menuItem -> {
					int id = menuItem.getItemId();

					if (id == R.id.create) {
						processCreateLabel();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});
	}

	private void newColorPicker() {

		ColorPickerDialog.Builder builder =
				new ColorPickerDialog.Builder(this)
						.setPreferenceName("colorPickerDialogLabels")
						.setPositiveButton(
								getString(R.string.okButton),
								(ColorEnvelopeListener)
										(envelope, clicked) -> {
											activityCreateLabelBinding.colorPicker
													.setBackgroundColor(envelope.getColor());
											labelColor =
													String.format(
															"#%06X",
															(0xFFFFFF & envelope.getColor()));
										})
						.attachAlphaSlideBar(true)
						.attachBrightnessSlideBar(true)
						.setBottomSpace(16);

		builder.getColorPickerView().setFlagView(new BubbleFlag(this));

		if (!labelColorDefault.equalsIgnoreCase("")) {
			int labelColorCurrent = Color.parseColor(labelColorDefault);
			builder.getColorPickerView().setInitialColor(labelColorCurrent);
		} else {
			colorManager.setColor("colorPickerDialogLabels", Color.RED);
		}

		builder.getColorPickerView().setLifecycleOwner(this);
		builder.show();
	}

	private void processUpdateLabel() {

		String updateLabelName =
				Objects.requireNonNull(activityCreateLabelBinding.labelName.getText()).toString();

		String updateLabelColor;
		if (labelColor.isEmpty()) {

			updateLabelColor = labelColorDefault;
		} else {

			updateLabelColor = labelColor;
		}

		if (updateLabelName.isEmpty()) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.labelEmptyError));
			return;
		}

		if (!AppUtil.checkLabel(updateLabelName)) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.labelNameError));
			return;
		}

		patchLabel(
				repository,
				updateLabelName,
				updateLabelColor,
				Integer.parseInt(Objects.requireNonNull(getIntent().getStringExtra("labelId"))));
	}

	private void processCreateLabel() {

		String newLabelName =
				Objects.requireNonNull(activityCreateLabelBinding.labelName.getText()).toString();
		String newLabelColor;

		if (labelColor.isEmpty()) {

			newLabelColor =
					String.format(
							"#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, R.color.releasePre)));
		} else {

			newLabelColor = labelColor;
		}

		if (newLabelName.isEmpty()) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.labelEmptyError));
			return;
		}

		if (!AppUtil.checkLabel(newLabelName)) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.labelNameError));
			return;
		}

		createNewLabel(newLabelName, newLabelColor);
	}

	private void createNewLabel(String newLabelName, String newLabelColor) {

		CreateLabelOption createLabelFunc = new CreateLabelOption();
		createLabelFunc.setColor(newLabelColor);
		createLabelFunc.setName(newLabelName);

		Call<Label> call;

		if (getIntent().getStringExtra("type") != null
				&& Objects.requireNonNull(getIntent().getStringExtra("type")).equals("org")) {

			call =
					RetrofitClient.getApiInterface(ctx)
							.orgCreateLabel(getIntent().getStringExtra("orgName"), createLabelFunc);
		} else if (repository != null) {

			call =
					RetrofitClient.getApiInterface(ctx)
							.issueCreateLabel(
									repository.getOwner(), repository.getName(), createLabelFunc);
		} else {
			return;
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Label> call,
							@NonNull retrofit2.Response<Label> response) {

						if (response.code() == 201) {

							SnackBar.success(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.labelCreated));
							refreshLabels = true;
							new Handler().postDelayed(() -> finish(), 3000);
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Label> call, @NonNull Throwable t) {

						labelColor = "";
					}
				});
	}

	private void patchLabel(
			RepositoryContext repository,
			String updateLabelName,
			String updateLabelColor,
			int labelId) {

		EditLabelOption createLabelFunc = new EditLabelOption();
		createLabelFunc.setColor(updateLabelColor);
		createLabelFunc.setName(updateLabelName);

		Call<Label> call;

		if (getIntent().getStringExtra("type") != null
				&& Objects.requireNonNull(getIntent().getStringExtra("type")).equals("org")) {

			call =
					RetrofitClient.getApiInterface(ctx)
							.orgEditLabel(
									getIntent().getStringExtra("orgName"),
									(long) labelId,
									createLabelFunc);
		} else {

			call =
					RetrofitClient.getApiInterface(ctx)
							.issueEditLabel(
									repository.getOwner(),
									repository.getName(),
									(long) labelId,
									createLabelFunc);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Label> call,
							@NonNull retrofit2.Response<Label> response) {

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								SnackBar.success(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.labelUpdated));
								refreshLabels = true;
								new Handler().postDelayed(() -> finish(), 3000);
							}
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Label> call, @NonNull Throwable t) {

						labelColor = "";
						labelColorDefault = "";
					}
				});
	}

	private void deleteLabel(int labelId) {

		Call<Void> call;

		if (getIntent().getStringExtra("type") != null
				&& Objects.requireNonNull(getIntent().getStringExtra("type")).equals("org")) {

			call =
					RetrofitClient.getApiInterface(ctx)
							.orgDeleteLabel(getIntent().getStringExtra("orgName"), (long) labelId);
		} else {

			call =
					RetrofitClient.getApiInterface(ctx)
							.issueDeleteLabel(
									repository.getOwner(), repository.getName(), (long) labelId);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						if (response.isSuccessful()) {

							if (response.code() == 204) {

								SnackBar.success(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.labelDeleteText));

								if (getIntent().getStringExtra("type") != null
										&& Objects.requireNonNull(
														getIntent().getStringExtra("type"))
												.equals("org")) {

									LabelsViewModel.loadLabelsList(
											getIntent().getStringExtra("orgName"),
											null,
											"org",
											ctx,
											null,
											page,
											resultLimit);
								} else {

									LabelsViewModel.loadLabelsList(
											repository.getOwner(),
											repository.getName(),
											"repo",
											ctx,
											null,
											page,
											resultLimit);
								}
							}
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (repository == null) {
			return;
		}
		repository.checkAccountSwitch(this);
	}
}
