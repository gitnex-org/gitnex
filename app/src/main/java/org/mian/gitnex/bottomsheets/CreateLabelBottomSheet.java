package org.mian.gitnex.bottomsheets;

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
import org.mian.gitnex.helpers.LabelStylingHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.LabelsViewModel;

/**
 * @author mmarif
 */
public class CreateLabelBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateLabelBinding binding;
	private LabelsViewModel viewModel;
	private String type, owner, repo;
	private Long labelId = null;
	private String selectedColor = "#2E7D32";
	private String initialName = "";
	private String initialDesc = "";
	private boolean isExclusive = false;
	private boolean updatingSwitch = false;

	public static CreateLabelBottomSheet newInstance(
			String type, String owner, String repo, Label label) {
		CreateLabelBottomSheet fragment = new CreateLabelBottomSheet();
		Bundle args = new Bundle();
		args.putString("type", type);
		args.putString("owner", owner);
		args.putString("repo", repo);
		if (label != null) {
			args.putLong("id", label.getId());
			args.putString("name", label.getName());
			args.putString("color", label.getColor());
			args.putString("desc", label.getDescription());
			args.putBoolean("exclusive", label.isExclusive());
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
			if (getArguments().containsKey("exclusive")) {
				isExclusive = getArguments().getBoolean("exclusive");
			}
		}
	}

	private void setupInitialUI() {
		if (labelId != null) {
			binding.sheetTitle.setText(R.string.pageTitleLabelUpdate);
			binding.labelName.setText(initialName);
			binding.labelDesc.setText(initialDesc);
			binding.exclusiveSwitch.setEnabled(initialName.contains("/"));
			binding.exclusiveSwitch.setChecked(isExclusive);
		} else {
			binding.exclusiveSwitch.setEnabled(false);
		}
		updateLivePreview(initialName, selectedColor, isExclusive);
	}

	private void setupListeners() {
		binding.labelName.addTextChangedListener(
				new TextWatcher() {
					@Override
					public void beforeTextChanged(
							CharSequence s, int start, int count, int after) {}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						String text = s.toString();
						boolean hasSlash = text.contains("/");

						binding.exclusiveSwitch.setEnabled(hasSlash);

						if (hasSlash && !isExclusive) {
							updatingSwitch = true;
							isExclusive = true;
							binding.exclusiveSwitch.setChecked(true);
							updatingSwitch = false;
						} else if (!hasSlash) {
							isExclusive = false;
							binding.exclusiveSwitch.setChecked(false);
						}

						updateLivePreview(text, selectedColor, isExclusive);
					}

					@Override
					public void afterTextChanged(Editable s) {}
				});

		binding.exclusiveSwitch.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (updatingSwitch) return;
					isExclusive = isChecked;
					updateLivePreview(
							Objects.requireNonNull(binding.labelName.getText()).toString(),
							selectedColor,
							isExclusive);
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
					createOpt.setExclusive(isExclusive);

					editOpt.setName(name);
					editOpt.setColor(apiColor);
					editOpt.setDescription(desc);
					editOpt.setExclusive(isExclusive);

					viewModel.saveLabel(
							requireContext(), type, owner, repo, labelId, createOpt, editOpt);
				});
	}

	private void updateLivePreview(String name, String colorStr, boolean exclusive) {
		try {
			String formattedColor = colorStr.startsWith("#") ? colorStr : "#" + colorStr;
			int color = Color.parseColor(formattedColor);
			int contrast = ColorInverter.getContrastColor(color);
			String textColorHex = String.format("#%06X", (0xFFFFFF & contrast));

			if (LabelStylingHelper.isScopedLabel(name, exclusive)) {
				LabelStylingHelper.getInstance(requireContext())
						.styleScopedLabel(
								name,
								formattedColor,
								textColorHex,
								binding.labelPreviewKey,
								binding.labelPreviewValue,
								13,
								6,
								12);
			} else {
				binding.labelPreviewValue.setVisibility(View.GONE);
				LabelStylingHelper.getInstance(requireContext())
						.styleRegularLabel(
								name.isEmpty() ? getString(R.string.labelName) : name,
								formattedColor,
								textColorHex,
								binding.labelPreviewKey,
								13,
								6,
								12);
			}

			binding.selectedColorIndicator.setBackgroundTintList(ColorStateList.valueOf(color));
		} catch (Exception e) {
			binding.labelPreviewKey.setBackgroundColor(Color.LTGRAY);
		}
	}

	private void openColorPicker() {
		ColorPickerBottomSheet picker = ColorPickerBottomSheet.newInstance(selectedColor);

		picker.setOnColorSelectedListener(
				hexColor -> {
					selectedColor = hexColor;
					updateLivePreview(
							Objects.requireNonNull(binding.labelName.getText()).toString(),
							selectedColor,
							isExclusive);
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

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
