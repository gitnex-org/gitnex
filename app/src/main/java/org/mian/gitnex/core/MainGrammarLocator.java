package org.mian.gitnex.core;

/**
 * @author opyale
 */
public class MainGrammarLocator {

	public static final String DEFAULT_FALLBACK_LANGUAGE = null;

	public static String fromExtension(String extension) {
		if (extension == null || extension.isEmpty()) {
			return DEFAULT_FALLBACK_LANGUAGE;
		}

		return switch (extension.toLowerCase()) {
			case "b", "bf" -> "brainfuck";

			case "c", "h", "hdl" -> "c";

			case "clj", "cljs", "cljc", "edn" -> "clojure";

			case "cc", "cpp", "cxx", "c++", "hh", "hpp", "hxx", "h++" -> "cpp";

			case "cs", "csx" -> "csharp";

			case "bash", "sh", "bsh", "zsh" -> "sh";

			case "d" -> "d";

			case "groovy", "gradle", "gvy", "gy", "gsh" -> "groovy";

			case "java", "jav" -> "java";

			case "js", "cjs", "mjs", "jsx" -> "javascript";
			case "ts", "tsx" -> "typescript";

			case "kt", "kts", "ktm" -> "kotlin";

			case "md", "markdown" -> "markdown";

			case "xml", "html", "htm", "xhtml", "mathml", "svg" -> "markup";

			case "php", "php3", "php4", "php5", "php7", "php8", "phtml" -> "php";

			case "py", "pyi", "pyc", "pyd", "pyo", "pyw", "pyz" -> "python";

			case "rb", "rbw", "rake", "gemspec" -> "ruby";

			case "rs", "rlib" -> "rust";

			case "scala", "sc" -> "scala";

			case "swift" -> "swift";

			case "go" -> "go";

			case "sql" -> "sql";

			case "json" -> "json";

			case "css" -> "css";

			case "el", "lisp", "cl", "lsp" -> "lisp";

			case "yaml", "yml", "properties" -> "yaml";

			default -> extension;
		};
	}
}
