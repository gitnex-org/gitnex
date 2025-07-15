package org.mian.gitnex.helpers;

import java.util.HashMap;
import java.util.Map;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil.FileType;

/**
 * @author mmarif
 */
public class FileIcon {

	private static final Map<FileType, Integer> typeIcons = new HashMap<>();
	private static final Map<String, Integer> iconCache = new HashMap<>();
	private static final Map<String, Integer> extensionIcons = new HashMap<>();

	static {
		typeIcons.put(FileType.IMAGE, R.drawable.ic_image);
		typeIcons.put(FileType.AUDIO, R.drawable.ic_audio);
		typeIcons.put(FileType.VIDEO, R.drawable.ic_video);
		typeIcons.put(FileType.DOCUMENT, R.drawable.ic_document);
		typeIcons.put(FileType.EXECUTABLE, R.drawable.ic_executable);
		typeIcons.put(FileType.TEXT, R.drawable.ic_document);
		typeIcons.put(FileType.FONT, R.drawable.ic_font);
		typeIcons.put(FileType.UNKNOWN, R.drawable.ic_document);
		typeIcons.put(FileType.KEYSTORE, R.drawable.ic_lock);

		extensionIcons.put("txt", R.drawable.ic_text);
		extensionIcons.put("md", R.drawable.ic_markdown);
		extensionIcons.put("json", R.drawable.ic_json);
		extensionIcons.put("java", R.drawable.ic_java);
		extensionIcons.put("go", R.drawable.ic_go);
		extensionIcons.put("php", R.drawable.ic_php);
		extensionIcons.put("c", R.drawable.ic_c);
		extensionIcons.put("cc", R.drawable.ic_cpp);
		extensionIcons.put("cpp", R.drawable.ic_cpp);
		extensionIcons.put("d", R.drawable.ic_d);
		extensionIcons.put("h", R.drawable.ic_c);
		extensionIcons.put("cxx", R.drawable.ic_cpp);
		extensionIcons.put("cyc", R.drawable.ic_c);
		extensionIcons.put("m", R.drawable.ic_m);
		extensionIcons.put("cs", R.drawable.ic_cs);
		extensionIcons.put("bash", R.drawable.ic_bash);
		extensionIcons.put("sh", R.drawable.ic_bash);
		extensionIcons.put("bsh", R.drawable.ic_bash);
		extensionIcons.put("cv", R.drawable.ic_cvs);
		extensionIcons.put("python", R.drawable.ic_python);
		extensionIcons.put("perl", R.drawable.ic_perl);
		extensionIcons.put("pm", R.drawable.ic_perl);
		extensionIcons.put("rb", R.drawable.ic_ruby);
		extensionIcons.put("ruby", R.drawable.ic_ruby);
		extensionIcons.put("javascript", R.drawable.ic_javascript);
		extensionIcons.put("coffee", R.drawable.ic_coffee);
		extensionIcons.put("rc", R.drawable.ic_rust);
		extensionIcons.put("rs", R.drawable.ic_rust);
		extensionIcons.put("rust", R.drawable.ic_rust);
		extensionIcons.put("basic", R.drawable.ic_file_code);
		extensionIcons.put("clj", R.drawable.ic_clj);
		extensionIcons.put("css", R.drawable.ic_css);
		extensionIcons.put("dart", R.drawable.ic_dart);
		extensionIcons.put("lisp", R.drawable.ic_lisp);
		extensionIcons.put("erl", R.drawable.ic_erl);
		extensionIcons.put("hs", R.drawable.ic_hs);
		extensionIcons.put("lsp", R.drawable.ic_lisp);
		extensionIcons.put("rkt", R.drawable.ic_rkt);
		extensionIcons.put("ss", R.drawable.ic_ss);
		extensionIcons.put("lua", R.drawable.ic_lua);
		extensionIcons.put("matlab", R.drawable.ic_matlab);
		extensionIcons.put("pascal", R.drawable.ic_pascal);
		extensionIcons.put("r", R.drawable.ic_r);
		extensionIcons.put("scala", R.drawable.ic_scala);
		extensionIcons.put("sql", R.drawable.ic_sql);
		extensionIcons.put("latex", R.drawable.ic_latex);
		extensionIcons.put("tex", R.drawable.ic_latex);
		extensionIcons.put("vb", R.drawable.ic_vb);
		extensionIcons.put("vbs", R.drawable.ic_vb);
		extensionIcons.put("vhd", R.drawable.ic_vhd);
		extensionIcons.put("tcl", R.drawable.ic_tcl);
		extensionIcons.put("wiki.meta", R.drawable.ic_wiki);
		extensionIcons.put("yaml", R.drawable.ic_yaml);
		extensionIcons.put("yml", R.drawable.ic_yaml);
		extensionIcons.put("markdown", R.drawable.ic_markdown);
		extensionIcons.put("xml", R.drawable.ic_xml);
		extensionIcons.put("proto", R.drawable.ic_proto);
		extensionIcons.put("regex", R.drawable.ic_file_code);
		extensionIcons.put("py", R.drawable.ic_python);
		extensionIcons.put("pl", R.drawable.ic_perl);
		extensionIcons.put("js", R.drawable.ic_javascript);
		extensionIcons.put("html", R.drawable.ic_html);
		extensionIcons.put("htm", R.drawable.ic_html);
		extensionIcons.put("volt", R.drawable.ic_volt);
		extensionIcons.put("ini", R.drawable.ic_conf);
		extensionIcons.put("htaccess", R.drawable.ic_apache);
		extensionIcons.put("conf", R.drawable.ic_conf);
		extensionIcons.put("gitignore", R.drawable.ic_git);
		extensionIcons.put("gradle", R.drawable.ic_gradle);
		extensionIcons.put("properties", R.drawable.ic_conf);
		extensionIcons.put("bat", R.drawable.ic_bat);
		extensionIcons.put("twig", R.drawable.ic_twig);
		extensionIcons.put("cvs", R.drawable.ic_cvs);
		extensionIcons.put("cmake", R.drawable.ic_cmake);
		extensionIcons.put("in", R.drawable.ic_in);
		extensionIcons.put("info", R.drawable.ic_info);
		extensionIcons.put("spec", R.drawable.ic_spec);
		extensionIcons.put("m4", R.drawable.ic_file_code);
		extensionIcons.put("am", R.drawable.ic_am);
		extensionIcons.put("dist", R.drawable.ic_python);
		extensionIcons.put("pam", R.drawable.ic_file_lock);
		extensionIcons.put("hx", R.drawable.ic_hx);
		extensionIcons.put("ts", R.drawable.ic_ts);
		extensionIcons.put("kt", R.drawable.ic_kotlin);
		extensionIcons.put("kts", R.drawable.ic_kotlin);
		extensionIcons.put("el", R.drawable.ic_el);
		extensionIcons.put("gitattributes", R.drawable.ic_git);
		extensionIcons.put("gitmodules", R.drawable.ic_git);
		extensionIcons.put("editorconfig", R.drawable.ic_editorconfig);
		extensionIcons.put("cjs", R.drawable.ic_javascript);
		extensionIcons.put("jenkinsfile", R.drawable.ic_jenkins);
		extensionIcons.put("toml", R.drawable.ic_toml);
		extensionIcons.put("lock", R.drawable.ic_file_lock);
		extensionIcons.put("pro", R.drawable.ic_prolog);
		extensionIcons.put("gradlew", R.drawable.ic_gradle);
		extensionIcons.put("ll", R.drawable.ic_file_code);
		extensionIcons.put("llvm", R.drawable.ic_file_code);
		extensionIcons.put("csv", R.drawable.ic_csv);
		extensionIcons.put("mjs", R.drawable.ic_javascript);
		extensionIcons.put("next", R.drawable.ic_file_next);
		extensionIcons.put("nvmrc", R.drawable.ic_node_js);
		extensionIcons.put("license", R.drawable.ic_license);
	}

