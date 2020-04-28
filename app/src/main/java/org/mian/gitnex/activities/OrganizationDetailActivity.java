package org.mian.gitnex.activities;

import com.google.android.material.tabs.TabLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
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
import org.mian.gitnex.R;
import org.mian.gitnex.fragments.MembersByOrgFragment;
import org.mian.gitnex.fragments.BottomSheetOrganizationFragment;
import org.mian.gitnex.fragments.OrganizationInfoFragment;
import org.mian.gitnex.fragments.RepositoriesByOrgFragment;
import org.mian.gitnex.fragments.TeamsByOrgFragment;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class OrganizationDetailActivity extends BaseActivity implements BottomSheetOrganizationFragment.BottomSheetListener {

    final Context ctx = this;
    private Context appCtx;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_org_detail;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appCtx = getApplicationContext();

        TinyDB tinyDb = new TinyDB(appCtx);
        String orgName = tinyDb.getString("orgName");

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(orgName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        OrganizationDetailActivity.SectionsPagerAdapter mSectionsPagerAdapter = new OrganizationDetailActivity.SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        Typeface myTypeface;

        switch(tinyDb.getInt("customFontId", -1)) {

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
                BottomSheetOrganizationFragment bottomSheet = new BottomSheetOrganizationFragment();
                bottomSheet.show(getSupportFragmentManager(), "orgBottomSheet");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onButtonClicked(String text) {

        TinyDB tinyDb = new TinyDB(appCtx);

        switch (text) {
            case "repository":
                tinyDb.putBoolean("organizationAction", true);
                startActivity(new Intent(OrganizationDetailActivity.this, CreateRepoActivity.class));
                break;
            case "team":
                startActivity(new Intent(OrganizationDetailActivity.this, CreateTeamByOrgActivity.class));
                break;
        }
        //Log.i("clicked", text);

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            TinyDB tinyDb = new TinyDB(appCtx);
            String orgName;
            if(getIntent().getStringExtra("orgName") != null || !Objects.equals(getIntent().getStringExtra("orgName"), "")) {
                orgName = getIntent().getStringExtra("orgName");
            }
            else {
                orgName = tinyDb.getString("orgName");
            }

            Fragment fragment = null;
            switch (position) {
                case 0: // info
                    return OrganizationInfoFragment.newInstance(orgName);
                case 1: // repos
                    return RepositoriesByOrgFragment.newInstance(orgName);
                case 2: // teams
                    return TeamsByOrgFragment.newInstance(orgName);
                case 3: // members
                    return MembersByOrgFragment.newInstance(orgName);
            }
            return fragment;

        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
