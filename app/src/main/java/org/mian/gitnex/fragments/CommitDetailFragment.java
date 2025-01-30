package org.mian.gitnex.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Commit;
import org.gitnex.tea4j.v2.models.CommitStatus;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.adapters.CommitStatusesAdapter;
import org.mian.gitnex.adapters.DiffFilesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentCommitDetailsBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.FileDiffView;
import org.mian.gitnex.helpers.ParseDiff;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author qwerty287
 */
public class CommitDetailFragment extends Fragment {

	private FragmentCommitDetailsBinding binding;
	private String repoOwner;
	private String repoName;
	private String sha;
	private List<FileDiffView> fileDiffViews = new ArrayList<>();
	private DiffFilesAdapter adapter;
	private int loadingFinished = 0;

	public static CommitDetailFragment newInstance() {
		return new CommitDetailFragment();
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);

		if (binding != null) {
			return binding.getRoot();
		}

		binding = FragmentCommitDetailsBinding.inflate(getLayoutInflater(), container, false);

		IssueContext issue = IssueContext.fromIntent(requireActivity().getIntent());
		RepositoryContext repository = RepositoryContext.fromIntent(requireActivity().getIntent());
		repoOwner = repository.getOwner();
		repoName = repository.getName();
		sha = requireActivity().getIntent().getStringExtra("sha");
		assert sha != null;
		binding.toolbarTitle.setText(sha.substring(0, Math.min(sha.length(), 10)));

		adapter = new DiffFilesAdapter(requireContext(), fileDiffViews, issue, "commit");

		binding.diffFiles.setHasFixedSize(true);
		binding.diffFiles.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.diffFiles.setAdapter(adapter);

		getCommit();
		getDiff();
		getStatuses();

		binding.statuses.setOnClickListener(
				view -> {
					if (binding.statusesLv.getVisibility() == View.GONE) {
						binding.statusesExpandCollapse.setImageResource(R.drawable.ic_chevron_up);
						binding.statusesLv.setVisibility(View.VISIBLE);
					} else {
						binding.statusesExpandCollapse.setImageResource(R.drawable.ic_chevron_down);
						binding.statusesLv.setVisibility(View.GONE);
					}
				});

		binding.close.setOnClickListener((v) -> requireActivity().finish());

