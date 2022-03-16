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
import org.gitnex.tea4j.models.Commits;
import org.gitnex.tea4j.models.FileDiffView;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.adapters.DiffFilesAdapter;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomCommitHeaderBinding;
import org.mian.gitnex.databinding.FragmentCommitDetailsBinding;
import org.mian.gitnex.helpers.*;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import okhttp3.ResponseBody;
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
		Call<ResponseBody> call = new Version(TinyDB.getInstance(requireContext()).getString("giteaVersion")).higherOrEqual("1.16.0") ?
			RetrofitClient.getApiInterface(requireContext()).getCommitDiff(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repoOwner, repoName, sha) :
			RetrofitClient.getWebInterface(requireContext()).getCommitDiff(((BaseActivity) requireActivity()).getAccount().getWebAuthorization(), repoOwner, repoName, sha);

		call.enqueue(new Callback<ResponseBody>() {

			@Override
			public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
				checkLoading();
				assert response.body() != null;
				switch(response.code()) {

					case 200:
						List<FileDiffView> fileDiffViews;
						try {
							fileDiffViews = ParseDiff.getFileDiffViewArray(response.body().string());
						} catch(IOException e) {
							onFailure(call, e);
							return;
						}

						DiffFilesAdapter adapter = new DiffFilesAdapter(requireContext(), fileDiffViews);
						requireActivity().runOnUiThread(() -> binding.diffFiles.setAdapter(adapter));
						break;

					case 401:
						requireActivity().runOnUiThread(() -> AlertDialogs.authorizationTokenRevokedDialog(requireContext(), getString(R.string.alertDialogTokenRevokedTitle),
							getString(R.string.alertDialogTokenRevokedMessage), getString(R.string.cancelButton), getString(R.string.cancelButton)));
						break;

					case 403:
						requireActivity().runOnUiThread(() -> Toasty.error(requireContext(), getString(R.string.authorizeError)));
						break;

					case 404:
						requireActivity().runOnUiThread(() -> Toasty.warning(requireContext(), getString(R.string.apiNotFound)));
						break;

					default:
						requireActivity().runOnUiThread(() -> Toasty.error(requireContext(), getString(R.string.labelGeneralError)));
				}
			}

			@Override
			public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
				checkLoading();
				Toasty.error(requireContext(), getString(R.string.genericError));
			}
		});
	}

	private void getCommit() {

		RetrofitClient.getApiInterface(requireContext()).getCommit(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repoOwner, repoName, sha)
			.enqueue(new Callback<Commits>() {

				@Override
				public void onResponse(@NonNull Call<Commits> call, @NonNull Response<Commits> response) {
					checkLoading();
					CustomCommitHeaderBinding binding = CustomCommitHeaderBinding.inflate(getLayoutInflater());
					binding.getRoot().setOnClickListener((v) -> {
						// we need a ClickListener here to prevent that the ItemClickListener of the diffFiles ListView handles clicks for the header
					});
					CommitDetailFragment.this.binding.diffFiles.addHeaderView(binding.getRoot());
					assert response.body() != null;
					Commits commitsModel = response.body();
					String[] commitMessageParts = commitsModel.getCommit().getMessage().split("(\r\n|\n)", 2);

					if(commitMessageParts.length > 1 && !commitMessageParts[1].trim().isEmpty()) {
						binding.commitBody.setVisibility(View.VISIBLE);
						binding.commitSubject.setText(EmojiParser.parseToUnicode(commitMessageParts[0].trim()));
						binding.commitBody.setText(EmojiParser.parseToUnicode(commitMessageParts[1].trim()));
					} else {
						binding.commitSubject.setText(EmojiParser.parseToUnicode(commitMessageParts[0].trim()));
						binding.commitBody.setVisibility(View.GONE);
					}

					if(!Objects.equals(commitsModel.getCommit().getCommitter().getEmail(), commitsModel.getCommit().getCommitter().getEmail())) {
						binding.commitAuthorAndCommitter.setText(HtmlCompat.fromHtml(CommitDetailFragment.this
							.getString(R.string.commitAuthoredByAndCommittedByWhen, commitsModel.getCommit().getAuthor().getName(), commitsModel.getCommit().getCommitter().getName(),
								TimeHelper
									.formatTime(commitsModel.getCommit().getCommitter().getDate(), getResources().getConfiguration().locale, "pretty",
										requireContext())), HtmlCompat.FROM_HTML_MODE_COMPACT));
					} else {
						binding.commitAuthorAndCommitter.setText(HtmlCompat.fromHtml(CommitDetailFragment.this
							.getString(R.string.commitCommittedByWhen, commitsModel.getCommit().getCommitter().getName(),
								TimeHelper
									.formatTime(commitsModel.getCommit().getCommitter().getDate(), getResources().getConfiguration().locale, "pretty",
										requireContext())), HtmlCompat.FROM_HTML_MODE_COMPACT));
					}

					if(commitsModel.getAuthor() != null && commitsModel.getAuthor().getAvatar_url() != null &&
						!commitsModel.getAuthor().getAvatar_url().isEmpty()) {

						binding.commitAuthorAvatar.setVisibility(View.VISIBLE);

						int imgRadius = AppUtil.getPixelsFromDensity(requireContext(), 3);

						PicassoService.getInstance(requireContext()).get()
							.load(commitsModel.getAuthor().getAvatar_url())
							.placeholder(R.drawable.loader_animated)
							.transform(new RoundedTransformation(imgRadius, 0))
							.resize(120, 120)
							.centerCrop().into(binding.commitAuthorAvatar);

						binding.commitAuthorAvatar.setOnClickListener((v) -> {
							Intent intent = new Intent(requireContext(), ProfileActivity.class);
							intent.putExtra("username", commitsModel.getAuthor().getUsername());
							startActivity(intent);
						});

					} else {
						binding.commitAuthorAvatar.setImageDrawable(null);
						binding.commitAuthorAvatar.setVisibility(View.GONE);
					}

					if(commitsModel.getCommitter() != null &&
						!commitsModel.getAuthor().getLogin().equals(commitsModel.getCommitter().getLogin()) &&
						commitsModel.getCommitter().getAvatar_url() != null &&
						!commitsModel.getCommitter().getAvatar_url().isEmpty()) {

						binding.commitCommitterAvatar.setVisibility(View.VISIBLE);

						int imgRadius = AppUtil.getPixelsFromDensity(requireContext(), 3);

						PicassoService.getInstance(requireContext()).get()
							.load(commitsModel.getCommitter().getAvatar_url())
							.placeholder(R.drawable.loader_animated)
							.transform(new RoundedTransformation(imgRadius, 0))
							.resize(120, 120)
							.centerCrop().into(binding.commitCommitterAvatar);

						binding.commitCommitterAvatar.setOnClickListener((v) -> {
							Intent intent = new Intent(requireContext(), ProfileActivity.class);
							intent.putExtra("username", commitsModel.getCommitter().getUsername());
							startActivity(intent);
						});

					} else {
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
				public void onFailure(@NonNull Call<Commits> call, @NonNull Throwable t) {

					checkLoading();
					Toasty.error(requireContext(), getString(R.string.genericError));
					requireActivity().finish();
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