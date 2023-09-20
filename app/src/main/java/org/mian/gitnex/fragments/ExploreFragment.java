package org.mian.gitnex.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.ViewPager2Transformers;

/**
 * @author M M Arif
 */
public class ExploreFragment extends Fragment {

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_explore, container, false);

		Context ctx = getContext();
		TinyDB tinyDB = TinyDB.getInstance(ctx);

		((MainActivity) requireActivity())
				.setActionBarTitle(getResources().getString(R.string.pageTitleExplore));

		ViewPager2 viewPager = view.findViewById(R.id.containerExplore);
		viewPager.setOffscreenPageLimit(1);
		TabLayout tabLayout = view.findViewById(R.id.tabsExplore);

		ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);

		Typeface myTypeface = AppUtil.getTypeface(requireContext());
		viewPager.setAdapter(new ViewPagerAdapter(this));

		ViewPager2Transformers.returnSelectedTransformer(
				viewPager, tinyDB.getInt("fragmentTabsAnimationId", 0));

		String[] tabTitles = {
			getResources().getString(R.string.navRepos),
			getResources().getString(R.string.pageTitleIssues),
			getResources().getString(R.string.navOrg),
			getResources().getString(R.string.pageTitleUsers)
		};
		new TabLayoutMediator(
						tabLayout, viewPager, (tab, position) -> tab.setText(tabTitles[position]))
				.attach();

		for (int j = 0; j < tabTitles.length; j++) {

			ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
			int tabChildCount = vgTab.getChildCount();

			for (int i = 0; i < tabChildCount; i++) {
				View tabViewChild = vgTab.getChildAt(i);
				if (tabViewChild instanceof TextView) {
					((TextView) tabViewChild).setTypeface(myTypeface);
				}
			}
		}

		return view;
	}

	public static class ViewPagerAdapter extends FragmentStateAdapter {

		public ViewPagerAdapter(@NonNull ExploreFragment fa) {
			super(fa);
		}

		@NonNull @Override
		public Fragment createFragment(int position) {
			Fragment fragment = null;
			switch (position) {
				case 0: // Repositories
					fragment = new ExploreRepositoriesFragment();
					break;
				case 1: // Issues
					fragment = new ExploreIssuesFragment();
					break;
				case 2: // Organizations
					fragment = new ExplorePublicOrganizationsFragment();
					break;
				case 3: // Users
					fragment = new ExploreUsersFragment();
					break;
			}
			assert fragment != null;
			return fragment;
		}

		@Override
		public int getItemCount() {
			return 4;
		}
	}
}
