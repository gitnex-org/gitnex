package org.mian.gitnex.fragments;

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
import org.mian.gitnex.activities.AdminUnadoptedReposActivity;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.databinding.FragmentAdministrationBinding;

/**
 * @author M M Arif
 */
public class AdministrationFragment extends Fragment {

	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		FragmentAdministrationBinding fragmentAdministrationBinding =
				FragmentAdministrationBinding.inflate(inflater, container, false);

		fragmentAdministrationBinding.systemUsersFrame.setOnClickListener(
				v1 -> startActivity(new Intent(getContext(), AdminGetUsersActivity.class)));

		// if gitea version is greater/equal(1.13.0) than user installed version
		// (installed.higherOrEqual(compareVer))
		if (((BaseActivity) requireActivity()).getAccount().requiresVersion("1.13.0")) {

			fragmentAdministrationBinding.adminCronFrame.setVisibility(View.VISIBLE);
		}

		fragmentAdministrationBinding.adminCronFrame.setOnClickListener(
				v1 -> startActivity(new Intent(getContext(), AdminCronTasksActivity.class)));
		fragmentAdministrationBinding.unadoptedReposFrame.setOnClickListener(
				v1 -> startActivity(new Intent(getContext(), AdminUnadoptedReposActivity.class)));

		String action = requireActivity().getIntent().getStringExtra("giteaAdminAction");
		if (action != null) {
			if (action.equals("users")) {
				startActivity(new Intent(getContext(), AdminGetUsersActivity.class));
			} else if (action.equals("monitor")) {
				startActivity(new Intent(getContext(), AdminCronTasksActivity.class));
			}
		}

		return fragmentAdministrationBinding.getRoot();
	}
}
