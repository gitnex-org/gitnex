package org.mian.gitnex.helpers.highlightjs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.mian.gitnex.helpers.highlightjs.models.Language;
import org.mian.gitnex.helpers.highlightjs.models.Theme;
import org.mian.gitnex.helpers.highlightjs.utils.SourceUtils;

/**
 * This Class was created by Patrick J
 * on 09.06.16. (modified by opyale)
 */

public class HighlightJsView extends WebView {

    private Language language = Language.AUTO_DETECT;
    private Theme theme = Theme.DEFAULT;

	private boolean zoomSupport = false;
    private boolean showLineNumbers = true;
    private TextWrap textWrap = TextWrap.NO_WRAP;

    public HighlightJsView(Context context) {

        super(context);
        setup();
    }

    public HighlightJsView(Context context, AttributeSet attrs) {

        super(context, attrs);
        setup();
    }

    public HighlightJsView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        setup();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setup() {

        WebSettings settings = getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(zoomSupport);
        settings.setDisplayZoomControls(false);

        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

    }

	private void changeZoomSettings(boolean enable) {

		this.zoomSupport = enable;
		getSettings().setSupportZoom(enable);
	}

    public void setSource(String source) {

        source = (source == null) ? " " : source;

        String html_content = SourceUtils.generateContent(source, theme.getName(), language.getName(), zoomSupport, showLineNumbers, textWrap);
        loadDataWithBaseURL("file:///android_asset/", html_content, "text/html", "utf-8", null);

    }

    public void refresh() {

        super.reload();
    }

    public void setHighlightLanguage(Language language) {
        this.language = language;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

	public void setTextWrap(TextWrap textWrap) {
    	this.textWrap = textWrap;
    }

    public Language getHighlightLanguage() {
        return language;
    }

    public Theme getTheme() {
        return theme;
    }

	public void setZoomSupportEnabled(boolean supportZoom) {
		changeZoomSettings(supportZoom);
	}

	public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
    }

	public enum TextWrap {
		NO_WRAP, WORD_WRAP, BREAK_ALL
	}

}