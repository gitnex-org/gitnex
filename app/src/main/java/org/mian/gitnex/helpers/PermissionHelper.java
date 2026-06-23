package org.mian.gitnex.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

/**
 * @author mmarif
 */
public class PermissionHelper {

	private PermissionHelper() {}

	public static boolean isBelowApiLevel(int apiLevel) {
		return Build.VERSION.SDK_INT < apiLevel;
	}

	public static boolean isGranted(Context context, String permission, int requiredApiLevel) {
		if (isBelowApiLevel(requiredApiLevel)) {
			return true;
		}
		return ContextCompat.checkSelfPermission(context, permission)
				== PackageManager.PERMISSION_GRANTED;
	}

	public static boolean shouldShowRationale(
			android.app.Activity activity, String permission, int requiredApiLevel) {
		if (isBelowApiLevel(requiredApiLevel)) {
			return false;
		}
		if (isGranted(activity, permission, requiredApiLevel)) {
			return false;
		}
		return activity.shouldShowRequestPermissionRationale(permission);
	}

	public static boolean isPermanentlyDenied(
			android.app.Activity activity, String permission, int requiredApiLevel) {
		if (isBelowApiLevel(requiredApiLevel)) {
			return false;
		}
		if (isGranted(activity, permission, requiredApiLevel)) {
			return false;
		}
		return !activity.shouldShowRequestPermissionRationale(permission);
	}
}
