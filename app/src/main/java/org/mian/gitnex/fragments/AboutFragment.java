package org.mian.gitnex.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreditsActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.activities.SponsorsActivity;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class AboutFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_about, container, false);

        TinyDB tinyDb = new TinyDB(getContext());

        final TextView appVerBuild;
        final TextView donationLink;
        final TextView donationLinkPatreon;
        final TextView translateLink;
        final TextView creditsButton;
        final TextView sponsorsButton;
        final TextView appWebsite;
        final TextView appRepo;

        appVerBuild = v.findViewById(R.id.appVerBuild);
        TextView viewTextGiteaVersion = v.findViewById(R.id.giteaVersion);
        creditsButton = v.findViewById(R.id.creditsButton);
        donationLink = v.findViewById(R.id.donationLink);
        donationLinkPatreon = v.findViewById(R.id.donationLinkPatreon);
        translateLink = v.findViewById(R.id.translateLink);
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

        translateLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getResources().getString(R.string.crowdInLink)));
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

        String commit = getResources().getString(R.string.commitPage) + tinyDb.getString("giteaVersion");
        viewTextGiteaVersion.setText(commit);

        return v;
    }

}
