package org.mian.gitnex.bottomsheets;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetAndroidPermissionsBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.PermissionHelper;

/**
 * @author mmarif
 */
public class AndroidPermissionsBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetAndroidPermissionsBinding binding;
	private String permission;
	private int requiredApiLevel;
	private String title;
	private String description;
	private ActivityResultLauncher<String> permissionLauncher;
	private OnPermissionResultListener listener;

	public interface OnPermissionResultListener {
		void onPermissionResult(boolean granted);
	}

	public static AndroidPermissionsBottomSheet newInstance(
			String permissionType, String permission, int requiredApiLevel) {
		AndroidPermissionsBottomSheet sheet = new AndroidPermissionsBottomSheet();
		Bundle args = new Bundle();
		args.putString("permissionType", permissionType);
		args.putString("permission", permission);
		args.putInt("requiredApiLevel", requiredApiLevel);
		sheet.setArguments(args);
		return sheet;
	}

	public void setOnPermissionResultListener(OnPermissionResultListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			String permissionType = getArguments().getString("permissionType");
			permission = getArguments().getString("permission");
			requiredApiLevel = getArguments().getInt("requiredApiLevel");

			if ((permissionType != null ? permissionType : "").equals("local_network_permission")) {
				title = getString(R.string.localNetworkPermissionTitle);
				description = getString(R.string.localNetworkPermissionDescription);
			} else {
				title = getString(R.string.permissionRequired);
				description = getString(R.string.permissionRequiredDescription);
			}
		} else {
			permission = null;
			requiredApiLevel = 0;
		}

		permissionLauncher =
				registerForActivityResult(
						new ActivityResultContracts.RequestPermission(),
						isGranted -> {
							updateButtonState();
							if (listener != null) {
								listener.onPermissionResult(isGranted);
							}
						});
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetAndroidPermissionsBinding.inflate(inflater, container, false);

		binding.permissionTitle.setText(title);
		binding.permissionDescription.setText(description);

		updateButtonState();

		binding.btnGrantPermission.setOnClickListener(
				v -> {
					if (permission == null) return;

					if (PermissionHelper.isGranted(
							requireContext(), permission, requiredApiLevel)) {
						return;
					}

					if (PermissionHelper.isPermanentlyDenied(
							requireActivity(), permission, requiredApiLevel)) {
						Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
						startActivity(intent);
					} else {
						permissionLauncher.launch(permission);
					}
				});

		return binding.getRoot();
	}

	private void updateButtonState() {
		if (permission == null) return;

		if (PermissionHelper.isGranted(requireContext(), permission, requiredApiLevel)) {
			binding.btnGrantPermission.setText(R.string.permissionGranted);
			binding.btnGrantPermission.setEnabled(false);
		} else if (PermissionHelper.isPermanentlyDenied(
				requireActivity(), permission, requiredApiLevel)) {
			binding.btnGrantPermission.setText(R.string.openSettings);
			binding.btnGrantPermission.setEnabled(true);
		} else {
			binding.btnGrantPermission.setText(R.string.grantPermission);
			binding.btnGrantPermission.setEnabled(true);
		}
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
