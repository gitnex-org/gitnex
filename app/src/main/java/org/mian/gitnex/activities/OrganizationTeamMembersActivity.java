package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.TeamMembersByOrgAdapter;
import org.mian.gitnex.fragments.BottomSheetOrganizationFragment;
import org.mian.gitnex.fragments.BottomSheetOrganizationTeamsFragment;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.viewmodels.TeamMembersByOrgViewModel;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class OrganizationTeamMembersActivity extends BaseActivity implements BottomSheetOrganizationTeamsFragment.BottomSheetListener {

    private TextView noDataMembers;
    private View.OnClickListener onClickListener;
    private TeamMembersByOrgAdapter adapter;
    private GridView mGridView;
	private ProgressBar progressBar;

    final Context ctx = this;
    private Context appCtx;

    private String teamId;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_org_team_members;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appCtx = getApplicationContext();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TinyDB tinyDb = new TinyDB(appCtx);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        ImageView closeActivity = findViewById(R.id.close);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        noDataMembers = findViewById(R.id.noDataMembers);
        mGridView = findViewById(R.id.gridView);
	    progressBar = findViewById(R.id.progressBar);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        if(getIntent().getStringExtra("teamTitle") != null && !Objects.requireNonNull(getIntent().getStringExtra("teamTitle")).equals("")) {

        	toolbarTitle.setText(getIntent().getStringExtra("teamTitle"));
        }
        else {

        	toolbarTitle.setText(R.string.orgTeamMembers);
        }

        if(getIntent().getStringExtra("teamId") != null && !Objects.requireNonNull(getIntent().getStringExtra("teamId")).equals("")){

        	teamId = getIntent().getStringExtra("teamId");
        }
        else {

        	teamId = "0";
        }

        assert teamId != null;
        fetchDataAsync(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), Integer.parseInt(teamId));
    }

    @Override
    public void onResume() {

        super.onResume();
        TinyDB tinyDb = new TinyDB(appCtx);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        if(tinyDb.getBoolean("teamActionFlag")) {

            fetchDataAsync(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), Integer.parseInt(teamId));
            tinyDb.putBoolean("teamActionFlag", false);
        }
    }

    private void fetchDataAsync(String instanceUrl, String instanceToken, int teamId) {

        TeamMembersByOrgViewModel teamMembersModel = new ViewModelProvider(this).get(TeamMembersByOrgViewModel.class);

        teamMembersModel.getMembersByOrgList(instanceUrl, instanceToken, teamId, ctx).observe(this, teamMembersListMain -> {

            adapter = new TeamMembersByOrgAdapter(ctx, teamMembersListMain);

            if(adapter.getCount() > 0) {

                mGridView.setAdapter(adapter);
                noDataMembers.setVisibility(View.GONE);
            }
            else {

                adapter.notifyDataSetChanged();
                mGridView.setAdapter(adapter);
                noDataMembers.setVisibility(View.VISIBLE);
            }

	        progressBar.setVisibility(View.GONE);
        });
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

	    if(id == android.R.id.home) {

		    finish();
		    return true;
	    }
	    else if(id == R.id.genericMenu) {

		    BottomSheetOrganizationTeamsFragment bottomSheet = new BottomSheetOrganizationTeamsFragment();
		    bottomSheet.show(getSupportFragmentManager(), "orgTeamsBottomSheet");
		    return true;
	    }
	    else {

		    return super.onOptionsItemSelected(item);
	    }
    }

    @Override
    public void onButtonClicked(String text) {

        if("newMember".equals(text)) {

            Intent intent = new Intent(OrganizationTeamMembersActivity.this, AddNewTeamMemberActivity.class);
            intent.putExtra("teamId", teamId);
            startActivity(intent);
        }
    }

    private void initCloseListener() {
        onClickListener = view -> finish();
    }
}
