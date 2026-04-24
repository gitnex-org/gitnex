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
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
import org.mian.gitnex.helpers.UIHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author qwerty287
 * @author mmarif
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

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.diffFiles, null, binding.contentScrollView);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentCommitDetailsBinding.inflate(inflater, container, false);

		repoOwner = requireActivity().getIntent().getStringExtra("owner");
		repoName = requireActivity().getIntent().getStringExtra("repo");
		sha = requireActivity().getIntent().getStringExtra("sha");

		if (sha != null) {
			String shortSha = sha.substring(0, Math.min(sha.length(), 10));
			binding.toolbarTitle.setText(shortSha);
		}

		setupRecyclerView();
		setupListeners();

		getCommit();
		getDiff();
		getStatuses();

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter =
				new DiffFilesAdapter(
						requireContext(), fileDiffViews, repoOwner, repoName, sha, "commit", -1);
		binding.diffFiles.setHasFixedSize(true);
		binding.diffFiles.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.diffFiles.setAdapter(adapter);
		binding.diffFiles.setNestedScrollingEnabled(false);
	}

	private void setupListeners() {
		binding.btnBack.setOnClickListener(v -> requireActivity().finish());

		binding.statuses.setOnClickListener(
				view -> {
					if (binding.statusesList.getVisibility() == View.GONE) {
						binding.statusesExpandCollapse.setImageResource(R.drawable.ic_chevron_up);
						binding.statusesList.setVisibility(View.VISIBLE);
					} else {
						binding.statusesExpandCollapse.setImageResource(R.drawable.ic_chevron_down);
						binding.statusesList.setVisibility(View.GONE);
					}
				});

		binding.toolbarTitle.setOnLongClickListener(
				v -> {
					copyToClipboard(sha);
					return true;
				});
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
						if (binding == null) return;

						checkLoading();
						if (response.isSuccessful() && response.body() != null) {
							fileDiffViews = ParseDiff.getFileDiffViewArray(response.body());
							adapter.updateList(fileDiffViews);
						} else {
							handleErrorCodes(response.code());
						}
					}

					@Override
					public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
						if (binding != null) {
							checkLoading();
							showGenericError();
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
								if (binding == null) return;

								checkLoading();
								Commit commitsModel = response.body();
								if (commitsModel == null) {
									onFailure(call, new Throwable());
									return;
								}
								updateCommitUI(commitsModel);
							}

							@Override
							public void onFailure(
									@NonNull Call<Commit> call, @NonNull Throwable t) {
								if (binding != null) {
									checkLoading();
									showGenericError();
								}
							}
						});
	}

	private void updateCommitUI(Commit commitsModel) {
		if (binding == null) {
			return;
		}

		String[] commitMessageParts = commitsModel.getCommit().getMessage().split("(\r\n|\n)", 2);
		binding.commitSubject.setText(EmojiParser.parseToUnicode(commitMessageParts[0].trim()));

		if (commitMessageParts.length > 1 && !commitMessageParts[1].trim().isEmpty()) {
			binding.commitBody.setVisibility(View.VISIBLE);
			binding.commitBody.setText(EmojiParser.parseToUnicode(commitMessageParts[1].trim()));
		} else {
			binding.commitBody.setVisibility(View.GONE);
		}

		Date date = TimeHelper.parseIso8601(commitsModel.getCommit().getCommitter().getDate());
		String time = TimeHelper.getFullDateTime(date, Locale.getDefault());
		String authoredBy = commitsModel.getCommit().getAuthor().getName();
		String committedBy = commitsModel.getCommit().getCommitter().getName();

		if (!authoredBy.equals(committedBy)) {
			binding.commitAuthorAndCommitter.setText(
					HtmlCompat.fromHtml(
							getString(
									R.string.commitAuthoredByAndCommittedByWhen,
									authoredBy,
									committedBy,
									time),
							HtmlCompat.FROM_HTML_MODE_COMPACT));
		} else {
			binding.commitAuthorAndCommitter.setText(
					HtmlCompat.fromHtml(
							getString(R.string.commitCommittedByWhen, committedBy, time),
							HtmlCompat.FROM_HTML_MODE_COMPACT));
		}

		loadAvatar(commitsModel, binding.commitAuthorAvatar, binding.commitAuthorAvatarFrame);
		if (commitsModel.getCommitter() != null
				&& (commitsModel.getAuthor() == null
						|| !commitsModel
								.getAuthor()
								.getLogin()
								.equals(commitsModel.getCommitter().getLogin()))) {
			loadAvatar(
					commitsModel,
					binding.commitCommitterAvatar,
					binding.commitCommitterAvatarFrame);
		} else {
			binding.commitCommitterAvatarFrame.setVisibility(View.GONE);
		}

		binding.commitSha.setText(sha.substring(0, Math.min(sha.length(), 10)));
		binding.commitSha.setOnClickListener(v -> copyToClipboard(sha));
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
								if (binding == null) {
									return;
								}

								checkLoading();

								if (response.isSuccessful() && response.body() != null) {
									if (response.body().isEmpty()) {
										binding.statusesLvMain.setVisibility(View.GONE);
										return;
									}

									ArrayList<CommitStatus> result = new ArrayList<>();
									for (CommitStatus c : response.body()) {
										boolean exists = false;
										for (int i = 0; i < result.size(); i++) {
											if (Objects.equals(
													result.get(i).getContext(), c.getContext())) {
												if (result.get(i).getCreatedAt() != null
														&& result.get(i)
																.getCreatedAt()
																.before(c.getCreatedAt())) {
													result.set(i, c);
												}
												exists = true;
												break;
											}
										}
										if (!exists) result.add(c);
									}

									if (getContext() != null) {
										binding.statusesList.setLayoutManager(
												new LinearLayoutManager(requireContext()));
										binding.statusesList.setAdapter(
												new CommitStatusesAdapter(result));
									}
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<CommitStatus>> call, @NonNull Throwable t) {
								if (binding != null) {
									checkLoading();
									showGenericError();
								}
							}
						});
	}

	private void loadAvatar(Commit commit, android.widget.ImageView view, View frame) {
		if (!isAdded() || commit == null || view == null) return;
		if (frame != null) frame.setVisibility(View.VISIBLE);

		String avatarUrl = null;
		String login = null;

		if (commit.getAuthor() != null) {
			avatarUrl = commit.getAuthor().getAvatarUrl();
			login = commit.getAuthor().getLogin();
		}

		if (avatarUrl != null && !avatarUrl.isEmpty()) {
			Glide.with(this)
					.load(avatarUrl)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.error(R.drawable.ic_person)
					.centerCrop()
					.into(view);
		} else {
			Glide.with(this)
					.load(R.drawable.ic_person)
					.placeholder(R.drawable.loader_animated)
					.into(view);
		}

		if (login != null) {
			final String finalLogin = login;
			view.setOnClickListener(
					v -> {
						Intent intent = new Intent(requireContext(), ProfileActivity.class);
						intent.putExtra("username", finalLogin);
						startActivity(intent);
					});
		} else {
			view.setOnClickListener(null);
			view.setClickable(false);
		}
	}

	private void checkLoading() {
		loadingFinished++;

		if (binding == null) {
			return;
		}

		if (loadingFinished >= 3) {
			binding.expressiveLoader.setVisibility(View.GONE);
			binding.contentScrollView.setVisibility(View.VISIBLE);
			binding.contentScrollView.setAlpha(0f);
			binding.contentScrollView.animate().alpha(1f).setDuration(300).start();
			binding.dockedToolbar.setVisibility(View.VISIBLE);
		}
	}

	private void copyToClipboard(String text) {
		ClipboardManager clipboard =
				(ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("commitSha", text);
		if (clipboard != null) {
			clipboard.setPrimaryClip(clip);
			Toasty.show(requireContext(), getString(R.string.copyShaToastMsg));
		}
	}

	private void handleErrorCodes(int code) {
		switch (code) {
			case 401 -> AlertDialogs.authorizationTokenRevokedDialog(requireContext());
			case 403 -> Toasty.show(requireContext(), getString(R.string.authorizeError));
			case 404 -> Toasty.show(requireContext(), getString(R.string.apiNotFound));
			default -> showGenericError();
		}
	}

	private void showGenericError() {
		if (getContext() != null) {
			Toasty.show(requireContext(), getString(R.string.genericError));
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
