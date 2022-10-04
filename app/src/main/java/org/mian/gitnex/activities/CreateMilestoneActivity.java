package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import com.vdurmont.emoji.EmojiParser;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateMilestoneOption;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateMilestoneBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class CreateMilestoneActivity extends BaseActivity implements View.OnClickListener {

	private ActivityCreateMilestoneBinding binding;
	private View.OnClickListener onClickListener;
	private RepositoryContext repository;
	private Date currentDate = null;
	private final View.OnClickListener createMilestoneListener = v -> processNewMilestone();
	private boolean renderMd = false;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityCreateMilestoneBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		setSupportActionBar(binding.toolbar);

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		InputMethodManager imm =
				(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		repository = RepositoryContext.fromIntent(getIntent());

		binding.milestoneTitle.requestFocus();
		assert imm != null;
		imm.showSoftInput(binding.milestoneTitle, InputMethodManager.SHOW_IMPLICIT);

		binding.milestoneDescription.setOnTouchListener(
				(touchView, motionEvent) -> {
					touchView.getParent().requestDisallowInterceptTouchEvent(true);

					if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0
							&& (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

						touchView.getParent().requestDisallowInterceptTouchEvent(false);
					}
					return false;
				});

		initCloseListener();
		binding.close.setOnClickListener(onClickListener);
		binding.milestoneDueDate.setOnClickListener(this);

		if (!connToInternet) {

			binding.createNewMilestoneButton.setEnabled(false);
		} else {

			binding.createNewMilestoneButton.setOnClickListener(createMilestoneListener);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.markdown_switcher, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if (id == R.id.markdown) {

			if (!renderMd) {
				Markdown.render(
						ctx,
						EmojiParser.parseToUnicode(
								Objects.requireNonNull(
										Objects.requireNonNull(
														binding.milestoneDescription.getText())
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
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void processNewMilestone() {

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		String newMilestoneTitle =
				Objects.requireNonNull(binding.milestoneTitle.getText()).toString();
		String newMilestoneDescription =
				Objects.requireNonNull(binding.milestoneDescription.getText()).toString();

		if (!connToInternet) {

			Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
			return;
		}

		if (newMilestoneTitle.equals("")) {

			Toasty.error(ctx, getString(R.string.milestoneNameErrorEmpty));
			return;
		}

		if (!newMilestoneDescription.equals("")) {

			if (newMilestoneDescription.length() > 255) {

				Toasty.warning(ctx, getString(R.string.milestoneDescError));
				return;
			}
		}

		disableProcessButton();
		createNewMilestone(
				repository.getOwner(),
				repository.getName(),
				newMilestoneTitle,
				newMilestoneDescription);
	}

	private void createNewMilestone(
			String repoOwner,
			String repoName,
			String newMilestoneTitle,
			String newMilestoneDescription) {

		CreateMilestoneOption createMilestone = new CreateMilestoneOption();
		createMilestone.setDescription(newMilestoneDescription);
		createMilestone.setTitle(newMilestoneTitle);
		createMilestone.setDueOn(currentDate);

		Call<Milestone> call;

		call =
				RetrofitClient.getApiInterface(ctx)
						.issueCreateMilestone(repoOwner, repoName, createMilestone);

		call.enqueue(
				new Callback<Milestone>() {

					@Override
					public void onResponse(
							@NonNull Call<Milestone> call,
							@NonNull retrofit2.Response<Milestone> response) {

						if (response.isSuccessful()) {

							if (response.code() == 201) {

								Intent result = new Intent();
								result.putExtra("milestoneCreated", true);
								setResult(201, result);
								Toasty.success(ctx, getString(R.string.milestoneCreated));
								enableProcessButton();
								finish();
							}
						} else if (response.code() == 401) {

							enableProcessButton();
							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							enableProcessButton();
							Toasty.error(ctx, getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Milestone> call, @NonNull Throwable t) {

						Log.e("onFailure", t.toString());
						enableProcessButton();
					}
				});
	}

	@Override
	public void onClick(View v) {

		if (v == binding.milestoneDueDate) {

			final Calendar c = Calendar.getInstance();
			int mYear = c.get(Calendar.YEAR);
			final int mMonth = c.get(Calendar.MONTH);
			final int mDay = c.get(Calendar.DAY_OF_MONTH);

			DatePickerDialog datePickerDialog =
					new DatePickerDialog(
							this,
							(view, year, monthOfYear, dayOfMonth) -> {
								binding.milestoneDueDate.setText(
										getString(
												R.string.setDueDate,
												year,
												(monthOfYear + 1),
												dayOfMonth));
								currentDate = new Date(year - 1900, monthOfYear, dayOfMonth);
							},
							mYear,
							mMonth,
							mDay);
			datePickerDialog.show();
		}
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private void disableProcessButton() {

		binding.createNewMilestoneButton.setEnabled(false);
	}

	private void enableProcessButton() {

		binding.createNewMilestoneButton.setEnabled(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
