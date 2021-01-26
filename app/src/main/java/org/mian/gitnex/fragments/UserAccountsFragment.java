package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.AddNewAccountActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.UserAccountsAdapter;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.FragmentUserAccountsBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class UserAccountsFragment extends Fragment {

	private Context ctx;
	private UserAccountsAdapter adapter;
	private RecyclerView mRecyclerView;
	private UserAccountsApi userAccountsApi;
	private List<UserAccount> userAccountsList;
	private ExtendedFloatingActionButton addNewAccount;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		FragmentUserAccountsBinding fragmentUserAccountsBinding = FragmentUserAccountsBinding.inflate(inflater, container, false);
		ctx = getContext();
		setHasOptionsMenu(true);

		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.pageTitleUserAccounts));

		userAccountsList = new ArrayList<>();
		userAccountsApi = new UserAccountsApi(ctx);

		mRecyclerView = fragmentUserAccountsBinding.recyclerView;
		final SwipeRefreshLayout swipeRefresh = fragmentUserAccountsBinding.pullToRefresh;

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
			DividerItemDecoration.VERTICAL);
		mRecyclerView.addItemDecoration(dividerItemDecoration);

		adapter = new UserAccountsAdapter(getContext(), userAccountsList);

		swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			userAccountsList.clear();
			swipeRefresh.setRefreshing(false);
			fetchDataAsync();

		}, 250));

		addNewAccount = fragmentUserAccountsBinding.addNewAccount;
		addNewAccount.setOnClickListener(view -> {

			Intent intent = new Intent(view.getContext(), AddNewAccountActivity.class);
			startActivity(intent);
		});

		fetchDataAsync();

		return fragmentUserAccountsBinding.getRoot();

	}

	private void fetchDataAsync() {

		userAccountsApi.getAllAccounts().observe(getViewLifecycleOwner(), userAccounts -> {

			assert userAccounts != null;
			if(userAccounts.size() > 0) {

				userAccountsList.clear();
				userAccountsList.addAll(userAccounts);
				adapter.notifyDataSetChanged();
				mRecyclerView.setAdapter(adapter);

			}

		});

	}

	@Override
	public void onResume() {
		super.onResume();
		userAccountsList.clear();
		fetchDataAsync();
	}

}
