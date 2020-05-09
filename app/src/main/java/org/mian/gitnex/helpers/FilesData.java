package org.mian.gitnex.helpers;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Author M M Arif
 */

public class FilesData {

	public static int returnOnlyNumber(String fileSize) {

		return Integer.parseInt(fileSize.substring(0, fileSize.indexOf(" ")));

	}

	public static long getFileSizeRecursively(Set<File> alreadySeen, File dirDirectory) {

		long fileSize = 0;

		for (File filItem : Objects.requireNonNull(dirDirectory.listFiles())) {

			if (filItem.isDirectory()) {

				fileSize += getFileSize(filItem);

			}
			else {

				alreadySeen.add(new File(filItem.getName()));
				fileSize += filItem.length();

			}

		}

		return fileSize;

	}

	private static long getFileSize(File subDirectory) {

		long fileSize = 0;

		Deque<File> unprocessedDirs = new ArrayDeque<>();
		unprocessedDirs.add(subDirectory);
		Set<File> alreadySeen = new HashSet<>();

		while (!unprocessedDirs.isEmpty()) {

			File dir = unprocessedDirs.removeFirst();

			for (File filItem : Objects.requireNonNull(dir.listFiles())) {

				if (filItem.isDirectory()) {

					unprocessedDirs.addFirst(filItem);

				}
				else {

					if (! alreadySeen.contains(filItem.getName())) {

						alreadySeen.add(new File(filItem.getName()));
						fileSize += filItem.length();

					}

				}

			}

		}

		return fileSize;

	}

}
