package org.mian.gitnex.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.tabs.TabLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class IssuesMainFragment extends Fragment {

	private Context ctx;

	public IssuesMainFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_issues_main, container, false);
		setHasOptionsMenu(true);

		TinyDB tinyDb = new TinyDB(getContext());

		SectionsPagerAdapter mSectionsPagerAdapter = new IssuesMainFragment.SectionsPagerAdapter(getChildFragmentManager());

		ViewPager mViewPager = v.findViewById(R.id.issuesContainer);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		Typeface myTypeface;
		if(tinyDb.getInt("customFontId") == 0) {

			myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getContext()).getAssets(), "fonts/roboto.ttf");

		}
		else if (tinyDb.getInt("customFontId") == 1) {

			myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getContext()).getAssets(), "fonts/manroperegular.ttf");

		}
		else if (tinyDb.getInt("customFontId") == 2) {

			myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getContext()).getAssets(), "fonts/sourcecodeproregular.ttf");

		}
		else {

			myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getContext()).getAssets(), "fonts/roboto.ttf");

		}

		TabLayout tabLayout = v.findViewById(R.id.tabs);

		ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
		int tabsCount = vg.getChildCount();

		for (int j = 0; j < tabsCount; j++) {

			ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
			int tabChildCount = vgTab.getChildCount();

			for (int i = 0; i < tabChildCount; i++) {

				View tabViewChild = vgTab.getChildAt(i);
				if (tabViewChild instanceof TextView) {
					((TextView) tabViewChild).setTypeface(myTypeface);
				}

			}

		}

		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

		return v;

	}

	public static class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		SectionsPagerAdapter(FragmentManager fm) {
			super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		}

		@NonNull
		@Override
		public Fragment getItem(int position) {

			Fragment fragment = null;
			switch (position) {
				case 0: // open issues
					fragment = new IssuesOpenFragment();
					break;
				case 1: // closed issues
					fragment = new IssuesClosedFragment();
					break;
			}
			assert fragment != null;
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		menu.clear();
		Objects.requireNonNull(getActivity()).getMenuInflater().inflate(R.menu.repo_dotted_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		switch (id) {
			case android.R.id.home:
				return true;
			case R.id.repoMenu:
				BottomSheetRepoFragment bottomSheet = new BottomSheetRepoFragment();
				bottomSheet.show(getChildFragmentManager(), "repoBottomSheet");
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}

	}

}
