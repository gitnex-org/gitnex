package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.databinding.FragmentOrganizationMembersBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.viewmodels.MembersByOrgViewModel;

/**
 * @author M M Arif
 */

public class OrganizationMembersFragment extends Fragment {

	private TextView noDataMembers;
	private static String orgNameF = "param2";
	private String orgName;
	private UserGridAdapter adapter;
	private GridView mGridView;
	private ProgressBar progressBar;

	public OrganizationMembersFragment() {
	}

	public static OrganizationMembersFragment newInstance(String param1) {
		OrganizationMembersFragment fragment = new OrganizationMembersFragment();
		Bundle args = new Bundle();
		args.putString(orgNameF, param1);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getArguments() != null) {
			orgName = getArguments().getString(orgNameF);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		FragmentOrganizationMembersBinding fragmentMembersByOrgBinding = FragmentOrganizationMembersBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);

		noDataMembers = fragmentMembersByOrgBinding.noDataMembers;

		progressBar = fragmentMembersByOrgBinding.progressBar;
		mGridView = fragmentMembersByOrgBinding.gridView;

		fetchDataAsync(orgName);

		return fragmentMembersByOrgBinding.getRoot();
	}

	private void fetchDataAsync(String owner) {

		MembersByOrgViewModel membersModel = new ViewModelProvider(this).get(MembersByOrgViewModel.class);

		membersModel.getMembersList(owner, getContext()).observe(getViewLifecycleOwner(), membersListMain -> {
			adapter = new UserGridAdapter(getContext(), membersListMain);
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
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		boolean connToInternet = AppUtil.hasNetworkConnection(requireContext());

		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
		//searchView.setQueryHint(getContext().getString(R.string.strFilter));

		if(!connToInternet) {
			return;
		}

		searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				if(mGridView.getAdapter() != null) {
					adapter.getFilter().filter(newText);
				}
				return false;
			}
		});

	}

}
