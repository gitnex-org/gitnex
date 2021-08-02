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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.helpers.TinyDB;

/**
 * Author M M Arif
 */

public class ExploreFragment extends Fragment {

	private int tabsCount;
	public ViewPager mViewPager;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_explore, container, false);

		Context ctx = getContext();
		TinyDB tinyDB = TinyDB.getInstance(ctx);

		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navExplore));

		TabLayout tabLayout = view.findViewById(R.id.tabsExplore);

		ViewGroup viewGroup = (ViewGroup) tabLayout.getChildAt(0);
		tabsCount = viewGroup.getChildCount();

		Typeface myTypeface;

		switch(tinyDB.getInt("customFontId", -1)) {

			case 0:
				myTypeface = Typeface.createFromAsset(ctx.getAssets(), "fonts/roboto.ttf");
				break;

			case 2:
				myTypeface = Typeface.createFromAsset(ctx.getAssets(), "fonts/sourcecodeproregular.ttf");
				break;

			default:
				myTypeface = Typeface.createFromAsset(ctx.getAssets(), "fonts/manroperegular.ttf");
				break;

		}

		for(int j = 0; j < tabsCount; j++) {

			ViewGroup vgTab = (ViewGroup) viewGroup.getChildAt(j);
			int tabChildCount = vgTab.getChildCount();

			for(int i = 0; i < tabChildCount; i++) {

				View tabViewChild = vgTab.getChildAt(i);

				if(tabViewChild instanceof TextView) {
					((TextView) tabViewChild).setTypeface(myTypeface);
				}
			}
		}

		mViewPager = view.findViewById(R.id.containerExplore);

		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

		SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
		mViewPager.setAdapter(mSectionsPagerAdapter);

		if(requireActivity().getIntent().getBooleanExtra("exploreOrgs", false)) {
			mViewPager.setCurrentItem(2);
		}

		return view;

	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		SectionsPagerAdapter(FragmentManager fm) {

			super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		}

		@NonNull
		@Override
		public Fragment getItem(int position) {

			Fragment fragment = null;

			switch(position) {

				case 0: // Repositories
					fragment = new ExploreRepositoriesFragment();
					break;

				case 1: // Issues
					fragment = new ExploreIssuesFragment();
					break;

				case 2: // Organizations
					fragment = new ExplorePublicOrganizationsFragment();
					break;
			}

			assert fragment != null;
			return fragment;

		}

		@Override
		public int getCount() {

			return tabsCount;
		}

	}

}
