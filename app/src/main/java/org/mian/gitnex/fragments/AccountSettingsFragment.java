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
public class AccountSettingsFragment extends Fragment {

	public ViewPager2 viewPager;
	private Context ctx;
	private View view;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		ctx = getContext();

		view = inflater.inflate(R.layout.fragment_account_settings, container, false);

		viewData();

		return view;
	}

	public void viewData() {

		TabLayout tabLayout = view.findViewById(R.id.tabs);

		if (viewPager == null) {

			viewPager = view.findViewById(R.id.accountSettingsContainer);
			viewPager.setOffscreenPageLimit(1);

			viewPager.setAdapter(new ViewPagerAdapter(this));

			ViewPager2Transformers.returnSelectedTransformer(
					viewPager,
					Integer.parseInt(
							AppDatabaseSettings.getSettingsValue(
									ctx, AppDatabaseSettings.APP_TABS_ANIMATION_KEY)));

			String[] tabTitles = {
				ctx.getResources().getString(R.string.accountEmails),
				ctx.getResources().getString(R.string.sshKeys)
			};
			new TabLayoutMediator(
							tabLayout,
							viewPager,
							(tab, position) -> tab.setText(tabTitles[position]))
					.attach();
		}
	}

	public static class ViewPagerAdapter extends FragmentStateAdapter {

		public ViewPagerAdapter(@NonNull Fragment fa) {
			super(fa);
		}

		@NonNull @Override
		public Fragment createFragment(int position) {

			Fragment fragment = null;

			switch (position) {
				case 0: // Emails
					return new AccountSettingsEmailsFragment();
				case 1: // SSH keys
					return new SSHKeysFragment();
			}
			assert false;
			return fragment;
		}

		@Override
		public int getItemCount() {
			return 2;
		}
	}
}
