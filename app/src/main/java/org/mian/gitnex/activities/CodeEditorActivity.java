package org.mian.gitnex.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import com.amrdeveloper.codeview.Code;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityCodeEditorBinding;
import org.mian.gitnex.helpers.codeeditor.CustomCodeViewAdapter;
import org.mian.gitnex.helpers.codeeditor.LanguageManager;
import org.mian.gitnex.helpers.codeeditor.LanguageName;
import org.mian.gitnex.helpers.codeeditor.SourcePositionListener;
import org.mian.gitnex.helpers.codeeditor.ThemeName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author AmrDeveloper
 * @author M M Arif
 */

public class CodeEditorActivity extends BaseActivity {

	private ActivityCodeEditorBinding binding;
	private LanguageManager languageManager;
	private final LanguageName currentLanguage = LanguageName.JAVA;
	private final ThemeName currentTheme = ThemeName.FIVE_COLOR;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityCodeEditorBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.close.setOnClickListener(view -> finish());

		configCodeView(currentLanguage);
		configCodeViewPlugins();
	}

	private void configCodeView(LanguageName currentLanguage) {

		binding.codeView.setTypeface(Typeface.createFromAsset(ctx.getAssets(), "fonts/sourcecodeproregular.ttf"));

		// Setup Line number feature
		binding.codeView.setEnableLineNumber(true);
		binding.codeView.setLineNumberTextColor(Color.GRAY);
		binding.codeView.setLineNumberTextSize(44f);

		// Setup Auto indenting feature
		binding.codeView.setTabLength(4);
		binding.codeView.setEnableAutoIndentation(true);

		// Setup the language and theme with SyntaxManager helper class
		languageManager = new LanguageManager(this, binding.codeView);
		languageManager.applyTheme(currentLanguage, currentTheme);

		// Setup auto pair complete
		final Map<Character, Character> pairCompleteMap = new HashMap<>();
		pairCompleteMap.put('{', '}');
		pairCompleteMap.put('[', ']');
		pairCompleteMap.put('(', ')');
		pairCompleteMap.put('<', '>');
		pairCompleteMap.put('"', '"');
		pairCompleteMap.put('\'', '\'');

		binding.codeView.setPairCompleteMap(pairCompleteMap);
		binding.codeView.enablePairComplete(true);
		binding.codeView.enablePairCompleteCenterCursor(true);

		// Setup the auto complete and auto indenting for the current language
		configLanguageAutoComplete();
		configLanguageAutoIndentation();
	}

	private void configLanguageAutoComplete() {

		boolean useModernAutoCompleteAdapter = true;
		if (useModernAutoCompleteAdapter) {
			List<Code> codeList = languageManager.getLanguageCodeList(currentLanguage);

			CustomCodeViewAdapter adapter = new CustomCodeViewAdapter(this, codeList);

			binding.codeView.setAdapter(adapter);
		}
		else {
			String[] languageKeywords = languageManager.getLanguageKeywords(currentLanguage);

			final int layoutId = R.layout.list_item_suggestion;

			final int viewId = R.id.suggestItemTextView;
			ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layoutId, viewId, languageKeywords);

			binding.codeView.setAdapter(adapter);
		}
	}

	private void configLanguageAutoIndentation() {
		binding.codeView.setIndentationStarts(languageManager.getLanguageIndentationStarts(currentLanguage));
		binding.codeView.setIndentationEnds(languageManager.getLanguageIndentationEnds(currentLanguage));
	}

	private void configCodeViewPlugins() {
		configLanguageName();

		binding.sourcePosition.setText(getString(R.string.sourcePosition, 0, 0));
		configSourcePositionListener();
	}

	private void configLanguageName() {
		binding.languageName.setText(currentLanguage.name().toLowerCase());
	}

	private void configSourcePositionListener() {
		SourcePositionListener sourcePositionListener = new SourcePositionListener(binding.codeView);
		sourcePositionListener.setOnPositionChanged((line, column) -> {
			binding.sourcePosition.setText(getString(R.string.sourcePosition, line, column));
		});
	}
}