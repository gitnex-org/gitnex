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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateKeyOption;
import org.gitnex.tea4j.v2.models.PublicKey;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.SSHKeysAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomAccountSettingsAddSshKeyBinding;
import org.mian.gitnex.databinding.FragmentAccountSettingsSshKeysBinding;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.AccountSettingsSSHKeysViewModel;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class SSHKeysFragment extends Fragment {

	private FragmentAccountSettingsSshKeysBinding viewBinding;
	private Context context;
	private SSHKeysAdapter adapter;
	private AccountSettingsSSHKeysViewModel accountSettingsSSHKeysViewModel;
	private CustomAccountSettingsAddSshKeyBinding newSSHKeyBinding;
	private MaterialAlertDialogBuilder materialAlertDialogBuilder;
	private AlertDialog dialogSaveKey;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentAccountSettingsSshKeysBinding.inflate(inflater, container, false);
		context = getContext();

		assert context != null;
		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

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

		viewBinding.addNewSSHKey.setOnClickListener(editProperties -> showNewSSHKeyDialog());

		return viewBinding.getRoot();
	}

	private void showNewSSHKeyDialog() {

		newSSHKeyBinding =
				CustomAccountSettingsAddSshKeyBinding.inflate(LayoutInflater.from(context));

		View view = newSSHKeyBinding.getRoot();
		materialAlertDialogBuilder.setView(view);

		newSSHKeyBinding.keyStatus.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked) {
						newSSHKeyBinding.keyStatus.setText(
								getString(R.string.sshKeyStatusReadWrite));
					} else {
						newSSHKeyBinding.keyStatus.setText(
								getString(R.string.sshKeyStatusReadOnly));
					}
				});

		newSSHKeyBinding.save.setOnClickListener(
				saveKey -> {
					if (Objects.requireNonNull(newSSHKeyBinding.keyTitle.getText())
									.toString()
									.isEmpty()
							|| Objects.requireNonNull(newSSHKeyBinding.key.getText())
									.toString()
									.isEmpty()) {
						Toasty.error(context, getString(R.string.emptyFields));
					} else {
						saveSSHKey(
								String.valueOf(newSSHKeyBinding.keyTitle.getText()),
								String.valueOf(newSSHKeyBinding.key.getText()),
								newSSHKeyBinding.keyStatus.isChecked());
					}
				});

		dialogSaveKey = materialAlertDialogBuilder.show();
	}

	private void saveSSHKey(String title, String key, boolean keyStatus) {

		CreateKeyOption createKeyOption = new CreateKeyOption();
		createKeyOption.setTitle(title);
		createKeyOption.setKey(key);
		createKeyOption.setReadOnly(keyStatus);

		Call<PublicKey> saveNewKey =
				RetrofitClient.getApiInterface(context).userCurrentPostKey(createKeyOption);

		saveNewKey.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<PublicKey> call,
							@NonNull retrofit2.Response<PublicKey> response) {

						if (response.code() == 202 || response.code() == 201) {

							dialogSaveKey.dismiss();
							accountSettingsSSHKeysViewModel.loadKeysList(context);
							Toasty.success(context, getString(R.string.sshKeySuccess));
						} else if (response.code() == 422) {

							Toasty.error(context, getString(R.string.sshKeyError));
						} else {

							Toasty.error(context, getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<PublicKey> call, @NonNull Throwable t) {

						Toasty.error(context, getString(R.string.genericServerResponseError));
					}
				});
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
