package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.AdminGetUsersActivity;
import org.mian.gitnex.helpers.TinyDB;

/**
 * Author M M Arif
 */

public class AdministrationFragment extends Fragment {

	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_administration, container, false);

		TinyDB tinyDb = new TinyDB(getContext());

		TextView adminUsers = v.findViewById(R.id.adminUsers);

		adminUsers.setOnClickListener(v1 -> startActivity(new Intent(getContext(), AdminGetUsersActivity.class)));

		return v;
	}

}
