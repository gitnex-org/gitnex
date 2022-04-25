package org.mian.gitnex.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.jetbrains.annotations.NotNull;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.BottomSheetOrganizationFragment;
import org.mian.gitnex.fragments.MembersByOrgFragment;
import org.mian.gitnex.fragments.OrganizationInfoFragment;
import org.mian.gitnex.fragments.OrganizationLabelsFragment;
import org.mian.gitnex.fragments.RepositoriesByOrgFragment;
import org.mian.gitnex.fragments.TeamsByOrgFragment;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.structs.BottomSheetListener;
import java.util.Objects;
import io.mikael.urlbuilder.UrlBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class OrganizationDetailActivity extends BaseActivity implements BottomSheetListener {

	public OrganizationPermissions permissions;
	private String orgName;
	private boolean isMember = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    setContentView(R.layout.activity_org_detail);

	    orgName = getIntent().getStringExtra("orgName");

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(orgName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		checkIsMember();

	    if(getAccount().requiresVersion("1.16.0")) {
		    RetrofitClient.getApiInterface(this)
			    .orgGetUserPermissions(getAccount().getAccount().getUserName(), orgName).enqueue(new Callback<>() {

				    @Override
				    public void onResponse(@NonNull Call<OrganizationPermissions> call, @NonNull Response<OrganizationPermissions> response) {

					    if(response.isSuccessful()) {
						    permissions = response.body();
					    }
					    else {
						    permissions = null;
					    }
				    }

				    @Override
				    public void onFailure(@NonNull Call<OrganizationPermissions> call, @NonNull Throwable t) {

					    permissions = null;
				    }
			    });
	    } else {
	    	permissions = null;
	    }
    }

	public void checkIsMember() {
		RetrofitClient.getApiInterface(this).orgIsMember(orgName, getAccount().getAccount().getUserName()).enqueue(new Callback<>() {

			@Override
			public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
				isMember = response.code() != 404;
				init();
			}

			@Override
			public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
				isMember = false;
				init();
			}
		});
	}

	public void init() {
		OrganizationDetailActivity.SectionsPagerAdapter mSectionsPagerAdapter = new OrganizationDetailActivity.SectionsPagerAdapter(getSupportFragmentManager());

		ViewPager mViewPager = findViewById(R.id.container);
		mViewPager.setVisibility(View.VISIBLE);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setVisibility(View.VISIBLE);

		if(!isMember) {
			tabLayout.removeTabAt(3);
		}

		Typeface myTypeface = AppUtil.getTypeface(this);
		TextView toolbarTitle = findViewById(R.id.toolbar_title);

		toolbarTitle.setTypeface(myTypeface);
		toolbarTitle.setText(orgName);

		ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
		int tabsCount = vg.getChildCount();

		for (int j = 0; j < tabsCount; j++) {

			ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
			int tabChildCount = vgTab.getChildCount();

			for (int i = 0; i < tabChildCount; i++) {

				View tabViewChild = vgTab.getChildAt(i);

				if (tabViewChild instanceof TextView) {

					((TextView) tabViewChild).setTypeface(myTypeface);
				}
			}
		}

		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
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

	        BottomSheetOrganizationFragment bottomSheet = new BottomSheetOrganizationFragment(permissions);
	        bottomSheet.show(getSupportFragmentManager(), "orgBottomSheet");
	        return true;
        }
        else {

	        return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onButtonClicked(String text) {

	    String url = UrlBuilder.fromString(getAccount().getAccount().getInstanceUrl())
		    .withPath("/")
		    .toString();
	    url = url + getIntent().getStringExtra("orgName");

        switch (text) {
            case "repository":
            	Intent intentRepo = new Intent(this, CreateRepoActivity.class);
                intentRepo.putExtra("organizationAction", true);
                intentRepo.putExtra("orgName", getIntent().getStringExtra("orgName"));
                intentRepo.putExtras(getIntent().getExtras());
                startActivity(intentRepo);
                break;
	        case "label":

		        Intent intent = new Intent(ctx, CreateLabelActivity.class);
		        intent.putExtra("orgName", getIntent().getStringExtra("orgName"));
		        intent.putExtra("type", "org");
		        ctx.startActivity(intent);
		        break;
            case "team":
				Intent intentTeam = new Intent(OrganizationDetailActivity.this, CreateTeamByOrgActivity.class);
				intentTeam.putExtras(getIntent().getExtras());
                startActivity(intentTeam);
                break;
	        case "copyOrgUrl":
		        AppUtil.copyToClipboard(this, url, ctx.getString(R.string.copyIssueUrlToastMsg));
		        break;
	        case "share":
		        AppUtil.sharingIntent(this, url);
		        break;
	        case "open":
		        AppUtil.openUrlInBrowser(this, url);
		        break;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            String orgName = getIntent().getStringExtra("orgName");

            Fragment fragment = null;
            switch (position) {

                case 0: // info

                    return OrganizationInfoFragment.newInstance(orgName);
                case 1: // repos

	                return RepositoriesByOrgFragment.newInstance(orgName);
	            case 2: // labels

                    return OrganizationLabelsFragment.newInstance(orgName);
                case 3: // teams / members

	                if(isMember) {
		                return TeamsByOrgFragment.newInstance(orgName, permissions);
	                } else {
		                return MembersByOrgFragment.newInstance(orgName);
	                }
                case 4: // members

	                if(isMember) {
		                return MembersByOrgFragment.newInstance(orgName);
	                }
            }
            return fragment;
        }

        @Override
        public int getCount() {
			if(isMember) {
				return 5;
			} else {
				return 4;
			}
        }
    }
}
