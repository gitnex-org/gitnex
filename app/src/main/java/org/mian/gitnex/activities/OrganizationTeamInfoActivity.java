package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.gitnex.tea4j.v2.models.Team;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityOrgTeamInfoBinding;
import org.mian.gitnex.fragments.BottomSheetOrganizationTeamsFragment;
import org.mian.gitnex.fragments.OrganizationTeamInfoMembersFragment;
import org.mian.gitnex.fragments.OrganizationTeamInfoPermissionsFragment;
import org.mian.gitnex.fragments.OrganizationTeamInfoReposFragment;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * @author M M Arif
 */

public class OrganizationTeamInfoActivity extends BaseActivity implements BottomSheetListener {

	private Team team;

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    org.mian.gitnex.databinding.ActivityOrgTeamInfoBinding binding = ActivityOrgTeamInfoBinding.inflate(getLayoutInflater());

	    setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

	    team = (Team) getIntent().getSerializableExtra("team");

	    if(team.getName() != null && !team.getName().isEmpty()) {
		    binding.toolbarTitle.setText(team.getName());
	    }
	    else {
		    binding.toolbarTitle.setText(R.string.orgTeamMembers);
	    }

	    binding.close.setOnClickListener(view -> finish());
	    binding.pager.setOffscreenPageLimit(1);
	    binding.pager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
		    @NonNull
		    @Override
		    public Fragment createFragment(int position) {
			    switch(position) {
				    case 0:
					    return OrganizationTeamInfoReposFragment.newInstance(team);
				    case 1:
					    return OrganizationTeamInfoMembersFragment.newInstance(team);
				    case 2:
					    return OrganizationTeamInfoPermissionsFragment.newInstance(team);
			    }
			    return null;
		    }

		    @Override
		    public int getItemCount() {
			    return 3;
		    }
	    });

	    new TabLayoutMediator(binding.tabs, binding.pager, (tab, position) -> {
		    TextView textView = (TextView) LayoutInflater.from(ctx).inflate(R.layout.layout_tab_text, findViewById(android.R.id.content), false);

		    switch(position) {
			    case 0:
				    textView.setText(R.string.navRepos);
				    break;
			    case 1:
				    textView.setText(R.string.orgTabMembers);
				    break;
			    case 2:
				    textView.setText(R.string.teamPermissions);
					break;
		    }

		    tab.setCustomView(textView);
	    }).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	OrganizationPermissions permissions = (OrganizationPermissions) getIntent().getSerializableExtra("permissions");
    	if(permissions == null || permissions.isIsOwner()) {
		    MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.generic_nav_dotted_menu, menu);
	    }
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
			Bundle args = new Bundle();
			args.putBoolean("showRepo", !team.isIncludesAllRepositories());
			bottomSheet.setArguments(args);
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
            Intent intent = new Intent(OrganizationTeamInfoActivity.this, AddNewTeamMemberActivity.class);
            intent.putExtra("teamId", team.getId());
            startActivity(intent);
        } else if("newRepo".equals(text)) {
	        Intent intent = new Intent(OrganizationTeamInfoActivity.this, AddNewTeamRepoActivity.class);
	        intent.putExtra("teamId", team.getId());
	        intent.putExtra("teamName", team.getName());
			intent.putExtra("orgName", getIntent().getStringExtra("orgName"));
	        startActivity(intent);
        }
    }
}
