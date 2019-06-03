package org.mian.gitnex.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreditsActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.activities.SponsorsActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.GiteaVersion;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;
import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class AboutFragment extends Fragment {

    private TextView viewTextGiteaVersion;
    private ProgressBar mProgressBar;
    private LinearLayout pageContent;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_about, container, false);
        ((MainActivity) Objects.requireNonNull(getActivity())).setActionBarTitle(getResources().getString(R.string.pageTitleAbout));

        TinyDB tinyDb = new TinyDB(getContext());
        String instanceUrl = tinyDb.getString("instanceUrl");

        final TextView appVerBuild;
        final TextView donationLink;
        final TextView donationLinkPatreon;
        final TextView creditsButton;
        final TextView sponsorsButton;
        final TextView appWebsite;
        final TextView appRepo;

        pageContent = v.findViewById(R.id.aboutFrame);
        pageContent.setVisibility(View.GONE);

        mProgressBar = v.findViewById(R.id.progress_bar);

        appVerBuild = v.findViewById(R.id.appVerBuild);
        viewTextGiteaVersion = v.findViewById(R.id.giteaVersion);
        creditsButton = v.findViewById(R.id.creditsButton);
        donationLink = v.findViewById(R.id.donationLink);
        donationLinkPatreon = v.findViewById(R.id.donationLinkPatreon);
        sponsorsButton = v.findViewById(R.id.sponsorsButton);
        appWebsite = v.findViewById(R.id.appWebsite);
        appRepo = v.findViewById(R.id.appRepo);

        appVerBuild.setText(getString(R.string.appVerBuild, AppUtil.getAppVersion(Objects.requireNonNull(getContext())), AppUtil.getAppBuildNo(getContext())));

        donationLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getResources().getString(R.string.supportLink)));
                startActivity(intent);
            }
        });

        donationLinkPatreon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getResources().getString(R.string.supportLinkPatreon)));
                startActivity(intent);
            }
        });

        appWebsite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getResources().getString(R.string.appWebsiteLink)));
                startActivity(intent);
            }
        });

        appRepo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getResources().getString(R.string.appRepoLink)));
                startActivity(intent);
            }
        });

        creditsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreditsActivity.class));
            }
        });

        sponsorsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SponsorsActivity.class));
            }
        });

        boolean connToInternet = AppUtil.haveNetworkConnection(getContext());

        if(!connToInternet) {

            mProgressBar.setVisibility(View.GONE);
            pageContent.setVisibility(View.VISIBLE);

        } else {

            giteaVer(instanceUrl);

        }

        return v;
    }

    private void giteaVer(String instanceUrl) {

        TinyDB tinyDb = new TinyDB(getContext());
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        Call<GiteaVersion> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getGiteaVersion(Authorization.returnAuthentication(getContext(), loginUid, instanceToken));

        call.enqueue(new Callback<GiteaVersion>() {

            @Override
            public void onResponse(@NonNull Call<GiteaVersion> call, @NonNull Response<GiteaVersion> response) {

                if (response.isSuccessful()) {
                    if (response.code() == 200) {

                        GiteaVersion version = response.body();
                        assert version != null;
                        String commit = getResources().getString(R.string.commitPage) + version.getVersion();
                        viewTextGiteaVersion.setText(commit);
                        mProgressBar.setVisibility(View.GONE);
                        pageContent.setVisibility(View.VISIBLE);

                    }

                }
                else {

                    String commit = getResources().getString(R.string.commitPage);
                    viewTextGiteaVersion.setText(commit);
                    mProgressBar.setVisibility(View.GONE);
                    pageContent.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onFailure(@NonNull Call<GiteaVersion> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

}
