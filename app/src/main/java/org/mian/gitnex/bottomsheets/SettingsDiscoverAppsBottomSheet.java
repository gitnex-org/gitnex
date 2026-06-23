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
import org.mian.gitnex.databinding.BottomsheetSettingsDiscoverAppsBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class SettingsDiscoverAppsBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetSettingsDiscoverAppsBinding binding;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetSettingsDiscoverAppsBinding.inflate(inflater, container, false);

		binding.labnexCard.setOnClickListener(
				v -> AppUtil.openUrlInBrowser(requireContext(), "https://labnex.app"));
		binding.oceannexCard.setOnClickListener(
				v -> AppUtil.openUrlInBrowser(requireContext(), "https://oceannex.swatian.com"));
		binding.nexnodeCard.setOnClickListener(
				v -> AppUtil.openUrlInBrowser(requireContext(), "https://nexnode.swatian.com"));

		return binding.getRoot();
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applySheetStyle((BottomSheetDialog) dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
