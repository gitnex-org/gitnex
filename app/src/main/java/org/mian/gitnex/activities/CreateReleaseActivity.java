package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import org.mian.gitnex.adapters.BranchAdapter;
import org.mian.gitnex.adapters.NotesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.ActivityCreateReleaseBinding;
import org.mian.gitnex.databinding.CustomInsertNoteBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class CreateReleaseActivity extends BaseActivity {

	private ActivityCreateReleaseBinding binding;
	List<String> branchesList = new ArrayList<>();
	private String selectedBranch;
	private RepositoryContext repository;
	private boolean renderMd = false;
	private MaterialAlertDialogBuilder materialAlertDialogBuilder;
	private CustomInsertNoteBinding customInsertNoteBinding;
	private NotesAdapter adapter;
	private NotesApi notesApi;
	private List<Notes> notesList;
	public AlertDialog dialogNotes;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityCreateReleaseBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		repository = RepositoryContext.fromIntent(getIntent());

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(ctx, R.style.ThemeOverlay_Material3_Dialog_Alert);

		binding.releaseContent.setOnTouchListener(
				(touchView, motionEvent) -> {
					touchView.getParent().requestDisallowInterceptTouchEvent(true);

					if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0
							&& (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

						touchView.getParent().requestDisallowInterceptTouchEvent(false);
					}
					return false;
				});

		binding.topAppBar.setNavigationOnClickListener(v -> finish());

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
																	binding.releaseContent
																			.getText())
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
					} else if (id == R.id.create) {
						processNewRelease();
						return true;
					} else if (id == R.id.create_tag) {
						createNewTag();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});

		binding.insertNote.setOnClickListener(insertNote -> showAllNotes());

		binding.releaseBranch.setKeyListener(null);
		binding.releaseBranch.setCursorVisible(false);

		binding.releaseBranch.setOnFocusChangeListener(
				(v, hasFocus) -> {
					if (hasFocus) {
						getBranches();
						binding.releaseBranch.clearFocus();
					}
				});
	}

	private void showAllNotes() {

		notesList = new ArrayList<>();
		notesApi = BaseApi.getInstance(ctx, NotesApi.class);

		customInsertNoteBinding = CustomInsertNoteBinding.inflate(LayoutInflater.from(ctx));

		View view = customInsertNoteBinding.getRoot();
		materialAlertDialogBuilder.setView(view);

		customInsertNoteBinding.recyclerView.setHasFixedSize(true);
		customInsertNoteBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		adapter = new NotesAdapter(ctx, notesList, "insert", "release");

		customInsertNoteBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											notesList.clear();
											customInsertNoteBinding.pullToRefresh.setRefreshing(
													false);
											customInsertNoteBinding.progressBar.setVisibility(
													View.VISIBLE);
											fetchNotes();
										},
										250));

		if (notesApi.getCount() > 0) {
			fetchNotes();
			dialogNotes = materialAlertDialogBuilder.show();
		} else {
			Toasty.warning(ctx, getResources().getString(R.string.noNotes));
		}
	}

	private void fetchNotes() {

		notesApi.fetchAllNotes()
				.observe(
						this,
						allNotes -> {
							assert allNotes != null;
							if (!allNotes.isEmpty()) {

								notesList.clear();

								notesList.addAll(allNotes);
								adapter.notifyDataChanged();
								customInsertNoteBinding.recyclerView.setAdapter(adapter);
							}
							customInsertNoteBinding.progressBar.setVisibility(View.GONE);
						});
	}

	private void createNewTag() {

		String tagName = Objects.requireNonNull(binding.releaseTagName.getText()).toString();
		String message =
				Objects.requireNonNull(binding.releaseTitle.getText())
						+ "\n\n"
						+ Objects.requireNonNull(binding.releaseContent.getText());

		if (tagName.isEmpty()) {
			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.tagNameErrorEmpty));
			return;
		}

		if (selectedBranch == null) {
			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.selectBranchError));
			return;
		}

		CreateTagOption createReleaseJson = new CreateTagOption();
		createReleaseJson.setMessage(message);
		createReleaseJson.setTagName(tagName);
		createReleaseJson.setTarget(selectedBranch);

		Call<Tag> call =
				RetrofitClient.getApiInterface(ctx)
						.repoCreateTag(
								repository.getOwner(), repository.getName(), createReleaseJson);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Tag> call, @NonNull retrofit2.Response<Tag> response) {

						if (response.code() == 201) {

							RepoDetailActivity.updateFABActions = true;
							SnackBar.success(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.tagCreated));
							new Handler().postDelayed(() -> finish(), 3000);
						} else if (response.code() == 401) {
							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 403) {
							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.authorizeError));
						} else if (response.code() == 404) {
							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.apiNotFound));
						} else {
							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Tag> call, @NonNull Throwable t) {}
				});
	}

	private void processNewRelease() {

		String newReleaseTagName =
				Objects.requireNonNull(binding.releaseTagName.getText()).toString();
		String newReleaseTitle = Objects.requireNonNull(binding.releaseTitle.getText()).toString();
		String newReleaseContent =
				Objects.requireNonNull(binding.releaseContent.getText()).toString();
		String checkBranch = selectedBranch;
		boolean newReleaseType = binding.releaseType.isChecked();
		boolean newReleaseDraft = binding.releaseDraft.isChecked();

		if (newReleaseTitle.isEmpty()) {
			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.titleErrorEmpty));
			return;
		}

		if (newReleaseTagName.isEmpty()) {
			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.tagNameErrorEmpty));
			return;
		}

		if (checkBranch == null) {
			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.selectBranchError));
			return;
		}

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

							RepoDetailActivity.updateFABActions = true;
							SnackBar.success(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.releaseCreatedText));
							new Handler().postDelayed(() -> finish(), 3000);
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 403) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.authorizeError));
						} else if (response.code() == 404) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.apiNotFound));
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Release> call, @NonNull Throwable t) {}
				});
	}

	private void getBranches() {

		Dialog progressDialog = new Dialog(ctx);
		progressDialog.setCancelable(false);
		progressDialog.setContentView(R.layout.custom_progress_loader);
		progressDialog.show();

		MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(ctx);
		View dialogView = getLayoutInflater().inflate(R.layout.custom_branches_dialog, null);
		dialogBuilder.setView(dialogView);

		RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		recyclerView.addItemDecoration(
				new RecyclerView.ItemDecoration() {
					@Override
					public void getItemOffsets(
							@NonNull Rect outRect,
							@NonNull View view,
							@NonNull RecyclerView parent,
							@NonNull RecyclerView.State state) {

						int position = parent.getChildAdapterPosition(view);
						int spacingSides = (int) ctx.getResources().getDimension(R.dimen.dimen16dp);
						int spacingTop = (int) ctx.getResources().getDimension(R.dimen.dimen12dp);

						outRect.right = spacingSides;
						outRect.left = spacingSides;

						if (position > 0) {
							outRect.top = spacingTop;
						}
					}
				});

		dialogBuilder.setNeutralButton(R.string.close, (dialog, which) -> dialog.dismiss());
		AlertDialog dialog = dialogBuilder.create();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

		final int[] page = {1};
		final int resultLimit = Constants.getCurrentResultLimit(ctx);
		final boolean[] isLoading = {false};
		final boolean[] isLastPage = {false};

		BranchAdapter adapter =
				new BranchAdapter(
						branchName -> {
							binding.releaseBranch.setText(branchName);
							selectedBranch = branchName;
							dialog.dismiss();
						});
		recyclerView.setAdapter(adapter);

		Runnable fetchBranches =
				() -> {
					if (isLoading[0] || isLastPage[0]) return;
					isLoading[0] = true;

					Call<List<Branch>> call =
							RetrofitClient.getApiInterface(ctx)
									.repoListBranches(
											repository.getOwner(),
											repository.getName(),
											page[0],
											resultLimit);

					call.enqueue(
							new Callback<>() {
								@Override
								public void onResponse(
										@NonNull Call<List<Branch>> call,
										@NonNull Response<List<Branch>> response) {

									isLoading[0] = false;

									if (response.code() == 200 && response.body() != null) {
										List<Branch> newBranches = response.body();
										adapter.addBranches(newBranches);

										String totalCountStr =
												response.headers().get("X-Total-Count");

										if (totalCountStr != null) {

											int totalItems = Integer.parseInt(totalCountStr);
											int totalPages =
													(int)
															Math.ceil(
																	(double) totalItems
																			/ resultLimit);
											isLastPage[0] = page[0] >= totalPages;
										} else {
											isLastPage[0] = newBranches.size() < resultLimit;
										}
										page[0]++;

										if (page[0] == 2 && !dialog.isShowing()) {
											progressDialog.dismiss();
											dialog.show();
										}
									} else {
										progressDialog.dismiss();
									}
								}

								@Override
								public void onFailure(
										@NonNull Call<List<Branch>> call, @NonNull Throwable t) {
									isLoading[0] = false;
									progressDialog.dismiss();
								}
							});
				};

		recyclerView.addOnScrollListener(
				new RecyclerView.OnScrollListener() {
					@Override
					public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

						super.onScrolled(recyclerView, dx, dy);
						LinearLayoutManager layoutManager =
								(LinearLayoutManager) recyclerView.getLayoutManager();

						if (layoutManager != null) {

							int visibleItemCount = layoutManager.getChildCount();
							int totalItemCount = layoutManager.getItemCount();
							int firstVisibleItemPosition =
									layoutManager.findFirstVisibleItemPosition();

							if (!isLoading[0]
									&& !isLastPage[0]
									&& (visibleItemCount + firstVisibleItemPosition)
											>= totalItemCount - 5) {
								fetchBranches.run();
							}
						}
					}
				});

		adapter.clear();
		fetchBranches.run();
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
