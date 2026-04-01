package org.mian.gitnex.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateUserOption;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AdminGetUsersAdapter;
import org.mian.gitnex.databinding.ActivityAdminGetUsersBinding;
import org.mian.gitnex.databinding.BottomsheetAdminAddUserBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.AdministrationViewModel;

/**
 * @author mmarif
 */
public class AdminGetUsersActivity extends BaseActivity {

	private ActivityAdminGetUsersBinding binding;
	private AdministrationViewModel viewModel;
	private AdminGetUsersAdapter adapter;
	private BottomSheetDialog bottomSheetAddUser;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityAdminGetUsersBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		viewModel = new ViewModelProvider(this).get(AdministrationViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(this);

		setupRecyclerView();
		setupListeners();
		observeViewModel();
		setupSearch();

		viewModel.resetUsersPagination();
		viewModel.fetchUsers(this, 1, resultLimit, true);
	}

	private void setupRecyclerView() {
		adapter = new AdminGetUsersAdapter(new ArrayList<>(), this);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		binding.recyclerView.addOnScrollListener(
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (binding.searchView.isShowing()) return;

						viewModel.fetchUsers(AdminGetUsersActivity.this, page, resultLimit, false);
					}
				});
	}

	private void setupListeners() {
		binding.btnBack.setOnClickListener(v -> finish());

		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					viewModel.resetUsersPagination();
					viewModel.fetchUsers(this, 1, resultLimit, true);
				});

		binding.btnSearch.setOnClickListener(v -> binding.searchView.show());

		binding.btnNavNewUser.setOnClickListener(v -> showAddUserSheet());
	}

	private void observeViewModel() {
		viewModel
				.getUsers()
				.observe(
						this,
						list -> {
							binding.pullToRefresh.setRefreshing(false);
							adapter.updateList(list);
							updateEmptyLayoutVisibility();
						});

		viewModel
				.getIsUsersLoading()
				.observe(
						this,
						loading -> {
							binding.expressiveLoader.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							if (loading) {
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							} else {
								updateEmptyLayoutVisibility();
							}
						});

		viewModel
				.getCreateUserStatus()
				.observe(
						this,
						code -> {
							if (code == -1) return;
							handlePostUserCreation(code);
							viewModel.resetCreateUserStatus();
						});

		viewModel
				.getErrorMessage()
				.observe(
						this,
						error -> {
							if (error != null) Toasty.show(this, error);
						});
	}

	private void updateEmptyLayoutVisibility() {
		List<User> currentList = viewModel.getUsers().getValue();
		Boolean isLoading = viewModel.getIsUsersLoading().getValue();

		boolean isEmpty = currentList == null || currentList.isEmpty();
		boolean notLoading = isLoading == null || !isLoading;

		if (isEmpty && notLoading) {
			binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
		} else {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		}
	}

	public void showAddUserSheet() {
		BottomsheetAdminAddUserBinding sheetBinding =
				BottomsheetAdminAddUserBinding.inflate(LayoutInflater.from(this));

		bottomSheetAddUser = new BottomSheetDialog(this);
		bottomSheetAddUser.setContentView(sheetBinding.getRoot());
		AppUtil.applySheetStyle(bottomSheetAddUser, false);

		viewModel
				.getIsAddingUser()
				.observe(
						this,
						loading -> {
							sheetBinding.create.setEnabled(!loading);
							sheetBinding.create.setText(
									loading ? "" : getString(R.string.newCreateButtonCopy));
							sheetBinding.loadingIndicator.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							sheetBinding.fullName.setEnabled(!loading);
							sheetBinding.userUserName.setEnabled(!loading);
							sheetBinding.userEmail.setEnabled(!loading);
							sheetBinding.userPassword.setEnabled(!loading);
						});

		sheetBinding.create.setOnClickListener(
				v -> {
					String fullName =
							Objects.requireNonNull(sheetBinding.fullName.getText())
									.toString()
									.trim();
					String userName =
							Objects.requireNonNull(sheetBinding.userUserName.getText())
									.toString()
									.trim();
					String email =
							Objects.requireNonNull(sheetBinding.userEmail.getText())
									.toString()
									.trim();
					String password =
							Objects.requireNonNull(sheetBinding.userPassword.getText()).toString();

					if (userName.isEmpty() || email.isEmpty() || password.isEmpty()) {
						Toasty.show(this, getString(R.string.email_username_pw_required));
						return;
					}

					if (!AppUtil.checkStringsWithAlphaNumeric(userName)) {
						Toasty.show(this, getString(R.string.userInvalidUserName));
						return;
					}

					if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
						Toasty.show(this, getString(R.string.userInvalidEmail));
						return;
					}

					if (!fullName.isEmpty() && !AppUtil.checkStrings(fullName)) {
						Toasty.show(this, getString(R.string.userInvalidFullName));
						return;
					}

					String visibility = "public";
					int checkedId = sheetBinding.visibilityGroup.getCheckedChipId();
					if (checkedId == R.id.chipLimited) visibility = "limited";
					else if (checkedId == R.id.chipPrivate) visibility = "private";

					CreateUserOption option = new CreateUserOption();
					if (!fullName.isEmpty()) option.setFullName(fullName);
					option.setUsername(userName);
					option.setEmail(email);
					option.setPassword(password);
					option.setVisibility(visibility);
					option.setSendNotify(sheetBinding.switchNotify.isChecked());
					option.setMustChangePassword(sheetBinding.switchMustChangePassword.isChecked());
					option.setRestricted(sheetBinding.switchRestricted.isChecked());

					viewModel.createNewUser(this, option);
				});

		sheetBinding.btnClose.setOnClickListener(v -> bottomSheetAddUser.dismiss());
		bottomSheetAddUser.show();
	}

	private void handlePostUserCreation(int code) {
		if (code == 201) {
			if (bottomSheetAddUser != null && bottomSheetAddUser.isShowing()) {
				bottomSheetAddUser.dismiss();
			}
			Toasty.show(this, getString(R.string.userCreatedText));
			viewModel.resetUsersPagination();
			viewModel.fetchUsers(this, 1, resultLimit, true);
		} else if (code == 401) {
			AlertDialogs.authorizationTokenRevokedDialog(this);
		} else if (code == 422) {
			Toasty.show(this, getString(R.string.userExistsError));
		} else if (code != 400) {
			Toasty.show(this, getString(R.string.genericError));
		}
	}

	private void setupSearch() {
		binding.searchResultsRecycler.setLayoutManager(new LinearLayoutManager(this));
		binding.searchResultsRecycler.setAdapter(adapter);

		binding.btnSearch.setOnClickListener(v -> binding.searchView.show());

		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								filter(s.toString());
							}

							@Override
							public void afterTextChanged(Editable s) {}
						});

		binding.searchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState.toString().equals("HIDDEN")) {
						binding.searchView.setText("");
						filter("");
						binding.recyclerView.scrollToPosition(0);
					}
				});

		binding.searchView
				.getEditText()
				.setOnEditorActionListener(
						(v, actionId, event) -> {
							binding.searchView.hide();
							return false;
						});
	}

	private void filter(String text) {
		List<User> originalList = viewModel.getUsers().getValue();
		if (originalList == null) return;

		if (text == null || text.isEmpty()) {
			adapter.updateList(originalList);
			return;
		}

		List<User> filtered = new ArrayList<>();
		String query = text.toLowerCase().trim();

		for (User user : originalList) {
			String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
			String fullName = user.getFullName() != null ? user.getFullName().toLowerCase() : "";
			String login = user.getLogin() != null ? user.getLogin().toLowerCase() : "";

			if (email.contains(query) || fullName.contains(query) || login.contains(query)) {
				filtered.add(user);
			}
		}
		adapter.updateList(filtered);
	}
}
