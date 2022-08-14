package org.mian.gitnex.helpers.codeeditor.languages;

import android.content.Context;
import android.content.res.Resources;
import com.amrdeveloper.codeview.Code;
import com.amrdeveloper.codeview.CodeView;
import com.amrdeveloper.codeview.Keyword;
import org.mian.gitnex.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author M M Arif
 */

public class XmlLanguage {

	//Language Keywords
	private static final Pattern PATTERN_KEYWORDS = Pattern.compile("\\b(<xml|version|encoding)\\b");

	//Brackets and Colons
	private static final Pattern PATTERN_BUILTINS = Pattern.compile("[,:;[->]{}()]");

	//Data
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_CHAR = Pattern.compile("['](.*?)[']");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");
	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("//[^\\n]*");
	private static final Pattern PATTERN_MULTI_LINE_COMMENT = Pattern.compile("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("\\.[a-zA-Z0-9_]+");
	private static final Pattern PATTERN_OPERATION =Pattern.compile( ":|==|>|<|!=|>=|<=|->|=|>|<|%|-|-=|%=|\\+|\\-|\\-=|\\+=|\\^|\\&|\\|::|\\?|\\*");

	public static void applyFiveColorsDarkTheme(Context context, CodeView codeView) {
		codeView.resetSyntaxPatternList();
		codeView.resetHighlighter();

		Resources resources = context.getResources();

		//View Background
		codeView.setBackgroundColor(resources.getColor(R.color.five_dark_black, null));

		//Syntax Colors
		codeView.addSyntaxPattern(PATTERN_HEX, resources.getColor(R.color.five_dark_purple, null));
		codeView.addSyntaxPattern(PATTERN_CHAR, resources.getColor(R.color.five_dark_yellow, null));
		codeView.addSyntaxPattern(PATTERN_STRING, resources.getColor(R.color.five_dark_yellow, null));
		codeView.addSyntaxPattern(PATTERN_NUMBERS, resources.getColor(R.color.five_dark_purple, null));
		codeView.addSyntaxPattern(PATTERN_KEYWORDS, resources.getColor(R.color.five_dark_purple, null));
		codeView.addSyntaxPattern(PATTERN_BUILTINS, resources.getColor(R.color.five_dark_white, null));
		codeView.addSyntaxPattern(PATTERN_SINGLE_LINE_COMMENT, resources.getColor(R.color.five_dark_grey, null));
		codeView.addSyntaxPattern(PATTERN_MULTI_LINE_COMMENT, resources.getColor(R.color.five_dark_grey, null));
		codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, resources.getColor(R.color.five_dark_blue, null));
		codeView.addSyntaxPattern(PATTERN_OPERATION, resources.getColor(R.color.five_dark_purple, null));

		//Default Color
		codeView.setTextColor(resources.getColor(R.color.five_dark_white, null));

		codeView.reHighlightSyntax();
	}

	public static String[] getKeywords(Context context) {
		return context.getResources().getStringArray(R.array.xml_keywords);
	}

	public static List<Code> getCodeList(Context context) {
		List<Code> codeList = new ArrayList<>();
		String[] keywords = getKeywords(context);
		for (String keyword : keywords) {
			codeList.add(new Keyword(keyword));
		}
		return codeList;
	}

	public static Set<Character> getIndentationStarts() {
		Set<Character> characterSet = new HashSet<>();
		characterSet.add('{');
		return characterSet;
	}

	public static Set<Character> getIndentationEnds() {
		Set<Character> characterSet = new HashSet<>();
		characterSet.add('}');
		return characterSet;
	}

	public static String getCommentStart() {
		return "//";
	}

	public static String getCommentEnd() {
		return "";
	}
}
