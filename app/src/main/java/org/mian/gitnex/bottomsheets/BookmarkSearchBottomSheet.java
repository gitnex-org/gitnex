package org.mian.gitnex.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetBookmarkSearchBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class BookmarkSearchBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetBookmarkSearchBinding binding;
	private OnSearchListener listener;
	private String selectedType = null;
	private String currentQuery = "";
	private int selectedChipIndex = 0;

	private static final String ARG_QUERY = "query";
	private static final String ARG_TYPE = "type";
	private static final String ARG_CHIP_INDEX = "chipIndex";

	public interface OnSearchListener {
		void onSearch(String query, String type);
	}

	public static BookmarkSearchBottomSheet newInstance(String query, String type, int chipIndex) {
		BookmarkSearchBottomSheet fragment = new BookmarkSearchBottomSheet();
		Bundle args = new Bundle();
		args.putString(ARG_QUERY, query);
		args.putString(ARG_TYPE, type);
		args.putInt(ARG_CHIP_INDEX, chipIndex);
		fragment.setArguments(args);
		return fragment;
	}

	public void setOnSearchListener(OnSearchListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			currentQuery = getArguments().getString(ARG_QUERY, "");
			selectedType = getArguments().getString(ARG_TYPE);
			selectedChipIndex = getArguments().getInt(ARG_CHIP_INDEX, 0);
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetBookmarkSearchBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.searchInput.setText(currentQuery);
		setupFilterChips();
		setupListeners();
	}

	private void setupFilterChips() {
		String[] types = {null, "repo", "issue", "pr", "profile", "org", "wiki"};
		int[] labels = {
			R.string.all,
			R.string.repository,
			R.string.issue,
			R.string.pullRequest,
			R.string.profile,
			R.string.organization,
			R.string.wiki
		};

		binding.filterChipGroup.removeAllViews();
		for (int i = 0; i < types.length; i++) {
			Chip chip = new Chip(requireContext());
			chip.setText(labels[i]);
			chip.setCheckable(true);
			chip.setChecked(i == selectedChipIndex);
			final int index = i;
			final String type = types[i];
			chip.setOnClickListener(
					v -> {
						selectedType = type;
						selectedChipIndex = index;
					});
			binding.filterChipGroup.addView(chip);
		}
	}

	private void setupListeners() {
		binding.btnClose.setOnClickListener(v -> dismiss());

		binding.btnSearch.setOnClickListener(
				v -> {
					currentQuery =
							binding.searchInput.getText() != null
									? binding.searchInput.getText().toString()
									: "";
					if (listener != null) {
						listener.onSearch(currentQuery, selectedType);
					}
					dismiss();
				});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applySheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
