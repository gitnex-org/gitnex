package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityPullRequestDiffBinding;
import org.mian.gitnex.fragments.DiffFilesFragment;
import org.mian.gitnex.fragments.PullRequestCommitsFragment;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.PullRequestDiffViewModel;

/**
 * @author mmarif
 */
public class PullRequestDiffActivity extends BaseActivity {

	private ActivityPullRequestDiffBinding binding;
	private View detachedDivider;
	private View detachedSearchBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityPullRequestDiffBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, null, null, null);

		PullRequestDiffViewModel viewModel =
				new ViewModelProvider(this).get(PullRequestDiffViewModel.class);
		viewModel.reset();

		detachedDivider = binding.dividerSearch;
		detachedSearchBtn = binding.btnSearch;

		setupDock();
		updateNavUI(true);
		showFragment(new DiffFilesFragment());
	}

	private void setupDock() {
		binding.btnBack.setOnClickListener(v -> finish());

		binding.btnNavFiles.setBackgroundResource(R.drawable.nav_pill_background);
		binding.btnNavCommits.setBackgroundResource(R.drawable.nav_pill_background);

		binding.btnNavFiles.setOnClickListener(
				v -> {
					updateNavUI(true);
					showFragment(new DiffFilesFragment());
				});

		binding.btnNavCommits.setOnClickListener(
				v -> {
					updateNavUI(false);
					showFragment(new PullRequestCommitsFragment());
				});

		detachedSearchBtn.setOnClickListener(v -> binding.searchView.show());
	}

	private void updateNavUI(boolean showingFiles) {
		binding.btnNavFiles.setSelected(showingFiles);
		binding.btnNavCommits.setSelected(!showingFiles);

		ViewGroup parent = (ViewGroup) binding.btnNavFiles.getParent();

		if (showingFiles) {
			parent.removeView(detachedDivider);
			parent.removeView(detachedSearchBtn);
		} else {
			if (detachedDivider.getParent() == null) {
				parent.addView(detachedDivider);
			}
			if (detachedSearchBtn.getParent() == null) {
				parent.addView(detachedSearchBtn);
			}
		}

		binding.dockedToolbar.requestLayout();
	}

	private void showFragment(Fragment fragment) {
		getSupportFragmentManager()
				.beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.replace(R.id.fragment_container, fragment)
				.commit();
	}
}
