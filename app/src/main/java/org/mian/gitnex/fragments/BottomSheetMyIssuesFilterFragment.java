package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetMyIssuesFilterBinding;

/**
 * @author M M Arif
 */
public class BottomSheetMyIssuesFilterFragment extends BottomSheetDialogFragment {

	private BottomSheetListener bmListener;
	private String selectedState = "open";
	private String selectedFilter = "created_by_me";
	private static final String ARG_FILTER = "currentFilter";

	public static BottomSheetMyIssuesFilterFragment newInstance(String currentFilter) {

		BottomSheetMyIssuesFilterFragment fragment = new BottomSheetMyIssuesFilterFragment();
		Bundle args = new Bundle();
		args.putString(ARG_FILTER, currentFilter);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Bundle args = getArguments();

		if (args != null && args.containsKey(ARG_FILTER)) {
			String currentFilter = args.getString(ARG_FILTER, "open_created_by_me");
			String[] parts = currentFilter.split("_");
			selectedState = parts[0];
			selectedFilter = parts.length > 1 ? parts[1] : "created_by_me";
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		BottomSheetMyIssuesFilterBinding binding =
				BottomSheetMyIssuesFilterBinding.inflate(inflater, container, false);

		binding.chipOpen.setChecked(false);
		binding.chipClosed.setChecked(false);
		binding.chipCreatedByMe.setChecked(false);
		binding.chipAssignedToMe.setChecked(false);

		binding.chipOpen.setChecked(selectedState.equals("open"));
		binding.chipClosed.setChecked(selectedState.equals("closed"));
		binding.chipCreatedByMe.setChecked(selectedFilter.equals("created_by_me"));
		binding.chipAssignedToMe.setChecked(selectedFilter.equals("assignedToMe"));

		binding.stateChipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					if (!checkedIds.isEmpty()) {
						int checkedId = checkedIds.get(0);
						if (checkedId == binding.chipOpen.getId()) {
							selectedState = "open";
						} else if (checkedId == binding.chipClosed.getId()) {
							selectedState = "closed";
						}
						applyFilter();
					}
				});

		binding.filterChipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					if (!checkedIds.isEmpty()) {

						int checkedId = checkedIds.get(0);
						if (checkedId == binding.chipCreatedByMe.getId()) {
							selectedFilter = "created_by_me";
							binding.chipAssignedToMe.setChecked(false);
						} else if (checkedId == binding.chipAssignedToMe.getId()) {
							selectedFilter = "assignedToMe";
							binding.chipCreatedByMe.setChecked(false);
						}
						applyFilter();
					}
				});

		return binding.getRoot();
	}

	private void applyFilter() {
		String result = selectedState + "_" + selectedFilter;
		bmListener.onButtonClicked(result);
		dismiss();
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		try {
			bmListener = (BottomSheetListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context + " must implement BottomSheetListener");
		}
	}

	public interface BottomSheetListener {
		void onButtonClicked(String text);
	}
}
