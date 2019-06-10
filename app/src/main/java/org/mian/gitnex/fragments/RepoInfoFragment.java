package org.mian.gitnex.fragments;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class RepoInfoFragment extends Fragment {

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

        getRepoInfo(instanceUrl, instanceToken, repoOwner, repoName, locale, timeFormat);
        Log.i("timeFormat", timeFormat);

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
}
