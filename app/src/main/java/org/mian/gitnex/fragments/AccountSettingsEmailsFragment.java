package org.mian.gitnex.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.activities.AccountSettingsEmailActivity;
import org.mian.gitnex.adapters.AccountSettingsEmailsAdapter;
import org.mian.gitnex.databinding.FragmentAccountSettingsEmailsBinding;
import org.mian.gitnex.viewmodels.AccountSettingsEmailsViewModel;

/**
 * @author M M Arif
 */
public class AccountSettingsEmailsFragment extends Fragment {

	public static boolean refreshEmails = false;
	private AccountSettingsEmailsViewModel accountSettingsEmailsViewModel;
	private AccountSettingsEmailsAdapter adapter;

	public AccountSettingsEmailsFragment() {}

	private FragmentAccountSettingsEmailsBinding fragmentAccountSettingsEmailsBinding;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentAccountSettingsEmailsBinding =
				FragmentAccountSettingsEmailsBinding.inflate(inflater, container, false);
		accountSettingsEmailsViewModel =
				new ViewModelProvider(this).get(AccountSettingsEmailsViewModel.class);

		final SwipeRefreshLayout swipeRefresh = fragmentAccountSettingsEmailsBinding.pullToRefresh;

		fragmentAccountSettingsEmailsBinding.recyclerView.setHasFixedSize(true);
		fragmentAccountSettingsEmailsBinding.recyclerView.setLayoutManager(
				new LinearLayoutManager(getContext()));

		swipeRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											swipeRefresh.setRefreshing(false);
											accountSettingsEmailsViewModel.loadEmailsList(
													getContext());
										},
										200));

		fetchDataAsync();

		fragmentAccountSettingsEmailsBinding.addNewEmailAddress.setOnClickListener(
				v1 -> startActivity(new Intent(getContext(), AccountSettingsEmailActivity.class)));

		return fragmentAccountSettingsEmailsBinding.getRoot();
	}

	@SuppressLint("NotifyDataSetChanged")
	private void fetchDataAsync() {

		accountSettingsEmailsViewModel
				.getEmailsList(getContext())
				.observe(
						getViewLifecycleOwner(),
						emailsListMain -> {
							adapter =
									new AccountSettingsEmailsAdapter(getContext(), emailsListMain);
							if (adapter.getItemCount() > 0) {
								fragmentAccountSettingsEmailsBinding.recyclerView.setAdapter(
										adapter);
								fragmentAccountSettingsEmailsBinding.noDataEmails.setVisibility(
										View.GONE);
							} else {
								adapter.notifyDataSetChanged();
								fragmentAccountSettingsEmailsBinding.recyclerView.setAdapter(
										adapter);
								fragmentAccountSettingsEmailsBinding.noDataEmails.setVisibility(
										View.VISIBLE);
							}
							fragmentAccountSettingsEmailsBinding.progressBar.setVisibility(
									View.GONE);
						});
	}

	@Override
	public void onResume() {
		super.onResume();

		if (refreshEmails) {
			accountSettingsEmailsViewModel.loadEmailsList(getContext());
			refreshEmails = false;
		}
	}
}
