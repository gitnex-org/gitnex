package org.mian.gitnex.activities;

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
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.databinding.ActivityOrgTeamMembersBinding;
import org.mian.gitnex.fragments.BottomSheetOrganizationTeamsFragment;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.structs.BottomSheetListener;
import org.mian.gitnex.viewmodels.TeamMembersByOrgViewModel;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class OrganizationTeamMembersActivity extends BaseActivity implements BottomSheetListener {

    private TextView noDataMembers;
    private View.OnClickListener onClickListener;
    private UserGridAdapter adapter;
    private GridView mGridView;
	private ProgressBar progressBar;

    private String teamId;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityOrgTeamMembersBinding activityOrgTeamMembersBinding = ActivityOrgTeamMembersBinding.inflate(getLayoutInflater());
	    setContentView(activityOrgTeamMembersBinding.getRoot());

        Toolbar toolbar = activityOrgTeamMembersBinding.toolbar;
        setSupportActionBar(toolbar);

        ImageView closeActivity = activityOrgTeamMembersBinding.close;
        TextView toolbarTitle = activityOrgTeamMembersBinding.toolbarTitle;
        noDataMembers = activityOrgTeamMembersBinding.noDataMembers;
        mGridView = activityOrgTeamMembersBinding.gridView;
	    progressBar = activityOrgTeamMembersBinding.progressBar;

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
        fetchDataAsync(Authorization.get(ctx), Integer.parseInt(teamId));
    }

    @Override
    public void onResume() {

        super.onResume();
        TinyDB tinyDb = TinyDB.getInstance(appCtx);

        if(tinyDb.getBoolean("teamActionFlag")) {

            fetchDataAsync(Authorization.get(ctx), Integer.parseInt(teamId));
            tinyDb.putBoolean("teamActionFlag", false);
        }
    }

    private void fetchDataAsync(String instanceToken, int teamId) {

        TeamMembersByOrgViewModel teamMembersModel = new ViewModelProvider(this).get(TeamMembersByOrgViewModel.class);

        teamMembersModel.getMembersByOrgList(instanceToken, teamId, ctx).observe(this, teamMembersListMain -> {

            adapter = new UserGridAdapter(ctx, teamMembersListMain);

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
