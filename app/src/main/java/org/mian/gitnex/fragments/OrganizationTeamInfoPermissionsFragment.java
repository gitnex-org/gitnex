package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.gitnex.tea4j.v2.models.Team;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.FragmentOrganizationTeamInfoPermissionsBinding;
import java.util.Collections;

/**
 * @author opyale
 */

public class OrganizationTeamInfoPermissionsFragment extends Fragment {

	private FragmentOrganizationTeamInfoPermissionsBinding binding;
	private Team team;

	public OrganizationTeamInfoPermissionsFragment() {}

	public static OrganizationTeamInfoPermissionsFragment newInstance(Team team) {
		OrganizationTeamInfoPermissionsFragment fragment = new OrganizationTeamInfoPermissionsFragment();

		Bundle bundle = new Bundle();
		bundle.putSerializable("team", team);
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentOrganizationTeamInfoPermissionsBinding.inflate(inflater, container, false);

		team = (Team) requireArguments().getSerializable("team");

		StringBuilder permissions = new StringBuilder();

		// Future proofing in case of gitea becoming able to assign multiple permissions per team
		for(String permission : Collections.singletonList(team.getPermission().getValue())) {

			switch(permission) {
				case "none":
					permissions.append(getString(R.string.teamPermissionNone)).append("\n");
					break;
				case "read":
					permissions.append(getString(R.string.teamPermissionRead)).append("\n");
					break;
				case "write":
					permissions.append(getString(R.string.teamPermissionWrite)).append("\n");
					break;
				case "admin":
					permissions.append(getString(R.string.teamPermissionAdmin)).append("\n");
					break;
				case "owner":
					permissions.append(getString(R.string.teamPermissionOwner)).append("\n");
					break;
			}
		}

		binding.permissions.setText(permissions.toString());
		binding.progressBar.setVisibility(View.GONE);

		return binding.getRoot();
	}

}
