package org.mian.gitnex.helpers.codeeditor;

import android.content.Context;
import com.amrdeveloper.codeview.Code;
import com.amrdeveloper.codeview.CodeView;
import org.mian.gitnex.helpers.codeeditor.languages.GoLanguage;
import org.mian.gitnex.helpers.codeeditor.languages.HtmlLanguage;
import org.mian.gitnex.helpers.codeeditor.languages.JavaLanguage;
import org.mian.gitnex.helpers.codeeditor.languages.PhpLanguage;
import org.mian.gitnex.helpers.codeeditor.languages.PythonLanguage;
import org.mian.gitnex.helpers.codeeditor.languages.XmlLanguage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author AmrDeveloper
 * @author M M Arif
 */

public class LanguageManager {

	private final Context context;
	private final CodeView codeView;

	public LanguageManager(Context context, CodeView codeView) {
		this.context = context;
		this.codeView = codeView;
	}

	public void applyTheme(LanguageName language, ThemeName theme) {

		if(theme == ThemeName.FIVE_COLOR) {
			applyFiveColorsDarkTheme(language);
		}
	}

	public String[] getLanguageKeywords(LanguageName language) {
		switch(language) {
			case JAVA:
				return JavaLanguage.getKeywords(context);
			case PY:
				return PythonLanguage.getKeywords(context);
			case GO:
				return GoLanguage.getKeywords(context);
			case PHP:
				return PhpLanguage.getKeywords(context);
			case XML:
				return XmlLanguage.getKeywords(context);
			case HTML:
				return HtmlLanguage.getKeywords(context);
			default:
				return new String[]{};
		}
	}

	public List<Code> getLanguageCodeList(LanguageName language) {
		switch(language) {
			case JAVA:
				return JavaLanguage.getCodeList(context);
			case PY:
				return PythonLanguage.getCodeList(context);
			case GO:
				return GoLanguage.getCodeList(context);
			case PHP:
				return PhpLanguage.getCodeList(context);
			case XML:
				return XmlLanguage.getCodeList(context);
			case HTML:
				return HtmlLanguage.getCodeList(context);
			default:
				return new ArrayList<>();
		}
	}

	public Set<Character> getLanguageIndentationStarts(LanguageName language) {
		switch(language) {
			case JAVA:
				return JavaLanguage.getIndentationStarts();
			case PY:
				return PythonLanguage.getIndentationStarts();
			case GO:
				return GoLanguage.getIndentationStarts();
			case PHP:
				return PhpLanguage.getIndentationStarts();
			case XML:
				return XmlLanguage.getIndentationStarts();
			case HTML:
				return HtmlLanguage.getIndentationStarts();
			default:
				return new HashSet<>();
		}
	}

	public Set<Character> getLanguageIndentationEnds(LanguageName language) {
		switch(language) {
			case JAVA:
				return JavaLanguage.getIndentationEnds();
			case PY:
				return PythonLanguage.getIndentationEnds();
			case GO:
				return GoLanguage.getIndentationEnds();
			case PHP:
				return PhpLanguage.getIndentationEnds();
			case XML:
				return XmlLanguage.getIndentationEnds();
			case HTML:
				return HtmlLanguage.getIndentationEnds();
			default:
				return new HashSet<>();
		}
	}

	private void applyFiveColorsDarkTheme(LanguageName language) {
		switch(language) {
			case JAVA:
				JavaLanguage.applyFiveColorsDarkTheme(context, codeView);
				break;
			case PY:
				PythonLanguage.applyFiveColorsDarkTheme(context, codeView);
				break;
			case GO:
				GoLanguage.applyFiveColorsDarkTheme(context, codeView);
				break;
			case PHP:
				PhpLanguage.applyFiveColorsDarkTheme(context, codeView);
				break;
			case XML:
				XmlLanguage.applyFiveColorsDarkTheme(context, codeView);
				break;
			case HTML:
				HtmlLanguage.applyFiveColorsDarkTheme(context, codeView);
				break;
		}
	}

}
