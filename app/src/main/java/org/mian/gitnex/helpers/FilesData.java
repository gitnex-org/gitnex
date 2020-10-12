package org.mian.gitnex.helpers;

/**
 * Author M M Arif
 */

public class FilesData {

	public static int returnOnlyNumber(String fileSize) {

		return Integer.parseInt(fileSize.substring(0, fileSize.indexOf(" ")));
	}
}
