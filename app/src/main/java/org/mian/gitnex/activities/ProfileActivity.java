package org.mian.gitnex.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import org.mian.gitnex.R;
import org.mian.gitnex.fragments.profile.DetailFragment;
import org.mian.gitnex.fragments.profile.FollowersFragment;
import org.mian.gitnex.fragments.profile.FollowingFragment;
import org.mian.gitnex.fragments.profile.OrganizationsFragment;
import org.mian.gitnex.fragments.profile.RepositoriesFragment;
import org.mian.gitnex.fragments.profile.StarredRepositoriesFragment;
import org.mian.gitnex.helpers.Toasty;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class ProfileActivity extends BaseActivity {

	private String username;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		Intent profileIntent = getIntent();
		Typeface myTypeface;

		Toolbar toolbar = findViewById(R.id.toolbar);
		TextView toolbarTitle = findViewById(R.id.toolbarTitle);

		if(profileIntent.getStringExtra("username") != null && !Objects.equals(profileIntent.getStringExtra("username"), "")) {
			username = profileIntent.getStringExtra("username");
		}
		else {
			Toasty.warning(ctx, ctx.getResources().getString(R.string.userInvalidUserName));
			finish();
		}

		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle(username);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		ViewPager2 viewPager = findViewById(R.id.profileContainer);
		viewPager.setOffscreenPageLimit(1);
		TabLayout tabLayout = findViewById(R.id.tabs);

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

		toolbarTitle.setTypeface(myTypeface);
		toolbarTitle.setText(username);

		viewPager.setAdapter(new ViewPagerAdapter(this));

		String[] tabTitles = {ctx.getResources().getString(R.string.tabTextInfo), ctx.getResources().getString(R.string.navRepos), ctx.getResources().getString(R.string.navStarredRepos), ctx.getResources().getString(R.string.navOrg), ctx.getResources().getString(R.string.profileTabFollowers), ctx.getResources().getString(R.string.profileTabFollowing)};
		new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabTitles[position])).attach();

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
	}

	public class ViewPagerAdapter extends FragmentStateAdapter {

		public ViewPagerAdapter(@NonNull FragmentActivity fa) { super(fa); }

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			switch(position) {
				case 0: // detail
					return DetailFragment.newInstance(username);
				case 1: // repos
					return RepositoriesFragment.newInstance(username);
				case 2: // starred repos
					return StarredRepositoriesFragment.newInstance(username);
				case 3: // organizations
					return OrganizationsFragment.newInstance(username);
				case 4: // followers
					return FollowersFragment.newInstance(username);
				case 5: // following
					return FollowingFragment.newInstance(username);
			}
			return null;
		}

		@Override
		public int getItemCount() {
			return 6;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if(id == android.R.id.home) {
			finish();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}
}
