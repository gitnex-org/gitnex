package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserAccountsAdapter;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_user_accounts, container, false);
		ctx = getContext();
		setHasOptionsMenu(true);

		userAccountsList = new ArrayList<>();
		userAccountsApi = new UserAccountsApi(ctx);

		mRecyclerView = v.findViewById(R.id.recyclerView);
		final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
			DividerItemDecoration.VERTICAL);
		mRecyclerView.addItemDecoration(dividerItemDecoration);

		adapter = new UserAccountsAdapter(getContext(), userAccountsList);

		swipeRefresh.setOnRefreshListener(() -> new Handler().postDelayed(() -> {

			userAccountsList.clear();
			swipeRefresh.setRefreshing(false);
			fetchDataAsync();

		}, 250));

		fetchDataAsync();

		return v;

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
