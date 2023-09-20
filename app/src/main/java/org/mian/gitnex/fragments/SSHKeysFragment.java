package org.mian.gitnex.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
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
import org.mian.gitnex.adapters.SSHKeysAdapter;
import org.mian.gitnex.databinding.FragmentAccountSettingsSshKeysBinding;
import org.mian.gitnex.viewmodels.AccountSettingsSSHKeysViewModel;

/**
 * @author M M Arif
 */
public class SSHKeysFragment extends Fragment {

	private FragmentAccountSettingsSshKeysBinding viewBinding;
	private Context context;
	private SSHKeysAdapter adapter;
	private AccountSettingsSSHKeysViewModel accountSettingsSSHKeysViewModel;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentAccountSettingsSshKeysBinding.inflate(inflater, container, false);
		context = getContext();

		accountSettingsSSHKeysViewModel =
				new ViewModelProvider(this).get(AccountSettingsSSHKeysViewModel.class);

		viewBinding.recyclerView.setHasFixedSize(true);
		viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));

		viewBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											viewBinding.pullToRefresh.setRefreshing(false);
											accountSettingsSSHKeysViewModel.loadKeysList(context);
										},
										200));

		fetchDataAsync();

		return viewBinding.getRoot();
	}

	@SuppressLint("NotifyDataSetChanged")
	private void fetchDataAsync() {

		accountSettingsSSHKeysViewModel
				.getKeysList(context)
				.observe(
						getViewLifecycleOwner(),
						keysListMain -> {
							adapter = new SSHKeysAdapter(keysListMain);
							if (adapter.getItemCount() > 0) {
								viewBinding.recyclerView.setAdapter(adapter);
								viewBinding.noData.setVisibility(View.GONE);
							} else {
								adapter.notifyDataSetChanged();
								viewBinding.recyclerView.setAdapter(adapter);
								viewBinding.noData.setVisibility(View.VISIBLE);
							}
							viewBinding.progressBar.setVisibility(View.GONE);
						});
	}
}
