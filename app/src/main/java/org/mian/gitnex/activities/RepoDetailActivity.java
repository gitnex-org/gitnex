package org.mian.gitnex.activities;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonElement;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import retrofit2.Call;
import retrofit2.Callback;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.BranchesFragment;
import org.mian.gitnex.fragments.ClosedIssuesFragment;
import org.mian.gitnex.fragments.CollaboratorsFragment;
import org.mian.gitnex.fragments.FilesFragment;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.fragments.LabelsFragment;
import org.mian.gitnex.fragments.MilestonesFragment;
import org.mian.gitnex.fragments.ReleasesFragment;
import org.mian.gitnex.fragments.RepoBottomSheetFragment;
import org.mian.gitnex.fragments.RepoInfoFragment;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;
import android.net.Uri;

/**
 * Author M M Arif
 */

public class RepoDetailActivity extends AppCompatActivity implements RepoBottomSheetFragment.BottomSheetListener {

    private TextView textViewBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo_detail);

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        String repoName1 = parts[1];

        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String repoOwner = parts[0];
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        String appLocale = tinyDb.getString("locale");
        AppUtil.setAppLocale(getResources(), appLocale);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(repoName1);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        if(tinyDb.getBoolean("enableCounterIssueBadge")) {

            @SuppressLint("InflateParams") View tabHeader = LayoutInflater.from(this).inflate(R.layout.badge, null);
            textViewBadge = tabHeader.findViewById(R.id.counterBadge);
            if(!tinyDb.getString("issuesCounter").isEmpty()) {
                getRepoInfo(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName1);
            }
            Objects.requireNonNull(tabLayout.getTabAt(2)).setCustomView(tabHeader);

            TabLayout.Tab tabOpenIssues = tabLayout.getTabAt(2);
            ColorStateList textColor = tabLayout.getTabTextColors();
            assert tabOpenIssues != null;
            TextView openIssueTabView = Objects.requireNonNull(tabOpenIssues.getCustomView()).findViewById(R.id.counterBadgeText);
            openIssueTabView.setTextColor(textColor);

        }

        checkRepositoryStarStatus(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName1);
    }

    @Override
    public void onResume() {

        super.onResume();
        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        if(tinyDb.getBoolean("enableCounterIssueBadge")) {
            getRepoInfo(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName);
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

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.repoMenu:
                RepoBottomSheetFragment bottomSheet = new RepoBottomSheetFragment();
                bottomSheet.show(getSupportFragmentManager(), "repoBottomSheet");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onButtonClicked(String text) {

        switch (text) {
            case "label":
                startActivity(new Intent(RepoDetailActivity.this, CreateLabelActivity.class));
                break;
            case "newIssue":
                startActivity(new Intent(RepoDetailActivity.this, CreateIssueActivity.class));
                break;
            case "newMilestone":
                startActivity(new Intent(RepoDetailActivity.this, NewMilestoneActivity.class));
                break;
            case "addCollaborator":
                startActivity(new Intent(RepoDetailActivity.this, AddCollaboratorToRepositoryActivity.class));
                break;
            case "createRelease":
                startActivity(new Intent(RepoDetailActivity.this, CreateReleaseActivity.class));
                break;
            case "openWebRepo":
                TinyDB tinyDb = new TinyDB(getApplicationContext());
                String repoFullName = tinyDb.getString("repoFullName");
                String instanceUrlWithProtocol = "https://" + tinyDb.getString("instanceUrlRaw");
                if(!tinyDb.getString("instanceUrlWithProtocol").isEmpty()) {
                    instanceUrlWithProtocol = tinyDb.getString("instanceUrlWithProtocol");
                }
                Uri url = Uri.parse(instanceUrlWithProtocol + "/" + repoFullName);
                Intent i = new Intent(Intent.ACTION_VIEW, url);
                startActivity(i);
                break;
            case "newFile":
                startActivity(new Intent(RepoDetailActivity.this, NewFileActivity.class));
                break;
        }

    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            TinyDB tinyDb = new TinyDB(getApplicationContext());
            String repoFullName = tinyDb.getString("repoFullName");
            String[] parts = repoFullName.split("/");
            String repoOwner = parts[0];
            String repoName = parts[1];

            Fragment fragment = null;
            switch (position) {
                case 0: // information
                    return RepoInfoFragment.newInstance(repoOwner, repoName);
                case 1: // files
                    return FilesFragment.newInstance(repoOwner, repoName);
                case 2: // issues
                    fragment = new IssuesFragment();
                    break;
                case 3: // closed issues
                    fragment = new ClosedIssuesFragment();
                    break;
                case 4: // milestones
                    return MilestonesFragment.newInstance(repoOwner, repoName);
                case 5: // labels
                    return LabelsFragment.newInstance(repoOwner, repoName);
                case 6: // branches
                    return BranchesFragment.newInstance(repoOwner, repoName);
                case 7: // releases
                    return ReleasesFragment.newInstance(repoOwner, repoName);
                case 8: // collaborators
                    return CollaboratorsFragment.newInstance(repoOwner, repoName);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 9;
        }

    }

    private void getRepoInfo(String instanceUrl, String token, final String owner, String repo) {

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
                        textViewBadge.setText(repoInfo.getOpen_issues_count());

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

        Call<JsonElement> call;

        call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .checkRepoStarStatus(instanceToken, owner, repo);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                TinyDB tinyDb = new TinyDB(getApplicationContext());
                tinyDb.putInt("repositoryStarStatus", response.code());

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

}
