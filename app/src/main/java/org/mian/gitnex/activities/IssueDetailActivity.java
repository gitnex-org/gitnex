package org.mian.gitnex.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.v2.models.EditIssueOption;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.IssueLabelsOption;
import org.gitnex.tea4j.v2.models.Label;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.User;
import org.gitnex.tea4j.v2.models.WatchInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.AssigneesActions;
import org.mian.gitnex.actions.LabelsActions;
import org.mian.gitnex.adapters.AssigneesListAdapter;
import org.mian.gitnex.adapters.IssueCommentsAdapter;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityIssueDetailBinding;
import org.mian.gitnex.databinding.CustomAssigneesSelectionDialogBinding;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.fragments.BottomSheetReplyFragment;
import org.mian.gitnex.fragments.BottomSheetSingleIssueFragment;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.DividerItemDecorator;
import org.mian.gitnex.helpers.LabelWidthCalculator;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.structs.BottomSheetListener;
import org.mian.gitnex.viewmodels.IssueCommentsViewModel;
import org.mian.gitnex.views.ReactionList;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class IssueDetailActivity extends BaseActivity implements LabelsListAdapter.LabelsListAdapterListener, AssigneesListAdapter.AssigneesListAdapterListener, BottomSheetListener {

	private IssueCommentsAdapter adapter;

	private String repoOwner;
	private String repoName;
	private int issueIndex;
	private String issueCreator;
	private IssueContext issue;

	private LabelsListAdapter labelsAdapter;
	private AssigneesListAdapter assigneesAdapter;

	private List<Integer> currentLabelsIds = new ArrayList<>();
	private List<Integer> labelsIds = new ArrayList<>();
	private final List<Label> labelsList = new ArrayList<>();
	private final List<User> assigneesList = new ArrayList<>();
	private List<String> assigneesListData = new ArrayList<>();
	private List<String> currentAssignees = new ArrayList<>();

	private Dialog dialogLabels;
	private Dialog dialogAssignees;

	private CustomLabelsSelectionDialogBinding labelsBinding;
	private CustomAssigneesSelectionDialogBinding assigneesBinding;
	private ActivityIssueDetailBinding viewBinding;

	public static boolean singleIssueUpdate = false;
	public boolean commentEdited = false;
	public boolean commentPosted = false;

	private IssueCommentsViewModel issueCommentsModel;

	public ActivityResultLauncher<Intent> editIssueLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
		result -> {
			if(result.getResultCode() == 200) {
				assert result.getData() != null;
				if(result.getData().getBooleanExtra("issueEdited", false)) {
					new Handler(Looper.getMainLooper()).postDelayed(() -> {

						viewBinding.frameAssignees.removeAllViews();
						viewBinding.frameLabels.removeAllViews();
						issue.setIssue(null);
						getSingleIssue(repoOwner, repoName, issueIndex);

					}, 500);
				}
			}
		});

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityIssueDetailBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		issue = IssueContext.fromIntent(getIntent());
		repoOwner = issue.getRepository().getOwner();
		repoName = issue.getRepository().getName();
		issueIndex = issue.getIssueIndex();

		setSupportActionBar(viewBinding.toolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle(repoName);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		issueCommentsModel = new ViewModelProvider(this).get(IssueCommentsViewModel.class);

		viewBinding.recyclerView.setHasFixedSize(true);
		viewBinding.recyclerView.setNestedScrollingEnabled(false);
		viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(this, R.drawable.shape_list_divider));
		viewBinding.recyclerView.addItemDecoration(dividerItemDecoration);

		viewBinding.addNewComment.setOnClickListener(v -> {

			BottomSheetReplyFragment bottomSheetReplyFragment = BottomSheetReplyFragment.newInstance(new Bundle(), issue);
			bottomSheetReplyFragment.setOnInteractedListener(this::onResume);
			bottomSheetReplyFragment.show(getSupportFragmentManager(), "replyBottomSheet");
		});

		labelsAdapter = new LabelsListAdapter(labelsList, IssueDetailActivity.this, currentLabelsIds);
		assigneesAdapter = new AssigneesListAdapter(ctx, assigneesList, IssueDetailActivity.this, currentAssignees);
		LabelsActions.getCurrentIssueLabels(ctx, repoOwner, repoName, issueIndex, currentLabelsIds);
		AssigneesActions.getCurrentIssueAssignees(ctx, repoOwner, repoName, issueIndex, currentAssignees);

		viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			viewBinding.pullToRefresh.setRefreshing(false);
			issueCommentsModel
				.loadIssueComments(repoOwner, repoName, issueIndex,
					ctx);

		}, 500));

		Typeface myTypeface = AppUtil.getTypeface(this);
		viewBinding.toolbarTitle.setTypeface(myTypeface);
		viewBinding.toolbarTitle.setText(repoName);

		getSingleIssue(repoOwner, repoName, issueIndex);
		fetchDataAsync(repoOwner, repoName, issueIndex);

		if(getIntent().getStringExtra("openPrDiff") != null && getIntent().getStringExtra("openPrDiff").equals("true")) {
			startActivity(issue.getIntent(ctx, DiffActivity.class));
		}
	}

	@Override
	public void onButtonClicked(String text) {

		switch(text) {

			case "onResume":
				onResume();
				break;

			case "showLabels":
				showLabels();
				break;

			case "showAssignees":
				showAssignees();
				break;
		}
	}

	@Override
	public void labelsInterface(List<String> data) { }

	@Override
	public void labelsIdsInterface(List<Integer> data) {

		labelsIds = data;
	}

	@Override
	public void assigneesInterface(List<String> data) {

		assigneesListData = data;
	}

	private void showAssignees() {

		assigneesAdapter.updateList(currentAssignees);
		dialogAssignees = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
		dialogAssignees.setCancelable(true);

		if (dialogAssignees.getWindow() != null) {

			dialogAssignees.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		assigneesBinding = CustomAssigneesSelectionDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = assigneesBinding.getRoot();
		dialogAssignees.setContentView(view);

		assigneesBinding.save.setOnClickListener(assigneesBinding_ -> {

			currentAssignees = new ArrayList<>(new LinkedHashSet<>(currentAssignees));
			assigneesListData = new ArrayList<>(new LinkedHashSet<>(assigneesListData));
			Collections.sort(assigneesListData);
			Collections.sort(currentAssignees);

			if(!assigneesListData.equals(currentAssignees)) {

				updateIssueAssignees();
			}
			else {

				dialogAssignees.dismiss();
			}
		});

		dialogAssignees.show();
		AssigneesActions.getRepositoryAssignees(ctx, repoOwner, repoName, assigneesList, dialogAssignees, assigneesAdapter, assigneesBinding);
	}

	public void showLabels() {

		labelsAdapter.updateList(currentLabelsIds);
		dialogLabels = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
		dialogLabels.setCancelable(true);

		if (dialogLabels.getWindow() != null) {

			dialogLabels.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		labelsBinding = CustomLabelsSelectionDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = labelsBinding.getRoot();
		dialogLabels.setContentView(view);

		labelsBinding.save.setOnClickListener(labelsBinding_ -> {

			currentLabelsIds = new ArrayList<>(new LinkedHashSet<>(currentLabelsIds));
			labelsIds = new ArrayList<>(new LinkedHashSet<>(labelsIds));
			Collections.sort(labelsIds);
			Collections.sort(currentLabelsIds);

			if(!labelsIds.equals(currentLabelsIds)) {

				updateIssueLabels();
			}
			else {

				dialogLabels.dismiss();
			}
		});

		dialogLabels.show();
		LabelsActions.getRepositoryLabels(ctx, repoOwner, repoName, labelsList, dialogLabels, labelsAdapter, labelsBinding);
	}

	private void updateIssueAssignees() {

		EditIssueOption updateAssigneeJson = new EditIssueOption().assignees(assigneesListData);

		Call<Issue> call3 = RetrofitClient
			.getApiInterface(ctx)
			.issueEditIssue(repoOwner, repoName, (long) issueIndex, updateAssigneeJson);

		call3.enqueue(new Callback<Issue>() {

			@Override
			public void onResponse(@NonNull Call<Issue> call, @NonNull retrofit2.Response<Issue> response2) {

				if(response2.code() == 201) {

					Toasty.success(ctx, ctx.getString(R.string.assigneesUpdated));

					dialogAssignees.dismiss();

					viewBinding.frameAssignees.removeAllViews();
					viewBinding.frameLabels.removeAllViews();
					issue.setIssue(response2.body());
					getSingleIssue(repoOwner, repoName, issueIndex);
					currentAssignees.clear();
					new Handler(Looper.getMainLooper()).postDelayed(() -> AssigneesActions.getCurrentIssueAssignees(ctx, repoOwner, repoName, issueIndex, currentAssignees), 1000);
				}
				else if(response2.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx);
				}
				else if(response2.code() == 403) {

					Toasty.error(ctx, ctx.getString(R.string.authorizeError));
				}
				else if(response2.code() == 404) {

					Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
				}
				else {

					Toasty.error(ctx, getString(R.string.genericError));
				}

			}

			@Override
			public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
				Log.e("onFailure", t.toString());
			}
		});
	}

	private void updateIssueLabels() {

		ArrayList<Long> labelIds = new ArrayList<>();
		for(Integer i : labelsIds) {
			labelIds.add((long) i);
		}

		IssueLabelsOption patchIssueLabels = new IssueLabelsOption();
		patchIssueLabels.setLabels(labelIds);

		Call<List<Label>> call = RetrofitClient
			.getApiInterface(ctx)
			.issueReplaceLabels(repoOwner, repoName, (long) issueIndex, patchIssueLabels);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Label>> call, @NonNull retrofit2.Response<List<Label>> response) {

				if(response.code() == 200) {

					Toasty.success(ctx, ctx.getString(R.string.labelsUpdated));
					dialogLabels.dismiss();

					viewBinding.frameAssignees.removeAllViews();
					viewBinding.frameLabels.removeAllViews();
					issue.setIssue(null);
					getSingleIssue(repoOwner, repoName, issueIndex);
					currentLabelsIds.clear();
					new Handler(Looper.getMainLooper()).postDelayed(() -> LabelsActions.getCurrentIssueLabels(ctx, repoOwner, repoName, issueIndex, currentLabelsIds), 1000);
					IssuesFragment.resumeIssues = true;
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx);
				}
				else if(response.code() == 403) {

					Toasty.error(ctx, ctx.getString(R.string.authorizeError));
				}
				else if(response.code() == 404) {

					Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
				}
				else {

					Toasty.error(ctx, getString(R.string.genericError));
				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.generic_nav_dotted_menu, menu);
		if(issue.getIssueType().equalsIgnoreCase("pull")) {
			inflater.inflate(R.menu.pr_info_menu, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if(id == android.R.id.home) {

			if(getIntent().getStringExtra("openedFromLink") != null && getIntent().getStringExtra("openedFromLink").equals("true")) {
				Intent intent = issue.getRepository().getIntent(ctx, RepoDetailActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			finish();
			return true;
		}
		else if(id == R.id.genericMenu) {

			if(issue.hasIssue()) {
				BottomSheetSingleIssueFragment bottomSheet = new BottomSheetSingleIssueFragment(issue, issueCreator);
				bottomSheet.show(getSupportFragmentManager(), "singleIssueBottomSheet");
			}
			return true;
		}
		else if(id == R.id.prInfo) {

			View view = LayoutInflater.from(ctx).inflate(R.layout.custom_pr_info_dialog, null);

			TextView baseBranch = view.findViewById(R.id.baseBranch);
			TextView headBranch = view.findViewById(R.id.headBranch);

			baseBranch.setText(issue.getPullRequest().getBase().getRef());
			headBranch.setText(issue.getPullRequest().getHead().getRef());

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);

			alertDialog.setTitle(getResources().getString(R.string.prMergeInfo));
			alertDialog.setView(view);
			alertDialog.setPositiveButton(getString(R.string.okButton), null);
			alertDialog.create().show();
			return true;
		}
		else {

			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {

		super.onResume();
		issue.getRepository().checkAccountSwitch(this);

		if(commentPosted) {

			viewBinding.scrollViewComments.post(() -> {

				issueCommentsModel
					.loadIssueComments(repoOwner, repoName, issueIndex,
						ctx, () -> viewBinding.scrollViewComments.fullScroll(ScrollView.FOCUS_DOWN));

				commentPosted = false;
			});
		}

		if(commentEdited) {

			viewBinding.scrollViewComments.post(() -> {

				issueCommentsModel
					.loadIssueComments(repoOwner, repoName, issueIndex,
						ctx);
				commentEdited = false;
			});
		}

		if(singleIssueUpdate) {

			new Handler(Looper.getMainLooper()).postDelayed(() -> {

				viewBinding.frameAssignees.removeAllViews();
				viewBinding.frameLabels.removeAllViews();
				issue.setIssue(null);
				getSingleIssue(repoOwner, repoName, issueIndex);
				singleIssueUpdate = false;

			}, 500);
		}
	}

	private void fetchDataAsync(String owner, String repo, int index) {

		issueCommentsModel.getIssueCommentList(owner, repo, index, ctx)
			.observe(this, issueCommentsMain -> {

				assert issueCommentsMain != null;

				if(issueCommentsMain.size() > 0) {

					viewBinding.divider.setVisibility(View.VISIBLE);
				}

				Bundle bundle = new Bundle();
				bundle.putString("repoOwner", repoOwner);
				bundle.putString("repoName", repoName);
				bundle.putInt("issueNumber", issueIndex);

				adapter = new IssueCommentsAdapter(ctx, bundle, issueCommentsMain, getSupportFragmentManager(), this::onResume, issue);

				viewBinding.recyclerView.setAdapter(adapter);

			});
	}

	private void getSingleIssue(String repoOwner, String repoName, int issueIndex) {
		if(issue.hasIssue()) {
			viewBinding.progressBar.setVisibility(View.GONE);
			getSubscribed();
			initWithIssue();
			return;
		}

		Call<Issue> call = RetrofitClient.getApiInterface(ctx)
			.issueGetIssue(repoOwner, repoName, (long) issueIndex);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<Issue> call, @NonNull Response<Issue> response) {

				viewBinding.progressBar.setVisibility(View.GONE);

				if(response.code() == 200) {

					Issue singleIssue = response.body();
					assert singleIssue != null;

					issue.setIssue(singleIssue);
					initWithIssue();
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx);

				}
				else if(response.code() == 404) {

					Toasty.warning(ctx, getResources().getString(R.string.noDataFound));
					finish();
				}
			}

			@Override
			public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {

				viewBinding.progressBar.setVisibility(View.GONE);
				Log.e("onFailure", t.toString());
			}

		});

		getSubscribed();

	}

	private void getSubscribed() {
		RetrofitClient.getApiInterface(ctx)
			.issueCheckSubscription(repoOwner, repoName, (long) issueIndex)
			.enqueue(new Callback<>() {

				@Override
				public void onResponse(@NonNull Call<WatchInfo> call, @NonNull Response<WatchInfo> response) {

					if(response.isSuccessful()) {
						assert response.body() != null;
						issue.setSubscribed(response.body().isSubscribed());
					}
					else {
						issue.setSubscribed(false);
					}
				}

				@Override
				public void onFailure(@NonNull Call<WatchInfo> call, @NonNull Throwable t) {

					issue.setSubscribed(false);
				}
			});
	}

	private void initWithIssue() {
		if(!issue.getRepository().hasRepository()) {
			getRepoInfo();
		}

		viewBinding.issuePrState.setVisibility(View.VISIBLE);

		if(issue.getIssue().getPullRequest() != null) {
			getPullRequest();
			if(issue.getIssue().getPullRequest().isMerged()) { // merged

				viewBinding.issuePrState.setImageResource(R.drawable.ic_pull_request);
				ImageViewCompat.setImageTintList(viewBinding.issuePrState, ColorStateList.valueOf(ctx.getResources().getColor(R.color.iconPrMergedColor)));
			}
			else if(!issue.getIssue().getPullRequest().isMerged() && issue.getIssue().getState().equals("closed")) { // closed

				viewBinding.issuePrState.setImageResource(R.drawable.ic_pull_request);
				ImageViewCompat.setImageTintList(viewBinding.issuePrState, ColorStateList.valueOf(ctx.getResources().getColor(R.color.iconIssuePrClosedColor)));
			}
			else { // open

				viewBinding.issuePrState.setImageResource(R.drawable.ic_pull_request);
				ImageViewCompat.setImageTintList(viewBinding.issuePrState, ColorStateList.valueOf(ctx.getResources().getColor(R.color.darkGreen)));
			}
		}
		else if(issue.getIssue().getState().equals("closed")) { // issue closed

			viewBinding.issuePrState.setImageResource(R.drawable.ic_issue);
			ImageViewCompat.setImageTintList(viewBinding.issuePrState, ColorStateList.valueOf(ctx.getResources().getColor(R.color.iconIssuePrClosedColor)));
		} else {
			viewBinding.issuePrState.setImageResource(R.drawable.ic_issue);
			ImageViewCompat.setImageTintList(viewBinding.issuePrState, ColorStateList.valueOf(ctx.getResources().getColor(R.color.darkGreen)));
		}

		TinyDB tinyDb = TinyDB.getInstance(appCtx);
		final Locale locale = getResources().getConfiguration().locale;
		final String timeFormat = tinyDb.getString("dateFormat", "pretty");
		issueCreator = issue.getIssue().getUser().getLogin();

		PicassoService.getInstance(ctx).get().load(issue.getIssue().getUser().getAvatarUrl()).placeholder(R.drawable.loader_animated)
			.transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(viewBinding.assigneeAvatar);
		String issueNumber_ = "<font color='" + ResourcesCompat.getColor(getResources(), R.color.lightGray, null) + "'>" + appCtx.getResources()
			.getString(R.string.hash) + issue.getIssue().getNumber() + "</font>";
		viewBinding.issueTitle.setText(HtmlCompat.fromHtml(issueNumber_ + " " + EmojiParser.parseToUnicode(issue.getIssue().getTitle()), HtmlCompat.FROM_HTML_MODE_LEGACY));
		String cleanIssueDescription = issue.getIssue().getBody().trim();

		viewBinding.assigneeAvatar.setOnClickListener(loginId -> {
			Intent intent = new Intent(ctx, ProfileActivity.class);
			intent.putExtra("username", issue.getIssue().getUser().getLogin());
			ctx.startActivity(intent);
		});

		viewBinding.assigneeAvatar.setOnLongClickListener(loginId -> {
			AppUtil.copyToClipboard(ctx, issue.getIssue().getUser().getLogin(), ctx.getString(R.string.copyLoginIdToClipBoard, issue.getIssue().getUser().getLogin()));
			return true;
		});

		Markdown.render(ctx, EmojiParser.parseToUnicode(cleanIssueDescription), viewBinding.issueDescription, issue.getRepository());

		RelativeLayout.LayoutParams paramsDesc = (RelativeLayout.LayoutParams) viewBinding.issueDescription.getLayoutParams();

		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(80, 80);
		params1.setMargins(15, 0, 0, 0);

		if(issue.getIssue().getAssignees() != null) {

			viewBinding.assigneesScrollView.setVisibility(View.VISIBLE);

			for(int i = 0; i < issue.getIssue().getAssignees().size(); i++) {

				ImageView assigneesView = new ImageView(ctx);

				PicassoService.getInstance(ctx).get().load(issue.getIssue().getAssignees().get(i).getAvatarUrl())
					.placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(100, 100).centerCrop()
					.into(assigneesView);

				viewBinding.frameAssignees.addView(assigneesView);
				assigneesView.setLayoutParams(params1);

				int finalI = i;
				assigneesView.setOnClickListener(loginId -> {
					Intent intent = new Intent(ctx, ProfileActivity.class);
					intent.putExtra("username", issue.getIssue().getAssignees().get(finalI).getLogin());
					ctx.startActivity(intent);
				});

				assigneesView.setOnLongClickListener(loginId -> {
					AppUtil.copyToClipboard(ctx, issue.getIssue().getAssignees().get(finalI).getLogin(), ctx.getString(R.string.copyLoginIdToClipBoard, issue.getIssue().getAssignees().get(finalI).getLogin()));
					return true;
				});

				/*if(!issue.getIssue().getAssignees().get(i).getFull_name().equals("")) {

					assigneesView.setOnClickListener(
						new ClickListener(getString(R.string.assignedTo, issue.getIssue().getAssignees().get(i).getFull_name()), ctx));
				}
				else {

					assigneesView.setOnClickListener(
						new ClickListener(getString(R.string.assignedTo, issue.getIssue().getAssignees().get(i).getLogin()), ctx));
				}*/
			}
		}
		else {

			viewBinding.assigneesScrollView.setVisibility(View.GONE);
		}

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 15, 0);

		if(issue.getIssue().getLabels() != null) {

			viewBinding.labelsScrollView.setVisibility(View.VISIBLE);

			for(int i = 0; i < issue.getIssue().getLabels().size(); i++) {

				String labelColor = issue.getIssue().getLabels().get(i).getColor();
				String labelName = issue.getIssue().getLabels().get(i).getName();
				int color = Color.parseColor("#" + labelColor);

				ImageView labelsView = new ImageView(ctx);
				viewBinding.frameLabels.setOrientation(LinearLayout.HORIZONTAL);
				viewBinding.frameLabels.setGravity(Gravity.START | Gravity.TOP);
				labelsView.setLayoutParams(params);

				int height = AppUtil.getPixelsFromDensity(ctx, 20);
				int textSize = AppUtil.getPixelsFromScaledDensity(ctx, 12);

				TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT)
					.textColor(new ColorInverter().getContrastColor(color)).fontSize(textSize)
					.width(LabelWidthCalculator.calculateLabelWidth(labelName, Typeface.DEFAULT, textSize, AppUtil.getPixelsFromDensity(ctx, 10)))
					.height(height).endConfig().buildRoundRect(labelName, color, AppUtil.getPixelsFromDensity(ctx, 18));

				labelsView.setImageDrawable(drawable);
				viewBinding.frameLabels.addView(labelsView);
			}
		}
		else {

			viewBinding.labelsScrollView.setVisibility(View.GONE);
		}

		if(issue.getIssue().getDueDate() != null) {

			viewBinding.dueDateFrame.setVisibility(View.VISIBLE);
			if(timeFormat.equals("normal") || timeFormat.equals("pretty")) {

				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", locale);
				String dueDate = formatter.format(issue.getIssue().getDueDate());
				viewBinding.issueDueDate.setText(dueDate);
				viewBinding.issueDueDate
					.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(issue.getIssue().getDueDate()), ctx));
			}
			else if(timeFormat.equals("normal1")) {

				DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", locale);
				String dueDate = formatter.format(issue.getIssue().getDueDate());
				viewBinding.issueDueDate.setText(dueDate);
			}
		}
		else {

			viewBinding.dueDateFrame.setVisibility(View.GONE);
		}

		String edited;

		if(!issue.getIssue().getUpdatedAt().equals(issue.getIssue().getUpdatedAt())) {

			edited = getString(R.string.colorfulBulletSpan) + getString(R.string.modifiedText);
			viewBinding.issueModified.setVisibility(View.VISIBLE);
			viewBinding.issueModified.setText(edited);
			viewBinding.issueModified
				.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(issue.getIssue().getUpdatedAt()), ctx));
		}
		else {

			viewBinding.issueModified.setVisibility(View.INVISIBLE);
		}

		if((issue.getIssue().getDueDate() == null && issue.getIssue().getMilestone() == null) && issue.getIssue().getAssignees() != null) {

			paramsDesc.setMargins(0, 35, 0, 0);
			viewBinding.issueDescription.setLayoutParams(paramsDesc);
		}
		else if(issue.getIssue().getDueDate() == null && issue.getIssue().getMilestone() == null) {

			paramsDesc.setMargins(0, 55, 0, 0);
			viewBinding.issueDescription.setLayoutParams(paramsDesc);
		}
		else if(issue.getIssue().getAssignees() == null) {

			paramsDesc.setMargins(0, 35, 0, 0);
			viewBinding.issueDescription.setLayoutParams(paramsDesc);
		}
		else {

			paramsDesc.setMargins(0, 15, 0, 0);
			viewBinding.issueDescription.setLayoutParams(paramsDesc);
		}

		viewBinding.issueCreatedTime.setText(TimeHelper.formatTime(issue.getIssue().getCreatedAt(), locale, timeFormat, ctx));
		viewBinding.issueCreatedTime.setVisibility(View.VISIBLE);

		if(timeFormat.equals("pretty")) {

			viewBinding.issueCreatedTime
				.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(issue.getIssue().getCreatedAt()), ctx));
		}

		Bundle bundle = new Bundle();
		bundle.putString("repoOwner", repoOwner);
		bundle.putString("repoName", repoName);
		bundle.putInt("issueId", Math.toIntExact(issue.getIssue().getNumber()));

		ReactionList reactionList = new ReactionList(ctx, bundle);

		viewBinding.commentReactionBadges.removeAllViews();
		viewBinding.commentReactionBadges.addView(reactionList);

		reactionList.setOnReactionAddedListener(() -> {

			if(viewBinding.commentReactionBadges.getVisibility() != View.VISIBLE) {
				viewBinding.commentReactionBadges.post(() -> viewBinding.commentReactionBadges.setVisibility(View.VISIBLE));
			}
		});

		if(issue.getIssue().getMilestone() != null) {

			viewBinding.milestoneFrame.setVisibility(View.VISIBLE);
			viewBinding.issueMilestone.setText(issue.getIssue().getMilestone().getTitle());
		}
		else {

			viewBinding.milestoneFrame.setVisibility(View.GONE);
		}

		/*if(!issue.getIssue().getUser().getFull_name().equals("")) {

			viewBinding.assigneeAvatar.setOnClickListener(
				new ClickListener(ctx.getResources().getString(R.string.issueCreator) + issue.getIssue().getUser().getFull_name(), ctx));
		}
		else {

			viewBinding.assigneeAvatar.setOnClickListener(
				new ClickListener(ctx.getResources().getString(R.string.issueCreator) + issue.getIssue().getUser().getLogin(), ctx));
		}*/
	}

	private void getPullRequest() {
		RetrofitClient.getApiInterface(this).repoGetPullRequest(repoOwner, repoName, (long) issueIndex).enqueue(new Callback<PullRequest>() {

			@Override
			public void onResponse(@NonNull Call<PullRequest> call, @NonNull Response<PullRequest> response) {
				if(response.isSuccessful() && response.body() != null) {
					issue.setPullRequest(response.body());
				}
			}

			@Override
			public void onFailure(@NonNull Call<PullRequest> call, @NonNull Throwable t) {
			}
		});
	}

	private void getRepoInfo() {
		Call<Repository> call = RetrofitClient.getApiInterface(ctx).repoGet(issue.getRepository().getOwner(), issue.getRepository().getName());
		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<Repository> call, @NonNull retrofit2.Response<Repository> response) {

				Repository repoInfo = response.body();

				if(response.code() == 200) {
					assert repoInfo != null;
					issue.getRepository().setRepository(repoInfo);
				}
				else {
					Toasty.error(ctx, getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {

				Toasty.error(ctx, getString(R.string.genericError));
			}
		});
	}
}
