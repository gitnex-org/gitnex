package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.ViewPager2Transformers;

/**
 * @author mmarif
 */
public class ExploreFragment extends Fragment {

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_explore, container, false);

		Context ctx = getContext();

		ViewPager2 viewPager = view.findViewById(R.id.containerExplore);
		viewPager.setOffscreenPageLimit(1);
		TabLayout tabLayout = view.findViewById(R.id.tabsExplore);

		ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);

		viewPager.setAdapter(new ViewPagerAdapter(this));

		ViewPager2Transformers.returnSelectedTransformer(
				viewPager,
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_TABS_ANIMATION_KEY)));

		String[] tabTitles = {
			getResources().getString(R.string.navRepos),
			getResources().getString(R.string.pageTitleIssues),
			getResources().getString(R.string.navOrg),
			getResources().getString(R.string.pageTitleUsers)
		};
		new TabLayoutMediator(
						tabLayout, viewPager, (tab, position) -> tab.setText(tabTitles[position]))
				.attach();

		return view;
	}

	public static class ViewPagerAdapter extends FragmentStateAdapter {

		public ViewPagerAdapter(@NonNull ExploreFragment fa) {
			super(fa);
		}

		@NonNull @Override
		public Fragment createFragment(int position) {
			Fragment fragment =
					switch (position) {
						case 0 -> // Repositories
								new ExploreRepositoriesFragment();
						case 1 -> // Issues
								new ExploreIssuesFragment();
						case 2 -> // Organizations
								new ExplorePublicOrganizationsFragment();
						case 3 -> // Users
								new ExploreUsersFragment();
						default -> null;
					};
			assert fragment != null;
			return fragment;
		}

		@Override
		public int getItemCount() {
			return 4;
		}
	}
}
