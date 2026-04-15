package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.adapters.AssigneeSelectionAdapter;
import org.mian.gitnex.databinding.BottomsheetAssigneesPickerBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.AssigneesViewModel;

/**
 * @author mmarif
 */
public class BottomSheetAssigneesPicker extends BottomSheetDialogFragment {

	public interface OnAssigneesSelectedListener {
		void onSelected(Set<String> selected);
	}

	private BottomsheetAssigneesPickerBinding binding;
	private OnAssigneesSelectedListener listener;
	private Set<String> selectedAssignees;
	private RepositoryContext repository;
	private AssigneesViewModel assigneesViewModel;
	private AssigneeSelectionAdapter adapter;
	private int resultLimit;
	private String excludeUser = null;

	public static BottomSheetAssigneesPicker newInstance(
			RepositoryContext repo, List<String> current, @Nullable String excludeUser) {
		BottomSheetAssigneesPicker f = new BottomSheetAssigneesPicker();
		Bundle b = repo.getBundle();
		b.putStringArrayList("current_assignees", new ArrayList<>(current));
		if (excludeUser != null) {
			b.putString("exclude_user", excludeUser);
		}
		f.setArguments(b);
		return f;
	}

	public static BottomSheetAssigneesPicker newInstance(
			RepositoryContext repo, List<String> current) {
		return newInstance(repo, current, null);
	}

	public void setOnAssigneesSelectedListener(OnAssigneesSelectedListener l) {
		this.listener = l;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetAssigneesPickerBinding.inflate(inflater, container, false);
		assigneesViewModel = new ViewModelProvider(this).get(AssigneesViewModel.class);

		Bundle args = requireArguments();
		repository = RepositoryContext.fromBundle(args);
		selectedAssignees =
				new HashSet<>(Objects.requireNonNull(args.getStringArrayList("current_assignees")));
		excludeUser = args.getString("exclude_user");
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupRecyclerView();
		observeViewModel();

		if (Boolean.FALSE.equals(assigneesViewModel.getHasLoadedOnce().getValue())) {
			fetchPage(1, true);
		}

		binding.btnDone.setOnClickListener(
				v -> {
					if (listener != null) {
						listener.onSelected(selectedAssignees);
					}
					dismiss();
				});

		return binding.getRoot();
	}

	private void fetchPage(int page, boolean isRefresh) {
		assigneesViewModel.fetchAssignees(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				page,
				resultLimit,
				isRefresh);
	}

	private void setupRecyclerView() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.rvAssignees.setLayoutManager(layoutManager);
		adapter = new AssigneeSelectionAdapter(new ArrayList<>(), selectedAssignees, excludeUser);
		binding.rvAssignees.setAdapter(adapter);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						fetchPage(page, false);
					}
				};
		binding.rvAssignees.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		assigneesViewModel
				.getAssignees()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							List<User> data = (list != null) ? list : new ArrayList<>();
							adapter.updateList(data);
							if (!adapter.isEmpty()) {
								binding.rvAssignees.setVisibility(View.VISIBLE);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							} else {
								binding.rvAssignees.setVisibility(View.GONE);
								binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
							}
						});

		assigneesViewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (loading) {
								if (adapter.getItemCount() == 0) {
									binding.expressiveLoader.setVisibility(View.VISIBLE);
									binding.layoutEmpty.getRoot().setVisibility(View.GONE);
									binding.rvAssignees.setVisibility(View.GONE);
								}
							} else {
								binding.expressiveLoader.setVisibility(View.GONE);
								if (adapter.getItemCount() == 0) {
									binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
									binding.rvAssignees.setVisibility(View.GONE);
								} else {
									binding.layoutEmpty.getRoot().setVisibility(View.GONE);
									binding.rvAssignees.setVisibility(View.VISIBLE);
								}
							}
						});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applyFullScreenSheetStyle((BottomSheetDialog) dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
