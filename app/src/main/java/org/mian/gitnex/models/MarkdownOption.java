package org.mian.gitnex.models;

/**
 * Author opyale
 */

public class MarkdownOption {

	private String Context;
	private String Mode;
	private String Text;
	private boolean Wiki;

	public MarkdownOption(String context, String mode, String text, boolean wiki) {

		Context = context;
		Mode = mode;
		Text = text;
		Wiki = wiki;
	}

	public String getContext() {

		return Context;
	}

	public String getMode() {

		return Mode;
	}

	public String getText() {

		return Text;
	}

	public boolean isWiki() {

		return Wiki;
	}

}
