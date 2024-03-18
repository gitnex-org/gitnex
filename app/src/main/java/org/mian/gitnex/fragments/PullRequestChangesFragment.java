package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.FragmentPrChangesBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.ViewPager2Transformers;

/**
 * @author qwerty287
 */
public class PullRequestChangesFragment extends Fragment {

	private final DiffFilesFragment diffFilesFragment = DiffFilesFragment.newInstance();
	private final PullRequestCommitsFragment pullRequestCommitsFragment =
			PullRequestCommitsFragment.newInstance();

	public PullRequestChangesFragment() {}

	public static PullRequestChangesFragment newInstance() {
		return new PullRequestChangesFragment();
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		FragmentPrChangesBinding binding =
				FragmentPrChangesBinding.inflate(inflater, container, false);

		binding.close.setOnClickListener(v -> requireActivity().finish());

		binding.container.setAdapter(
				new FragmentStateAdapter(requireActivity()) {

					@NonNull @Override
					public Fragment createFragment(int position) {

						if (position == 0) {
							return diffFilesFragment;
						} else {
							return pullRequestCommitsFragment;
						}
					}

					@Override
					public int getItemCount() {

						return 2;
					}
				});
		String[] tabs =
				new String[] {getString(R.string.tabTextFiles), getString(R.string.commits)};

		ViewPager2Transformers.returnSelectedTransformer(
				binding.container,
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								getContext(), AppDatabaseSettings.APP_TABS_ANIMATION_KEY)));

		new TabLayoutMediator(
						binding.tabs,
						binding.container,
						(tab, position) -> tab.setText(tabs[position]))
				.attach();

		return binding.getRoot();
	}
}
