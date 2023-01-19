package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
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
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Branch;
import org.gitnex.tea4j.v2.models.CreateReleaseOption;
import org.gitnex.tea4j.v2.models.CreateTagOption;
import org.gitnex.tea4j.v2.models.Release;
import org.gitnex.tea4j.v2.models.Tag;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateReleaseBinding;
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
public class CreateReleaseActivity extends BaseActivity {

	private ActivityCreateReleaseBinding binding;
	List<String> branchesList = new ArrayList<>();
	private View.OnClickListener onClickListener;
	private String selectedBranch;
	private RepositoryContext repository;
	private final View.OnClickListener createReleaseListener = v -> processNewRelease();
	private boolean renderMd = false;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityCreateReleaseBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		setSupportActionBar(binding.toolbar);

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		InputMethodManager imm =
				(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		repository = RepositoryContext.fromIntent(getIntent());

		binding.releaseTitle.requestFocus();
		assert imm != null;
		imm.showSoftInput(binding.releaseTitle, InputMethodManager.SHOW_IMPLICIT);

		binding.releaseContent.setOnTouchListener(
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

		getBranches(repository.getOwner(), repository.getName());

		disableProcessButton();

		if (!connToInternet) {

			disableProcessButton();
		} else {

			binding.createNewRelease.setOnClickListener(createReleaseListener);
		}

		binding.createNewTag.setOnClickListener(v -> createNewTag());
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
										Objects.requireNonNull(binding.releaseContent.getText())
												.toString())),
						binding.markdownPreview);

				binding.markdownPreview.setVisibility(View.VISIBLE);
				binding.releaseContentLayout.setVisibility(View.GONE);
				renderMd = true;
			} else {
				binding.markdownPreview.setVisibility(View.GONE);
				binding.releaseContentLayout.setVisibility(View.VISIBLE);
				renderMd = false;
			}

			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void createNewTag() {
		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		String tagName = Objects.requireNonNull(binding.releaseTagName.getText()).toString();
		String message =
				Objects.requireNonNull(binding.releaseTitle.getText()).toString()
						+ "\n\n"
						+ Objects.requireNonNull(binding.releaseContent.getText()).toString();

		if (!connToInternet) {
			Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
			return;
		}

		if (tagName.equals("")) {
			Toasty.error(ctx, getString(R.string.tagNameErrorEmpty));
			return;
		}

		if (selectedBranch == null) {
			Toasty.error(ctx, getString(R.string.selectBranchError));
			return;
		}

		disableProcessButton();

		CreateTagOption createReleaseJson = new CreateTagOption();
		createReleaseJson.setMessage(message);
		createReleaseJson.setTagName(tagName);
		createReleaseJson.setTarget(selectedBranch);

		Call<Tag> call =
				RetrofitClient.getApiInterface(ctx)
						.repoCreateTag(
								repository.getOwner(), repository.getName(), createReleaseJson);

		call.enqueue(
				new Callback<Tag>() {

					@Override
					public void onResponse(
							@NonNull Call<Tag> call, @NonNull retrofit2.Response<Tag> response) {

						if (response.code() == 201) {

							Intent result = new Intent();
							result.putExtra("updateReleases", true);
							setResult(201, result);
							Toasty.success(ctx, getString(R.string.tagCreated));
							finish();
						} else if (response.code() == 401) {
							enableProcessButton();
							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 403) {
							enableProcessButton();
							Toasty.error(ctx, ctx.getString(R.string.authorizeError));
						} else if (response.code() == 404) {
							enableProcessButton();
							Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
						} else {
							enableProcessButton();
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Tag> call, @NonNull Throwable t) {
						Log.e("onFailure", t.toString());
						enableProcessButton();
					}
				});
	}

	private void processNewRelease() {

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		String newReleaseTagName =
				Objects.requireNonNull(binding.releaseTagName.getText()).toString();
		String newReleaseTitle = Objects.requireNonNull(binding.releaseTitle.getText()).toString();
		String newReleaseContent =
				Objects.requireNonNull(binding.releaseContent.getText()).toString();
		String checkBranch = selectedBranch;
		boolean newReleaseType = binding.releaseType.isChecked();
		boolean newReleaseDraft = binding.releaseDraft.isChecked();

		if (!connToInternet) {

			Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
			return;
		}

		if (newReleaseTitle.equals("")) {

			Toasty.error(ctx, getString(R.string.titleErrorEmpty));
			return;
		}

		if (newReleaseTagName.equals("")) {

			Toasty.error(ctx, getString(R.string.tagNameErrorEmpty));
			return;
		}

		if (checkBranch == null) {

			Toasty.error(ctx, getString(R.string.selectBranchError));
			return;
		}

		disableProcessButton();
		createNewReleaseFunc(
				repository.getOwner(),
				repository.getName(),
				newReleaseTagName,
				newReleaseTitle,
				newReleaseContent,
				selectedBranch,
				newReleaseType,
				newReleaseDraft);
	}

	private void createNewReleaseFunc(
			String repoOwner,
			String repoName,
			String newReleaseTagName,
			String newReleaseTitle,
			String newReleaseContent,
			String selectedBranch,
			boolean newReleaseType,
			boolean newReleaseDraft) {

		CreateReleaseOption createReleaseJson = new CreateReleaseOption();
		createReleaseJson.setName(newReleaseTitle);
		createReleaseJson.setTagName(newReleaseTagName);
		createReleaseJson.setBody(newReleaseContent);
		createReleaseJson.setDraft(newReleaseDraft);
		createReleaseJson.setPrerelease(newReleaseType);
		createReleaseJson.setTargetCommitish(selectedBranch);

		Call<Release> call =
				RetrofitClient.getApiInterface(ctx)
						.repoCreateRelease(repoOwner, repoName, createReleaseJson);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Release> call,
							@NonNull retrofit2.Response<Release> response) {

						if (response.code() == 201) {

							Intent result = new Intent();
							result.putExtra("updateReleases", true);
							setResult(201, result);
							Toasty.success(ctx, getString(R.string.releaseCreatedText));
							finish();
						} else if (response.code() == 401) {

							enableProcessButton();
							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 403) {

							enableProcessButton();
							Toasty.error(ctx, ctx.getString(R.string.authorizeError));
						} else if (response.code() == 404) {

							enableProcessButton();
							Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
						} else {

							enableProcessButton();
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Release> call, @NonNull Throwable t) {
						enableProcessButton();
					}
				});
	}

	private void getBranches(final String repoOwner, final String repoName) {

		Call<List<Branch>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoListBranches(repoOwner, repoName, null, null);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Branch>> call,
							@NonNull retrofit2.Response<List<Branch>> response) {

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								List<Branch> branchesList_ = response.body();

								assert branchesList_ != null;
								for (Branch i : branchesList_) {
									branchesList.add(i.getName());
								}

								ArrayAdapter<String> adapter =
										new ArrayAdapter<>(
												CreateReleaseActivity.this,
												R.layout.list_spinner_items,
												branchesList);

								binding.releaseBranch.setAdapter(adapter);
								enableProcessButton();

								binding.releaseBranch.setOnItemClickListener(
										(parent, view, position, id) ->
												selectedBranch = branchesList.get(position));
							}
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Branch>> call, @NonNull Throwable t) {}
				});
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private void disableProcessButton() {
		binding.createNewTag.setEnabled(false);
		binding.createNewRelease.setEnabled(false);
	}

	private void enableProcessButton() {
		binding.createNewTag.setEnabled(true);
		binding.createNewRelease.setEnabled(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
