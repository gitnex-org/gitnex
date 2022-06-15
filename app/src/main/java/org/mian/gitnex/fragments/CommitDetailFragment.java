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
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.v2.models.Commit;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.adapters.DiffFilesAdapter;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomCommitHeaderBinding;
import org.mian.gitnex.databinding.FragmentCommitDetailsBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.FileDiffView;
import org.mian.gitnex.helpers.ParseDiff;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.List;
import java.util.Objects;
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

	private int loadingFinished = 0;

	public static CommitDetailFragment newInstance() {
		return new CommitDetailFragment();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);

		if(binding != null) return binding.getRoot();

		binding = FragmentCommitDetailsBinding.inflate(getLayoutInflater(), container, false);

		RepositoryContext repository = RepositoryContext.fromIntent(requireActivity().getIntent());
		repoOwner = repository.getOwner();
		repoName = repository.getName();
		sha = requireActivity().getIntent().getStringExtra("sha");
		binding.toolbarTitle.setText(sha.substring(0, Math.min(sha.length(), 10)));

		getCommit();
		getDiff();

		binding.close.setOnClickListener((v) -> requireActivity().finish());

		binding.diffFiles.setOnItemClickListener((parent, view, position, id) -> requireActivity().getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.fragment_container, DiffFragment.newInstance((FileDiffView) parent.getItemAtPosition(position), "commit"))
			.commit());

		return binding.getRoot();
	}

	private void getDiff() {
		Call<String> call = ((BaseActivity) requireActivity()).getAccount().requiresVersion("1.16.0") ?
			RetrofitClient.getApiInterface(requireContext()).repoDownloadCommitDiffOrPatch(repoOwner, repoName, sha, "diff") :
			RetrofitClient.getWebInterface(requireContext()).repoDownloadCommitDiffOrPatch(repoOwner, repoName, sha, "diff");

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {

				checkLoading();
				assert response.body() != null;
				switch(response.code()) {

					case 200:
						List<FileDiffView> fileDiffViews;
						fileDiffViews = ParseDiff.getFileDiffViewArray(response.body());

						DiffFilesAdapter adapter = new DiffFilesAdapter(requireContext(), fileDiffViews);
						requireActivity().runOnUiThread(() -> binding.diffFiles.setAdapter(adapter));
						break;

					case 401:
						requireActivity().runOnUiThread(() -> AlertDialogs.authorizationTokenRevokedDialog(requireContext()));
						break;

					case 403:
						requireActivity().runOnUiThread(() -> Toasty.error(requireContext(), getString(R.string.authorizeError)));
						break;

					case 404:
						requireActivity().runOnUiThread(() -> Toasty.warning(requireContext(), getString(R.string.apiNotFound)));
						break;

					default:
						requireActivity().runOnUiThread(() -> Toasty.error(requireContext(), getString(R.string.genericError)));
				}
			}

			@Override
			public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

				checkLoading();
				if(getContext() != null) Toasty.error(requireContext(), getString(R.string.genericError));
			}
		});
	}

	private void getCommit() {

		RetrofitClient.getApiInterface(requireContext()).repoGetSingleCommit(repoOwner, repoName, sha)
			.enqueue(new Callback<>() {

				@Override
				public void onResponse(@NonNull Call<Commit> call, @NonNull Response<Commit> response) {

					checkLoading();
					CustomCommitHeaderBinding binding = CustomCommitHeaderBinding.inflate(getLayoutInflater());
					binding.getRoot().setOnClickListener((v) -> {
						// we need a ClickListener here to prevent that the ItemClickListener of the diffFiles ListView handles clicks for the header
					});
					CommitDetailFragment.this.binding.diffFiles.addHeaderView(binding.getRoot());
					Commit commitsModel = response.body();
					if(commitsModel == null) {
						onFailure(call, new Throwable());
						return;
					}
					String[] commitMessageParts = commitsModel.getCommit().getMessage().split("(\r\n|\n)", 2);

					if(commitMessageParts.length > 1 && !commitMessageParts[1].trim().isEmpty()) {
						binding.commitBody.setVisibility(View.VISIBLE);
						binding.commitSubject.setText(EmojiParser.parseToUnicode(commitMessageParts[0].trim()));
						binding.commitBody.setText(EmojiParser.parseToUnicode(commitMessageParts[1].trim()));
					}
					else {
						binding.commitSubject.setText(EmojiParser.parseToUnicode(commitMessageParts[0].trim()));
						binding.commitBody.setVisibility(View.GONE);
					}

					if(!Objects.equals(commitsModel.getCommit().getCommitter().getEmail(), commitsModel.getCommit().getCommitter().getEmail())) {
						binding.commitAuthorAndCommitter.setText(HtmlCompat.fromHtml(
							CommitDetailFragment.this.getString(R.string.commitAuthoredByAndCommittedByWhen, commitsModel.getCommit().getAuthor().getName(), commitsModel.getCommit().getCommitter().getName(),
								TimeHelper.formatTime(TimeHelper.parseIso8601(commitsModel.getCommit().getCommitter().getDate()), getResources().getConfiguration().locale, "pretty",
									requireContext())), HtmlCompat.FROM_HTML_MODE_COMPACT));
					}
					else {
						binding.commitAuthorAndCommitter.setText(HtmlCompat.fromHtml(
							CommitDetailFragment.this.getString(R.string.commitCommittedByWhen, commitsModel.getCommit().getCommitter().getName(),
								TimeHelper.formatTime(TimeHelper.parseIso8601(commitsModel.getCommit().getCommitter().getDate()), getResources().getConfiguration().locale, "pretty",
									requireContext())), HtmlCompat.FROM_HTML_MODE_COMPACT));
					}

					if(commitsModel.getAuthor() != null && commitsModel.getAuthor().getAvatarUrl() != null && !commitsModel.getAuthor().getAvatarUrl()
						.isEmpty()) {

						binding.commitAuthorAvatar.setVisibility(View.VISIBLE);

						int imgRadius = AppUtil.getPixelsFromDensity(requireContext(), 3);

						PicassoService.getInstance(requireContext()).get().load(commitsModel.getAuthor().getAvatarUrl())
							.placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(binding.commitAuthorAvatar);

						binding.commitAuthorAvatar.setOnClickListener((v) -> {
							Intent intent = new Intent(requireContext(), ProfileActivity.class);
							intent.putExtra("username", commitsModel.getAuthor().getLogin());
							startActivity(intent);
						});

					}
					else {
						binding.commitAuthorAvatar.setImageDrawable(null);
						binding.commitAuthorAvatar.setVisibility(View.GONE);
					}

					if(commitsModel.getCommitter() != null && (commitsModel.getAuthor() == null || !commitsModel.getAuthor().getLogin()
						.equals(commitsModel.getCommitter().getLogin())) && commitsModel.getCommitter().getAvatarUrl() != null && !commitsModel.getCommitter().getAvatarUrl().isEmpty()) {

						binding.commitCommitterAvatar.setVisibility(View.VISIBLE);

						int imgRadius = AppUtil.getPixelsFromDensity(requireContext(), 3);

						PicassoService.getInstance(requireContext()).get().load(commitsModel.getCommitter().getAvatarUrl())
							.placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(binding.commitCommitterAvatar);

						binding.commitCommitterAvatar.setOnClickListener((v) -> {
							Intent intent = new Intent(requireContext(), ProfileActivity.class);
							intent.putExtra("username", commitsModel.getCommitter().getLogin());
							startActivity(intent);
						});

					}
					else {
						binding.commitCommitterAvatar.setImageDrawable(null);
						binding.commitCommitterAvatar.setVisibility(View.GONE);
					}

					binding.commitSha.setText(commitsModel.getSha().substring(0, Math.min(commitsModel.getSha().length(), 10)));
					binding.commitSha.setOnClickListener((v) -> {
						ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("commitSha", commitsModel.getSha());
						assert clipboard != null;
						clipboard.setPrimaryClip(clip);
						Toasty.success(requireContext(), getString(R.string.copyShaToastMsg));
					});
				}

				@Override
				public void onFailure(@NonNull Call<Commit> call, @NonNull Throwable t) {

					checkLoading();
					if(getContext() != null) {
						Toasty.error(requireContext(), getString(R.string.genericError));
						requireActivity().finish();
					}
				}
			});
	}

	private void checkLoading() {
		loadingFinished += 1;
		if(loadingFinished >= 2) {
			binding.progressBar.setVisibility(View.GONE);
		}
	}

}
