package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.gitnex.tea4j.models.FileDiffView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.DiffFilesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentDiffFilesBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.ParseDiff;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author opyale
 */

public class DiffFilesFragment extends Fragment {

	private FragmentDiffFilesBinding binding;
	private Context ctx;
	private TinyDB tinyDB;

	public DiffFilesFragment() {}

	public static DiffFilesFragment newInstance() {
		return new DiffFilesFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentDiffFilesBinding.inflate(inflater, container, false);
		ctx = requireContext();
		tinyDB = TinyDB.getInstance(ctx);

		String repoFullName = tinyDB.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];

		String pullIndex = tinyDB.getString("issueNumber");

		binding.progressBar.setVisibility(View.VISIBLE);
		binding.toolbarTitle.setText(R.string.processingText);
		binding.close.setOnClickListener(v -> requireActivity().finish());

		binding.diffFiles.setOnItemClickListener((parent, view, position, id) -> requireActivity().getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.fragment_container, DiffFragment.newInstance((FileDiffView) parent.getItemAtPosition(position)))
			.commit());

		getPullDiffFiles(repoOwner, repoName, pullIndex);

		return binding.getRoot();

	}

	private void getPullDiffFiles(String owner, String repo, String pullIndex) {

		Thread thread = new Thread(() -> {

			Call<ResponseBody> call = new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.13.0") ?
				RetrofitClient.getApiInterface(ctx).getPullDiffContent(Authorization.get(ctx), owner, repo, pullIndex) :
				RetrofitClient.getWebInterface(ctx).getPullDiffContent(Authorization.getWeb(ctx), owner, repo, pullIndex);

			try {

				Response<ResponseBody> response = call.execute();
				assert response.body() != null;

				switch(response.code()) {

					case 200:
						List<FileDiffView> fileDiffViews = ParseDiff.getFileDiffViewArray(response.body().string());

						int filesCount = fileDiffViews.size();

						String toolbarTitleText = (filesCount > 1) ?
							getResources().getString(R.string.fileDiffViewHeader, Integer.toString(filesCount)) :
							getResources().getString(R.string.fileDiffViewHeaderSingle, Integer.toString(filesCount));

						DiffFilesAdapter adapter = new DiffFilesAdapter(ctx, fileDiffViews);

						requireActivity().runOnUiThread(() -> {
							binding.progressBar.setVisibility(View.GONE);
							binding.diffFiles.setAdapter(adapter);
							binding.toolbarTitle.setText(toolbarTitleText);
						});
						break;

					case 401:
						requireActivity().runOnUiThread(() -> AlertDialogs.authorizationTokenRevokedDialog(ctx,
							getString(R.string.alertDialogTokenRevokedTitle),
							getString(R.string.alertDialogTokenRevokedMessage),
							getString(R.string.cancelButton),
							getString(R.string.cancelButton)));
						break;

					case 403:
						requireActivity().runOnUiThread(() -> Toasty.error(ctx, ctx.getString(R.string.authorizeError)));
						break;

					case 404:
						requireActivity().runOnUiThread(() -> Toasty.warning(ctx, ctx.getString(R.string.apiNotFound)));
						break;

					default:
						requireActivity().runOnUiThread(() -> Toasty.error(ctx, getString(R.string.labelGeneralError)));

				}
			} catch(IOException ignored) {}

		});

		thread.start();

	}

}
