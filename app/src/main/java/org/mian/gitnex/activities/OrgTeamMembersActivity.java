package org.mian.gitnex.activities;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.TeamMembersByOrgAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.viewmodels.TeamMembersByOrgViewModel;
import java.util.List;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class OrgTeamMembersActivity extends BaseActivity {

    private TextView noDataMembers;
    private View.OnClickListener onClickListener;
    private TeamMembersByOrgAdapter adapter;
    private GridView mGridView;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_org_team_members;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        ImageView closeActivity = findViewById(R.id.close);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        noDataMembers = findViewById(R.id.noDataMembers);
        mGridView = findViewById(R.id.gridView);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        if(getIntent().getStringExtra("teamTitle") != null && !Objects.requireNonNull(getIntent().getStringExtra("teamTitle")).equals("")) {
            toolbarTitle.setText(getIntent().getStringExtra("teamTitle"));
        }
        else {
            toolbarTitle.setText(R.string.orgTeamMembers);
        }

        String teamId;
        if(getIntent().getStringExtra("teamId") != null && !Objects.requireNonNull(getIntent().getStringExtra("teamId")).equals("")){
            teamId = getIntent().getStringExtra("teamId");
        }
        else {
            teamId = "0";
        }

        getIntent().getStringExtra("teamId");
        //Log.i("teamId", getIntent().getStringExtra("teamId"));

        assert teamId != null;
        fetchDataAsync(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), Integer.valueOf(teamId));

    }

    private void fetchDataAsync(String instanceUrl, String instanceToken, int teamId) {

        TeamMembersByOrgViewModel teamMembersModel = new ViewModelProvider(this).get(TeamMembersByOrgViewModel.class);

        teamMembersModel.getMembersByOrgList(instanceUrl, instanceToken, teamId, getApplicationContext()).observe(this, new Observer<List<UserInfo>>() {
            @Override
            public void onChanged(@Nullable List<UserInfo> teamMembersListMain) {
                adapter = new TeamMembersByOrgAdapter(getApplicationContext(), teamMembersListMain);
                if(adapter.getCount() > 0) {
                    mGridView.setAdapter(adapter);
                    noDataMembers.setVisibility(View.GONE);
                }
                else {
                    adapter.notifyDataSetChanged();
                    mGridView.setAdapter(adapter);
                    noDataMembers.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void initCloseListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }
}
