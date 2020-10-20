package org.mian.gitnex.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
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
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.amulyakhare.textdrawable.TextDrawable;
import com.google.gson.JsonElement;
import com.vdurmont.emoji.EmojiParser;
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
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.LabelWidthCalculator;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UserMentions;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.models.Labels;
import org.mian.gitnex.models.UpdateIssueAssignees;
import org.mian.gitnex.models.WatchInfo;
import org.mian.gitnex.viewmodels.IssueCommentsViewModel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.DefaultMediaDecoder;
import io.noties.markwon.image.ImageItem;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.SchemeHandler;
import io.noties.markwon.image.gif.GifMediaDecoder;
import io.noties.markwon.image.svg.SvgMediaDecoder;
import io.noties.markwon.linkify.LinkifyPlugin;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class IssueDetailActivity extends BaseActivity implements LabelsListAdapter.LabelsListAdapterListener, AssigneesListAdapter.AssigneesListAdapterListener, BottomSheetSingleIssueFragment.BottomSheetListener {

	private IssueCommentsAdapter adapter;
	final Context ctx = this;
	private Context appCtx;
	private TinyDB tinyDb;

	private String instanceUrl;
	private String loginUid;
	private String instanceToken;
	private String repoOwner;
	private String repoName;
	private int issueIndex;

	private LabelsListAdapter labelsAdapter;
	private AssigneesListAdapter assigneesAdapter;

	private List<Integer> currentLabelsIds = new ArrayList<>();
	private List<Integer> labelsIds = new ArrayList<>();
	private List<Labels> labelsList = new ArrayList<>();
	private List<Collaborators> assigneesList = new ArrayList<>();
	private List<String> assigneesListData = new ArrayList<>();
	private List<String> currentAssignees = new ArrayList<>();

	private Dialog dialogLabels;
	private Dialog dialogAssignees;

	private CustomLabelsSelectionDialogBinding labelsBinding;
	private CustomAssigneesSelectionDialogBinding assigneesBinding;
	private ActivityIssueDetailBinding viewBinding;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_issue_detail;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();
		tinyDb = new TinyDB(appCtx);

		viewBinding = ActivityIssueDetailBinding.inflate(getLayoutInflater());
		View view = viewBinding.getRoot();
		setContentView(view);

		instanceUrl = tinyDb.getString("instanceUrl");
		loginUid = tinyDb.getString("loginUid");
		instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		repoOwner = parts[0];
		repoName = parts[1];
		issueIndex = Integer.parseInt(tinyDb.getString("issueNumber"));

		setSupportActionBar(viewBinding.toolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle(repoName);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		viewBinding.recyclerView.setHasFixedSize(true);
		viewBinding.recyclerView.setNestedScrollingEnabled(false);
		viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(viewBinding.recyclerView.getContext(), DividerItemDecoration.VERTICAL);
		viewBinding.recyclerView.addItemDecoration(dividerItemDecoration);

		viewBinding.addNewComment.setOnClickListener(v -> BottomSheetReplyFragment.newInstance(new Bundle()).show(getSupportFragmentManager(), "replyBottomSheet"));

		labelsAdapter = new LabelsListAdapter(labelsList, IssueDetailActivity.this, currentLabelsIds);
		assigneesAdapter = new AssigneesListAdapter(ctx, assigneesList, IssueDetailActivity.this, currentAssignees);
		LabelsActions.getCurrentIssueLabels(ctx, instanceUrl, loginUid, instanceToken, repoOwner, repoName, issueIndex, currentLabelsIds);
		AssigneesActions.getCurrentIssueAssignees(ctx, instanceUrl, loginUid, instanceToken, repoOwner, repoName, issueIndex, currentAssignees);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

			viewBinding.scrollViewComments.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

				if((scrollY - oldScrollY) > 0 && viewBinding.addNewComment.isShown()) {
					viewBinding.addNewComment.setVisibility(View.GONE);
				}
				else if((scrollY - oldScrollY) < 0) {
					viewBinding.addNewComment.setVisibility(View.VISIBLE);
				}

				if(!viewBinding.scrollViewComments.canScrollVertically(1)) { // bottom
					viewBinding.addNewComment.setVisibility(View.GONE);
				}

				if(!viewBinding.scrollViewComments.canScrollVertically(-1)) { // top
					viewBinding.addNewComment.setVisibility(View.VISIBLE);
				}

			});

		}

		viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			viewBinding.pullToRefresh.setRefreshing(false);
			IssueCommentsViewModel
				.loadIssueComments(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex,
					ctx);

		}, 500));

		Typeface myTypeface;

		switch(tinyDb.getInt("customFontId", -1)) {

			case 1:
				myTypeface = Typeface.createFromAsset(Objects.requireNonNull(ctx).getAssets(), "fonts/manroperegular.ttf");
				break;

			case 2:
				myTypeface = Typeface.createFromAsset(Objects.requireNonNull(ctx).getAssets(), "fonts/sourcecodeproregular.ttf");
				break;

			default:
				myTypeface = Typeface.createFromAsset(Objects.requireNonNull(ctx).getAssets(), "fonts/roboto.ttf");
				break;

		}

		viewBinding.toolbarTitle.setTypeface(myTypeface);
		viewBinding.toolbarTitle.setText(repoName);

		getSingleIssue(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid);
		fetchDataAsync(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid);

	}

	@Override
	public void onButtonClicked(String text) {

		switch(text) {

			case "showLabels":
				showLabels();
				break;

			case "showAssignees":
				showAssignees();
				break;
		}

	}

	@Override
	public void labelsInterface(List<String> data) {
	}

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
		dialogAssignees.setCancelable(false);

		if (dialogAssignees.getWindow() != null) {

			dialogAssignees.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		assigneesBinding = CustomAssigneesSelectionDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = assigneesBinding.getRoot();
		dialogAssignees.setContentView(view);

		assigneesBinding.cancel.setOnClickListener(assigneesBinding_ -> {

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

		AssigneesActions.getRepositoryAssignees(ctx, instanceUrl, instanceToken, repoOwner, repoName, assigneesList, dialogAssignees, assigneesAdapter, assigneesBinding);
	}

	public void showLabels() {

		labelsAdapter.updateList(currentLabelsIds);
		dialogLabels = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
		dialogLabels.setCancelable(false);

		if (dialogLabels.getWindow() != null) {

			dialogLabels.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		labelsBinding = CustomLabelsSelectionDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = labelsBinding.getRoot();
		dialogLabels.setContentView(view);

		labelsBinding.cancel.setOnClickListener(labelsBinding_ -> {

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

		LabelsActions.getRepositoryLabels(ctx, instanceUrl, instanceToken, repoOwner, repoName, labelsList, dialogLabels, labelsAdapter, labelsBinding);
	}

	private void updateIssueAssignees() {

		UpdateIssueAssignees updateAssigneeJson = new UpdateIssueAssignees(assigneesListData);

		Call<JsonElement> call3;

		call3 = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.patchIssueAssignees(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex, updateAssigneeJson);

		call3.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response2) {

				if(response2.code() == 201) {

					Toasty.success(ctx, ctx.getString(R.string.assigneesUpdated));

					dialogAssignees.dismiss();

					viewBinding.frameAssignees.removeAllViews();
					viewBinding.frameLabels.removeAllViews();
					getSingleIssue(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid);
					currentAssignees.clear();
					new Handler(Looper.getMainLooper()).postDelayed(() -> AssigneesActions.getCurrentIssueAssignees(ctx, instanceUrl, loginUid, instanceToken, repoOwner, repoName, issueIndex, currentAssignees), 1000);
				}
				else if(response2.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
						getResources().getString(R.string.alertDialogTokenRevokedMessage),
						getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
						getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
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
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
				Log.e("onFailure", t.toString());
			}
		});

	}

	private void updateIssueLabels() {

		Labels patchIssueLabels = new Labels(labelsIds);

		Call<JsonElement> call = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.updateIssueLabels(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex, patchIssueLabels);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.code() == 200) {

					Toasty.success(ctx, ctx.getString(R.string.labelsUpdated));
					dialogLabels.dismiss();

					viewBinding.frameAssignees.removeAllViews();
					viewBinding.frameLabels.removeAllViews();
					getSingleIssue(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid);
					currentLabelsIds.clear();
					new Handler(Looper.getMainLooper()).postDelayed(() -> LabelsActions.getCurrentIssueLabels(ctx, instanceUrl, loginUid, instanceToken, repoOwner, repoName, issueIndex, currentLabelsIds), 1000);
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
						getResources().getString(R.string.alertDialogTokenRevokedMessage),
						getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
						getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
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
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
				Log.e("onFailure", t.toString());
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.generic_nav_dotted_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		switch(id) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.genericMenu:
				BottomSheetSingleIssueFragment bottomSheet = new BottomSheetSingleIssueFragment();
				bottomSheet.show(getSupportFragmentManager(), "singleIssueBottomSheet");
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onResume() {

		super.onResume();

		if(tinyDb.getBoolean("commentPosted")) {
			viewBinding.scrollViewComments.post(() -> {

				IssueCommentsViewModel
					.loadIssueComments(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex,
						ctx);

				new Handler(Looper.getMainLooper()).postDelayed(() -> viewBinding.scrollViewComments.fullScroll(ScrollView.FOCUS_DOWN), 1000);

				tinyDb.putBoolean("commentPosted", false);

			});
		}

		if(tinyDb.getBoolean("commentEdited")) {
			viewBinding.scrollViewComments.post(() -> {

				IssueCommentsViewModel
					.loadIssueComments(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex,
						ctx);
				tinyDb.putBoolean("commentEdited", false);

			});
		}

		if(tinyDb.getBoolean("singleIssueUpdate")) {

			new Handler(Looper.getMainLooper()).postDelayed(() -> {

				viewBinding.frameAssignees.removeAllViews();
				viewBinding.frameLabels.removeAllViews();
				getSingleIssue(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid);
				tinyDb.putBoolean("singleIssueUpdate", false);

			}, 500);

		}

		if(tinyDb.getBoolean("issueEdited")) {

			new Handler(Looper.getMainLooper()).postDelayed(() -> {

				viewBinding.frameAssignees.removeAllViews();
				viewBinding.frameLabels.removeAllViews();
				getSingleIssue(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid);
				tinyDb.putBoolean("issueEdited", false);

			}, 500);

		}

	}

	private void fetchDataAsync(String instanceUrl, String instanceToken, String owner, String repo, int index, String loginUid) {

		IssueCommentsViewModel issueCommentsModel = new ViewModelProvider(this).get(IssueCommentsViewModel.class);

		issueCommentsModel.getIssueCommentList(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), owner, repo, index, ctx)
			.observe(this, issueCommentsMain -> {

				assert issueCommentsMain != null;

				if(issueCommentsMain.size() > 0) {
					viewBinding.divider.setVisibility(View.VISIBLE);
				}

				adapter = new IssueCommentsAdapter(ctx, getSupportFragmentManager(), issueCommentsMain);
				viewBinding.recyclerView.setAdapter(adapter);

			});

	}

	private void getSingleIssue(String instanceUrl, String instanceToken, String repoOwner, String repoName, int issueIndex, String loginUid) {

		final TinyDB tinyDb = new TinyDB(appCtx);
		Call<Issues> call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface()
			.getIssueByIndex(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex);

		call.enqueue(new Callback<Issues>() {

			@Override
			public void onResponse(@NonNull Call<Issues> call, @NonNull Response<Issues> response) {

				if(response.code() == 200) {

					Issues singleIssue = response.body();
					assert singleIssue != null;

					viewBinding.issuePrState.setVisibility(View.VISIBLE);
					if(singleIssue.getPull_request() != null) {

						if(singleIssue.getPull_request().isMerged()) { // merged

							viewBinding.issuePrState.setImageResource(R.drawable.ic_pull_request_merged);
						}
						else if(!singleIssue.getPull_request().isMerged() && singleIssue.getState().equals("closed")) { // closed

							viewBinding.issuePrState.setImageResource(R.drawable.ic_pull_request_closed);
						}
						else { // open

							viewBinding.issuePrState.setImageResource(R.drawable.ic_pull_request);
						}
					}
					else if(singleIssue.getState().equals("closed")) { // issue closed

						viewBinding.issuePrState.setImageResource(R.drawable.ic_issue_closed_red);
					}

					final Markwon markwon = Markwon.builder(Objects.requireNonNull(ctx)).usePlugin(CorePlugin.create())
						.usePlugin(ImagesPlugin.create(plugin -> {
							plugin.addSchemeHandler(new SchemeHandler() {

								@NonNull
								@Override
								public ImageItem handle(@NonNull String raw, @NonNull Uri uri) {

									final int resourceId = ctx.getResources()
										.getIdentifier(raw.substring("drawable://".length()), "drawable", ctx.getPackageName());

									final Drawable drawable = ctx.getDrawable(resourceId);

									assert drawable != null;
									return ImageItem.withResult(drawable);
								}

								@NonNull
								@Override
								public Collection<String> supportedSchemes() {

									return Collections.singleton("drawable");
								}
							});
							plugin.placeholderProvider(drawable -> null);
							plugin.addMediaDecoder(GifMediaDecoder.create(false));
							plugin.addMediaDecoder(SvgMediaDecoder.create(ctx.getResources()));
							plugin.addMediaDecoder(SvgMediaDecoder.create());
							plugin.defaultMediaDecoder(DefaultMediaDecoder.create(ctx.getResources()));
							plugin.defaultMediaDecoder(DefaultMediaDecoder.create());

						})).usePlugin(new AbstractMarkwonPlugin() {

							@Override
							public void configureTheme(@NonNull MarkwonTheme.Builder builder) {

								builder.codeTextColor(tinyDb.getInt("codeBlockColor")).codeBackgroundColor(tinyDb.getInt("codeBlockBackground"))
									.linkColor(getResources().getColor(R.color.lightBlue));
							}
						}).usePlugin(TablePlugin.create(ctx)).usePlugin(TaskListPlugin.create(ctx)).usePlugin(HtmlPlugin.create())
						.usePlugin(StrikethroughPlugin.create()).usePlugin(LinkifyPlugin.create()).build();

					TinyDB tinyDb = new TinyDB(appCtx);
					final String locale = tinyDb.getString("locale");
					final String timeFormat = tinyDb.getString("dateFormat");
					tinyDb.putString("issueState", singleIssue.getState());
					tinyDb.putString("issueTitle", singleIssue.getTitle());
					tinyDb.putString("singleIssueHtmlUrl", singleIssue.getHtml_url());

					PicassoService.getInstance(ctx).get().load(singleIssue.getUser().getAvatar_url()).placeholder(R.drawable.loader_animated)
						.transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(viewBinding.assigneeAvatar);
					String issueNumber_ = "<font color='" + appCtx.getResources().getColor(R.color.lightGray) + "'>" + appCtx.getResources()
						.getString(R.string.hash) + singleIssue.getNumber() + "</font>";
					viewBinding.issueTitle.setText(Html.fromHtml(issueNumber_ + " " + singleIssue.getTitle()));
					String cleanIssueDescription = singleIssue.getBody().trim();
					Spanned bodyWithMD = markwon.toMarkdown(EmojiParser.parseToUnicode(cleanIssueDescription));
					markwon.setParsedMarkdown(viewBinding.issueDescription, UserMentions.UserMentionsFunc(ctx, bodyWithMD, cleanIssueDescription));

					RelativeLayout.LayoutParams paramsDesc = (RelativeLayout.LayoutParams) viewBinding.issueDescription.getLayoutParams();

					LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(80, 80);
					params1.setMargins(15, 0, 0, 0);

					if(singleIssue.getAssignees() != null) {
						viewBinding.assigneesScrollView.setVisibility(View.VISIBLE);
						for(int i = 0; i < singleIssue.getAssignees().size(); i++) {

							ImageView assigneesView = new ImageView(ctx);

							PicassoService.getInstance(ctx).get().load(singleIssue.getAssignees().get(i).getAvatar_url())
								.placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(100, 100).centerCrop()
								.into(assigneesView);

							viewBinding.frameAssignees.addView(assigneesView);
							assigneesView.setLayoutParams(params1);
							if(!singleIssue.getAssignees().get(i).getFull_name().equals("")) {
								assigneesView.setOnClickListener(
									new ClickListener(getString(R.string.assignedTo, singleIssue.getAssignees().get(i).getFull_name()), ctx));
							}
							else {
								assigneesView.setOnClickListener(
									new ClickListener(getString(R.string.assignedTo, singleIssue.getAssignees().get(i).getLogin()), ctx));
							}

						}
					}
					else {
						viewBinding.assigneesScrollView.setVisibility(View.GONE);
					}

					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
					params.setMargins(0, 0, 15, 0);

					if(singleIssue.getLabels() != null) {
						viewBinding.labelsScrollView.setVisibility(View.VISIBLE);

						for(int i = 0; i < singleIssue.getLabels().size(); i++) {

							String labelColor = singleIssue.getLabels().get(i).getColor();
							String labelName = singleIssue.getLabels().get(i).getName();
							int color = Color.parseColor("#" + labelColor);

							ImageView labelsView = new ImageView(ctx);
							viewBinding.frameLabels.setOrientation(LinearLayout.HORIZONTAL);
							viewBinding.frameLabels.setGravity(Gravity.START | Gravity.TOP);
							labelsView.setLayoutParams(params);

							int height = AppUtil.getPixelsFromDensity(ctx, 25);
							int textSize = AppUtil.getPixelsFromScaledDensity(ctx, 15);

							TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT)
								.textColor(new ColorInverter().getContrastColor(color)).fontSize(textSize)
								.width(LabelWidthCalculator.calculateLabelWidth(labelName, Typeface.DEFAULT, textSize, AppUtil.getPixelsFromDensity(ctx, 10)))
								.height(height).endConfig().buildRoundRect(labelName, color, AppUtil.getPixelsFromDensity(ctx, 5));

							labelsView.setImageDrawable(drawable);
							viewBinding.frameLabels.addView(labelsView);

						}
					}
					else {
						viewBinding.labelsScrollView.setVisibility(View.GONE);
					}

					if(singleIssue.getDue_date() != null) {

						if(timeFormat.equals("normal") || timeFormat.equals("pretty")) {
							DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", new Locale(locale));
							String dueDate = formatter.format(singleIssue.getDue_date());
							viewBinding.issueDueDate.setText(dueDate);
							viewBinding.issueDueDate
								.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(singleIssue.getDue_date()), ctx));
						}
						else if(timeFormat.equals("normal1")) {
							DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", new Locale(locale));
							String dueDate = formatter.format(singleIssue.getDue_date());
							viewBinding.issueDueDate.setText(dueDate);
						}

					}
					else {

						viewBinding.issueDueDate.setVisibility(View.GONE);
					}

					String edited;

					if(!singleIssue.getUpdated_at().equals(singleIssue.getCreated_at())) {
						edited = getString(R.string.colorfulBulletSpan) + getString(R.string.modifiedText);
						viewBinding.issueModified.setVisibility(View.VISIBLE);
						viewBinding.issueModified.setText(edited);
						viewBinding.issueModified
							.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(singleIssue.getUpdated_at()), ctx));
					}
					else {
						viewBinding.issueModified.setVisibility(View.INVISIBLE);
					}

					if((singleIssue.getDue_date() == null && singleIssue.getMilestone() == null) && singleIssue.getAssignees() != null) {
						paramsDesc.setMargins(0, 35, 0, 0);
						viewBinding.issueDescription.setLayoutParams(paramsDesc);
					}
					else if(singleIssue.getDue_date() == null && singleIssue.getMilestone() == null) {
						paramsDesc.setMargins(0, 55, 0, 0);
						viewBinding.issueDescription.setLayoutParams(paramsDesc);
					}
					else if(singleIssue.getAssignees() == null) {
						paramsDesc.setMargins(0, 35, 0, 0);
						viewBinding.issueDescription.setLayoutParams(paramsDesc);
					}
					else {
						paramsDesc.setMargins(0, 15, 0, 0);
						viewBinding.issueDescription.setLayoutParams(paramsDesc);
					}

					viewBinding.issueCreatedTime.setText(TimeHelper.formatTime(singleIssue.getCreated_at(), new Locale(locale), timeFormat, ctx));
					viewBinding.issueCreatedTime.setVisibility(View.VISIBLE);

					if(timeFormat.equals("pretty")) {
						viewBinding.issueCreatedTime
							.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(singleIssue.getCreated_at()), ctx));
					}

					if(singleIssue.getMilestone() != null) {
						viewBinding.issueMilestone.setVisibility(View.VISIBLE);
						viewBinding.issueMilestone.setText(getString(R.string.issueMilestone, singleIssue.getMilestone().getTitle()));
					}
					else {
						viewBinding.issueMilestone.setVisibility(View.GONE);
					}

					if(!singleIssue.getUser().getFull_name().equals("")) {
						viewBinding.assigneeAvatar.setOnClickListener(
							new ClickListener(ctx.getResources().getString(R.string.issueCreator) + singleIssue.getUser().getFull_name(), ctx));
					}
					else {
						viewBinding.assigneeAvatar.setOnClickListener(
							new ClickListener(ctx.getResources().getString(R.string.issueCreator) + singleIssue.getUser().getLogin(), ctx));
					}

					viewBinding.progressBar.setVisibility(View.GONE);

				}

				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
						getResources().getString(R.string.alertDialogTokenRevokedMessage),
						getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
						getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}

			}

			@Override
			public void onFailure(@NonNull Call<Issues> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}

		});

		if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {

			Call<WatchInfo> call2 = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface()
				.checkIssueWatchStatus(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex);

			call2.enqueue(new Callback<WatchInfo>() {

				@Override
				public void onResponse(@NonNull Call<WatchInfo> call, @NonNull Response<WatchInfo> response) {

					if(response.isSuccessful()) {

						assert response.body() != null;
						tinyDb.putBoolean("issueSubscribed", response.body().getSubscribed());

					}
					else {

						tinyDb.putBoolean("issueSubscribed", false);

					}

				}

				@Override
				public void onFailure(@NonNull Call<WatchInfo> call, @NonNull Throwable t) {

					tinyDb.putBoolean("issueSubscribed", false);

				}

			});

		}

	}

}
