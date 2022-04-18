package org.mian.gitnex.helpers;

import java.io.Serializable;
import java.util.List;

/**
 * Author M M Arif
 * Author 6543
 */

public class FileDiffView implements Serializable {

	private String fileNewName;
	private String fileOldName;
	private String diffType;
	private String fileInfo;
	private Stats stats;
	private List<Content> contents;

	public static class Stats implements Serializable {

		private int lineAdded;
		private int lineRemoved;

		public Stats(int added, int removed) {

			this.lineAdded = added;
			this.lineRemoved = removed;
		}

		public int getAdded() {

			return lineAdded;
		}

		public int getRemoved() {

			return lineRemoved;
		}

		public String toString() {

			return "+" + this.lineAdded + ", -" + this.lineRemoved;
		}

	}

	public static class Content implements Serializable {

		private int lineAdded;
		private int lineRemoved;
		private int oldLineStart;
		private int newLineStart;
		private String raw;

		public Content(String content) {

			this.raw = content;
		}

		public Content(String content, int oldStart, int newStart, int removed, int added) {

			this.raw = content;
			this.lineAdded = added;
			this.lineRemoved = removed;
			this.oldLineStart = oldStart;
			this.newLineStart = newStart;
		}

		public String getRaw() {

			return raw;
		}

		public int getLineAdded() {

			return this.lineAdded;
		}

		public int getLineRemoved() {

			return this.lineRemoved;
		}

		public int getOldLineStart() {

			return this.oldLineStart;
		}

		public int getNewLineStart() {

			return this.newLineStart;
		}

	}

	public FileDiffView(String oldName, String newName, String diffType, String fileInfo, List<Content> fileContents) {

		this.fileNewName = newName.trim();
		this.fileOldName = oldName.trim();
		this.diffType = diffType;
		this.fileInfo = fileInfo;
		this.contents = fileContents;
		this.stats = new Stats(0, 0);
		if(fileContents != null) {
			for(Content content : this.contents) {
				stats.lineAdded += content.lineAdded;
				stats.lineRemoved += content.lineRemoved;
			}
		}

	}

	public String getFileName() {

		if(fileOldName.length() != 0 && !fileOldName.equals(fileNewName)) {
			return fileOldName + " -> " + fileNewName;
		}
		return fileNewName;
	}

	public boolean isFileBinary() {

		return diffType.equals("binary");
	}

	public String getFileInfo() {

		if(diffType.equals("binary")) {
			return diffType + " " + fileInfo;
		}

		if(fileInfo.equals("change") && this.stats != null) {
			return this.stats.toString();
		}

		return fileInfo;
	}

	public String toString() {

		StringBuilder raw = new StringBuilder();
		if(this.contents != null) {
			for(Content c : this.contents) {
				raw.append(c.getRaw());
			}
		}
		return raw.toString();
	}

	public List<Content> getFileContents() {

		return this.contents;
	}

}
