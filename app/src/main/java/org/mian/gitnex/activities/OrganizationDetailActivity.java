package org.mian.gitnex.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.BottomSheetOrganizationFragment;
import org.mian.gitnex.fragments.MembersByOrgFragment;
import org.mian.gitnex.fragments.OrganizationInfoFragment;
import org.mian.gitnex.fragments.OrganizationLabelsFragment;
import org.mian.gitnex.fragments.RepositoriesByOrgFragment;
import org.mian.gitnex.fragments.TeamsByOrgFragment;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.structs.BottomSheetListener;
import java.util.Objects;
import io.mikael.urlbuilder.UrlBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class OrganizationDetailActivity extends BaseActivity implements BottomSheetListener {

	public OrganizationPermissions permissions;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    setContentView(R.layout.activity_org_detail);

	    String orgName = getIntent().getStringExtra("orgName");

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(orgName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        OrganizationDetailActivity.SectionsPagerAdapter mSectionsPagerAdapter = new OrganizationDetailActivity.SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        Typeface myTypeface;

        switch(tinyDB.getInt("customFontId", -1)) {

            case 0:

                myTypeface = Typeface.createFromAsset(ctx.getAssets(), "fonts/roboto.ttf");
                break;
            case 2:

                myTypeface = Typeface.createFromAsset(ctx.getAssets(), "fonts/sourcecodeproregular.ttf");
                break;
            default:

                myTypeface = Typeface.createFromAsset(ctx.getAssets(), "fonts/manroperegular.ttf");
                break;
        }

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

	    if(getAccount().requiresVersion("1.16.0")) {
		    RetrofitClient.getApiInterface(this)
			    .orgGetUserPermissions(getAccount().getAccount().getUserName(), orgName).enqueue(new Callback<OrganizationPermissions>() {

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

		        String url = UrlBuilder.fromString(getAccount().getAccount().getInstanceUrl())
			        .withPath("/")
			        .toString();
		        ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
		        ClipData clip = ClipData.newPlainText("orgUrl", url + getIntent().getStringExtra("orgName"));
		        assert clipboard != null;
		        clipboard.setPrimaryClip(clip);
		        Toasty.info(ctx, ctx.getString(R.string.copyIssueUrlToastMsg));
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
                case 3: // teams

                    return TeamsByOrgFragment.newInstance(orgName, permissions);
                case 4: // members

                    return MembersByOrgFragment.newInstance(orgName);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}
