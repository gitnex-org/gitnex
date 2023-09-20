package org.mian.gitnex.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayoutMediator;
import io.mikael.urlbuilder.UrlBuilder;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityOrgDetailBinding;
import org.mian.gitnex.fragments.BottomSheetOrganizationFragment;
import org.mian.gitnex.fragments.OrganizationInfoFragment;
import org.mian.gitnex.fragments.OrganizationLabelsFragment;
import org.mian.gitnex.fragments.OrganizationMembersFragment;
import org.mian.gitnex.fragments.OrganizationRepositoriesFragment;
import org.mian.gitnex.fragments.OrganizationTeamsFragment;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ViewPager2Transformers;
import org.mian.gitnex.structs.BottomSheetListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class OrganizationDetailActivity extends BaseActivity implements BottomSheetListener {

	public static boolean updateOrgFABActions = false;
	public OrganizationPermissions permissions;
	private String orgName;
	private boolean isMember = false;
	private ActivityOrgDetailBinding activityOrgDetailBinding;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityOrgDetailBinding = ActivityOrgDetailBinding.inflate(getLayoutInflater());
		setContentView(activityOrgDetailBinding.getRoot());

		orgName = getIntent().getStringExtra("orgName");

		setSupportActionBar(activityOrgDetailBinding.toolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle(orgName);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		checkIsMember();

		if (getAccount().requiresVersion("1.16.0")) {
			RetrofitClient.getApiInterface(this)
					.orgGetUserPermissions(getAccount().getAccount().getUserName(), orgName)
					.enqueue(
							new Callback<>() {

								@Override
								public void onResponse(
										@NonNull Call<OrganizationPermissions> call,
										@NonNull Response<OrganizationPermissions> response) {

									if (response.isSuccessful()) {
										permissions = response.body();
									} else {
										permissions = null;
									}
								}

								@Override
								public void onFailure(
										@NonNull Call<OrganizationPermissions> call,
										@NonNull Throwable t) {

									permissions = null;
								}
							});
		} else {
			permissions = null;
		}
	}

	public void checkIsMember() {
		RetrofitClient.getApiInterface(this)
				.orgIsMember(orgName, getAccount().getAccount().getUserName())
				.enqueue(
						new Callback<>() {

							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								isMember = response.code() != 404;
								init();
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								isMember = false;
								init();
							}
						});
	}

	public void init() {

		ViewPager2 viewPager = activityOrgDetailBinding.container;
		viewPager.setOffscreenPageLimit(1);

		ViewGroup vg = (ViewGroup) activityOrgDetailBinding.tabs.getChildAt(0);

		Typeface myTypeface = AppUtil.getTypeface(ctx);

		activityOrgDetailBinding.toolbarTitle.setTypeface(myTypeface);
		activityOrgDetailBinding.toolbarTitle.setText(orgName);

		viewPager.setAdapter(new OrganizationDetailActivity.ViewPagerAdapter(this));

		ViewPager2Transformers.returnSelectedTransformer(
				viewPager, tinyDB.getInt("fragmentTabsAnimationId", 0));

		String[] tabTitles = {
			getResources().getString(R.string.tabTextInfo),
			getResources().getString(R.string.navRepos),
			getResources().getString(R.string.newIssueLabelsTitle),
			getResources().getString(R.string.orgTabTeams),
			getResources().getString(R.string.orgTabMembers)
		};

		if (!isMember) {
			activityOrgDetailBinding.tabs.removeTabAt(3);
		}

		new TabLayoutMediator(
						activityOrgDetailBinding.tabs,
						viewPager,
						(tab, position) -> tab.setText(tabTitles[position]))
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
	}

	public class ViewPagerAdapter extends FragmentStateAdapter {

		public ViewPagerAdapter(@NonNull FragmentActivity fa) {
			super(fa);
		}

		@NonNull @Override
		public Fragment createFragment(int position) {
			switch (position) {
				case 0: // info
					return OrganizationInfoFragment.newInstance(orgName);
				case 1: // repos
					return OrganizationRepositoriesFragment.newInstance(orgName, permissions);
				case 2: // labels
					return OrganizationLabelsFragment.newInstance(orgName, permissions);
				case 3: // teams / members
					if (isMember) {
						return OrganizationTeamsFragment.newInstance(orgName, permissions);
					} else {
						return OrganizationMembersFragment.newInstance(orgName);
					}
				case 4: // members
					if (isMember) {
						return OrganizationMembersFragment.newInstance(orgName);
					}
			}
			return null;
		}

		@Override
		public int getItemCount() {
			if (isMember) {
				return 5;
			} else {
				return 4;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.repo_dotted_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if (id == android.R.id.home) {

			finish();
			return true;
		} else if (id == R.id.repoMenu) {

			BottomSheetOrganizationFragment bottomSheet = new BottomSheetOrganizationFragment();
			bottomSheet.show(getSupportFragmentManager(), "orgBottomSheet");
			return true;
		} else {

			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onButtonClicked(String text) {

		String url =
				UrlBuilder.fromString(getAccount().getAccount().getInstanceUrl())
						.withPath("/")
						.toString();
		url = url + getIntent().getStringExtra("orgName");

		switch (text) {
			case "copyOrgUrl":
				AppUtil.copyToClipboard(this, url, ctx.getString(R.string.copyIssueUrlToastMsg));
				break;
			case "share":
				AppUtil.sharingIntent(this, url);
				break;
			case "open":
				AppUtil.openUrlInBrowser(this, url);
				break;
		}
	}
}
