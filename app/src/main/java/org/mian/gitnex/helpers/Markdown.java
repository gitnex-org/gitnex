package org.mian.gitnex.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spanned;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.InlineParserFactory;
import org.commonmark.parser.Parser;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.core.MainGrammarLocator;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.SoftBreakAddsNewLinePlugin;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TableAwareMovementMethod;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.picasso.PicassoImagesPlugin;
import io.noties.markwon.inlineparser.InlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParser;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.movement.MovementMethodPlugin;
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.SimpleEntry;
import io.noties.markwon.recycler.table.TableEntry;
import io.noties.markwon.recycler.table.TableEntryPlugin;
import de.qwerty287.markwonprism4j.Prism4jTheme;
import de.qwerty287.markwonprism4j.Prism4jThemeDarkula;
import de.qwerty287.markwonprism4j.Prism4jThemeDefault;
import de.qwerty287.markwonprism4j.SyntaxHighlightPlugin;
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
	private static final int MAX_CLAIM_TIMEOUT_SECONDS = 120;

	private static final Timeout timeout = new Timeout(MAX_CLAIM_TIMEOUT_SECONDS, TimeUnit.SECONDS);

	private static final ExecutorService executorService =
		new ThreadPoolExecutor(MAX_POOL_SIZE / 2, MAX_POOL_SIZE, MAX_THREAD_KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new SynchronousQueue<>());

	private static final Pool<Renderer> rendererPool;
	private static final Pool<RecyclerViewRenderer> rvRendererPool;

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

		Config<RecyclerViewRenderer> configRv = new Config<>();

		configRv.setBackgroundExpirationEnabled(true);
		configRv.setPreciseLeakDetectionEnabled(true);
		configRv.setSize(MAX_POOL_SIZE);
		configRv.setAllocator(new Allocator<RecyclerViewRenderer>() {

			@Override
			public RecyclerViewRenderer allocate(Slot slot) {
				return new RecyclerViewRenderer(slot);
			}

			@Override public void deallocate(RecyclerViewRenderer poolable) {}

		});

		rvRendererPool = new BlazePool<>(configRv);

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

	public static void render(Context context, String markdown, RecyclerView recyclerView) {

		try {
			RecyclerViewRenderer renderer = rvRendererPool.claim(timeout);

			if(renderer != null) {
				renderer.setParameters(context, markdown, recyclerView);
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
				.usePlugin(SoftBreakAddsNewLinePlugin.create())
				.usePlugin(TablePlugin.create(context))
				.usePlugin(MovementMethodPlugin.create(TableAwareMovementMethod.create()))
				.usePlugin(TaskListPlugin.create(context))
				.usePlugin(StrikethroughPlugin.create())
				.usePlugin(PicassoImagesPlugin.create(PicassoService.getInstance(context).get()))
				.usePlugin(SyntaxHighlightPlugin.create(new Prism4j(MainGrammarLocator.getInstance()), prism4jTheme, MainGrammarLocator.DEFAULT_FALLBACK_LANGUAGE))
				.usePlugin(new AbstractMarkwonPlugin() {

					private Typeface tf;

					private void setupTf(Context context) {
						switch(TinyDB.getInstance(context).getInt("customFontId", -1)) {
							case 0:
								tf = Typeface.createFromAsset(context.getAssets(), "fonts/roboto.ttf");
								break;
							case 2:
								tf = Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf");
								break;
							default:
								tf = Typeface.createFromAsset(context.getAssets(), "fonts/manroperegular.ttf");
								break;
						}
					}

					@Override
					public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
						if(tf == null) setupTf(textView.getContext());
						textView.setTypeface(tf);
						super.beforeSetText(textView, markdown);
					}

					@Override
					public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
						builder.codeBlockTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf"));
						builder.codeBlockMargin((int) (context.getResources().getDisplayMetrics().density * 10));
						builder.blockMargin((int) (context.getResources().getDisplayMetrics().density * 10));
						builder.codeTextSize((int) (context.getResources().getDisplayMetrics().scaledDensity * 13));
						builder.codeTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf"));
						builder.linkColor(ResourcesCompat.getColor(context.getResources(), R.color.lightBlue, null));

						if(tf == null) setupTf(context);
						builder.headingTypeface(tf);
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

	private static class RecyclerViewRenderer implements Runnable, Poolable {

		private final Slot slot;

		private Markwon markwon;

		private Context context;
		private String markdown;
		private RecyclerView recyclerView;
		private MarkwonAdapter adapter;

		public RecyclerViewRenderer(Slot slot) {
			this.slot = slot;
		}

		private void setup() {

			Objects.requireNonNull(context);

			Prism4jTheme prism4jTheme = TinyDB.getInstance(context).getString("currentTheme").equals("dark") ?
				Prism4jThemeDarkula.create() :
				Prism4jThemeDefault.create();

			final InlineParserFactory inlineParserFactory = MarkwonInlineParser.factoryBuilder()
				.addInlineProcessor(new IssueInlineProcessor(context))
				.addInlineProcessor(new UserInlineProcessor(context))
				.build();

			Markwon.Builder builder = Markwon.builder(context)
				.usePlugin(CorePlugin.create())
				.usePlugin(HtmlPlugin.create())
				.usePlugin(LinkifyPlugin.create(true)) // TODO not working
				.usePlugin(SoftBreakAddsNewLinePlugin.create())
				.usePlugin(TableEntryPlugin.create(context))
				.usePlugin(MovementMethodPlugin.create(TableAwareMovementMethod.create()))
				.usePlugin(TaskListPlugin.create(context))
				.usePlugin(StrikethroughPlugin.create())
				.usePlugin(PicassoImagesPlugin.create(PicassoService.getInstance(context).get()))
				.usePlugin(SyntaxHighlightPlugin.create(new Prism4j(MainGrammarLocator.getInstance()), prism4jTheme, MainGrammarLocator.DEFAULT_FALLBACK_LANGUAGE))
				.usePlugin(new AbstractMarkwonPlugin() {

					private Typeface tf;

					private void setupTf(Context context) {
						switch(TinyDB.getInstance(context).getInt("customFontId", -1)) {
							case 0:
								tf = Typeface.createFromAsset(context.getAssets(), "fonts/roboto.ttf");
								break;
							case 2:
								tf = Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf");
								break;
							default:
								tf = Typeface.createFromAsset(context.getAssets(), "fonts/manroperegular.ttf");
								break;
						}
					}

					@Override
					public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
						if(tf == null) setupTf(textView.getContext());
						textView.setTypeface(tf);
						super.beforeSetText(textView, markdown);
					}

					@Override
					public void configureParser(@NonNull Parser.Builder builder) {
						builder.inlineParserFactory(inlineParserFactory);
					}

					@Override
					public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
						builder.codeBlockTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf"));
						builder.codeBlockMargin((int) (context.getResources().getDisplayMetrics().density * 10));
						builder.blockMargin((int) (context.getResources().getDisplayMetrics().density * 10));
						builder.codeTextSize((int) (context.getResources().getDisplayMetrics().scaledDensity * 13));
						builder.codeTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf"));
						builder.linkColor(ResourcesCompat.getColor(context.getResources(), R.color.lightBlue, null));

						if(tf == null) setupTf(context);
						builder.headingTypeface(Typeface.create(tf, Typeface.BOLD));
					}
				});

			markwon = builder.build();
		}

		private void setupAdapter() {
			adapter = MarkwonAdapter.builderTextViewIsRoot(R.layout.custom_markdown_adapter)
				.include(TableBlock.class, TableEntry.create(builder2 -> builder2
					.tableLayout(R.layout.custom_markdown_table, R.id.table_layout)
					.textLayoutIsRoot(R.layout.custom_markdown_adapter)))
				.include(FencedCodeBlock.class, SimpleEntry.create(R.layout.custom_markdown_code_block, R.id.textCodeBlock))
				.build();
		}

		public void setParameters(Context context, String markdown, RecyclerView recyclerView) {
			TinyDB tinyDB = TinyDB.getInstance(context);
			String instanceUrl = tinyDB.getString("instanceUrl");
			instanceUrl = instanceUrl.substring(0, instanceUrl.lastIndexOf("api/v1/")).replaceAll("\\.", "\\.");

			// first step: replace comment urls with {url without comment} (comment)
			final Pattern patternComment = Pattern.compile("((?<!]\\(|`)" + instanceUrl + "[^/]+/[^/]+/(?:issues|pulls)/\\d+)(?:/#|#)issuecomment-(\\d+)(?!`|\\)|\\S+)", Pattern.MULTILINE);
			final Matcher matcherComment = patternComment.matcher(markdown);
			markdown = matcherComment.replaceAll("$1 ([" + context.getString(R.string.commentButtonText) + "]($1#issuecomment-$2))");

			// second step: remove links to issue descriptions
			final Pattern patternIssueDesc = Pattern.compile("((?<!]\\(|`)" + instanceUrl + "[^/]+/[^/]+/(?:issues|pulls)/\\d+)(?:/#|#)issue-(\\d+)(?!`|\\)|\\S+)", Pattern.MULTILINE);
			final Matcher matcherIssueDesc = patternIssueDesc.matcher(markdown);
			markdown = matcherIssueDesc.replaceAll("$1");

			// third step: replace issue links from the same repo
			final Pattern pattern = Pattern.compile("(?<!]\\(|`)" + instanceUrl + tinyDB.getString("repoFullName") + "/(?:issues|pulls)/(\\d+)(?!`|\\)|\\S+)", Pattern.MULTILINE);
			final Matcher matcher = pattern.matcher(markdown);
			markdown = matcher.replaceAll("#$1");

			// fourth step: replace issue links from other repos
			String substOtherRepo =
				"[$2/$3#$4](" + instanceUrl.replace("http://", "gitnex://").replace("http://", "gitnex://") + "$1)";
			final Pattern patternOtherRepo = Pattern.compile("(?<!]\\(|`)" + instanceUrl + "(([^/]+)/([^/]+)/(?:issues|pulls)/(\\d+))(?!`|\\)|\\S+)", Pattern.MULTILINE);
			final Matcher matcherOtherRepo = patternOtherRepo.matcher(markdown);
			markdown = matcherOtherRepo.replaceAll(substOtherRepo);

			// fifth step: render commit links
			String substCommit =
				"[$2](" + instanceUrl.replace("http://", "gitnex://").replace("http://", "gitnex://") + "$1)";
			final Pattern patternCommit = Pattern.compile("(?<!]\\(|`)" + instanceUrl + "([^/]+/[^/]+/commit/([a-z0-9_]+))(?!`|\\)|\\S+)", Pattern.MULTILINE);
			final Matcher matcherCommit = patternCommit.matcher(markdown);
			markdown = matcherCommit.replaceAll(substCommit);

			// sixth step: replace relative attachment links
			String substAttachments =
				instanceUrl + tinyDB.getString("repoFullName") + "/$1";
			final Pattern patternAttachments = Pattern.compile("(?<=\\()/(attachments/\\S+)(?=\\))", Pattern.MULTILINE); // TODO code block ``
			final Matcher matcherAttachments = patternAttachments.matcher(markdown);
			markdown = matcherAttachments.replaceAll(substAttachments);

			this.context = context;
			this.markdown = markdown;
			this.recyclerView = recyclerView;
		}

		@Override
		public void run() {

			Objects.requireNonNull(context);
			Objects.requireNonNull(markdown);
			Objects.requireNonNull(recyclerView);

			if(markwon == null) setup();

			setupAdapter();

			RecyclerView localReference = recyclerView;
			String localMd = markdown;
			MarkwonAdapter localAdapter = adapter;
			localReference.post(() -> {
				localReference.setLayoutManager(new LinearLayoutManager(context) {
					@Override
					public boolean canScrollVertically() {
						return false; // disable RecyclerView scrolling, handeled by seperate ScrollViews
					}
				});
				localReference.setAdapter(localAdapter);

				localAdapter.setMarkdown(markwon, localMd);
				localAdapter.notifyDataSetChanged();
			});

			release();

		}

		@Override
		public void release() {

			context = null;
			markdown = null;
			recyclerView = null;
			adapter = null;

			slot.release(this);

		}

		public void expire() {
			slot.expire(this);
		}
	}

	private static class IssueInlineProcessor extends InlineProcessor {

		private final Context context;

		public IssueInlineProcessor(Context context) {
			this.context = context;
		}

		private static final Pattern RE = Pattern.compile("(?<=#)\\d+");

		@Override
		public char specialCharacter() {
			return '#';
		}

		@Override
		protected Node parse() {
			final String id = match(RE);
			if (id != null) {
				final Link link = new Link(createIssueOrPullRequestLinkDestination(id, context), null);
				link.appendChild(text("#" + id));
				return link;
			}
			return null;
		}

		@NonNull
		private static String createIssueOrPullRequestLinkDestination(@NonNull String id, Context context) {
			String instanceUrl = TinyDB.getInstance(context).getString("instanceUrl");
			instanceUrl = instanceUrl.substring(0, instanceUrl.lastIndexOf("api/v1/"));
			instanceUrl = instanceUrl.replace("http://", "gitnex://");
			instanceUrl = instanceUrl.replace("https://", "gitnex://");

			return instanceUrl + TinyDB.getInstance(context).getString("repoFullName") + "/issues/" + id;
		}
	}

	private static class UserInlineProcessor extends InlineProcessor {

		private final Context context;

		public UserInlineProcessor(Context context) {
			this.context = context;
		}

		private static final Pattern RE = Pattern.compile("(?<!\\S)(?<=@)\\w+");

		@Override
		public char specialCharacter() {
			return '@';
		}

		@Override
		protected Node parse() {
			final String user = match(RE);
			if (user != null) {
				final Link link = new Link(createUserLinkDestination(user, context), null);
				link.appendChild(text("@" + user));
				return link;
			}
			return null;
		}

		@NonNull
		private static String createUserLinkDestination(@NonNull String user, Context context) {
			String instanceUrl = TinyDB.getInstance(context).getString("instanceUrl");
			instanceUrl = instanceUrl.substring(0, instanceUrl.lastIndexOf("api/v1/"));
			instanceUrl = instanceUrl.replace("http://", "gitnex://");
			instanceUrl = instanceUrl.replace("https://", "gitnex://");

			return instanceUrl + user;
		}
	}
}
