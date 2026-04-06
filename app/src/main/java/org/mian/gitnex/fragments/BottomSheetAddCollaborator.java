package org.mian.gitnex.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.CollaboratorSearchAdapter;
import org.mian.gitnex.databinding.BottomsheetAddTeamMemberBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.CollaboratorsViewModel;

/**
 * @author mmarif
 */
public class BottomSheetAddCollaborator extends BottomSheetDialogFragment
		implements CollaboratorSearchAdapter.OnCollaboratorActionListener {

	private BottomsheetAddTeamMemberBinding binding;
	private CollaboratorsViewModel viewModel;
	private CollaboratorSearchAdapter adapter;
	private RepositoryContext repository;

	public static BottomSheetAddCollaborator newInstance(RepositoryContext repository) {
		BottomSheetAddCollaborator fragment = new BottomSheetAddCollaborator();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NORMAL, R.style.Custom_BottomSheet);
		if (getArguments() != null) {
			repository = RepositoryContext.fromBundle(getArguments());
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		LayoutInflater themedInflater = inflater.cloneInContext(requireActivity());

		binding = BottomsheetAddTeamMemberBinding.inflate(themedInflater, container, false);
		viewModel =
				new ViewModelProvider(requireParentFragment()).get(CollaboratorsViewModel.class);

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		viewModel.clearSearch();
		binding.searchInput.setText("");

		setupRecyclerView();
		setupListeners();
		observeViewModel();
	}

	private void setupRecyclerView() {
		adapter = new CollaboratorSearchAdapter(requireContext(), this);
		binding.rvUserSearch.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.rvUserSearch.setAdapter(adapter);
	}

	private void setupListeners() {
		binding.searchInputLayout.setEndIconOnClickListener(v -> performSearch());
		binding.btnClose.setOnClickListener(v -> dismiss());

		binding.searchInput.setOnEditorActionListener(
				(v, actionId, event) -> {
					if (actionId == EditorInfo.IME_ACTION_SEARCH) {
						performSearch();
						return true;
					}
					return false;
				});
	}

	private void performSearch() {
		String query = Objects.requireNonNull(binding.searchInput.getText()).toString().trim();
		if (query.length() > 1) {
			AppUtil.hideKeyboard(requireActivity());
			binding.searchInput.clearFocus();
			viewModel.searchGlobalUsers(requireContext(), query, 1, 30);
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	private void observeViewModel() {
		viewModel
				.getSearchedUsers()
				.observe(
						getViewLifecycleOwner(),
						users -> {
							if (users != null && !users.isEmpty()) {
								adapter.updateList(users);
								binding.rvUserSearch.setVisibility(View.VISIBLE);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							} else {
								binding.rvUserSearch.setVisibility(View.GONE);
								binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
							}
						});

		viewModel
				.getCollaborators()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (adapter != null) {
								adapter.notifyDataSetChanged();
							}
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.expressiveLoader.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							if (loading) binding.layoutEmpty.getRoot().setVisibility(View.GONE);
						});

		viewModel
				.getActionResult()
				.observe(
						getViewLifecycleOwner(),
						code -> {
							if (code == 204) {
								adapter.notifyDataSetChanged();

								viewModel.fetchCollaborators(
										requireContext(),
										repository.getOwner(),
										repository.getName(),
										1,
										30,
										true);

								viewModel.resetActionResult();
							}
						});
	}

	@Override
	public boolean isAlreadyCollaborator(String login) {
		List<User> members = viewModel.getCollaborators().getValue();
		if (members == null) return false;
		for (User u : members) {
			if (u.getLogin().equalsIgnoreCase(login)) return true;
		}
		return false;
	}

	@Override
	public void onAdd(User user) {
		String[] permissions = {"Read", "Write", "Admin"};
		final int[] selectedIndex = {1};

		new MaterialAlertDialogBuilder(requireContext())
				.setTitle(R.string.newTeamPermission)
				.setSingleChoiceItems(
						permissions,
						selectedIndex[0],
						(dialog, which) -> {
							selectedIndex[0] = which;
						})
				.setNegativeButton(R.string.cancelButton, null)
				.setPositiveButton(
						R.string.addButton,
						(dialog, which) -> {
							String selectedPerm = permissions[selectedIndex[0]].toLowerCase();
							viewModel.addCollaborator(
									requireContext(),
									repository.getOwner(),
									repository.getName(),
									user.getLogin(),
									selectedPerm);
						})
				.show();
	}

	@Override
	public void onRemove(User user) {
		new MaterialAlertDialogBuilder(requireContext())
				.setTitle(getString(R.string.removeCollaboratorDialogTitle, user.getLogin()))
				.setMessage(R.string.removeCollaboratorMessage)
				.setPositiveButton(
						R.string.removeButton,
						(dialog, which) -> {
							viewModel.deleteCollaborator(
									requireContext(),
									repository.getOwner(),
									repository.getName(),
									user.getLogin());
						})
				.setNegativeButton(R.string.cancelButton, null)
				.show();
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applyFullScreenSheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
