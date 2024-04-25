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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateEmailOption;
import org.gitnex.tea4j.v2.models.Email;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AccountSettingsEmailsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomAccountSettingsAddNewEmailBinding;
import org.mian.gitnex.databinding.FragmentAccountSettingsEmailsBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.AccountSettingsEmailsViewModel;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class AccountSettingsEmailsFragment extends Fragment {

	public static boolean refreshEmails = false;
	private AccountSettingsEmailsViewModel accountSettingsEmailsViewModel;
	private CustomAccountSettingsAddNewEmailBinding customAccountSettingsAddNewEmailBinding;
	private AccountSettingsEmailsAdapter adapter;
	private Context context;
	private MaterialAlertDialogBuilder materialAlertDialogBuilder;
	private AlertDialog dialogSaveEmail;

	public AccountSettingsEmailsFragment() {}

	private FragmentAccountSettingsEmailsBinding fragmentAccountSettingsEmailsBinding;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentAccountSettingsEmailsBinding =
				FragmentAccountSettingsEmailsBinding.inflate(inflater, container, false);
		accountSettingsEmailsViewModel =
				new ViewModelProvider(this).get(AccountSettingsEmailsViewModel.class);

		context = getContext();

		assert context != null;
		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

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
				editProperties -> showAddEmailDialog());

		return fragmentAccountSettingsEmailsBinding.getRoot();
	}

	private void showAddEmailDialog() {

		customAccountSettingsAddNewEmailBinding =
				CustomAccountSettingsAddNewEmailBinding.inflate(LayoutInflater.from(context));

		View view = customAccountSettingsAddNewEmailBinding.getRoot();
		materialAlertDialogBuilder.setView(view);

		customAccountSettingsAddNewEmailBinding.save.setOnClickListener(
				saveKey -> {
					if (Objects.requireNonNull(
									customAccountSettingsAddNewEmailBinding.userEmail.getText())
							.toString()
							.isEmpty()) {
						Toasty.error(context, getString(R.string.emailErrorEmpty));
					} else {
						addNewEmail(
								String.valueOf(
										customAccountSettingsAddNewEmailBinding.userEmail
												.getText()));
					}
				});

		dialogSaveEmail = materialAlertDialogBuilder.show();
	}

	private void addNewEmail(String email) {

		List<String> newEmailList = new ArrayList<>(Arrays.asList(email.split(",")));

		CreateEmailOption addEmailFunc = new CreateEmailOption();
		addEmailFunc.setEmails(newEmailList);

		Call<List<Email>> call = RetrofitClient.getApiInterface(context).userAddEmail(addEmailFunc);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Email>> call,
							@NonNull retrofit2.Response<List<Email>> response) {

						if (response.code() == 201) {

							dialogSaveEmail.dismiss();
							accountSettingsEmailsViewModel.loadEmailsList(context);
							Toasty.success(context, getString(R.string.emailAddedText));
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(context);
						} else if (response.code() == 403) {

							Toasty.error(context, getString(R.string.authorizeError));
						} else if (response.code() == 422) {

							Toasty.error(context, getString(R.string.emailErrorInUse));
						} else {

							Toasty.error(context, getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Email>> call, @NonNull Throwable t) {}
				});
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
