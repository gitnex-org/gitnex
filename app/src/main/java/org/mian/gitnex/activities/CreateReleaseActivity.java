package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import org.mian.gitnex.adapters.NotesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.ActivityCreateReleaseBinding;
import org.mian.gitnex.databinding.CustomInsertNoteBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.SnackBar;
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

		getBranches(repository.getOwner(), repository.getName());
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

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
