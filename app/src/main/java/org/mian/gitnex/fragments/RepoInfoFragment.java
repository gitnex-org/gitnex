package org.mian.gitnex.fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import ru.noties.markwon.AbstractMarkwonPlugin;
import ru.noties.markwon.Markwon;
import ru.noties.markwon.core.CorePlugin;
import ru.noties.markwon.core.MarkwonTheme;
import ru.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import ru.noties.markwon.ext.tables.TablePlugin;
import ru.noties.markwon.ext.tables.TableTheme;
import ru.noties.markwon.ext.tasklist.TaskListPlugin;
import ru.noties.markwon.html.HtmlPlugin;
import ru.noties.markwon.image.ImagesPlugin;
import ru.noties.markwon.image.gif.GifPlugin;
import ru.noties.markwon.image.okhttp.OkHttpImagesPlugin;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class RepoInfoFragment extends Fragment {

    private Context ctx = getContext();
    private ProgressBar mProgressBar;
    private LinearLayout pageContent;
    private static String repoNameF = "param2";
    private static String repoOwnerF = "param1";

    private String repoName;
    private String repoOwner;
    private TextView repoNameInfo;
    private TextView repoOwnerInfo;
    private TextView repoDescriptionInfo;
    private TextView repoWebsiteInfo;
    private TextView repoSizeInfo;
    private TextView repoDefaultBranchInfo;
    private TextView repoSshUrlInfo;
    private TextView repoCloneUrlInfo;
    private TextView repoRepoUrlInfo;
    private TextView repoForksCountInfo;
    private TextView repoCreatedAtInfo;
    private TextView repoFileContents;
    private LinearLayout repoMetaFrame;
    private ImageView repoMetaDataExpandCollapse;
    private ImageView repoFilenameExpandCollapse;
    private LinearLayout fileContentsFrameHeader;
    private LinearLayout fileContentsFrame;

    private OnFragmentInteractionListener mListener;

    public RepoInfoFragment() {

    }

    public static RepoInfoFragment newInstance(String param1, String param2) {
        RepoInfoFragment fragment = new RepoInfoFragment();
        Bundle args = new Bundle();
        args.putString(repoOwnerF, param1);
        args.putString(repoNameF, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            repoName = getArguments().getString(repoNameF);
            repoOwner = getArguments().getString(repoOwnerF);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_repo_info, container, false);

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        final String locale = tinyDb.getString("locale");
        final String timeFormat = tinyDb.getString("dateFormat");

        pageContent = v.findViewById(R.id.repoInfoLayout);
        pageContent.setVisibility(View.GONE);

        mProgressBar = v.findViewById(R.id.progress_bar);
        repoNameInfo = v.findViewById(R.id.repoNameInfo);
        repoOwnerInfo = v.findViewById(R.id.repoOwnerInfo);
        repoDescriptionInfo = v.findViewById(R.id.repoDescriptionInfo);
        repoWebsiteInfo = v.findViewById(R.id.repoWebsiteInfo);
        repoSizeInfo = v.findViewById(R.id.repoSizeInfo);
        repoDefaultBranchInfo = v.findViewById(R.id.repoDefaultBranchInfo);
        repoSshUrlInfo = v.findViewById(R.id.repoSshUrlInfo);
        repoCloneUrlInfo = v.findViewById(R.id.repoCloneUrlInfo);
        repoRepoUrlInfo = v.findViewById(R.id.repoRepoUrlInfo);
        repoForksCountInfo = v.findViewById(R.id.repoForksCountInfo);
        repoCreatedAtInfo = v.findViewById(R.id.repoCreatedAtInfo);
        repoFileContents = v.findViewById(R.id.repoFileContents);
        TextView repoFilename = v.findViewById(R.id.repoFilename);
        repoMetaFrame = v.findViewById(R.id.repoMetaFrame);
        TextView repoMetaData = v.findViewById(R.id.repoMetaData);
        repoMetaDataExpandCollapse = v.findViewById(R.id.repoMetaDataExpandCollapse);
        repoFilenameExpandCollapse = v.findViewById(R.id.repoFilenameExpandCollapse);
        fileContentsFrameHeader = v.findViewById(R.id.fileContentsFrameHeader);
        fileContentsFrame = v.findViewById(R.id.fileContentsFrame);

        repoMetaFrame.setVisibility(View.GONE);

        getRepoInfo(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, locale, timeFormat);
        getFileContents(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, getResources().getString(R.string.defaultFilename));

        repoFilename.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                collapseExpandView();
            }
        });

        repoMetaData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                collapseExpandViewMeta();
            }
        });

        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void collapseExpandView() {

        if (repoFileContents.getVisibility() == View.GONE) {
            repoFilenameExpandCollapse.setImageResource(R.drawable.ic_arrow_up);
            repoFileContents.setVisibility(View.VISIBLE);
            //Animation slide_down = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
            //fileContentsFrame.startAnimation(slide_down);
        }
        else {
            repoFilenameExpandCollapse.setImageResource(R.drawable.ic_arrow_down);
            repoFileContents.setVisibility(View.GONE);
            //Animation slide_up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
            //fileContentsFrame.startAnimation(slide_up);
        }

    }

    private void collapseExpandViewMeta() {
        if (repoMetaFrame.getVisibility() == View.GONE) {
            repoMetaDataExpandCollapse.setImageResource(R.drawable.ic_arrow_up);
            repoMetaFrame.setVisibility(View.VISIBLE);
            //Animation slide_down = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
            //repoMetaFrame.startAnimation(slide_down);
        }
        else {
            repoMetaDataExpandCollapse.setImageResource(R.drawable.ic_arrow_down);
            repoMetaFrame.setVisibility(View.GONE);
            //Animation slide_up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
            //repoMetaFrame.startAnimation(slide_up);
        }

    }

    private void getRepoInfo(String instanceUrl, String token, final String owner, String repo, final String locale, final String timeFormat) {

        Call<UserRepositories> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getUserRepository(token, owner, repo);

        call.enqueue(new Callback<UserRepositories>() {

            @Override
            public void onResponse(@NonNull Call<UserRepositories> call, @NonNull retrofit2.Response<UserRepositories> response) {

                UserRepositories repoInfo = response.body();

                if (response.isSuccessful()) {

                    if (response.code() == 200) {

                        assert repoInfo != null;
                        repoNameInfo.setText(repoInfo.getName());
                        repoOwnerInfo.setText(owner);
                        repoDescriptionInfo.setText(repoInfo.getDescription());
                        repoWebsiteInfo.setText(repoInfo.getWebsite());
                        repoSizeInfo.setText(AppUtil.formatFileSize(repoInfo.getSize()));
                        repoDefaultBranchInfo.setText(repoInfo.getDefault_branch());
                        repoSshUrlInfo.setText(repoInfo.getSsh_url());
                        repoCloneUrlInfo.setText(repoInfo.getClone_url());
                        repoRepoUrlInfo.setText(repoInfo.getHtml_url());
                        repoForksCountInfo.setText(repoInfo.getForks_count());

                        switch (timeFormat) {
                            case "pretty": {
                                PrettyTime prettyTime = new PrettyTime(new Locale(locale));
                                String createdTime = prettyTime.format(repoInfo.getCreated_at());
                                repoCreatedAtInfo.setText(createdTime);
                                repoCreatedAtInfo.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(repoInfo.getCreated_at()), getContext()));
                                break;
                            }
                            case "normal": {
                                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
                                String createdTime = formatter.format(repoInfo.getCreated_at());
                                repoCreatedAtInfo.setText(createdTime);
                                break;
                            }
                            case "normal1": {
                                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
                                String createdTime = formatter.format(repoInfo.getCreated_at());
                                repoCreatedAtInfo.setText(createdTime);
                                break;
                            }
                        }

                        mProgressBar.setVisibility(View.GONE);
                        pageContent.setVisibility(View.VISIBLE);

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

    private void getFileContents(String instanceUrl, String token, final String owner, String repo, final String filename) {

        final TinyDB tinyDb = new TinyDB(getContext());

        Call<String> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getFileContents(token, owner, repo, filename);

        call.enqueue(new Callback<String>() {

            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {

                if (response.code() == 200) {

                    final Markwon markwon = Markwon.builder(Objects.requireNonNull(getContext()))
                            .usePlugin(CorePlugin.create())
                            .usePlugin(OkHttpImagesPlugin.create(new OkHttpClient()))
                            .usePlugin(ImagesPlugin.createWithAssets(getContext()))
                            .usePlugin(new AbstractMarkwonPlugin() {
                                @Override
                                public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                                    builder
                                            .codeTextColor(tinyDb.getInt("codeBlockColor"))
                                            .codeBackgroundColor(tinyDb.getInt("codeBlockBackground"))
                                            .linkColor(getResources().getColor(R.color.lightBlue));
                                }
                            })
                            .usePlugin(TablePlugin.create(getContext()))
                            .usePlugin(TaskListPlugin.create(getContext()))
                            .usePlugin(HtmlPlugin.create())
                            .usePlugin(GifPlugin.create())
                            .usePlugin(StrikethroughPlugin.create())
                            .build();

                    CharSequence bodyWithMD = null;
                    if (response.body() != null) {
                        bodyWithMD = markwon.toMarkdown(response.body());
                    }
                    repoFileContents.setText(bodyWithMD);

                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else if(response.code() == 403) {

                    Toasty.info(ctx, ctx.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    fileContentsFrameHeader.setVisibility(View.GONE);
                    fileContentsFrame.setVisibility(View.GONE);

                }
                else {

                    Toasty.info(getContext(), getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

}
