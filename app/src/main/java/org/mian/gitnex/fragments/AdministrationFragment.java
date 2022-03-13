package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.mian.gitnex.activities.AdminCronTasksActivity;
import org.mian.gitnex.activities.AdminGetUsersActivity;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.databinding.FragmentAdministrationBinding;

/**
 * Author M M Arif
 */

public class AdministrationFragment extends Fragment {

	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		Context ctx = getContext();
		FragmentAdministrationBinding fragmentAdministrationBinding = FragmentAdministrationBinding.inflate(inflater, container, false);

		fragmentAdministrationBinding.adminUsers.setOnClickListener(v1 -> startActivity(new Intent(getContext(), AdminGetUsersActivity.class)));

		// if gitea version is greater/equal(1.13.0) than user installed version (installed.higherOrEqual(compareVer))
		if(((BaseActivity) requireActivity()).getAccount().requiresVersion("1.13.0")) {

			fragmentAdministrationBinding.adminCron.setVisibility(View.VISIBLE);
		}

		fragmentAdministrationBinding.adminCron.setOnClickListener(v1 -> startActivity(new Intent(getContext(), AdminCronTasksActivity.class)));

		String action = requireActivity().getIntent().getStringExtra("giteaAdminAction");
		if(action != null) {
			if(action.equals("users")) {
				startActivity(new Intent(getContext(), AdminGetUsersActivity.class));
			}
			else if(action.equals("monitor")) {
				startActivity(new Intent(getContext(), AdminCronTasksActivity.class));
			}
		}

		return fragmentAdministrationBinding.getRoot();

	}

}
