package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.BottomSheetIssuesFilterFragment;
import org.mian.gitnex.fragments.BottomSheetPullRequestFilterFragment;
import org.mian.gitnex.fragments.BottomSheetRepoFragment;
import org.mian.gitnex.fragments.BranchesFragment;
import org.mian.gitnex.fragments.CollaboratorsFragment;
import org.mian.gitnex.fragments.FilesFragment;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.fragments.LabelsFragment;
import org.mian.gitnex.fragments.MilestonesFragment;
import org.mian.gitnex.fragments.PullRequestsFragment;
import org.mian.gitnex.fragments.ReleasesFragment;
import org.mian.gitnex.fragments.RepoInfoFragment;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.VersionCheck;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.models.WatchRepository;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class RepoDetailActivity extends BaseActivity implements BottomSheetRepoFragment.BottomSheetListener, BottomSheetIssuesFilterFragment.BottomSheetListener, BottomSheetPullRequestFilterFragment.BottomSheetListener {

	private TextView textViewBadgeIssue;
	private TextView textViewBadgePull;
	private TextView textViewBadgeRelease;

	private FragmentRefreshListener fragmentRefreshListener;
	private FragmentRefreshListenerPr fragmentRefreshListenerPr;

	final Context ctx = this;
	private Context appCtx;

	// issues interface
	public FragmentRefreshListener getFragmentRefreshListener() {

		return fragmentRefreshListener;
	}

	public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {

		this.fragmentRefreshListener = fragmentRefreshListener;
	}

	public interface FragmentRefreshListener {

		void onRefresh(String text);

	}

	// pr interface
	public FragmentRefreshListenerPr getFragmentRefreshListenerPr() {

		return fragmentRefreshListenerPr;
	}

	public void setFragmentRefreshListenerPr(FragmentRefreshListenerPr fragmentRefreshListenerPr) {

		this.fragmentRefreshListenerPr = fragmentRefreshListenerPr;
	}

	public interface FragmentRefreshListenerPr {

		void onRefresh(String text);

	}

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_repo_detail;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		TinyDB tinyDb = new TinyDB(appCtx);
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		String repoName1 = parts[1];

		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String repoOwner = parts[0];
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		tinyDb.putString("repoIssuesState", "open");
		tinyDb.putString("repoPrState", "open");

		String appLocale = tinyDb.getString("locale");
		AppUtil.setAppLocale(getResources(), appLocale);

		Toolbar toolbar = findViewById(R.id.toolbar);
		TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);

		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle(repoName1);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		ViewPager mViewPager = findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabLayout tabLayout = findViewById(R.id.tabs);

		Typeface myTypeface;

		switch(tinyDb.getInt("customFontId", -1)) {

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
		toolbarTitle.setText(repoName1);

		ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
		int tabsCount = vg.getChildCount();
		for(int j = 0; j < tabsCount; j++) {
			ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
			int tabChildCount = vgTab.getChildCount();
			for(int i = 0; i < tabChildCount; i++) {
				View tabViewChild = vgTab.getChildAt(i);
				if(tabViewChild instanceof TextView) {
					((TextView) tabViewChild).setTypeface(myTypeface);
				}
			}
		}

		// only show Collaborators if you have permission to
		final View collaboratorTab = vg.getChildAt(8);
		if(tinyDb.getBoolean("isRepoAdmin")) {
			collaboratorTab.setVisibility(View.VISIBLE);
		}
		else {
			collaboratorTab.setVisibility(View.GONE);
		}

		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

		if(tinyDb.getBoolean("enableCounterBadges")) {

			@SuppressLint("InflateParams") View tabHeader2 = LayoutInflater.from(this).inflate(R.layout.badge_issue, null);
			textViewBadgeIssue = tabHeader2.findViewById(R.id.counterBadgeIssue);

			@SuppressLint("InflateParams") View tabHeader4 = LayoutInflater.from(this).inflate(R.layout.badge_pull, null);
			textViewBadgePull = tabHeader4.findViewById(R.id.counterBadgePull);

			@SuppressLint("InflateParams") View tabHeader6 = LayoutInflater.from(this).inflate(R.layout.badge_release, null);
			textViewBadgeRelease = tabHeader6.findViewById(R.id.counterBadgeRelease);

			textViewBadgeIssue.setVisibility(View.GONE);
			textViewBadgePull.setVisibility(View.GONE);
			textViewBadgeRelease.setVisibility(View.GONE);

			getRepoInfo(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName1);
			ColorStateList textColor = tabLayout.getTabTextColors();

			// issue count
			if(textViewBadgeIssue.getText() != "") {
				TabLayout.Tab tabOpenIssues = tabLayout.getTabAt(2);
				Objects.requireNonNull(tabLayout.getTabAt(2)).setCustomView(tabHeader2);
				assert tabOpenIssues != null;
				TextView openIssueTabView = Objects.requireNonNull(tabOpenIssues.getCustomView()).findViewById(R.id.counterBadgeIssueText);
				openIssueTabView.setTextColor(textColor);
			}

			// pull count
			if(textViewBadgePull.getText() != "") { // only show if API returned a number
				Objects.requireNonNull(tabLayout.getTabAt(3)).setCustomView(tabHeader4);
				TabLayout.Tab tabOpenPulls = tabLayout.getTabAt(3);
				assert tabOpenPulls != null;
				TextView openPullTabView = Objects.requireNonNull(tabOpenPulls.getCustomView()).findViewById(R.id.counterBadgePullText);
				openPullTabView.setTextColor(textColor);
			}

			// release count
			if(VersionCheck.compareVersion("1.11.5", tinyDb.getString("giteaVersion")) < 1) {
				if(textViewBadgeRelease.getText() != "") { // only show if API returned a number
					Objects.requireNonNull(tabLayout.getTabAt(5)).setCustomView(tabHeader6);
					TabLayout.Tab tabOpenRelease = tabLayout.getTabAt(5);
					assert tabOpenRelease != null;
					TextView openReleaseTabView = Objects.requireNonNull(tabOpenRelease.getCustomView()).findViewById(R.id.counterBadgeReleaseText);
					openReleaseTabView.setTextColor(textColor);
				}
			}
		}

		checkRepositoryStarStatus(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName1);
		checkRepositoryWatchStatus(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName1);

	}

	@Override
	public void onResume() {

		super.onResume();
		TinyDB tinyDb = new TinyDB(appCtx);
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		if(tinyDb.getBoolean("enableCounterIssueBadge")) {
			getRepoInfo(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.repo_dotted_menu, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		switch(id) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.repoMenu:
				BottomSheetRepoFragment bottomSheet = new BottomSheetRepoFragment();
				bottomSheet.show(getSupportFragmentManager(), "repoBottomSheet");
				return true;
			case R.id.filter:
				BottomSheetIssuesFilterFragment filterBottomSheet = new BottomSheetIssuesFilterFragment();
				filterBottomSheet.show(getSupportFragmentManager(), "repoFilterMenuBottomSheet");
				return true;
			case R.id.filterPr:
				BottomSheetPullRequestFilterFragment filterPrBottomSheet = new BottomSheetPullRequestFilterFragment();
				filterPrBottomSheet.show(getSupportFragmentManager(), "repoFilterMenuPrBottomSheet");
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onButtonClicked(String text) {

		TinyDB tinyDb = new TinyDB(appCtx);

		switch(text) {
			case "label":
				startActivity(new Intent(RepoDetailActivity.this, CreateLabelActivity.class));
				break;
			case "newIssue":
				startActivity(new Intent(RepoDetailActivity.this, CreateIssueActivity.class));
				break;
			case "newMilestone":
				startActivity(new Intent(RepoDetailActivity.this, CreateMilestoneActivity.class));
				break;
			case "addCollaborator":
				startActivity(new Intent(RepoDetailActivity.this, AddCollaboratorToRepositoryActivity.class));
				break;
			case "createRelease":
				startActivity(new Intent(RepoDetailActivity.this, CreateReleaseActivity.class));
				break;
			case "openWebRepo":
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(tinyDb.getString("repoHtmlUrl")));
				startActivity(i);
				break;
			case "shareRepo":
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, tinyDb.getString("repoHtmlUrl"));
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, tinyDb.getString("repoHtmlUrl"));
				startActivity(Intent.createChooser(sharingIntent, tinyDb.getString("repoHtmlUrl")));
				break;
			case "newFile":
				startActivity(new Intent(RepoDetailActivity.this, CreateFileActivity.class));
				break;
			case "openIssues":
				if(getFragmentRefreshListener() != null) {
					getFragmentRefreshListener().onRefresh("open");
				}
				break;
			case "closedIssues":
				if(getFragmentRefreshListener() != null) {
					getFragmentRefreshListener().onRefresh("closed");
				}
				break;
			case "openPr":
				if(getFragmentRefreshListenerPr() != null) {
					getFragmentRefreshListenerPr().onRefresh("open");
				}
				break;
			case "closedPr":
				if(getFragmentRefreshListenerPr() != null) {
					getFragmentRefreshListenerPr().onRefresh("closed");
				}
				break;
		}

	}

	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		SectionsPagerAdapter(FragmentManager fm) {

			super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		}

		@NonNull
		@Override
		public Fragment getItem(int position) {

			TinyDB tinyDb = new TinyDB(appCtx);
			String repoFullName = tinyDb.getString("repoFullName");
			String[] parts = repoFullName.split("/");
			String repoOwner = parts[0];
			String repoName = parts[1];

			Fragment fragment = null;
			switch(position) {
				case 0: // information
					return RepoInfoFragment.newInstance(repoOwner, repoName);
				case 1: // files
					return FilesFragment.newInstance(repoOwner, repoName);
				case 2: // issues
					fragment = new IssuesFragment();
					break;
				case 3: // pull requests
					fragment = new PullRequestsFragment();
					break;
				case 4: // branches
					return BranchesFragment.newInstance(repoOwner, repoName);
				case 5: // releases
					return ReleasesFragment.newInstance(repoOwner, repoName);
				case 6: // milestones
					return MilestonesFragment.newInstance(repoOwner, repoName);
				case 7: // labels
					return LabelsFragment.newInstance(repoOwner, repoName);
				case 8: // collaborators
					return CollaboratorsFragment.newInstance(repoOwner, repoName);
			}
			assert fragment != null;
			return fragment;
		}

		@Override
		public int getCount() {

			return 9;
		}

	}

	private void getRepoInfo(String instanceUrl, String token, final String owner, String repo) {

		TinyDB tinyDb = new TinyDB(appCtx);

		Call<UserRepositories> call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().getUserRepository(token, owner, repo);

		call.enqueue(new Callback<UserRepositories>() {

			@Override
			public void onResponse(@NonNull Call<UserRepositories> call, @NonNull retrofit2.Response<UserRepositories> response) {

				UserRepositories repoInfo = response.body();

				if(response.isSuccessful()) {

					if(response.code() == 200) {

						if(tinyDb.getBoolean("enableCounterBadges")) {
							assert repoInfo != null;

							if(repoInfo.getOpen_issues_count() != null) {
								textViewBadgeIssue.setVisibility(View.VISIBLE);
								textViewBadgeIssue.setText(repoInfo.getOpen_issues_count());
							}

							if(repoInfo.getOpen_pull_count() != null) {
								textViewBadgePull.setVisibility(View.VISIBLE);
								textViewBadgePull.setText(repoInfo.getOpen_pull_count());
							}

							if(repoInfo.getRelease_count() != null) {
								textViewBadgeRelease.setVisibility(View.VISIBLE);
								textViewBadgeRelease.setText(repoInfo.getRelease_count());
							}
						}

					}

				}
				else {
					Log.e("onFailure", String.valueOf(response.code()));
				}

			}

			@Override
			public void onFailure(@NonNull Call<UserRepositories> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}

		});

	}

	private void checkRepositoryStarStatus(String instanceUrl, String instanceToken, final String owner, String repo) {

		Call<JsonElement> call;

		call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().checkRepoStarStatus(instanceToken, owner, repo);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				TinyDB tinyDb = new TinyDB(appCtx);
				tinyDb.putInt("repositoryStarStatus", response.code());

			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	private void checkRepositoryWatchStatus(String instanceUrl, String instanceToken, final String owner, String repo) {

		Call<WatchRepository> call;

		call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().checkRepoWatchStatus(instanceToken, owner, repo);

		call.enqueue(new Callback<WatchRepository>() {

			@Override
			public void onResponse(@NonNull Call<WatchRepository> call, @NonNull retrofit2.Response<WatchRepository> response) {

				TinyDB tinyDb = new TinyDB(appCtx);

				if(response.code() == 200) {
					assert response.body() != null;
					if(response.body().getSubscribed()) {
						tinyDb.putBoolean("repositoryWatchStatus", true);
					}
				}
				else {
					tinyDb.putBoolean("repositoryWatchStatus", false);
				}

			}

			@Override
			public void onFailure(@NonNull Call<WatchRepository> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

}
