package org.mian.gitnex.helpers.codeeditor.languages;

import com.amrdeveloper.codeview.Code;
import com.amrdeveloper.codeview.Keyword;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author qwerty287
 */
public class CLanguage extends Language {

	private static final Pattern PATTERN_BUILTINS = Pattern.compile("[,:;[->]{}()]");
	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("//[^\\n]*");
	private static final Pattern PATTERN_MULTI_LINE_COMMENT =
			Pattern.compile("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("\\.[a-zA-Z0-9_]+");
	private static final Pattern PATTERN_OPERATION =
			Pattern.compile(
					":|==|>|<|!=|>=|<=|->|=|>|<|%|-|-=|%=|\\+|\\-|\\-=|\\+=|\\^|\\&|\\|::|\\?|\\*");
	private static final Pattern PATTERN_TODO_COMMENT =
			Pattern.compile("//\\s?(TODO|todo)\\s[^\n]*");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_CHAR = Pattern.compile("['](.*?)[']");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");

	public static String getCommentStart() {
		return "//";
	}

	public static String getCommentEnd() {
		return "";
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
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"alignas",
			"alignof",
			"auto",
			"bool",
			"break",
			"case",
			"char",
			"const",
			"constexpr",
			"continue",
			"default",
			"do",
			"double",
			"else",
			"enum",
			"extern",
			"false",
			"float",
			"for",
			"goto",
			"if",
			"inline",
			"int",
			"long",
			"nullptr",
			"register",
			"restrict",
			"return",
			"short",
			"signed",
			"sizeof",
			"static",
			"static_assert",
			"struct",
			"switch",
			"thread_local",
			"true",
			"typedef",
			"typeof",
			"typeof_unqual",
			"union",
			"unsigned",
			"void",
			"volatile",
			"while",
			"_Alignas",
			"_Alignof",
			"_Atomic",
			"_BitInt",
			"_Bool",
			"_Complex",
			"_Decimal128",
			"_Decimal32",
			"_Decimal64",
			"_Generic",
			"_Imaginary",
			"_Noreturn",
			"_Static_assert",
			"_Thread_local",
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
		return "C";
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
}
