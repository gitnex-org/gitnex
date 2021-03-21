package org.mian.gitnex.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spanned;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.core.MainGrammarLocator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.picasso.PicassoImagesPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.syntax.Prism4jTheme;
import io.noties.markwon.syntax.Prism4jThemeDarkula;
import io.noties.markwon.syntax.Prism4jThemeDefault;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import io.noties.prism4j.Prism4j;

/**
 * @author opyale
 */

public class Markdown {

	private static final ExecutorService executorService = Executors.newCachedThreadPool();

	private final Context context;
	private final String markdown;
	private final TextView textView;

	public Markdown(@NonNull Context context, @NonNull String markdown, @NonNull TextView textView) {

		this.context = context;
		this.markdown = markdown;
		this.textView = textView;

		executorService.execute(new Renderer());

	}

	private class Renderer implements Runnable {

		@Override
		public void run() {

			Prism4jTheme prism4jTheme = TinyDB.getInstance(context).getString("currentTheme").equals("dark") ?
				Prism4jThemeDarkula.create() :
				Prism4jThemeDefault.create();

			Markwon.Builder builder = Markwon.builder(context)
				.usePlugin(CorePlugin.create())
				.usePlugin(HtmlPlugin.create())
				.usePlugin(LinkifyPlugin.create(true))
				.usePlugin(TablePlugin.create(context))
				.usePlugin(TaskListPlugin.create(context))
				.usePlugin(StrikethroughPlugin.create())
				.usePlugin(PicassoImagesPlugin.create(PicassoService.getInstance(context).get()))
				.usePlugin(SyntaxHighlightPlugin.create(new Prism4j(MainGrammarLocator.getInstance()), prism4jTheme, MainGrammarLocator.DEFAULT_FALLBACK_LANGUAGE))
				.usePlugin(new AbstractMarkwonPlugin() {

					@Override
					public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
						builder.codeBlockTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf"));
						builder.codeTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf"));
						builder.linkColor(ResourcesCompat.getColor(context.getResources(), R.color.lightBlue, null));
					}
				});

			Markwon markwon = builder.build();
			Spanned spanned = markwon.toMarkdown(markdown);

			textView.post(() -> markwon.setParsedMarkdown(textView, spanned));

		}
	}
}
