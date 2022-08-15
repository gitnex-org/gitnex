package org.mian.gitnex.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 6543
 */

public class ParseDiff {

	private static String[] getFileNames(String raw) {

		String[] lines2 = raw.split(" b/");
		if(lines2.length < 2) {
			return new String[1];
		}
		String oldName = lines2[0];
		String newName = lines2[1].split("\\n")[0];
		return new String[]{oldName, newName};
	}

	private static String getFileInfo(String raw) {

		if(raw.contains("\ndeleted file mode \\d+\n")) {
			return "delete";
		}
		else if(raw.contains("\nnew file mode \\d+\n")) {
			return "new";
		}
		return "change";
	}

	private static int[] countRemoveAdd(String raw) {

		int rm = 0, add = 0;

		Pattern rmPattern = Pattern.compile("\n-");
		Pattern addPattern = Pattern.compile("\n\\+");
		Matcher rmMatcher = rmPattern.matcher(raw);
		Matcher addMatcher = addPattern.matcher(raw);

		while(rmMatcher.find())
			rm++;
		while(addMatcher.find())
			add++;

		return new int[]{rm, add};
	}

	public static List<FileDiffView> getFileDiffViewArray(String raw) {

		String[] lines = raw.split("(^|\\n)diff --git a/");
		List<FileDiffView> fileContentsArray;

		if(lines.length > 1) {
			fileContentsArray = new ArrayList<>(lines.length);

			// for each file in diff
			for(int i = 1; i < lines.length; i++) {

				// check if it is a binary file
				if(lines[i].contains("\nBinary files a/")) {
					String[] fileNames = getFileNames(lines[i]);
					if(fileNames.length != 2) {
						continue;
					}
					fileContentsArray.add(new FileDiffView(fileNames[0], fileNames[1], "binary", "", null));
				}

				// check if it is a binary patch
				else if(lines[i].contains("\nGIT binary patch\n")) {
					String[] fileNames = getFileNames(lines[i]);
					if(fileNames.length != 2) {
						continue;
					}

					String[] tmp = lines[i].split("literal \\d+\\n");
					String rawContent = "";
					if(tmp.length >= 2) {
						rawContent = tmp[1].replace("\n", "");
					}

					List<FileDiffView.Content> contents = new ArrayList<>();
					contents.add(new FileDiffView.Content(rawContent));
					fileContentsArray.add(new FileDiffView(fileNames[0], fileNames[1], "binary", getFileInfo(lines[i]), contents));
				}

				// check if it is normal diff
				else if(lines[i].contains("\n@@ -")) {
					String[] fileNames = getFileNames(lines[i]);
					if(fileNames.length != 2) {
						continue;
					}
					String[] rawDiffs = lines[i].split("\n@@ -");
					if(rawDiffs.length <= 1) {
						continue;
					}
					List<FileDiffView.Content> contents = new ArrayList<>();
					// parse each section starting with "@@" at line beginning
					for(int j = 1; j < rawDiffs.length; j++) {
						// remove stats info (ending with @@)
						// raw diff is the whole raw diff without any diff meta info's
						String[] rawDiff = rawDiffs[j].split("^\\d+(,\\d+)? \\+\\d+(,\\d+)? @@");
						if(rawDiff.length <= 1) {
							continue;
						}
						if(rawDiff[1].startsWith("\n")) {
							rawDiff[1] = rawDiff[1].substring(1);
						}

						// extract the diff stats info of the first line
						String statsLine = rawDiffs[j].split("\n")[0].split(" @@")[0];

						// parse "-1,2 +2,3" and "-1 -3" and so on
						int oldStart = 0, newStart = 0;
						String[] diffPos = statsLine.split(" \\+");
						if(diffPos.length == 2) {
							oldStart = Integer.parseInt(diffPos[0].split(",")[0]);
							newStart = Integer.parseInt(diffPos[1].split(",")[0]);

						}

						// get stat
						int[] stats = countRemoveAdd(rawDiff[1]);

						contents.add(new FileDiffView.Content(rawDiff[1], oldStart, newStart, stats[0], stats[1]));
					}
					fileContentsArray.add(new FileDiffView(fileNames[0], fileNames[1], "diff", getFileInfo(lines[i]), contents));
				}

				// a rename
				else if(lines[i].contains("\nrename from")) {
					String[] lines2 = lines[i].split("\\nrename (from|to )");
					if(lines2.length != 3) {
						continue;
					}
					fileContentsArray.add(new FileDiffView(lines2[1], lines2[2].split("\\n")[0], "rename", "rename", null));
				}
			}
		}
		else {
			fileContentsArray = Collections.emptyList();
		}

		return fileContentsArray;
	}

}
