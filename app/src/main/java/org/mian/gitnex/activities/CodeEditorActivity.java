package org.mian.gitnex.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import com.amrdeveloper.codeview.Code;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mian.gitnex.R;
import org.mian.gitnex.core.MainGrammarLocator;
import org.mian.gitnex.databinding.ActivityCodeEditorBinding;
import org.mian.gitnex.helpers.codeeditor.CustomCodeViewAdapter;
import org.mian.gitnex.helpers.codeeditor.SourcePositionListener;
import org.mian.gitnex.helpers.codeeditor.languages.Language;
import org.mian.gitnex.helpers.codeeditor.languages.UnknownLanguage;
import org.mian.gitnex.helpers.codeeditor.theme.Theme;

/**
 * @author AmrDeveloper
 * @author M M Arif
 */
public class CodeEditorActivity extends BaseActivity {

	private Theme currentTheme;
	private ActivityCodeEditorBinding binding;
	private Language currentLanguage = new UnknownLanguage();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityCodeEditorBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.close.setOnClickListener(
				view -> {
					sendResults();
					finish();
				});

		String fileContent = getIntent().getStringExtra("fileContent");
		String fileExtension;
		currentTheme = Theme.getDefaultTheme(this);

		if (getIntent().getStringExtra("fileExtension") != null) {
			fileExtension =
					MainGrammarLocator.fromExtension(getIntent().getStringExtra("fileExtension"));

			currentLanguage = Language.fromName(fileExtension);
		}

		configCodeView(currentLanguage, fileContent);
		configCodeViewPlugins();
	}

	private void sendResults() {
		Intent intent = new Intent();
		intent.putExtra("fileContentFromActivity", binding.codeView.getText().toString());
		setResult(Activity.RESULT_OK, intent);
	}

	private void configCodeView(Language currentLanguage, String fileContent) {

		binding.codeView.setTypeface(
				Typeface.createFromAsset(ctx.getAssets(), "fonts/sourcecodeproregular.ttf"));

		// Setup Line number feature
		binding.codeView.setEnableLineNumber(true);
		binding.codeView.setLineNumberTextColor(Color.GRAY);
		binding.codeView.setLineNumberTextSize(32f);

		// Setup Auto indenting feature
		binding.codeView.setTabLength(4);
		binding.codeView.setEnableAutoIndentation(true);

		// Set up the language and theme with SyntaxManager helper class
		currentLanguage.applyTheme(this, binding.codeView, currentTheme);

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
		binding.codeView.setText(fileContent);

		// Set up the auto complete and auto indenting for the current language
		configLanguageAutoComplete();
		configLanguageAutoIndentation();
	}

	private void configLanguageAutoComplete() {

		boolean useModernAutoCompleteAdapter = true;
		if (useModernAutoCompleteAdapter) {
			List<Code> codeList = currentLanguage.getCodeList();

			CustomCodeViewAdapter adapter = new CustomCodeViewAdapter(this, codeList);

			binding.codeView.setAdapter(adapter);
		} else {
			String[] languageKeywords = currentLanguage.getKeywords();

			final int layoutId = R.layout.list_item_suggestion;

			final int viewId = R.id.suggestItemTextView;
			ArrayAdapter<String> adapter =
					new ArrayAdapter<>(this, layoutId, viewId, languageKeywords);

			binding.codeView.setAdapter(adapter);
		}
	}

	private void configLanguageAutoIndentation() {
		binding.codeView.setIndentationStarts(currentLanguage.getIndentationStarts());
		binding.codeView.setIndentationEnds(currentLanguage.getIndentationEnds());
	}

	private void configCodeViewPlugins() {
		configLanguageName();

		binding.sourcePosition.setText(getString(R.string.sourcePosition, 0, 0));
		configSourcePositionListener();
	}

	private void configLanguageName() {
		binding.languageName.setText(currentLanguage.getName().toLowerCase());
	}

	private void configSourcePositionListener() {
		SourcePositionListener sourcePositionListener =
				new SourcePositionListener(binding.codeView);
		sourcePositionListener.setOnPositionChanged(
				(line, column) -> {
					binding.sourcePosition.setText(
							getString(R.string.sourcePosition, line, column));
				});
	}
}