		return binding.getRoot();
	}

	private void getDiff() {
		Call<String> call =
				((BaseActivity) requireActivity()).getAccount().requiresVersion("1.16.0")
						? RetrofitClient.getApiInterface(requireContext())
								.repoDownloadCommitDiffOrPatch(repoOwner, repoName, sha, "diff")
						: RetrofitClient.getWebInterface(requireContext())
								.repoDownloadCommitDiffOrPatch(repoOwner, repoName, sha, "diff");

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<String> call, @NonNull Response<String> response) {

						checkLoading();
						switch (response.code()) {
							case 200:
								assert response.body() != null;
								fileDiffViews = ParseDiff.getFileDiffViewArray(response.body());

								requireActivity()
										.runOnUiThread(
												() -> {
													adapter.updateList(fileDiffViews);
													adapter.notifyDataChanged();
												});
								break;

							case 401:
								requireActivity()
										.runOnUiThread(
												() ->
														AlertDialogs
																.authorizationTokenRevokedDialog(
																		requireContext()));
								break;

							case 403:
								requireActivity()
										.runOnUiThread(
												() ->
														Toasty.error(
																requireContext(),
																getString(
																		R.string.authorizeError)));
								break;

							case 404:
								requireActivity()
										.runOnUiThread(
												() ->
														Toasty.warning(
																requireContext(),
																getString(R.string.apiNotFound)));
								break;

							default:
								requireActivity()
										.runOnUiThread(
												() ->
														Toasty.error(
																requireContext(),
																getString(R.string.genericError)));
						}
					}

					@Override
					public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

						checkLoading();
						if (getContext() != null) {
							Toasty.error(requireContext(), getString(R.string.genericError));
						}
					}
				});
	}

	private void getCommit() {

		RetrofitClient.getApiInterface(requireContext())
				.repoGetSingleCommit(repoOwner, repoName, sha, true, false, true)
				.enqueue(
						new Callback<>() {

							@Override
							public void onResponse(
									@NonNull Call<Commit> call,
									@NonNull Response<Commit> response) {

								checkLoading();
								Commit commitsModel = response.body();
								if (commitsModel == null) {
									onFailure(call, new Throwable());
									return;
								}
								String[] commitMessageParts =
										commitsModel.getCommit().getMessage().split("(\r\n|\n)", 2);

								if (commitMessageParts.length > 1
										&& !commitMessageParts[1].trim().isEmpty()) {
									binding.commitBody.setVisibility(View.VISIBLE);
									binding.commitSubject.setText(
											EmojiParser.parseToUnicode(
													commitMessageParts[0].trim()));
									binding.commitBody.setText(
											EmojiParser.parseToUnicode(
													commitMessageParts[1].trim()));
								} else {
									binding.commitSubject.setText(
											EmojiParser.parseToUnicode(
													commitMessageParts[0].trim()));
									binding.commitBody.setVisibility(View.GONE);
								}

								if (!Objects.equals(
										commitsModel.getCommit().getCommitter().getEmail(),
										commitsModel.getCommit().getCommitter().getEmail())) {
									binding.commitAuthorAndCommitter.setText(
											HtmlCompat.fromHtml(
													CommitDetailFragment.this.getString(
															R.string
																	.commitAuthoredByAndCommittedByWhen,
															commitsModel
																	.getCommit()
																	.getAuthor()
																	.getName(),
															commitsModel
																	.getCommit()
																	.getCommitter()
																	.getName(),
															TimeHelper.formatTime(
																	TimeHelper.parseIso8601(
																			commitsModel
																					.getCommit()
																					.getCommitter()
																					.getDate()),
																	getResources()
																			.getConfiguration()
																			.locale)),
													HtmlCompat.FROM_HTML_MODE_COMPACT));
								} else {
									binding.commitAuthorAndCommitter.setText(
											HtmlCompat.fromHtml(
													CommitDetailFragment.this.getString(
															R.string.commitCommittedByWhen,
															commitsModel
																	.getCommit()
																	.getCommitter()
																	.getName(),
															TimeHelper.formatTime(
																	TimeHelper.parseIso8601(
																			commitsModel
																					.getCommit()
																					.getCommitter()
																					.getDate()),
																	getResources()
																			.getConfiguration()
																			.locale)),
													HtmlCompat.FROM_HTML_MODE_COMPACT));
								}

								if (commitsModel.getAuthor() != null
										&& commitsModel.getAuthor().getAvatarUrl() != null
										&& !commitsModel.getAuthor().getAvatarUrl().isEmpty()) {

									binding.commitAuthorAvatarFrame.setVisibility(View.VISIBLE);

									Glide.with(requireContext())
											.load(commitsModel.getAuthor().getAvatarUrl())
											.diskCacheStrategy(DiskCacheStrategy.ALL)
											.placeholder(R.drawable.loader_animated)
											.centerCrop()
											.into(binding.commitAuthorAvatar);

									binding.commitAuthorAvatar.setOnClickListener(
											(v) -> {
												Intent intent =
														new Intent(
																requireContext(),
																ProfileActivity.class);
												intent.putExtra(
														"username",
														commitsModel.getAuthor().getLogin());
												startActivity(intent);
											});

								} else {
									binding.commitAuthorAvatar.setImageDrawable(null);
									binding.commitAuthorAvatarFrame.setVisibility(View.GONE);
								}

								if (commitsModel.getCommitter() != null
										&& (commitsModel.getAuthor() == null
												|| !commitsModel
														.getAuthor()
														.getLogin()
														.equals(
																commitsModel
																		.getCommitter()
																		.getLogin()))
										&& commitsModel.getCommitter().getAvatarUrl() != null
										&& !commitsModel.getCommitter().getAvatarUrl().isEmpty()) {

									binding.commitCommitterAvatarFrame.setVisibility(View.VISIBLE);

									Glide.with(requireContext())
											.load(commitsModel.getCommitter().getAvatarUrl())
											.diskCacheStrategy(DiskCacheStrategy.ALL)
											.placeholder(R.drawable.loader_animated)
											.centerCrop()
											.into(binding.commitCommitterAvatar);

									binding.commitCommitterAvatar.setOnClickListener(
											(v) -> {
												Intent intent =
														new Intent(
																requireContext(),
																ProfileActivity.class);
												intent.putExtra(
														"username",
														commitsModel.getCommitter().getLogin());
												startActivity(intent);
											});

								} else {
									binding.commitCommitterAvatar.setImageDrawable(null);
									binding.commitCommitterAvatarFrame.setVisibility(View.GONE);
								}

								binding.commitSha.setText(
										commitsModel
												.getSha()
												.substring(
														0,
														Math.min(
																commitsModel.getSha().length(),
																10)));
								binding.commitSha.setOnClickListener(
										(v) -> {
											ClipboardManager clipboard =
													(ClipboardManager)
															requireContext()
																	.getSystemService(
																			Context
																					.CLIPBOARD_SERVICE);
											ClipData clip =
													ClipData.newPlainText(
															"commitSha", commitsModel.getSha());
											assert clipboard != null;
											clipboard.setPrimaryClip(clip);
											Toasty.success(
													requireContext(),
													getString(R.string.copyShaToastMsg));
										});
							}

							@Override
							public void onFailure(
									@NonNull Call<Commit> call, @NonNull Throwable t) {

								checkLoading();
								if (getContext() != null) {
									Toasty.error(
											requireContext(), getString(R.string.genericError));
									requireActivity().finish();
								}
							}
						});
	}

	private void getStatuses() {
		RetrofitClient.getApiInterface(requireContext())
				.repoListStatuses(repoOwner, repoName, sha, null, null, null, null)
				.enqueue(
						new Callback<>() {

							@Override
							public void onResponse(
									@NonNull Call<List<CommitStatus>> call,
									@NonNull Response<List<CommitStatus>> response) {

								checkLoading();

								if (!response.isSuccessful() || response.body() == null) {
									onFailure(call, new Throwable());
									return;
								}

								if (response.body().isEmpty()) {
									binding.statusesLvMain.setVisibility(View.GONE);
									return;
								}

								// merge statuses: a status can be added multiple times with the
								// same context, so we only use the newest one
								ArrayList<CommitStatus> result = new ArrayList<>();
								for (CommitStatus c : response.body()) {
									CommitStatus statusInList = null;
									for (CommitStatus s : result) {
										if (Objects.equals(s.getContext(), c.getContext())) {
											statusInList = s;
											break;
										}
									}
									if (statusInList != null) {
										// if the status that's already in the list was created
										// before this one, replace it
										if (statusInList.getCreatedAt().before(c.getCreatedAt())) {
											result.remove(statusInList);
											result.add(c);
										}
									} else {
										result.add(c);
									}
								}

								binding.statusesList.setLayoutManager(
										new LinearLayoutManager(requireContext()));
								binding.statusesList.setAdapter(new CommitStatusesAdapter(result));
							}

							@Override
							public void onFailure(
									@NonNull Call<List<CommitStatus>> call, @NonNull Throwable t) {

								checkLoading();
								if (getContext() != null) {
									Toasty.error(
											requireContext(), getString(R.string.genericError));
									requireActivity().finish();
								}
							}
						});
	}

	private void checkLoading() {
		loadingFinished += 1;
		if (loadingFinished >= 3) {
			binding.progressBar.setVisibility(View.GONE);
		}
	}
}
