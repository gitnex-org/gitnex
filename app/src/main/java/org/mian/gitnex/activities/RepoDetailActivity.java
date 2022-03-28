package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonElement;
import org.gitnex.tea4j.models.Branches;
import org.gitnex.tea4j.models.Milestones;
import org.gitnex.tea4j.models.UserRepositories;
import org.gitnex.tea4j.models.WatchInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.BottomSheetIssuesFilterFragment;
import org.mian.gitnex.fragments.BottomSheetMilestonesFilterFragment;
import org.mian.gitnex.fragments.BottomSheetPullRequestFilterFragment;
import org.mian.gitnex.fragments.BottomSheetReleasesTagsFragment;
import org.mian.gitnex.fragments.BottomSheetRepoFragment;
import org.mian.gitnex.fragments.CollaboratorsFragment;
import org.mian.gitnex.fragments.FilesFragment;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.fragments.LabelsFragment;
import org.mian.gitnex.fragments.MilestonesFragment;
import org.mian.gitnex.fragments.PullRequestsFragment;
import org.mian.gitnex.fragments.ReleasesFragment;
import org.mian.gitnex.fragments.RepoInfoFragment;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.structs.BottomSheetListener;
import org.mian.gitnex.structs.FragmentRefreshListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class RepoDetailActivity extends BaseActivity implements BottomSheetListener {

	private TextView textViewBadgeIssue;
	private TextView textViewBadgePull;
	private TextView textViewBadgeRelease;
	private Typeface myTypeface;

	private FragmentRefreshListener fragmentRefreshListener;
	private FragmentRefreshListener fragmentRefreshListenerPr;
	private FragmentRefreshListener fragmentRefreshListenerMilestone;
	private FragmentRefreshListener fragmentRefreshListenerFiles;
	private FragmentRefreshListener fragmentRefreshListenerFilterIssuesByMilestone;
	private FragmentRefreshListener fragmentRefreshListenerReleases;

	public ViewPager mViewPager;
	private int tabsCount;

	public RepositoryContext repository;

	private final ActivityResultLauncher<Intent> createReleaseLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
		result -> {
			if(result.getResultCode() == 201) {
				assert result.getData() != null;
				if(result.getData().getBooleanExtra("updateReleases", false)) {
					if(fragmentRefreshListenerReleases != null) fragmentRefreshListenerReleases.onRefresh(null);
				}
			}
		});

	private final ActivityResultLauncher<Intent> createMilestoneLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
		result -> {
			if(result.getResultCode() == 201) {
				assert result.getData() != null;
				if(result.getData().getBooleanExtra("milestoneCreated", false)) {
					if(fragmentRefreshListenerMilestone != null) fragmentRefreshListenerMilestone.onRefresh(repository.getMilestoneState().toString());
				}
			}
		});

	private final ActivityResultLauncher<Intent> editFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
		result -> {
			if(result.getResultCode() == 200) {
				assert result.getData() != null;
				if(result.getData().getBooleanExtra("fileModified", false)) {
					if(fragmentRefreshListenerFiles != null) fragmentRefreshListenerFiles.onRefresh(repository.getBranchRef());
				}
			}
		});

	private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
		result -> {
			if(result.getResultCode() == 200) {
				assert result.getData() != null;
				if(result.getData().getBooleanExtra("nameChanged", false)) {
					recreate();
				}
			}
		});



	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_repo_detail);

		repository = RepositoryContext.fromIntent(getIntent());

		Toolbar toolbar = findViewById(R.id.toolbar);

		TextView toolbarTitle = findViewById(R.id.toolbar_title);
		toolbarTitle.setText(repository.getName());

		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle(repository.getName());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

		getRepoInfo(getAccount().getAuthorization(), repository.getOwner(), repository.getName());

		checkRepositoryStarStatus(getAccount().getAuthorization(), repository.getOwner(), repository.getName());
		checkRepositoryWatchStatus(getAccount().getAuthorization(), repository.getOwner(), repository.getName());
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
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

		if(id == android.R.id.home) {

			finish();
			return true;
		}
		else if(id == R.id.repoMenu) {

			if(repository.hasRepository()) {
				BottomSheetRepoFragment bottomSheet = new BottomSheetRepoFragment(repository);
				bottomSheet.show(getSupportFragmentManager(), "repoBottomSheet");
			}
			return true;
		}
		else if(id == R.id.filter) {

			BottomSheetIssuesFilterFragment filterBottomSheet = new BottomSheetIssuesFilterFragment();
			filterBottomSheet.show(getSupportFragmentManager(), "repoFilterMenuBottomSheet");
			return true;
		}
		else if(id == R.id.filterPr) {

			BottomSheetPullRequestFilterFragment filterPrBottomSheet = new BottomSheetPullRequestFilterFragment();
			filterPrBottomSheet.show(getSupportFragmentManager(), "repoFilterMenuPrBottomSheet");
			return true;
		}
		else if(id == R.id.filterMilestone) {

			BottomSheetMilestonesFilterFragment filterMilestoneBottomSheet = new BottomSheetMilestonesFilterFragment();
			filterMilestoneBottomSheet.show(getSupportFragmentManager(), "repoFilterMenuMilestoneBottomSheet");
			return true;
		}
		else if(id == R.id.switchBranches) {

			chooseBranch();
			return true;
		}
		else if(id == R.id.branchCommits) {

			Intent intent = repository.getIntent(ctx, CommitsActivity.class);

			ctx.startActivity(intent);
			return true;
		}
		else if(id == R.id.filterReleases && getAccount().requiresVersion("1.15.0")) {
			BottomSheetReleasesTagsFragment bottomSheetReleasesTagsFragment = new BottomSheetReleasesTagsFragment();
			bottomSheetReleasesTagsFragment.show(getSupportFragmentManager(), "repoFilterReleasesMenuBottomSheet");
			return true;
		}

		return super.onOptionsItemSelected(item);

	}

	@Override
	public void onButtonClicked(String text) {

		switch(text) {

			case "label":

				startActivity(repository.getIntent(ctx, CreateLabelActivity.class));
				break;
			case "newIssue":

				startActivity(repository.getIntent(ctx, CreateIssueActivity.class));
				break;
			case "newMilestone":

				createMilestoneLauncher.launch(repository.getIntent(ctx, CreateMilestoneActivity.class));
				break;
			case "addCollaborator":

				startActivity(repository.getIntent(ctx, AddCollaboratorToRepositoryActivity.class));
				break;
			case "chooseBranch":

				chooseBranch();
				break;
			case "createRelease":

				createReleaseLauncher.launch(repository.getIntent(ctx, CreateReleaseActivity.class));
				break;
			case "openWebRepo":
				AppUtil.openUrlInBrowser(this, repository.getRepository().getHtml_url());
				break;
			case "shareRepo":

				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, repository.getRepository().getHtml_url());
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, repository.getRepository().getHtml_url());
				startActivity(Intent.createChooser(sharingIntent, repository.getRepository().getHtml_url()));
				break;
			case "copyRepoUrl":

				ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("repoUrl", repository.getRepository().getHtml_url());
				assert clipboard != null;
				clipboard.setPrimaryClip(clip);
				Toasty.info(ctx, ctx.getString(R.string.copyIssueUrlToastMsg));
				break;
			case "newFile":

				editFileLauncher.launch(repository.getIntent(ctx, CreateFileActivity.class));
				break;
			case "filterByMilestone":
				filterIssuesByMilestone();
				break;
			case "openIssues":
				repository.setIssueState(RepositoryContext.State.OPEN);
				if(getFragmentRefreshListener() != null) {

					getFragmentRefreshListener().onRefresh("open");
				}
				break;
			case "closedIssues":
				repository.setIssueState(RepositoryContext.State.CLOSED);
				if(getFragmentRefreshListener() != null) {

					getFragmentRefreshListener().onRefresh("closed");
				}
				break;
			case "openPr":
				repository.setPrState(RepositoryContext.State.OPEN);
				if(getFragmentRefreshListenerPr() != null) {
					getFragmentRefreshListenerPr().onRefresh("open");
				}
				break;
			case "closedPr":
				repository.setPrState(RepositoryContext.State.CLOSED);
				if(getFragmentRefreshListenerPr() != null) {

					getFragmentRefreshListenerPr().onRefresh("closed");
				}
				break;
			case "openMilestone":
				repository.setMilestoneState(RepositoryContext.State.OPEN);
				if(getFragmentRefreshListenerMilestone() != null) {

					getFragmentRefreshListenerMilestone().onRefresh("open");
				}
				break;
			case "closedMilestone":
				repository.setMilestoneState(RepositoryContext.State.CLOSED);
				if(getFragmentRefreshListenerMilestone() != null) {

					getFragmentRefreshListenerMilestone().onRefresh("closed");
				}
				break;
			case "repoSettings":

				settingsLauncher.launch(repository.getIntent(ctx, RepositorySettingsActivity.class));
				break;
			case "newPullRequest":

				startActivity(repository.getIntent(ctx, CreatePullRequestActivity.class));
				break;
			case "tags":
				if(getFragmentRefreshListenerReleases() != null) {
					getFragmentRefreshListenerReleases().onRefresh("tags");
				}
				break;
			case "releases":
				if(getFragmentRefreshListenerReleases() != null) {
					getFragmentRefreshListenerReleases().onRefresh("releases");
				}
				break;
			case "unwatch":
				repository.setWatched(false);
				break;
			case "watch":
				repository.setWatched(true);
				break;
			case "unstar":
				repository.setStarred(false);
				break;
			case "star":
				repository.setStarred(true);
				break;
		}
	}

	private void filterIssuesByMilestone() {

		Dialog progressDialog = new Dialog(this);
		progressDialog.setContentView(R.layout.custom_progress_loader);
		progressDialog.show();

		Call<List<Milestones>> call = RetrofitClient
			.getApiInterface(ctx)
			.getMilestones(getAccount().getAuthorization(), repository.getOwner(), repository.getName(), 1, 50, "open");

		call.enqueue(new Callback<List<Milestones>>() {

			@Override
			public void onResponse(@NonNull Call<List<Milestones>> call, @NonNull Response<List<Milestones>> response) {

				progressDialog.hide();
				if(response.code() == 200) {

					Milestones milestones;
					List<String> milestonesList = new ArrayList<>();
					int selectedMilestone = 0;
					assert response.body() != null;

					milestonesList.add("All");
					for(int i = 0; i < response.body().size(); i++) {
						milestones = response.body().get(i);
						milestonesList.add(milestones.getTitle());
						if(repository.getIssueMilestoneFilterName().equals(milestones.getTitle())) {
							selectedMilestone = i;
						}
					}

					AlertDialog.Builder pBuilder = new AlertDialog.Builder(ctx);
					pBuilder.setTitle(R.string.selectMilestone);

					pBuilder.setSingleChoiceItems(milestonesList.toArray(new String[0]), selectedMilestone, (dialogInterface, i) -> {

						repository.setIssueMilestoneFilterName(response.body().get(i).getTitle());

						if(getFragmentRefreshListenerFilterIssuesByMilestone() != null) {
							getFragmentRefreshListenerFilterIssuesByMilestone().onRefresh(milestonesList.get(i));
						}
						dialogInterface.dismiss();
					});

					pBuilder.setNeutralButton(R.string.cancelButton, null);
					pBuilder.create().show();
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Milestones>> call, @NonNull Throwable t) {
				progressDialog.hide();
				Log.e("onFailure", t.toString());
			}
		});
	}

	private void chooseBranch() {

		Dialog progressDialog = new Dialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setContentView(R.layout.custom_progress_loader);
		progressDialog.show();

		Call<List<Branches>> call = RetrofitClient
			.getApiInterface(ctx)
			.getBranches(getAccount().getAuthorization(), repository.getOwner(), repository.getName());

		call.enqueue(new Callback<List<Branches>>() {

			@Override
			public void onResponse(@NonNull Call<List<Branches>> call, @NonNull Response<List<Branches>> response) {

				progressDialog.hide();
				if(response.code() == 200) {

					List<String> branchesList = new ArrayList<>();
					int selectedBranch = 0;
					assert response.body() != null;

					for(int i = 0; i < response.body().size(); i++) {

						Branches branches = response.body().get(i);
						branchesList.add(branches.getName());

						if(repository.getBranchRef().equals(branches.getName())) {
							selectedBranch = i;
						}
					}

					AlertDialog.Builder pBuilder = new AlertDialog.Builder(ctx);
					pBuilder.setTitle(R.string.pageTitleChooseBranch);

					pBuilder.setSingleChoiceItems(branchesList.toArray(new String[0]), selectedBranch, (dialogInterface, i) -> {

						repository.setBranchRef(branchesList.get(i));

						if(getFragmentRefreshListenerFiles() != null) {
							getFragmentRefreshListenerFiles().onRefresh(branchesList.get(i));
						}
						dialogInterface.dismiss();
					});

					pBuilder.setNeutralButton(R.string.cancelButton, null);
					pBuilder.create().show();
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Branches>> call, @NonNull Throwable t) {
				progressDialog.hide();
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

					return RepoInfoFragment.newInstance(repository);
				case 1: // Files

					return FilesFragment.newInstance(repository);
				case 2: // Issues

					fragment = IssuesFragment.newInstance(repository);
					break;
				case 3: // Pull requests

					fragment = PullRequestsFragment.newInstance(repository);
					break;
				case 4: // Releases

					return ReleasesFragment.newInstance(repository);
				case 5: // Milestones

					fragment = MilestonesFragment.newInstance(repository);
					break;
				case 6: // Labels

					return LabelsFragment.newInstance(repository);
				case 7: // Collaborators

					return CollaboratorsFragment.newInstance(repository);
			}

			assert fragment != null;
			return fragment;
		}

		@Override
		public int getCount() {

			return tabsCount;
		}
	}

	private void getRepoInfo(String token, final String owner, String repo) {

		LinearProgressIndicator loading = findViewById(R.id.loadingIndicator);
		if(repository.hasRepository()) {
			loading.setVisibility(View.GONE);
			initWithRepo();
			return;
		}

		Call<UserRepositories> call = RetrofitClient.getApiInterface(ctx).getUserRepository(token, owner, repo);
		call.enqueue(new Callback<UserRepositories>() {

			@Override
			public void onResponse(@NonNull Call<UserRepositories> call, @NonNull retrofit2.Response<UserRepositories> response) {

				UserRepositories repoInfo = response.body();
				loading.setVisibility(View.GONE);

				if(response.code() == 200) {
					assert repoInfo != null;
					repository.setRepository(repoInfo);
					initWithRepo();
				}
				else {
					Toasty.error(ctx, getString(R.string.genericError));
					Log.e("onFailure", String.valueOf(response.code()));
					finish();
				}
			}

			@Override
			public void onFailure(@NonNull Call<UserRepositories> call, @NonNull Throwable t) {
				Toasty.error(ctx, getString(R.string.genericError));
				Log.e("onFailure", t.toString());
				finish();
			}

		});

	}

	private void initWithRepo() {
		repository.setBranchRef(repository.getRepository().getDefault_branch());

		ImageView repoTypeToolbar = findViewById(R.id.repoTypeToolbar);
		if(repository.getRepository().isPrivateFlag()) {
			repoTypeToolbar.setVisibility(View.VISIBLE);
		} else {
			repoTypeToolbar.setVisibility(View.GONE);
		}

		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setVisibility(View.VISIBLE);

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

		mViewPager = findViewById(R.id.container);
		mViewPager.setVisibility(View.VISIBLE);

		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

		SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mSectionsPagerAdapter);

		if(tinyDB.getBoolean("enableCounterBadges", true)) {
			@SuppressLint("InflateParams") View tabHeader2 = LayoutInflater.from(ctx).inflate(R.layout.badge_issue, null);
			textViewBadgeIssue = tabHeader2.findViewById(R.id.counterBadgeIssue);

			@SuppressLint("InflateParams") View tabHeader4 = LayoutInflater.from(ctx).inflate(R.layout.badge_pull, null);
			textViewBadgePull = tabHeader4.findViewById(R.id.counterBadgePull);

			@SuppressLint("InflateParams") View tabHeader6 = LayoutInflater.from(ctx).inflate(R.layout.badge_release, null);
			textViewBadgeRelease = tabHeader6.findViewById(R.id.counterBadgeRelease);

			ColorStateList textColor = tabLayout.getTabTextColors();

			if(repository.getRepository().getOpen_issues_count() != null) {
				textViewBadgeIssue.setVisibility(View.VISIBLE);
				textViewBadgeIssue.setText(repository.getRepository().getOpen_issues_count());

				TabLayout.Tab tabOpenIssues = tabLayout.getTabAt(2);
				assert tabOpenIssues != null;

				tabOpenIssues.setCustomView(tabHeader2);
				TextView openIssueTabView = Objects.requireNonNull(tabOpenIssues.getCustomView()).findViewById(R.id.counterBadgeIssueText);
				openIssueTabView.setTextColor(textColor);
			} else {
				textViewBadgeIssue.setVisibility(View.GONE);
			}

			if(repository.getRepository().getOpen_pull_count() != null) {
				textViewBadgePull.setVisibility(View.VISIBLE);
				textViewBadgePull.setText(repository.getRepository().getOpen_pull_count());

				Objects.requireNonNull(tabLayout.getTabAt(3)).setCustomView(tabHeader4);
				TabLayout.Tab tabOpenPulls = tabLayout.getTabAt(3);
				assert tabOpenPulls != null; // FIXME This should be cleaned up
				TextView openPullTabView = Objects.requireNonNull(tabOpenPulls.getCustomView()).findViewById(R.id.counterBadgePullText);
				openPullTabView.setTextColor(textColor);
			} else {
				textViewBadgePull.setVisibility(View.GONE);
			}

			if(repository.getRepository().getRelease_count() != null) {
				textViewBadgeRelease.setVisibility(View.VISIBLE);
				textViewBadgeRelease.setText(repository.getRepository().getRelease_count());

				Objects.requireNonNull(tabLayout.getTabAt(4)).setCustomView(tabHeader6);
				TabLayout.Tab tabOpenRelease = tabLayout.getTabAt(4);
				assert tabOpenRelease != null; // FIXME This should be cleaned up
				TextView openReleaseTabView = Objects.requireNonNull(tabOpenRelease.getCustomView()).findViewById(R.id.counterBadgeReleaseText);
				openReleaseTabView.setTextColor(textColor);
			} else {
				textViewBadgeRelease.setVisibility(View.GONE);
			}
		}

		Intent mainIntent = getIntent();
		String goToSection = mainIntent.getStringExtra("goToSection");
		String goToSectionType = mainIntent.getStringExtra("goToSectionType");

		if(goToSection != null) {
			mainIntent.removeExtra("goToSection");
			mainIntent.removeExtra("goToSectionType");

			switch(goToSectionType) {
				case "branchesList":
					mViewPager.setCurrentItem(1);
					chooseBranch();
					break;
				case "branch":
					mViewPager.setCurrentItem(1);
					String selectedBranch = mainIntent.getStringExtra("selectedBranch");
					repository.setBranchRef(selectedBranch);
					if(getFragmentRefreshListenerFiles() != null) {
						getFragmentRefreshListenerFiles().onRefresh(selectedBranch);
					}
					break;
				case "file":
					mViewPager.setCurrentItem(1);
					String branch1 = mainIntent.getStringExtra("branch");
					repository.setBranchRef(branch1);
					if(getFragmentRefreshListenerFiles() != null) {
						getFragmentRefreshListenerFiles().onRefresh(branch1);
					}
					Intent intent = repository.getIntent(ctx, FileViewActivity.class);
					intent.putExtra("file", mainIntent.getSerializableExtra("file"));
					startActivity(intent);
					break;
				case "dir":
					mViewPager.setCurrentItem(1);
					String branch2 = mainIntent.getStringExtra("branch");
					repository.setBranchRef(branch2);
					if(getFragmentRefreshListenerFiles() != null) {
						getFragmentRefreshListenerFiles().onRefresh(branch2);
					}
					break;
				case "commitsList":
					mViewPager.setCurrentItem(1);
					String branch = mainIntent.getStringExtra("branchName");
					repository.setBranchRef(branch);
					if(getFragmentRefreshListenerFiles() != null) {
						getFragmentRefreshListenerFiles().onRefresh(branch);
					}
					Intent intent1 = repository.getIntent(ctx, CommitsActivity.class);
					ctx.startActivity(intent1);
					break;
				case "issue":
					mViewPager.setCurrentItem(2);
					break;
				case "issueNew":
					mViewPager.setCurrentItem(2);
					startActivity(repository.getIntent(ctx, CreateIssueActivity.class));
					break;
				case "pull":
					mViewPager.setCurrentItem(3);
					break;
				case "pullNew":
					mViewPager.setCurrentItem(3);
					startActivity(repository.getIntent(ctx, CreatePullRequestActivity.class));
					break;
				case "releases":
					mViewPager.setCurrentItem(4);
					break;
				case "newRelease":
					mViewPager.setCurrentItem(4);
					createReleaseLauncher.launch(repository.getIntent(ctx, CreateReleaseActivity.class));
					break;
				case "milestones":
					mViewPager.setCurrentItem(5);
					break;
				case "milestonesNew":
					mViewPager.setCurrentItem(5);
					createMilestoneLauncher.launch(repository.getIntent(ctx, CreateMilestoneActivity.class));
					break;
				case "labels":
					mViewPager.setCurrentItem(6);
					break;
				case "settings":
					settingsLauncher.launch(repository.getIntent(ctx, RepositorySettingsActivity.class));
					break;
			}
		}
	}

	private void checkRepositoryStarStatus(String instanceToken, final String owner, String repo) {

		Call<JsonElement> call = RetrofitClient.getApiInterface(ctx).checkRepoStarStatus(instanceToken, owner, repo);
		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				repository.setStarred(response.code() == 204);
			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	private void checkRepositoryWatchStatus(String instanceToken, final String owner, String repo) {

		Call<WatchInfo> call;

		call = RetrofitClient.getApiInterface(ctx).checkRepoWatchStatus(instanceToken, owner, repo);
		call.enqueue(new Callback<WatchInfo>() {

			@Override
			public void onResponse(@NonNull Call<WatchInfo> call, @NonNull retrofit2.Response<WatchInfo> response) {

				if(response.code() == 200) {
					assert response.body() != null;
					repository.setWatched(response.body().getSubscribed());
				}
				else {
					repository.setWatched(false);
				}
			}

			@Override
			public void onFailure(@NonNull Call<WatchInfo> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	// Issues milestone filter interface
	public FragmentRefreshListener getFragmentRefreshListenerFilterIssuesByMilestone() { return fragmentRefreshListenerFilterIssuesByMilestone; }

	public void setFragmentRefreshListenerFilterIssuesByMilestone(FragmentRefreshListener fragmentRefreshListener) { this.fragmentRefreshListenerFilterIssuesByMilestone = fragmentRefreshListener; }

	// Issues interface
	public FragmentRefreshListener getFragmentRefreshListener() { return fragmentRefreshListener; }

	public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) { this.fragmentRefreshListener = fragmentRefreshListener; }

	// Pull request interface
	public FragmentRefreshListener getFragmentRefreshListenerPr() { return fragmentRefreshListenerPr; }

	public void setFragmentRefreshListenerPr(FragmentRefreshListener fragmentRefreshListenerPr) { this.fragmentRefreshListenerPr = fragmentRefreshListenerPr; }

	// Milestones interface
	public FragmentRefreshListener getFragmentRefreshListenerMilestone() { return fragmentRefreshListenerMilestone; }

	public void setFragmentRefreshListenerMilestone(FragmentRefreshListener fragmentRefreshListenerMilestone) { this.fragmentRefreshListenerMilestone = fragmentRefreshListenerMilestone; }

	// Files interface
	public FragmentRefreshListener getFragmentRefreshListenerFiles() { return fragmentRefreshListenerFiles; }

	public void setFragmentRefreshListenerFiles(FragmentRefreshListener fragmentRefreshListenerFiles) { this.fragmentRefreshListenerFiles = fragmentRefreshListenerFiles; }

	//Releases interface
	public FragmentRefreshListener getFragmentRefreshListenerReleases() { return fragmentRefreshListenerReleases; }

	public void setFragmentRefreshListenerReleases(FragmentRefreshListener fragmentRefreshListener) { this.fragmentRefreshListenerReleases = fragmentRefreshListener; }

}
