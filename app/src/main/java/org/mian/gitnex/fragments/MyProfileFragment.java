package org.mian.gitnex.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import java.util.Locale;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.contexts.AccountContext;

/**
 * @author M M Arif
 */
public class MyProfileFragment extends Fragment {

	private Context ctx;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		ctx = getContext();

		View v = inflater.inflate(R.layout.fragment_profile, container, false);
		setHasOptionsMenu(true);

		((MainActivity) requireActivity())
				.setActionBarTitle(getResources().getString(R.string.navProfile));

		AccountContext account = ((BaseActivity) requireActivity()).getAccount();
		if (account.getUserInfo() != null) {
			viewData(v, account);
		} else {
			// we have to wait until loading is finished
			LinearProgressIndicator loading = v.findViewById(R.id.loadingIndicator);
			loading.setVisibility(View.VISIBLE);
			((MainActivity) requireActivity())
					.setProfileInitListener(
							(text) -> {
								loading.setVisibility(View.GONE);
								viewData(v, account);
							});
		}

		return v;
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		menu.clear();
		requireActivity().getMenuInflater().inflate(R.menu.profile_dotted_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {

		int id = item.getItemId();

		if (id == android.R.id.home) {
			((MainActivity) ctx).finish();
			return true;
		} else if (id == R.id.profileMenu) {
			BottomSheetMyProfileFragment bottomSheet = new BottomSheetMyProfileFragment();
			bottomSheet.show(getChildFragmentManager(), "profileBottomSheet");
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	public void viewData(View v, AccountContext account) {

		TextView userFullName = v.findViewById(R.id.userFullName);
		ImageView userAvatar = v.findViewById(R.id.userAvatar);
		TextView userLogin = v.findViewById(R.id.userLogin);
		TextView userLanguage = v.findViewById(R.id.userLanguage);
		TextView userFollowersCount = v.findViewById(R.id.user_followers_count);
		TextView userFollowingCount = v.findViewById(R.id.user_following_count);
		TextView userStarredReposCount = v.findViewById(R.id.user_starred_repos_count);

		String[] userLanguageCodes =
				account.getUserInfo().getLanguage() != null
						? account.getUserInfo().getLanguage().split("-")
						: new String[] {""};

		if (userLanguageCodes.length >= 2) {
			Locale locale = new Locale(userLanguageCodes[0], userLanguageCodes[1]);
			userLanguage.setText(locale.getDisplayLanguage());
		} else {
			userLanguage.setText(getResources().getConfiguration().locale.getDisplayLanguage());
		}

		userAvatar.setOnClickListener(
				loginId ->
						AppUtil.copyToClipboard(
								ctx,
								account.getAccount().getUserName(),
								ctx.getString(
										R.string.copyLoginIdToClipBoard,
										account.getAccount().getUserName())));

		userFullName.setText(Html.fromHtml(account.getFullName()));
		userLogin.setText(getString(R.string.usernameWithAt, account.getAccount().getUserName()));

		int avatarRadius = AppUtil.getPixelsFromDensity(ctx, 60);

		PicassoService.getInstance(ctx)
				.get()
				.load(account.getUserInfo().getAvatarUrl())
				.transform(new RoundedTransformation(avatarRadius, 0))
				.placeholder(R.drawable.loader_animated)
				.resize(120, 120)
				.centerCrop()
				.into(userAvatar);

		userFollowersCount.setText(String.valueOf(account.getUserInfo().getFollowersCount()));
		userFollowingCount.setText(String.valueOf(account.getUserInfo().getFollowingCount()));
		userStarredReposCount.setText(String.valueOf(account.getUserInfo().getStarredReposCount()));

		MyProfileFragment.SectionsPagerAdapter mSectionsPagerAdapter =
				new SectionsPagerAdapter(getChildFragmentManager());

		ViewPager mViewPager = v.findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		Typeface myTypeface = AppUtil.getTypeface(requireContext());
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
		tabLayout.addOnTabSelectedListener(
				new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
	}

	public static class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		SectionsPagerAdapter(FragmentManager fm) {
			super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		}

		@NonNull @Override
		public Fragment getItem(int position) {

			switch (position) {
				case 0: // followers
					return new MyProfileFollowersFragment();

				case 1: // following
					return new MyProfileFollowingFragment();

				case 2: // emails
					return new MyProfileEmailsFragment();
			}

			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}
	}
}
