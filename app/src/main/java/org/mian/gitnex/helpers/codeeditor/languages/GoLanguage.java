package org.mian.gitnex.helpers.codeeditor.languages;

import com.amrdeveloper.codeview.Code;
import com.amrdeveloper.codeview.Keyword;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author AmrDeveloper
 * @author M M Arif
 */
public class GoLanguage extends Language {

	// Brackets and Colons
	private static final Pattern PATTERN_BUILTINS = Pattern.compile("[,:;[->]{}()]");

	// Data
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_CHAR = Pattern.compile("['](.*?)[']");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"`](.*?)[\"`]");
	private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");
	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("//[^\\n]*");

	private static final Pattern PATTERN_TODO_COMMENT =
			Pattern.compile("//\\s?(TODO|todo)\\s[^\n]*");
	private static final Pattern PATTERN_MULTI_LINE_COMMENT =
			Pattern.compile("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("\\.[a-zA-Z0-9_]+");
	private static final Pattern PATTERN_OPERATION =
			Pattern.compile(
					":|==|>|<|!=|>=|<=|->|=|>|<|%|-|-=|%=|\\+|\\-|\\-=|\\+=|\\^|\\&|\\|::|\\?|\\*");

	@Override
	public String[] getKeywords() {
		return new String[] {
			"break",
			"default",
			"func",
			"interface",
			"select",
			"case",
			"defer",
			"go",
			"map",
			"struct",
			"chan",
			"else",
			"goto",
			"package",
			"switch",
			"const",
			"fallthrough",
			"if",
			"bool",
			"byte",
			"cap",
			"close",
			"complex",
			"complex64",
			"complex128",
			"uint16",
			"copy",
			"false",
			"float32",
			"float64",
			"imag",
			"int",
			"int8",
			"int16",
			"uint32",
			"int32",
			"int64",
			"len",
			"make",
			"new",
			"nil",
			"uint64",
			"range",
			"type",
			"continue",
			"for",
			"import",
			"return",
			"var"
		};
	}

	@Override
	public List<Code> getCodeList() {
		List<Code> codeList = new ArrayList<>();
		String[] keywords = getKeywords();
		for (String keyword : keywords) {
			codeList.add(new Keyword(keyword));
		}
		return codeList;
	}

	@Override
	public String getName() {
		return "Go";
	}

	@Override
	public Set<Character> getIndentationStarts() {
		Set<Character> characterSet = new HashSet<>();
		characterSet.add('{');
		return characterSet;
	}

	@Override
	public Set<Character> getIndentationEnds() {
		Set<Character> characterSet = new HashSet<>();
		characterSet.add('}');
		return characterSet;
	}

	@Override
	public Pattern getPattern(LanguageElement element) {
		return switch (element) {
			case KEYWORD -> Pattern.compile("\\b(" + String.join("|", getKeywords()) + ")\\b");
			case BUILTIN -> PATTERN_BUILTINS;
			case NUMBER -> PATTERN_NUMBERS;
			case CHAR -> PATTERN_CHAR;
			case STRING -> PATTERN_STRING;
			case HEX -> PATTERN_HEX;
			case SINGLE_LINE_COMMENT -> PATTERN_SINGLE_LINE_COMMENT;
			case MULTI_LINE_COMMENT -> PATTERN_MULTI_LINE_COMMENT;
			case ATTRIBUTE -> PATTERN_ATTRIBUTE;
			case OPERATION -> PATTERN_OPERATION;
			case TODO_COMMENT -> PATTERN_TODO_COMMENT;
			// TODO supported by Go 1.18
			default -> null;
		};
	}
}
