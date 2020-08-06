package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
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
import org.mian.gitnex.fragments.BottomSheetMilestonesFilterFragment;
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
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.Branches;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.models.WatchInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class RepoDetailActivity extends BaseActivity implements BottomSheetRepoFragment.BottomSheetListener, BottomSheetIssuesFilterFragment.BottomSheetListener,
		BottomSheetPullRequestFilterFragment.BottomSheetListener, BottomSheetMilestonesFilterFragment.BottomSheetListener {

	private TextView textViewBadgeIssue;
	private TextView textViewBadgePull;
	private TextView textViewBadgeRelease;

	private FragmentRefreshListener fragmentRefreshListener;
	private FragmentRefreshListenerPr fragmentRefreshListenerPr;
	private FragmentRefreshListenerMilestone fragmentRefreshListenerMilestone;
	private FragmentRefreshListenerFiles fragmentRefreshListenerFiles;

	private final Context ctx = this;
	private Context appCtx;

	private TinyDB tinyDB;

	private String instanceUrl;
	private String loginUid;
	private String instanceToken;

	private String repositoryOwner;
	private String repositoryName;

	private int tabsCount;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_repo_detail;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		tinyDB = new TinyDB(appCtx);

		String[] repoNameParts = tinyDB.getString("repoFullName").split("/");
		repositoryOwner = repoNameParts[0];
		repositoryName = repoNameParts[1];

		Toolbar toolbar = findViewById(R.id.toolbar);

		TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
		toolbarTitle.setText(repositoryName);

		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle(repositoryName);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		instanceUrl = tinyDB.getString("instanceUrl");
		loginUid = tinyDB.getString("loginUid");
		instanceToken = "token " + tinyDB.getString(loginUid + "-token");

		tinyDB.putString("repoIssuesState", "open");
		tinyDB.putString("repoPrState", "open");
		tinyDB.putString("milestoneState", "open");

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

		toolbarTitle.setTypeface(myTypeface);

		TabLayout tabLayout = findViewById(R.id.tabs);

		ViewGroup viewGroup = (ViewGroup) tabLayout.getChildAt(0);
		tabsCount = viewGroup.getChildCount();

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

		// Only show collaborators tab, if you have permission to
		View collaboratorTab = viewGroup.getChildAt(8);

		if(tinyDB.getBoolean("isRepoAdmin") || new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.12.0")) {

			collaboratorTab.setVisibility(View.VISIBLE);
		}
		else {

			tabsCount--;
			collaboratorTab.setVisibility(View.GONE);

		}

		ViewPager mViewPager = findViewById(R.id.container);

		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

		SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mSectionsPagerAdapter);

		if(tinyDB.getBoolean("enableCounterBadges")) {

			@SuppressLint("InflateParams") View tabHeader2 = LayoutInflater.from(this).inflate(R.layout.badge_issue, null);
			textViewBadgeIssue = tabHeader2.findViewById(R.id.counterBadgeIssue);

			@SuppressLint("InflateParams") View tabHeader4 = LayoutInflater.from(this).inflate(R.layout.badge_pull, null);
			textViewBadgePull = tabHeader4.findViewById(R.id.counterBadgePull);

			@SuppressLint("InflateParams") View tabHeader6 = LayoutInflater.from(this).inflate(R.layout.badge_release, null);
			textViewBadgeRelease = tabHeader6.findViewById(R.id.counterBadgeRelease);

			textViewBadgeIssue.setVisibility(View.GONE);
			textViewBadgePull.setVisibility(View.GONE);
			textViewBadgeRelease.setVisibility(View.GONE);

			getRepoInfo(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repositoryOwner, repositoryName);
			ColorStateList textColor = tabLayout.getTabTextColors();

			// Issue count
			if(textViewBadgeIssue.getText() != "") {

				TabLayout.Tab tabOpenIssues = tabLayout.getTabAt(2);
				Objects.requireNonNull(tabLayout.getTabAt(2)).setCustomView(tabHeader2);
				assert tabOpenIssues != null; // FIXME This should be cleaned up
				TextView openIssueTabView = Objects.requireNonNull(tabOpenIssues.getCustomView()).findViewById(R.id.counterBadgeIssueText);
				openIssueTabView.setTextColor(textColor);

			}

			// Pull request count
			if(textViewBadgePull.getText() != "") { // only show if API returned a number

				Objects.requireNonNull(tabLayout.getTabAt(3)).setCustomView(tabHeader4);
				TabLayout.Tab tabOpenPulls = tabLayout.getTabAt(3);
				assert tabOpenPulls != null; // FIXME This should be cleaned up
				TextView openPullTabView = Objects.requireNonNull(tabOpenPulls.getCustomView()).findViewById(R.id.counterBadgePullText);
				openPullTabView.setTextColor(textColor);

			}

			// Release count
			if(new Version("1.11.4").less(tinyDB.getString("giteaVersion"))) {

				if(textViewBadgeRelease.getText() != "") { // only show if API returned a number

					Objects.requireNonNull(tabLayout.getTabAt(5)).setCustomView(tabHeader6);
					TabLayout.Tab tabOpenRelease = tabLayout.getTabAt(5);
					assert tabOpenRelease != null; // FIXME This should be cleaned up
					TextView openReleaseTabView = Objects.requireNonNull(tabOpenRelease.getCustomView()).findViewById(R.id.counterBadgeReleaseText);
					openReleaseTabView.setTextColor(textColor);

				}
			}
		}

		checkRepositoryStarStatus(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repositoryOwner, repositoryName);
		checkRepositoryWatchStatus(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repositoryOwner, repositoryName);

	}

	@Override
	public void onResume() {

		super.onResume();

		if(tinyDB.getBoolean("enableCounterIssueBadge")) {

			getRepoInfo(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repositoryOwner, repositoryName);
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

			case R.id.filterMilestone:
				BottomSheetMilestonesFilterFragment filterMilestoneBottomSheet = new BottomSheetMilestonesFilterFragment();
				filterMilestoneBottomSheet.show(getSupportFragmentManager(), "repoFilterMenuMilestoneBottomSheet");
				return true;

			case R.id.switchBranches:
				chooseBranch();
				return true;

			default:
				return super.onOptionsItemSelected(item);

		}

	}

	@Override
	public void onButtonClicked(String text) {

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

			case "chooseBranch":
				chooseBranch();
				break;

			case "createRelease":
				startActivity(new Intent(RepoDetailActivity.this, CreateReleaseActivity.class));
				break;

			case "openWebRepo":
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(tinyDB.getString("repoHtmlUrl")));
				startActivity(i);
				break;

			case "shareRepo":
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, tinyDB.getString("repoHtmlUrl"));
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, tinyDB.getString("repoHtmlUrl"));
				startActivity(Intent.createChooser(sharingIntent, tinyDB.getString("repoHtmlUrl")));
				break;

			case "copyRepoUrl":
				ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("repoUrl", tinyDB.getString("repoHtmlUrl"));
				assert clipboard != null;
				clipboard.setPrimaryClip(clip);
				Toasty.info(ctx, ctx.getString(R.string.copyIssueUrlToastMsg));
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

			case "openMilestone":
				if(getFragmentRefreshListenerMilestone() != null) {
					getFragmentRefreshListenerMilestone().onRefresh("open");
				}
				break;

			case "closedMilestone":
				if(getFragmentRefreshListenerMilestone() != null) {
					getFragmentRefreshListenerMilestone().onRefresh("closed");
				}
				break;

		}

	}

	private void chooseBranch() {

		Call<List<Branches>> call = RetrofitClient.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.getBranches(instanceToken, repositoryOwner, repositoryName);

		call.enqueue(new Callback<List<Branches>>() {

			@Override
			public void onResponse(@NonNull Call<List<Branches>> call, @NonNull Response<List<Branches>> response) {

				if(response.code() == 200) {

					List<String> branchesList = new ArrayList<>();
					int selectedBranch = 0;
					assert response.body() != null;

					for(int i = 0; i < response.body().size(); i++) {

						Branches branches = response.body().get(i);
						branchesList.add(branches.getName());

						if(tinyDB.getString("repoBranch").equals(branches.getName())) {

							selectedBranch = i;
						}
					}

					AlertDialog.Builder pBuilder = new AlertDialog.Builder(ctx);
					pBuilder.setTitle(R.string.pageTitleChooseBranch);

					pBuilder.setSingleChoiceItems(branchesList.toArray(new String[0]), selectedBranch, new  DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialogInterface, int i) {

							tinyDB.putString("repoBranch", branchesList.get(i));
							if(getFragmentRefreshListenerFiles() != null) {
								getFragmentRefreshListenerFiles().onRefresh(branchesList.get(i));
							}
							dialogInterface.dismiss();
						}
					});
					pBuilder.setNeutralButton(R.string.cancelButton, null);

					pBuilder.create().show();

				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Branches>> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		SectionsPagerAdapter(FragmentManager fm) {

			super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		}

		@NonNull
		@Override
		public Fragment getItem(int position) {

			Fragment fragment = null;

			switch(position) {

				case 0: // Repository details
					return RepoInfoFragment.newInstance(repositoryOwner, repositoryName);

				case 1: // Files
					return FilesFragment.newInstance(repositoryOwner, repositoryName, tinyDB.getString("repoBranch"));

				case 2: // Issues
					fragment = new IssuesFragment();
					break;

				case 3: // Pull requests
					fragment = new PullRequestsFragment();
					break;

				case 4: // Branches
					return BranchesFragment.newInstance(repositoryOwner, repositoryName);

				case 5: // Releases
					return ReleasesFragment.newInstance(repositoryOwner, repositoryName);

				case 6: // Milestones
					fragment = new MilestonesFragment();
					break;

				case 7: // Labels
					return LabelsFragment.newInstance(repositoryOwner, repositoryName);

				case 8: // Collaborators
					return CollaboratorsFragment.newInstance(repositoryOwner, repositoryName);

			}

			assert fragment != null;
			return fragment;

		}

		@Override
		public int getCount() {

			return tabsCount;
		}

	}

	private void getRepoInfo(String instanceUrl, String token, final String owner, String repo) {

		Call<UserRepositories> call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().getUserRepository(token, owner, repo);
		call.enqueue(new Callback<UserRepositories>() {

			@Override
			public void onResponse(@NonNull Call<UserRepositories> call, @NonNull retrofit2.Response<UserRepositories> response) {

				UserRepositories repoInfo = response.body();

				if(response.code() == 200) {

					if(tinyDB.getBoolean("enableCounterBadges")) {
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

		Call<JsonElement> call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().checkRepoStarStatus(instanceToken, owner, repo);
		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				tinyDB.putInt("repositoryStarStatus", response.code());

			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	private void checkRepositoryWatchStatus(String instanceUrl, String instanceToken, final String owner, String repo) {

		Call<WatchInfo> call;

		call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().checkRepoWatchStatus(instanceToken, owner, repo);
		call.enqueue(new Callback<WatchInfo>() {

			@Override
			public void onResponse(@NonNull Call<WatchInfo> call, @NonNull retrofit2.Response<WatchInfo> response) {

				if(response.code() == 200) {

					assert response.body() != null;

					if(response.body().getSubscribed()) {
						tinyDB.putBoolean("repositoryWatchStatus", true);
					}

				}
				else {

					tinyDB.putBoolean("repositoryWatchStatus", false);
				}

			}

			@Override
			public void onFailure(@NonNull Call<WatchInfo> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	// Issues interface
	public FragmentRefreshListener getFragmentRefreshListener() { return fragmentRefreshListener; }

	public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) { this.fragmentRefreshListener = fragmentRefreshListener; }

	public interface FragmentRefreshListener { void onRefresh(String text); }

	// Pull request interface
	public FragmentRefreshListenerPr getFragmentRefreshListenerPr() { return fragmentRefreshListenerPr; }

	public void setFragmentRefreshListenerPr(FragmentRefreshListenerPr fragmentRefreshListenerPr) { this.fragmentRefreshListenerPr = fragmentRefreshListenerPr; }

	public interface FragmentRefreshListenerPr { void onRefresh(String text); }

	// Milestones interface
	public FragmentRefreshListenerMilestone getFragmentRefreshListenerMilestone() { return fragmentRefreshListenerMilestone; }

	public void setFragmentRefreshListenerMilestone(FragmentRefreshListenerMilestone fragmentRefreshListenerMilestone) { this.fragmentRefreshListenerMilestone = fragmentRefreshListenerMilestone; }

	public interface FragmentRefreshListenerMilestone { void onRefresh(String text); }

	// Files interface
	public FragmentRefreshListenerFiles getFragmentRefreshListenerFiles() { return fragmentRefreshListenerFiles; }

	public void setFragmentRefreshListenerFiles(FragmentRefreshListenerFiles fragmentRefreshListenerFiles) { this.fragmentRefreshListenerFiles = fragmentRefreshListenerFiles; }

	public interface FragmentRefreshListenerFiles { void onRefresh(String text); }

}
