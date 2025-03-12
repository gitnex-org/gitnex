package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Milestone;
import org.gitnex.tea4j.v2.models.Organization;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.WatchInfo;
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
import org.mian.gitnex.fragments.WikiFragment;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.ViewPager2Transformers;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.structs.BottomSheetListener;
import org.mian.gitnex.structs.FragmentRefreshListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class RepoDetailActivity extends BaseActivity implements BottomSheetListener {

	public static boolean updateFABActions = false;
	public static boolean updateRepo = false;
	private final ActivityResultLauncher<Intent> settingsLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == 200) {
							assert result.getData() != null;
							if (result.getData().getBooleanExtra("nameChanged", false)) {
								recreate();
							}
						}
					});
	public ViewPager2 viewPager;
	public RepositoryContext repository;
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
	private Dialog progressDialog;
	private MaterialAlertDialogBuilder materialAlertDialogBuilder;
	private Intent intentWiki;

	public ActivityResultLauncher<Intent> createIssueLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(), result -> {});

	public ActivityResultLauncher<Intent> createPrLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(), result -> {});

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_repo_detail);

		repository = RepositoryContext.fromIntent(getIntent());

		Toolbar toolbar = findViewById(R.id.toolbar);

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(ctx, R.style.ThemeOverlay_Material3_Dialog_Alert);

		intentWiki = new Intent(ctx, WikiActivity.class);

		TextView toolbarTitle = findViewById(R.id.toolbar_title);
		toolbarTitle.setText(repository.getFullName());

		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle(repository.getName());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		myTypeface = AppUtil.getTypeface(this);
		toolbarTitle.setTypeface(myTypeface);

		getRepoInfo(repository.getOwner(), repository.getName());

		checkRepositoryStarStatus(repository.getOwner(), repository.getName());
		checkRepositoryWatchStatus(repository.getOwner(), repository.getName());
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
		if (updateRepo) {
			updateRepo = false;
			repository.removeRepository();
			getRepoInfo(repository.getOwner(), repository.getName());
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

			if (!getIntent().getBooleanExtra("openedFromUserOrg", false)) {
				RetrofitClient.getApiInterface(ctx)
						.orgGet(repository.getOwner())
						.enqueue(
								new Callback<>() {

									@Override
									public void onResponse(
											@NonNull Call<Organization> call,
											@NonNull Response<Organization> response) {
										Intent intent =
												new Intent(
														ctx,
														response.isSuccessful()
																? OrganizationDetailActivity.class
																: ProfileActivity.class);
										intent.putExtra(
												response.isSuccessful() ? "orgName" : "username",
												repository.getOwner());
										intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(intent);
										finish();
									}

									@Override
									public void onFailure(
											@NonNull Call<Organization> call,
											@NonNull Throwable t) {
										finish();
									}
								});
			} else {
				finish();
			}
			return true;
		} else if (id == R.id.repoMenu) {

			if (repository.hasRepository()) {
				BottomSheetRepoFragment bottomSheet = new BottomSheetRepoFragment(repository);
				bottomSheet.show(getSupportFragmentManager(), "repoBottomSheet");
			}
			return true;
		} else if (id == R.id.filter) {

			BottomSheetIssuesFilterFragment filterBottomSheet =
					new BottomSheetIssuesFilterFragment();
			filterBottomSheet.show(getSupportFragmentManager(), "repoFilterMenuBottomSheet");
			return true;
		} else if (id == R.id.filterPr) {

			BottomSheetPullRequestFilterFragment filterPrBottomSheet =
					new BottomSheetPullRequestFilterFragment();
			filterPrBottomSheet.show(getSupportFragmentManager(), "repoFilterMenuPrBottomSheet");
			return true;
		} else if (id == R.id.filterMilestone) {

			BottomSheetMilestonesFilterFragment filterMilestoneBottomSheet =
					new BottomSheetMilestonesFilterFragment();
			filterMilestoneBottomSheet.show(
					getSupportFragmentManager(), "repoFilterMenuMilestoneBottomSheet");
			return true;
		} /*else if (id == R.id.switchBranches) {

			chooseBranch();
			return true;
		}*/ else if (id == R.id.branchCommits) {

			Intent intent = repository.getIntent(ctx, CommitsActivity.class);

			ctx.startActivity(intent);
			return true;
		} else if (id == R.id.filterReleases && getAccount().requiresVersion("1.15.0")) {
			BottomSheetReleasesTagsFragment bottomSheetReleasesTagsFragment =
					new BottomSheetReleasesTagsFragment();
			bottomSheetReleasesTagsFragment.show(
					getSupportFragmentManager(), "repoFilterReleasesMenuBottomSheet");
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onButtonClicked(String text) {

		switch (text) {
			case "openWebRepo":
				AppUtil.openUrlInBrowser(this, repository.getRepository().getHtmlUrl());
				break;
			case "shareRepo":
				AppUtil.sharingIntent(this, repository.getRepository().getHtmlUrl());
				break;
			case "copyRepoUrl":
				AppUtil.copyToClipboard(
						this,
						repository.getRepository().getHtmlUrl(),
						ctx.getString(R.string.copyIssueUrlToastMsg));
				break;
			case "filterByMilestone":
				filterIssuesByMilestone();
				break;
			case "openIssues":
				repository.setIssueState(RepositoryContext.State.OPEN);
				if (getFragmentRefreshListener() != null) {

					getFragmentRefreshListener().onRefresh("open");
				}
				break;
			case "closedIssues":
				repository.setIssueState(RepositoryContext.State.CLOSED);
				if (getFragmentRefreshListener() != null) {

					getFragmentRefreshListener().onRefresh("closed");
				}
				break;
			case "openPr":
				repository.setPrState(RepositoryContext.State.OPEN);
				if (getFragmentRefreshListenerPr() != null) {
					getFragmentRefreshListenerPr().onRefresh("open");
				}
				break;
			case "closedPr":
				repository.setPrState(RepositoryContext.State.CLOSED);
				if (getFragmentRefreshListenerPr() != null) {

					getFragmentRefreshListenerPr().onRefresh("closed");
				}
				break;
			case "openMilestone":
				repository.setMilestoneState(RepositoryContext.State.OPEN);
				if (getFragmentRefreshListenerMilestone() != null) {

					getFragmentRefreshListenerMilestone().onRefresh("open");
				}
				break;
			case "closedMilestone":
				repository.setMilestoneState(RepositoryContext.State.CLOSED);
				if (getFragmentRefreshListenerMilestone() != null) {

					getFragmentRefreshListenerMilestone().onRefresh("closed");
				}
				break;
			case "repoSettings":
				settingsLauncher.launch(
						repository.getIntent(ctx, RepositorySettingsActivity.class));
				break;
			case "newPullRequest":
				startActivity(repository.getIntent(ctx, CreatePullRequestActivity.class));
				break;
			case "tags":
				if (getFragmentRefreshListenerReleases() != null) {
					getFragmentRefreshListenerReleases().onRefresh("tags");
				}
				break;
			case "releases":
				if (getFragmentRefreshListenerReleases() != null) {
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

		progressDialog = new Dialog(this);
		progressDialog.setContentView(R.layout.custom_progress_loader);
		progressDialog.show();

		Call<List<Milestone>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueGetMilestonesList(
								repository.getOwner(), repository.getName(), "open", null, 1, 100);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Milestone>> call,
							@NonNull Response<List<Milestone>> response) {

						progressDialog.hide();
						if (response.code() == 200) {

							List<String> milestonesList = new ArrayList<>();
							int selectedMilestone = 0;
							assert response.body() != null;

							milestonesList.add("All");
							for (int i = 0; i < response.body().size(); i++) {
								Milestone milestones = response.body().get(i);
								milestonesList.add(milestones.getTitle());
							}

							for (int j = 0; j < milestonesList.size(); j++) {
								if (repository.getIssueMilestoneFilterName() != null) {
									if (repository
											.getIssueMilestoneFilterName()
											.equals(milestonesList.get(j))) {
										selectedMilestone = j;
									}
								}
							}

							materialAlertDialogBuilder
									.setTitle(R.string.selectMilestone)
									.setSingleChoiceItems(
											milestonesList.toArray(new String[0]),
											selectedMilestone,
											(dialogInterface, i) -> {
												repository.setIssueMilestoneFilterName(
														milestonesList.get(i));

												if (getFragmentRefreshListenerFilterIssuesByMilestone()
														!= null) {
													getFragmentRefreshListenerFilterIssuesByMilestone()
															.onRefresh(milestonesList.get(i));
												}
												dialogInterface.dismiss();
											})
									.setNeutralButton(R.string.cancelButton, null);
							materialAlertDialogBuilder.create().show();
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Milestone>> call, @NonNull Throwable t) {
						progressDialog.hide();
					}
				});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	private void getRepoInfo(final String owner, String repo) {

		LinearProgressIndicator loading = findViewById(R.id.loadingIndicator);
		if (repository.hasRepository()) {
			loading.setVisibility(View.GONE);
			initWithRepo();
			return;
		}

		Call<Repository> call = RetrofitClient.getApiInterface(ctx).repoGet(owner, repo);
		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Repository> call,
							@NonNull retrofit2.Response<Repository> response) {

						Repository repoInfo = response.body();
						loading.setVisibility(View.GONE);

						if (response.code() == 200) {
							assert repoInfo != null;
							repository.setRepository(repoInfo);
							initWithRepo();
						} else {
							Toasty.error(ctx, getString(R.string.genericError));
							finish();
						}
					}

					@Override
					public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {

						Toasty.error(ctx, getString(R.string.genericError));
						finish();
					}
				});
	}

	private void initWithRepo() {
		repository.setBranchRef(repository.getRepository().getDefaultBranch());

		ImageView repoTypeToolbar = findViewById(R.id.repoTypeToolbar);
		if (repository.getRepository().isPrivate()) {
			repoTypeToolbar.setVisibility(View.VISIBLE);
		} else {
			repoTypeToolbar.setVisibility(View.GONE);
		}

		TabLayout tabLayout = findViewById(R.id.tabs);

		if (viewPager == null) {

			viewPager = findViewById(R.id.repositoryContainer);
			viewPager.setOffscreenPageLimit(1);

			viewPager.setAdapter(new ViewPagerAdapter(this));

			ViewPager2Transformers.returnSelectedTransformer(
					viewPager,
					Integer.parseInt(
							AppDatabaseSettings.getSettingsValue(
									ctx, AppDatabaseSettings.APP_TABS_ANIMATION_KEY)));

			String[] tabTitles = {
				ctx.getResources().getString(R.string.tabTextInfo),
				ctx.getResources().getString(R.string.tabTextFiles),
				ctx.getResources().getString(R.string.pageTitleIssues),
				ctx.getResources().getString(R.string.tabPullRequests),
				ctx.getResources().getString(R.string.tabTextReleases),
				ctx.getResources().getString(R.string.wiki),
				ctx.getResources().getString(R.string.tabTextMl),
				ctx.getResources().getString(R.string.newIssueLabelsTitle),
				ctx.getResources().getString(R.string.tabTextCollaborators)
			};
			new TabLayoutMediator(
							tabLayout,
							viewPager,
							(tab, position) -> tab.setText(tabTitles[position]))
					.attach();

			ViewGroup viewGroup = (ViewGroup) tabLayout.getChildAt(0);
			int tabsCount = viewGroup.getChildCount();

			for (int j = 0; j < tabsCount; j++) {

				ViewGroup vgTab = (ViewGroup) viewGroup.getChildAt(j);
				int tabChildCount = vgTab.getChildCount();

				for (int i = 0; i < tabChildCount; i++) {

					View tabViewChild = vgTab.getChildAt(i);

					if (tabViewChild instanceof TextView) {

						((TextView) tabViewChild).setTypeface(myTypeface);
					}
				}
			}
		}

		if (Boolean.parseBoolean(
				AppDatabaseSettings.getSettingsValue(ctx, AppDatabaseSettings.APP_COUNTER_KEY))) {
			@SuppressLint("InflateParams")
			View tabHeader2 = LayoutInflater.from(ctx).inflate(R.layout.badge_issue, null);
			if (textViewBadgeIssue == null) {
				textViewBadgeIssue = tabHeader2.findViewById(R.id.counterBadgeIssue);
			}

			@SuppressLint("InflateParams")
			View tabHeader4 = LayoutInflater.from(ctx).inflate(R.layout.badge_pull, null);
			if (textViewBadgePull == null) {
				textViewBadgePull = tabHeader4.findViewById(R.id.counterBadgePull);
			}

			@SuppressLint("InflateParams")
			View tabHeader6 = LayoutInflater.from(ctx).inflate(R.layout.badge_release, null);
			if (textViewBadgeRelease == null) {
				textViewBadgeRelease = tabHeader6.findViewById(R.id.counterBadgeRelease);
			}

			ColorStateList textColor = tabLayout.getTabTextColors();

			if (repository.getRepository().getOpenIssuesCount() != null) {
				textViewBadgeIssue.setVisibility(View.VISIBLE);
				textViewBadgeIssue.setText(
						String.valueOf(repository.getRepository().getOpenIssuesCount()));

				TabLayout.Tab tabOpenIssues = tabLayout.getTabAt(2);
				assert tabOpenIssues != null;

				if (tabOpenIssues.getCustomView() == null) {
					tabOpenIssues.setCustomView(tabHeader2);
				}
				TextView openIssueTabView =
						Objects.requireNonNull(tabOpenIssues.getCustomView())
								.findViewById(R.id.counterBadgeIssueText);
				openIssueTabView.setTextColor(textColor);
			} else {
				textViewBadgeIssue.setVisibility(View.GONE);
			}

			if (repository.getRepository().getOpenPrCounter() != null) {
				textViewBadgePull.setVisibility(View.VISIBLE);
				textViewBadgePull.setText(
						String.valueOf(repository.getRepository().getOpenPrCounter()));

				TabLayout.Tab tabOpenPulls = tabLayout.getTabAt(3);
				assert tabOpenPulls != null;

				if (tabOpenPulls.getCustomView() == null) {
					tabOpenPulls.setCustomView(tabHeader4);
				}
				TextView openPullTabView =
						Objects.requireNonNull(tabOpenPulls.getCustomView())
								.findViewById(R.id.counterBadgePullText);
				openPullTabView.setTextColor(textColor);
			} else {
				textViewBadgePull.setVisibility(View.GONE);
			}

			if (repository.getRepository().getReleaseCounter() != null) {
				textViewBadgeRelease.setVisibility(View.VISIBLE);
				textViewBadgeRelease.setText(
						String.valueOf(repository.getRepository().getReleaseCounter()));

				TabLayout.Tab tabOpenRelease = tabLayout.getTabAt(4);
				assert tabOpenRelease != null;
				if (tabOpenRelease.getCustomView() == null) {
					tabOpenRelease.setCustomView(tabHeader6);
				}
				TextView openReleaseTabView =
						Objects.requireNonNull(tabOpenRelease.getCustomView())
								.findViewById(R.id.counterBadgeReleaseText);
				openReleaseTabView.setTextColor(textColor);
			} else {
				textViewBadgeRelease.setVisibility(View.GONE);
			}
		}

		Intent mainIntent = getIntent();
		String goToSection = mainIntent.getStringExtra("goToSection");
		String goToSectionType = mainIntent.getStringExtra("goToSectionType");

		if (goToSection != null) {
			mainIntent.removeExtra("goToSection");
			mainIntent.removeExtra("goToSectionType");

			switch (Objects.requireNonNull(goToSectionType)) {
				case "file":
					viewPager.setCurrentItem(1);
					String branch1 = mainIntent.getStringExtra("branch");
					repository.setBranchRef(branch1);
					if (getFragmentRefreshListenerFiles() != null) {
						getFragmentRefreshListenerFiles().onRefresh(branch1);
					}
					Intent intent = repository.getIntent(ctx, FileViewActivity.class);
					intent.putExtra("file", mainIntent.getSerializableExtra("file"));
					startActivity(intent);
					break;
				case "dir":
					viewPager.setCurrentItem(1);
					String branch2 = mainIntent.getStringExtra("branch");
					repository.setBranchRef(branch2);
					if (getFragmentRefreshListenerFiles() != null) {
						getFragmentRefreshListenerFiles().onRefresh(branch2);
					}
					break;
				case "commitsList":
					viewPager.setCurrentItem(1);
					String branch = mainIntent.getStringExtra("branchName");
					repository.setBranchRef(branch);
					if (getFragmentRefreshListenerFiles() != null) {
						getFragmentRefreshListenerFiles().onRefresh(branch);
					}
					Intent intent1 = repository.getIntent(ctx, CommitsActivity.class);
					ctx.startActivity(intent1);
					break;
				case "commit":
					viewPager.setCurrentItem(0);
					String sha = mainIntent.getStringExtra("sha");
					if (getFragmentRefreshListenerFiles() != null) {
						getFragmentRefreshListenerFiles().onRefresh(sha);
					}
					Intent commitIntent = repository.getIntent(ctx, CommitDetailActivity.class);
					commitIntent.putExtra("sha", sha);
					ctx.startActivity(commitIntent);
					break;
				case "issue":
					viewPager.setCurrentItem(2);
					break;
				case "issueNew":
					viewPager.setCurrentItem(2);
					startActivity(repository.getIntent(ctx, CreateIssueActivity.class));
					break;
				case "pull":
					viewPager.setCurrentItem(3);
					break;
				case "pullNew":
					viewPager.setCurrentItem(3);
					startActivity(repository.getIntent(ctx, CreatePullRequestActivity.class));
					break;
				case "releases":
					viewPager.setCurrentItem(4);
					break;
				case "newRelease":
					viewPager.setCurrentItem(4);
					startActivity(repository.getIntent(ctx, CreateReleaseActivity.class));
					break;
				case "wiki":
					viewPager.setCurrentItem(5);
					break;
				case "wikiNew":
					viewPager.setCurrentItem(5);
					intentWiki.putExtra("action", "add");
					intentWiki.putExtra(
							RepositoryContext.INTENT_EXTRA, ((RepoDetailActivity) ctx).repository);
					ctx.startActivity(intentWiki);
					break;
				case "milestones":
					viewPager.setCurrentItem(6);
					break;
				case "milestonesNew":
					viewPager.setCurrentItem(6);
					startActivity(repository.getIntent(ctx, CreateMilestoneActivity.class));
					break;
				case "labels":
					viewPager.setCurrentItem(7);
					break;
				case "settings":
					settingsLauncher.launch(
							repository.getIntent(ctx, RepositorySettingsActivity.class));
					break;
			}
		}
	}

	private void checkRepositoryStarStatus(final String owner, String repo) {

		Call<Void> call = RetrofitClient.getApiInterface(ctx).userCurrentCheckStarring(owner, repo);
		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						repository.setStarred(response.code() == 204);
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
				});
	}

	private void checkRepositoryWatchStatus(final String owner, String repo) {

		Call<WatchInfo> call =
				RetrofitClient.getApiInterface(ctx).userCurrentCheckSubscription(owner, repo);
		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<WatchInfo> call,
							@NonNull retrofit2.Response<WatchInfo> response) {

						if (response.code() == 200) {
							assert response.body() != null;
							repository.setWatched(response.body().isSubscribed());
						} else {
							repository.setWatched(false);
						}
					}

					@Override
					public void onFailure(@NonNull Call<WatchInfo> call, @NonNull Throwable t) {}
				});
	}

	// Issues milestone filter interface
	public FragmentRefreshListener getFragmentRefreshListenerFilterIssuesByMilestone() {
		return fragmentRefreshListenerFilterIssuesByMilestone;
	}

	public void setFragmentRefreshListenerFilterIssuesByMilestone(
			FragmentRefreshListener fragmentRefreshListener) {
		this.fragmentRefreshListenerFilterIssuesByMilestone = fragmentRefreshListener;
	}

	// Issues interface
	public FragmentRefreshListener getFragmentRefreshListener() {
		return fragmentRefreshListener;
	}

	public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
		this.fragmentRefreshListener = fragmentRefreshListener;
	}

	// Pull request interface
	public FragmentRefreshListener getFragmentRefreshListenerPr() {
		return fragmentRefreshListenerPr;
	}

	public void setFragmentRefreshListenerPr(FragmentRefreshListener fragmentRefreshListenerPr) {
		this.fragmentRefreshListenerPr = fragmentRefreshListenerPr;
	}

	// Milestones interface
	public FragmentRefreshListener getFragmentRefreshListenerMilestone() {
		return fragmentRefreshListenerMilestone;
	}

	public void setFragmentRefreshListenerMilestone(
			FragmentRefreshListener fragmentRefreshListenerMilestone) {
		this.fragmentRefreshListenerMilestone = fragmentRefreshListenerMilestone;
	}

	// Files interface
	public FragmentRefreshListener getFragmentRefreshListenerFiles() {
		return fragmentRefreshListenerFiles;
	}

	public void setFragmentRefreshListenerFiles(
			FragmentRefreshListener fragmentRefreshListenerFiles) {
		this.fragmentRefreshListenerFiles = fragmentRefreshListenerFiles;
	}

	// Releases interface
	public FragmentRefreshListener getFragmentRefreshListenerReleases() {
		return fragmentRefreshListenerReleases;
	}

	public void setFragmentRefreshListenerReleases(
			FragmentRefreshListener fragmentRefreshListener) {
		this.fragmentRefreshListenerReleases = fragmentRefreshListener;
	}

	public class ViewPagerAdapter extends FragmentStateAdapter {

		public ViewPagerAdapter(@NonNull FragmentActivity fa) {
			super(fa);
		}

		@NonNull @Override
		public Fragment createFragment(int position) {

			Fragment fragment = null;

			switch (position) {
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
				case 5: // Wiki
					return WikiFragment.newInstance(repository);
				case 6: // Milestones
					fragment = MilestonesFragment.newInstance(repository);
					break;
				case 7: // Labels
					return LabelsFragment.newInstance(repository);
				case 8: // Collaborators
					return CollaboratorsFragment.newInstance(repository);
			}
			assert fragment != null;
			return fragment;
		}

		@Override
		public int getItemCount() {
			return 9;
		}
	}
}
