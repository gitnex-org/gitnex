package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.DiffAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityDiffBinding;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author opyale
 * @author mmarif
 */
public class DiffActivity extends BaseActivity {

	private ActivityDiffBinding binding;
	private DiffAdapter adapter;
	private final List<String> diffLines = new ArrayList<>();
	private String owner, repo, sha, filePath, type;
	private int prId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityDiffBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		type = getIntent().getStringExtra("type");
		owner = getIntent().getStringExtra("owner");
		repo = getIntent().getStringExtra("repo");
		sha = getIntent().getStringExtra("sha");
		filePath = getIntent().getStringExtra("file_path");
		prId = getIntent().getIntExtra("pr_id", 0);

		setupUI();
		loadData();
	}

	private void setupUI() {
		binding.toolbarTitle.setText(filePath != null ? filePath : "Diff");
		binding.btnBack.setOnClickListener(v -> finish());

		adapter = new DiffAdapter(this, diffLines);
		binding.diffRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		binding.diffRecyclerView.setAdapter(adapter);
	}

	private void loadData() {
		binding.expressiveLoader.setVisibility(View.VISIBLE);

		Call<String> call;
		if ("pull".equalsIgnoreCase(type)) {
			call =
					RetrofitClient.getApiInterface(this)
							.repoDownloadPullDiffOrPatch(owner, repo, (long) prId, "diff", false);
		} else {
			call =
					RetrofitClient.getApiInterface(this)
							.repoDownloadCommitDiffOrPatch(owner, repo, sha, "diff");
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<String> call, @NonNull Response<String> response) {
						binding.expressiveLoader.setVisibility(View.GONE);
						if (response.isSuccessful() && response.body() != null) {
							processDiff(response.body());
						} else {
							Toasty.show(DiffActivity.this, getString(R.string.failed_diff));
						}
					}

					@Override
					public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
						binding.expressiveLoader.setVisibility(View.GONE);
						Toasty.show(DiffActivity.this, getString(R.string.network_error));
					}
				});
	}

	@SuppressLint("NotifyDataSetChanged")
	private void processDiff(String fullDiff) {
		if (fullDiff == null || filePath == null) return;

		String[] allLines = fullDiff.split("\\R");
		List<String> filteredLines = new ArrayList<>();
		boolean foundFile = false;
		boolean skippingHeader = true;

		for (String line : allLines) {
			if (line.startsWith("diff --git") && line.contains(filePath)) {
				foundFile = true;
				skippingHeader = true;
				continue;
			}

			if (line.startsWith("diff --git") && foundFile) break;

			if (foundFile) {
				if (skippingHeader) {
					if (line.startsWith("@@")) {
						skippingHeader = false;
						filteredLines.add(line);
					}
					continue;
				}
				filteredLines.add(line);
			}
		}

		diffLines.clear();
		if (filteredLines.isEmpty()) {
			diffLines.add(getString(R.string.no_changes_found));
		} else {
			diffLines.addAll(filteredLines);
		}
		adapter.notifyDataSetChanged();
	}
}
