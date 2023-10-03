package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.vdurmont.emoji.EmojiParser;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import org.gitnex.tea4j.v2.models.CreateMilestoneOption;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateMilestoneBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class CreateMilestoneActivity extends BaseActivity {

	private ActivityCreateMilestoneBinding binding;
	private RepositoryContext repository;
	private boolean renderMd = false;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityCreateMilestoneBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		repository = RepositoryContext.fromIntent(getIntent());

		binding.topAppBar.setNavigationOnClickListener(v -> finish());

		MenuItem attachment = binding.topAppBar.getMenu().getItem(0);
		attachment.setVisible(false);

		showDatePickerDialog();

		binding.milestoneDescription.setOnTouchListener(
				(touchView, motionEvent) -> {
					touchView.getParent().requestDisallowInterceptTouchEvent(true);

					if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0
							&& (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

						touchView.getParent().requestDisallowInterceptTouchEvent(false);
					}
					return false;
				});

		binding.topAppBar.setOnMenuItemClickListener(
				menuItem -> {
					int id = menuItem.getItemId();

					if (id == R.id.markdown) {

						if (!renderMd) {
							Markdown.render(
									ctx,
									EmojiParser.parseToUnicode(
											Objects.requireNonNull(
													Objects.requireNonNull(
																	binding.milestoneDescription
																			.getText())
															.toString())),
									binding.markdownPreview);

							binding.markdownPreview.setVisibility(View.VISIBLE);
							binding.milestoneDescriptionLayout.setVisibility(View.GONE);
							renderMd = true;
						} else {
							binding.markdownPreview.setVisibility(View.GONE);
							binding.milestoneDescriptionLayout.setVisibility(View.VISIBLE);
							renderMd = false;
						}

						return true;
					} else if (id == R.id.create) {
						processNewMilestone();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});
	}

	private void showDatePickerDialog() {

		MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
		builder.setSelection(Calendar.getInstance().getTimeInMillis());
		builder.setTitleText(R.string.newIssueDueDateTitle);
		MaterialDatePicker<Long> materialDatePicker = builder.build();

		binding.milestoneDueDate.setOnClickListener(
				v -> materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

		materialDatePicker.addOnPositiveButtonClickListener(
				selection -> {
					Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					calendar.setTimeInMillis(selection);
					SimpleDateFormat format =
							new SimpleDateFormat(
									"yyyy-MM-dd", new Locale(tinyDB.getString("locale")));
					String formattedDate = format.format(calendar.getTime());
					binding.milestoneDueDate.setText(formattedDate);
				});
	}

	private void processNewMilestone() {

		String newMilestoneTitle =
				Objects.requireNonNull(binding.milestoneTitle.getText()).toString();
		String newMilestoneDescription =
				Objects.requireNonNull(binding.milestoneDescription.getText()).toString();
		String milestoneDueDate =
				Objects.requireNonNull(binding.milestoneDueDate.getText()).toString();

		if (newMilestoneTitle.equals("")) {

			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.milestoneNameErrorEmpty));
			return;
		}

		if (!newMilestoneDescription.equals("")) {

			if (newMilestoneDescription.length() > 255) {

				SnackBar.error(
						ctx,
						findViewById(android.R.id.content),
						getString(R.string.milestoneDescError));
				return;
			}
		}

		createNewMilestone(
				repository.getOwner(),
				repository.getName(),
				newMilestoneTitle,
				newMilestoneDescription,
				milestoneDueDate);
	}

	private void createNewMilestone(
			String repoOwner,
			String repoName,
			String newMilestoneTitle,
			String newMilestoneDescription,
			String milestoneDueDate) {

		CreateMilestoneOption createMilestone = new CreateMilestoneOption();
		createMilestone.setDescription(newMilestoneDescription);
		createMilestone.setTitle(newMilestoneTitle);
		String[] date = milestoneDueDate.split("-");
		if (!milestoneDueDate.equalsIgnoreCase("")) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, Integer.parseInt(date[0]));
			calendar.set(Calendar.MONTH, Integer.parseInt(date[1]));
			calendar.set(Calendar.DATE, Integer.parseInt(date[2]));
			Date dueDate = calendar.getTime();
			createMilestone.setDueOn(dueDate);
		}

		Call<Milestone> call;

		call =
				RetrofitClient.getApiInterface(ctx)
						.issueCreateMilestone(repoOwner, repoName, createMilestone);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Milestone> call,
							@NonNull retrofit2.Response<Milestone> response) {

						if (response.isSuccessful()) {

							if (response.code() == 201) {

								RepoDetailActivity.updateFABActions = true;
								SnackBar.success(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.milestoneCreated));

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
					public void onFailure(@NonNull Call<Milestone> call, @NonNull Throwable t) {}
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