	public static int getIconResource(String fileName, String type) {

		if (fileName == null || type == null) {
			return R.drawable.ic_document;
		}

		switch (type) {
			case "dir":
				return R.drawable.ic_directory;
			case "submodule":
				return R.drawable.ic_submodule;
			case "symlink":
				return R.drawable.ic_symlink;
		}

		// Handle files without extension
		String extension = "";
		if (fileName.equalsIgnoreCase("Jenkinsfile")) {
			extension = "jenkinsfile";
		} else if (fileName.equalsIgnoreCase("gradlew")) {
			extension = "gradlew";
		} else if (fileName.contains(".")) {
			extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		} else if (fileName.equalsIgnoreCase("htaccess")) {
			extension = "htaccess";
		} else if (fileName.equalsIgnoreCase("license")) {
			extension = "license";
		}

		Integer cachedIcon = iconCache.get(extension);
		if (cachedIcon != null) {
			return cachedIcon;
		}

		FileType fileType = FileType.UNKNOWN;
		for (Map.Entry<String[], FileType> entry : AppUtil.getExtensions().entrySet()) {
			for (String ext : entry.getKey()) {
				if (ext.equalsIgnoreCase(extension)) {
					fileType = entry.getValue();
					break;
				}
			}
			if (fileType != FileType.UNKNOWN) {
				break;
			}
		}

		if (fileType == FileType.TEXT) {
			Integer iconId = extensionIcons.get(extension);
			if (iconId != null) {
				iconCache.put(extension, iconId);
				return iconId;
			}
		}

		Integer typeIcon = typeIcons.get(fileType);
		if (typeIcon != null) {
			iconCache.put(extension, typeIcon);
			return typeIcon;
		}

		iconCache.put(extension, R.drawable.ic_document);
		return R.drawable.ic_document;
	}
}
