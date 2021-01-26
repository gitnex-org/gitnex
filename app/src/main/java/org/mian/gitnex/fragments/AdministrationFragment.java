package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.mian.gitnex.activities.AdminGetUsersActivity;
import org.mian.gitnex.databinding.FragmentAdministrationBinding;

/**
 * Author M M Arif
 */

public class AdministrationFragment extends Fragment {

	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		FragmentAdministrationBinding fragmentAdministrationBinding = FragmentAdministrationBinding.inflate(inflater, container, false);

		fragmentAdministrationBinding.adminUsers.setOnClickListener(v1 -> startActivity(new Intent(getContext(), AdminGetUsersActivity.class)));

		return fragmentAdministrationBinding.getRoot();

	}

}
