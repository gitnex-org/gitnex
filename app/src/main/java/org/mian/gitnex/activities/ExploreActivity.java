package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityExploreBinding;
import org.mian.gitnex.fragments.ExploreIssuesFragment;
import org.mian.gitnex.fragments.ExplorePublicOrganizationsFragment;
import org.mian.gitnex.fragments.ExploreRepositoriesFragment;
import org.mian.gitnex.fragments.ExploreUsersFragment;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.ViewPager2Transformers;

/**
 * @author mmarif
 */
public class ExploreActivity extends BaseActivity {

	private ActivityExploreBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityExploreBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		setSupportActionBar(binding.toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		setupViewPager();
	}

	private void setupViewPager() {
		binding.containerExplore.setOffscreenPageLimit(1);

		ExplorePagerAdapter adapter = new ExplorePagerAdapter(this);
		binding.containerExplore.setAdapter(adapter);
		ViewPager2Transformers.returnSelectedTransformer(
				binding.containerExplore,
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								this, AppDatabaseSettings.APP_TABS_ANIMATION_KEY)));

		String[] tabTitles = {
			getString(R.string.navRepos),
			getString(R.string.pageTitleIssues),
			getString(R.string.navOrg),
			getString(R.string.pageTitleUsers)
		};

		new TabLayoutMediator(
						binding.tabsExplore,
						binding.containerExplore,
						(tab, position) -> tab.setText(tabTitles[position]))
				.attach();
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private static class ExplorePagerAdapter extends FragmentStateAdapter {

		public ExplorePagerAdapter(@NonNull FragmentActivity fa) {
			super(fa);
		}

		@NonNull @Override
		public Fragment createFragment(int position) {
			return switch (position) {
				case 0 -> new ExploreRepositoriesFragment();
				case 1 -> new ExploreIssuesFragment();
				case 2 -> new ExplorePublicOrganizationsFragment();
				case 3 -> new ExploreUsersFragment();
				default -> throw new IllegalStateException("Unexpected position: " + position);
			};
		}

		@Override
		public int getItemCount() {
			return 4;
		}
	}
}
