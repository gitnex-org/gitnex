package org.mian.gitnex.helpers;

/**
 * @author mmarif
 */
public class AppUIStateManager {

	private static int uiVersion = 0;

	public static void invalidateUI() {
		uiVersion++;
	}

	public static int getUiVersion() {
		return uiVersion;
	}
}
