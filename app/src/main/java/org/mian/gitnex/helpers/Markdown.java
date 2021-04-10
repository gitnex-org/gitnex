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
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
import stormpot.Allocator;
import stormpot.BlazePool;
import stormpot.Config;
import stormpot.Pool;
import stormpot.Poolable;
import stormpot.Slot;
import stormpot.Timeout;

/**
 * @author opyale
 */

public class Markdown {

	private static final int MAX_POOL_SIZE = 45;
	private static final int MAX_THREAD_KEEP_ALIVE_SECONDS = 120;
	private static final int MAX_CLAIM_TIMEOUT_SECONDS = 5;

	private static final Timeout timeout = new Timeout(MAX_CLAIM_TIMEOUT_SECONDS, TimeUnit.SECONDS);

	private static final ExecutorService executorService =
		new ThreadPoolExecutor(MAX_POOL_SIZE / 2, MAX_POOL_SIZE, MAX_THREAD_KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new SynchronousQueue<>());

	private static final Pool<Renderer> rendererPool;

	static {

		Config<Renderer> config = new Config<>();

		config.setBackgroundExpirationEnabled(true);
		config.setPreciseLeakDetectionEnabled(true);
		config.setSize(MAX_POOL_SIZE);
		config.setAllocator(new Allocator<Renderer>() {

			@Override
			public Renderer allocate(Slot slot) throws Exception {
				return new Renderer(slot);
			}

			@Override public void deallocate(Renderer poolable) throws Exception {}

		});

		rendererPool = new BlazePool<>(config);

	}

	public static void render(Context context, String markdown, TextView textView) {

		try {
			Renderer renderer = rendererPool.claim(timeout);

			if(renderer != null) {
				renderer.setParameters(context, markdown, textView);
				executorService.execute(renderer);
			}
		} catch(InterruptedException ignored) {}
	}

	private static class Renderer implements Runnable, Poolable {

		private final Slot slot;

		private Markwon markwon;

		private Context context;
		private String markdown;
		private TextView textView;

		public Renderer(Slot slot) {
			this.slot = slot;
		}

		private void setup() {

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
						builder.codeBlockMargin((int) (context.getResources().getDisplayMetrics().density * 10));
						builder.blockMargin((int) (context.getResources().getDisplayMetrics().density * 10));
						builder.codeTextSize((int) (context.getResources().getDisplayMetrics().scaledDensity * 13));
						builder.codeTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf"));
						builder.linkColor(ResourcesCompat.getColor(context.getResources(), R.color.lightBlue, null));
					}
				});

			markwon = builder.build();

		}

		public void setParameters(Context context, String markdown, TextView textView) {

			this.context = context;
			this.markdown = markdown;
			this.textView = textView;
		}

		@Override
		public void run() {

			Objects.requireNonNull(context);
			Objects.requireNonNull(markdown);
			Objects.requireNonNull(textView);

			if(markwon == null) setup();

			Spanned processedMarkdown = markwon.toMarkdown(markdown);

			TextView localReference = textView;
			localReference.post(() -> localReference.setText(processedMarkdown));

			release();

		}

		@Override
		public void release() {

			context = null;
			markdown = null;
			textView = null;

			slot.release(this);

		}

		public void expire() {
			slot.expire(this);
		}
	}
}
