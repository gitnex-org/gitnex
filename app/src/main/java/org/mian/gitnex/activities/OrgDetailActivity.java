package org.mian.gitnex.activities;

import com.google.android.material.tabs.TabLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
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
import org.mian.gitnex.fragments.OrgBottomSheetFragment;
import org.mian.gitnex.fragments.OrganizationInfoFragment;
import org.mian.gitnex.fragments.RepositoriesByOrgFragment;
import org.mian.gitnex.fragments.TeamsByOrgFragment;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class OrgDetailActivity extends BaseActivity implements OrgBottomSheetFragment.BottomSheetListener {

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_org_detail;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        String orgName = tinyDb.getString("orgName");

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(orgName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        OrgDetailActivity.SectionsPagerAdapter mSectionsPagerAdapter = new OrgDetailActivity.SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        Typeface myTypeface;
        if(tinyDb.getInt("customFontId") == 0) {

            myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getApplicationContext()).getAssets(), "fonts/roboto.ttf");

        }
        else if (tinyDb.getInt("customFontId") == 1) {

            myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getApplicationContext()).getAssets(), "fonts/manroperegular.ttf");

        }
        else if (tinyDb.getInt("customFontId") == 2) {

            myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getApplicationContext()).getAssets(), "fonts/sourcecodeproregular.ttf");

        }
        else {

            myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getApplicationContext()).getAssets(), "fonts/roboto.ttf");

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
                OrgBottomSheetFragment bottomSheet = new OrgBottomSheetFragment();
                bottomSheet.show(getSupportFragmentManager(), "orgBottomSheet");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onButtonClicked(String text) {

        switch (text) {
            case "team":
                startActivity(new Intent(OrgDetailActivity.this, CreateTeamByOrgActivity.class));
                break;
        }
        //Log.i("clicked", text);

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            TinyDB tinyDb = new TinyDB(getApplicationContext());
            String orgName;
            if(getIntent().getStringExtra("orgName") != null || !getIntent().getStringExtra("orgName").equals("")) {
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
