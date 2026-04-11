package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateLabelOption;
import org.gitnex.tea4j.v2.models.EditLabelOption;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetCreateLabelBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.LabelsViewModel;

/**
 * @author mmarif
 */
public class BottomSheetCreateLabelFragment extends BottomSheetDialogFragment {

	private BottomsheetCreateLabelBinding binding;
	private LabelsViewModel viewModel;
	private String type, owner, repo, orgName;
	private Long labelId = null;
	private String selectedColor = "#2E7D32";
	private String initialName = "";
	private String initialDesc = "";

	public static BottomSheetCreateLabelFragment newInstance(
			String type, String owner, String repo, Label label) {
		BottomSheetCreateLabelFragment fragment = new BottomSheetCreateLabelFragment();
		Bundle args = new Bundle();
		args.putString("type", type);
		args.putString("owner", owner);
		args.putString("repo", repo);
		if (label != null) {
			args.putLong("id", label.getId());
			args.putString("name", label.getName());
			args.putString("color", label.getColor());
			args.putString("desc", label.getDescription());
		}
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateLabelBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		viewModel = new ViewModelProvider(requireActivity()).get(LabelsViewModel.class);
		parseArguments();

		setupInitialUI();
		setupObservers();
		setupListeners();
	}

	private void parseArguments() {
		if (getArguments() != null) {
			type = getArguments().getString("type");
			owner = getArguments().getString("owner");
			repo = getArguments().getString("repo");
			if (getArguments().containsKey("id")) {
				labelId = getArguments().getLong("id");
				initialName = getArguments().getString("name");
				initialDesc = getArguments().getString("desc");
				selectedColor = "#" + getArguments().getString("color");
			}
		}
	}

	private void setupInitialUI() {
		if (labelId != null) {
			binding.sheetTitle.setText(R.string.pageTitleLabelUpdate);
			binding.labelName.setText(initialName);
			binding.labelDesc.setText(initialDesc);
		}
		updateLivePreview(initialName, selectedColor);
	}

	private void setupListeners() {
		binding.labelName.addTextChangedListener(
				new TextWatcher() {
					@Override
					public void beforeTextChanged(
							CharSequence s, int start, int count, int after) {}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						updateLivePreview(s.toString(), selectedColor);
					}

					@Override
					public void afterTextChanged(Editable s) {}
				});

		binding.colorPickerTrigger.setOnClickListener(v -> openColorPicker());

		binding.btnClose.setOnClickListener(v -> dismiss());

		binding.btnSave.setOnClickListener(
				v -> {
					String name =
							Objects.requireNonNull(binding.labelName.getText()).toString().trim();
					String desc =
							Objects.requireNonNull(binding.labelDesc.getText()).toString().trim();

					if (name.isEmpty()) {
						Toasty.show(requireContext(), getString(R.string.labelEmptyError));
						return;
					}

					CreateLabelOption createOpt = new CreateLabelOption();
					EditLabelOption editOpt = new EditLabelOption();

					String apiColor = selectedColor.replace("#", "");

					createOpt.setName(name);
					createOpt.setColor(apiColor);
					createOpt.setDescription(desc);

					editOpt.setName(name);
					editOpt.setColor(apiColor);
					editOpt.setDescription(desc);

					viewModel.saveLabel(
							requireContext(), type, owner, repo, labelId, createOpt, editOpt);
				});
	}

	private void updateLivePreview(String name, String colorStr) {
		try {
			String formattedColor = colorStr.startsWith("#") ? colorStr : "#" + colorStr;
			int color = Color.parseColor(formattedColor);
			int contrast = ColorInverter.getContrastColor(color);

			binding.previewCard.setCardBackgroundColor(color);
			binding.labelPreviewText.setTextColor(contrast);

			binding.labelPreviewText.setText(name.isEmpty() ? getString(R.string.labelName) : name);
			binding.selectedColorIndicator.setBackgroundTintList(ColorStateList.valueOf(color));
		} catch (Exception e) {
			binding.previewCard.setCardBackgroundColor(Color.LTGRAY);
		}
	}

	private void openColorPicker() {
		BottomSheetColorPicker picker = BottomSheetColorPicker.newInstance(selectedColor);

		picker.setOnColorSelectedListener(
				hexColor -> {
					selectedColor = hexColor;
					updateLivePreview(
							Objects.requireNonNull(binding.labelName.getText()).toString(),
							selectedColor);
				});

		picker.show(getChildFragmentManager(), "color_picker");
	}

	private void setupObservers() {
		viewModel
				.getIsActionLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.btnSave.setEnabled(!loading);
							binding.loadingIndicator.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							binding.btnSave.setText(loading ? "" : getString(R.string.saveButton));
						});

		viewModel
				.getActionResult()
				.observe(
						this,
						code -> {
							if (code != null && (code == 200 || code == 201)) {
								dismiss();
							}
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
}
