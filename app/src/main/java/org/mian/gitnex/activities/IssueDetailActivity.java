package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.IssueCommentsAdapter;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.BottomSheetSingleIssueFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.LabelWidthCalculator;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.UserMentions;
import org.mian.gitnex.models.IssueComments;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.viewmodels.IssueCommentsViewModel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class IssueDetailActivity extends BaseActivity {

    public ImageView closeActivity;
    private IssueCommentsAdapter adapter;
    private RecyclerView mRecyclerView;
    private ImageView assigneeAvatar;
    private TextView issueTitle;
    private TextView issueDescription;
    private TextView issueMilestone;
    private TextView issueDueDate;
    private TextView issueCreatedTime;
    private HorizontalScrollView labelsScrollView;
    private HorizontalScrollView assigneesScrollView;
    private ScrollView scrollViewComments;
    private TextView issueModified;
    private ImageView createNewComment;
    final Context ctx = this;
    private LinearLayout labelsLayout;
    private LinearLayout assigneesLayout;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_issue_detail;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final int issueIndex = Integer.parseInt(tinyDb.getString("issueNumber"));

        final SwipeRefreshLayout swipeRefresh = findViewById(R.id.pullToRefresh);

        assigneeAvatar = findViewById(R.id.assigneeAvatar);
        issueTitle = findViewById(R.id.issueTitle);
        issueDescription = findViewById(R.id.issueDescription);
        issueMilestone = findViewById(R.id.issueMilestone);
        issueDueDate = findViewById(R.id.issueDueDate);
        issueCreatedTime = findViewById(R.id.issueCreatedTime);
        labelsScrollView = findViewById(R.id.labelsScrollView);
        assigneesScrollView = findViewById(R.id.assigneesScrollView);
        scrollViewComments = findViewById(R.id.scrollViewComments);
        issueModified = findViewById(R.id.issueModified);
        createNewComment = findViewById(R.id.addNewComment);
        labelsLayout = findViewById(R.id.frameLabels);
        assigneesLayout = findViewById(R.id.frameAssignees);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(repoName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        createNewComment.setOnClickListener(v -> startActivity(new Intent(ctx, ReplyToIssueActivity.class)));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            scrollViewComments.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                if ((scrollY - oldScrollY) > 0 && createNewComment.isShown()) {
                    createNewComment.setVisibility(View.GONE);
                }
                else if ((scrollY - oldScrollY) < 0) {
                    createNewComment.setVisibility(View.VISIBLE);
                }

                if (!scrollViewComments.canScrollVertically(1)) { // bottom
                    createNewComment.setVisibility(View.GONE);
                }

                if (!scrollViewComments.canScrollVertically(-1)) { // top
                    createNewComment.setVisibility(View.VISIBLE);
                }

            });

        }

        swipeRefresh.setOnRefreshListener(() -> new Handler().postDelayed(() -> {

            swipeRefresh.setRefreshing(false);
            IssueCommentsViewModel.loadIssueComments(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, issueIndex, getApplicationContext());

        }, 500));

        Typeface myTypeface;

        switch(tinyDb.getInt("customFontId", -1)) {

            case 1:
                myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getApplicationContext()).getAssets(), "fonts/manroperegular.ttf");
                break;

            case 2:
                myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getApplicationContext()).getAssets(), "fonts/sourcecodeproregular.ttf");
                break;

            default:
                myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getApplicationContext()).getAssets(), "fonts/roboto.ttf");
                break;

        }

        toolbarTitle.setTypeface(myTypeface);
        toolbarTitle.setText(repoName);

        getSingleIssue(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid);
        fetchDataAsync(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid);

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

        switch (id) {
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
        final TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        final int issueIndex = Integer.parseInt(tinyDb.getString("issueNumber"));

        if(tinyDb.getBoolean("commentPosted")) {
            scrollViewComments.post(new Runnable() {
                @Override
                public void run() {
                    IssueCommentsViewModel.loadIssueComments(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, issueIndex, getApplicationContext());

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollViewComments.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    }, 1000);

                    tinyDb.putBoolean("commentPosted", false);
                }
            });
        }

        if(tinyDb.getBoolean("commentEdited")) {
            scrollViewComments.post(new Runnable() {
                @Override
                public void run() {
                    IssueCommentsViewModel.loadIssueComments(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, issueIndex, getApplicationContext());
                    tinyDb.putBoolean("commentEdited", false);
                }
            });
        }

        if(tinyDb.getBoolean("singleIssueUpdate")) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    assigneesLayout.removeAllViews();
                    labelsLayout.removeAllViews();
                    getSingleIssue(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid);
                    tinyDb.putBoolean("singleIssueUpdate", false);
                }
            }, 500);

        }

        if(tinyDb.getBoolean("issueEdited")) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    assigneesLayout.removeAllViews();
                    labelsLayout.removeAllViews();
                    getSingleIssue(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid);
                    tinyDb.putBoolean("issueEdited", false);
                }
            }, 500);

        }

    }

    private void fetchDataAsync(String instanceUrl, String instanceToken, String owner, String repo, int index, String loginUid) {

        IssueCommentsViewModel issueCommentsModel = new ViewModelProvider(this).get(IssueCommentsViewModel.class);

        issueCommentsModel.getIssueCommentList(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), owner, repo, index, getApplicationContext()).observe(this, new Observer<List<IssueComments>>() {
            @Override
            public void onChanged(@Nullable List<IssueComments> issueCommentsMain) {
                adapter = new IssueCommentsAdapter(getApplicationContext(), issueCommentsMain);
                mRecyclerView.setAdapter(adapter);
            }
        });

    }

    private void getSingleIssue(String instanceUrl, String instanceToken, String repoOwner, String repoName, int issueIndex, String loginUid) {

        final TinyDB tinyDb = new TinyDB(getApplicationContext());
        Call<Issues> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getIssueByIndex(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, issueIndex);

        call.enqueue(new Callback<Issues>() {

            @Override
            public void onResponse(@NonNull Call<Issues> call, @NonNull Response<Issues> response) {

                if (response.isSuccessful()) {
                    if (response.code() == 200) {

                        Issues singleIssue = response.body();
                        assert singleIssue != null;

                        final Markwon markwon = Markwon.builder(Objects.requireNonNull(getApplicationContext()))
                                .usePlugin(CorePlugin.create())
                                .usePlugin(ImagesPlugin.create(plugin -> {
                                    plugin.addSchemeHandler(new SchemeHandler() {
                                        @NonNull
                                        @Override
                                        public ImageItem handle(@NonNull String raw, @NonNull Uri uri) {

                                            final int resourceId = getApplicationContext().getResources().getIdentifier(
                                                    raw.substring("drawable://".length()),
                                                    "drawable",
                                                    getApplicationContext().getPackageName());

                                            final Drawable drawable = getApplicationContext().getDrawable(resourceId);

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
                                    plugin.addMediaDecoder(SvgMediaDecoder.create(getApplicationContext().getResources()));
                                    plugin.addMediaDecoder(SvgMediaDecoder.create());
                                    plugin.defaultMediaDecoder(DefaultMediaDecoder.create(getApplicationContext().getResources()));
                                    plugin.defaultMediaDecoder(DefaultMediaDecoder.create());

                                }))
                                .usePlugin(new AbstractMarkwonPlugin() {
                                    @Override
                                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                                        builder
                                                .codeTextColor(tinyDb.getInt("codeBlockColor"))
                                                .codeBackgroundColor(tinyDb.getInt("codeBlockBackground"))
                                                .linkColor(getResources().getColor(R.color.lightBlue));
                                    }
                                })
                                .usePlugin(TablePlugin.create(getApplicationContext()))
                                .usePlugin(TaskListPlugin.create(getApplicationContext()))
                                .usePlugin(HtmlPlugin.create())
                                .usePlugin(StrikethroughPlugin.create())
                                .usePlugin(LinkifyPlugin.create())
                                .build();

                        TinyDB tinyDb = new TinyDB(getApplicationContext());
                        final String locale = tinyDb.getString("locale");
                        final String timeFormat = tinyDb.getString("dateFormat");
                        tinyDb.putString("issueState", singleIssue.getState());
                        tinyDb.putString("issueTitle", singleIssue.getTitle());

                        PicassoService.getInstance(ctx).get().load(singleIssue.getUser().getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(assigneeAvatar);
                        String issueNumber_ = "<font color='" + getApplicationContext().getResources().getColor(R.color.lightGray) + "'>" + getApplicationContext().getResources().getString(R.string.hash) + singleIssue.getNumber() + "</font>";
                        issueTitle.setText(Html.fromHtml(issueNumber_ + " " + singleIssue.getTitle()));
                        String cleanIssueDescription = singleIssue.getBody().trim();
                        Spanned bodyWithMD = markwon.toMarkdown(EmojiParser.parseToUnicode(cleanIssueDescription));
                        markwon.setParsedMarkdown(issueDescription, UserMentions.UserMentionsFunc(getApplicationContext(), bodyWithMD, cleanIssueDescription));

                        RelativeLayout.LayoutParams paramsDesc = (RelativeLayout.LayoutParams)issueDescription.getLayoutParams();

                        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(80, 80);
                        params1.setMargins(15, 0, 0, 0);

                        if(singleIssue.getAssignees() != null) {
                            assigneesScrollView.setVisibility(View.VISIBLE);
                            for (int i = 0; i < singleIssue.getAssignees().size(); i++) {

                                ImageView assigneesView = new ImageView(getApplicationContext());

                                PicassoService.getInstance(ctx).get().load(singleIssue.getAssignees().get(i).getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(100, 100).centerCrop().into(assigneesView);

                                assigneesLayout.addView(assigneesView);
                                assigneesView.setLayoutParams(params1);
                                if (!singleIssue.getAssignees().get(i).getFull_name().equals("")) {
                                    assigneesView.setOnClickListener(new ClickListener(getString(R.string.assignedTo, singleIssue.getAssignees().get(i).getFull_name()), getApplicationContext()));
                                } else {
                                    assigneesView.setOnClickListener(new ClickListener(getString(R.string.assignedTo, singleIssue.getAssignees().get(i).getLogin()), getApplicationContext()));
                                }

                            }
                        }
                        else {
                            assigneesScrollView.setVisibility(View.GONE);
                        }

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 0, 15, 0);

                        if(singleIssue.getLabels() != null) {
                            labelsScrollView.setVisibility(View.VISIBLE);
                            int width = 25;
                            for (int i = 0; i < singleIssue.getLabels().size(); i++) {

                                String labelColor = singleIssue.getLabels().get(i).getColor();
                                String labelName = singleIssue.getLabels().get(i).getName();
                                int color = Color.parseColor("#" + labelColor);

                                ImageView labelsView = new ImageView(getApplicationContext());
                                labelsLayout.setOrientation(LinearLayout.HORIZONTAL);
                                labelsLayout.setGravity(Gravity.START | Gravity.TOP);
                                labelsView.setLayoutParams(params);

                                TextDrawable drawable = TextDrawable.builder()
                                        .beginConfig()
                                        .useFont(Typeface.DEFAULT)
                                        .textColor(new ColorInverter().getContrastColor(color))
                                        .fontSize(30)
                                        .width(LabelWidthCalculator.calculateLabelWidth(labelName, Typeface.DEFAULT, 30, 15))
                                        .height(50)
                                        .endConfig()
                                        .buildRoundRect(labelName, color, 10);
                                labelsView.setImageDrawable(drawable);

                                labelsLayout.addView(labelsView);

                            }
                        }
                        else {
                            labelsScrollView.setVisibility(View.GONE);
                        }

                        if(singleIssue.getDue_date() != null) {

                            if (timeFormat.equals("normal") || timeFormat.equals("pretty")) {
                                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", new Locale(locale));
                                String dueDate = formatter.format(singleIssue.getDue_date());
                                issueDueDate.setText(getString(R.string.dueDate, dueDate));
                                issueDueDate.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(singleIssue.getDue_date()), getApplicationContext()));
                            } else if (timeFormat.equals("normal1")) {
                                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", new Locale(locale));
                                String dueDate = formatter.format(singleIssue.getDue_date());
                                issueDueDate.setText(getString(R.string.dueDate, dueDate));
                            }

                        }
                        else {

                            issueDueDate.setVisibility(View.GONE);
                        }

                        String edited;

                        if(!singleIssue.getUpdated_at().equals(singleIssue.getCreated_at())) {
                            edited = getString(R.string.colorfulBulletSpan) + getString(R.string.modifiedText);
                            issueModified.setVisibility(View.VISIBLE);
                            issueModified.setText(edited);
                            issueModified.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(singleIssue.getUpdated_at()), ctx));
                        }
                        else {
                            issueModified.setVisibility(View.INVISIBLE);
                        }

                        if((singleIssue.getDue_date() == null && singleIssue.getMilestone() == null) && singleIssue.getAssignees() != null) {
                            paramsDesc.setMargins(0, 35, 0, 0);
                            issueDescription.setLayoutParams(paramsDesc);
                        }
                        else if(singleIssue.getDue_date() == null && singleIssue.getMilestone() == null) {
                            paramsDesc.setMargins(0, 55, 0, 0);
                            issueDescription.setLayoutParams(paramsDesc);
                        }
                        else if(singleIssue.getAssignees() == null) {
                            paramsDesc.setMargins(0, 35, 0, 0);
                            issueDescription.setLayoutParams(paramsDesc);
                        }
                        else {
                            paramsDesc.setMargins(0, 15, 0, 0);
                            issueDescription.setLayoutParams(paramsDesc);
                        }

                        issueCreatedTime.setText(TimeHelper.formatTime(singleIssue.getCreated_at(), new Locale(locale), timeFormat, ctx));
                        issueCreatedTime.setVisibility(View.VISIBLE);

                        if(timeFormat.equals("pretty")) {
                            issueCreatedTime.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(singleIssue.getCreated_at()), ctx));
                        }

                        if(singleIssue.getMilestone() != null) {
                            issueMilestone.setText(getString(R.string.issueMilestone, singleIssue.getMilestone().getTitle()));
                        }
                        else {
                            issueMilestone.setVisibility(View.GONE);
                        }

                        if (!singleIssue.getUser().getFull_name().equals("")) {
                            assigneeAvatar.setOnClickListener(new ClickListener(getApplicationContext().getResources().getString(R.string.issueCreator) + singleIssue.getUser().getFull_name(), getApplicationContext()));
                        } else {
                            assigneeAvatar.setOnClickListener(new ClickListener(getApplicationContext().getResources().getString(R.string.issueCreator) + singleIssue.getUser().getLogin(), getApplicationContext()));
                        }

                    }

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

    }

    private void initCloseListener() {
        View.OnClickListener onClickListener = view -> finish();
    }

}
