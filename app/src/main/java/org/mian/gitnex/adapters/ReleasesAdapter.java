package org.mian.gitnex.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.models.Releases;
import org.mian.gitnex.util.TinyDB;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.DefaultMediaDecoder;
import io.noties.markwon.image.ImageItem;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.SchemeHandler;
import io.noties.markwon.image.gif.GifMediaDecoder;
import io.noties.markwon.image.svg.SvgMediaDecoder;
import io.noties.markwon.linkify.LinkifyPlugin;

/**
 * Author M M Arif
 */

public class ReleasesAdapter extends RecyclerView.Adapter<ReleasesAdapter.ReleasesViewHolder> {

    private List<Releases> releasesList;
    private Context mCtx;

	static class ReleasesViewHolder extends RecyclerView.ViewHolder {

        private TextView releaseType;
        private TextView releaseName;
        private ImageView authorAvatar;
        private TextView authorName;
        private TextView releaseTag;
        private TextView releaseCommitSha;
        private TextView releaseDate;
        private TextView releaseBodyContent;
        private LinearLayout downloadFrame;
        private RelativeLayout downloads;
        private TextView releaseZipDownload;
	    private TextView releaseTarDownload;
	    private ImageView downloadDropdownIcon;
	    private RecyclerView downloadList;

        private ReleasesViewHolder(View itemView) {

            super(itemView);

	        releaseType = itemView.findViewById(R.id.releaseType);
	        releaseName = itemView.findViewById(R.id.releaseName);
	        authorAvatar = itemView.findViewById(R.id.authorAvatar);
	        authorName = itemView.findViewById(R.id.authorName);
	        releaseTag = itemView.findViewById(R.id.releaseTag);
	        releaseCommitSha = itemView.findViewById(R.id.releaseCommitSha);
	        releaseDate = itemView.findViewById(R.id.releaseDate);
	        releaseBodyContent = itemView.findViewById(R.id.releaseBodyContent);
	        downloadFrame = itemView.findViewById(R.id.downloadFrame);
	        downloads = itemView.findViewById(R.id.downloads);
	        releaseZipDownload = itemView.findViewById(R.id.releaseZipDownload);
	        releaseTarDownload = itemView.findViewById(R.id.releaseTarDownload);
	        downloadDropdownIcon = itemView.findViewById(R.id.downloadDropdownIcon);
	        downloadList = itemView.findViewById(R.id.downloadList);

	        downloadList.setHasFixedSize(true);
	        downloadList.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

        }
    }

    public ReleasesAdapter(Context mCtx, List<Releases> releasesMain) {
        this.mCtx = mCtx;
        this.releasesList = releasesMain;
    }

    @NonNull
    @Override
    public ReleasesAdapter.ReleasesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_releases, parent, false);
        return new ReleasesAdapter.ReleasesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReleasesAdapter.ReleasesViewHolder holder, int position) {

        final TinyDB tinyDb = new TinyDB(mCtx);
	    final String locale = tinyDb.getString("locale");
	    final String timeFormat = tinyDb.getString("dateFormat");

        Releases currentItem = releasesList.get(position);

	    holder.releaseName.setText(currentItem.getName());

	    if(currentItem.isPrerelease()) {
		    holder.releaseType.setBackgroundResource(R.drawable.shape_pre_release);
		    holder.releaseType.setText(R.string.releaseTypePre);
	    }
	    else if(currentItem.isDraft()) {
		    holder.releaseType.setVisibility(View.GONE);
	    }
	    else {
		    holder.releaseType.setBackgroundResource(R.drawable.shape_stable_release);
		    holder.releaseType.setText(R.string.releaseTypeStable);
	    }

	    if(currentItem.getAuthor().getAvatar_url() != null) {
		    PicassoService.getInstance(mCtx).get().load(currentItem.getAuthor().getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.authorAvatar);
	    }

	    holder.authorName.setText(mCtx.getResources().getString(R.string.releasePublishedBy, currentItem.getAuthor().getUsername()));

	    if(currentItem.getTag_name() != null) {
	    	holder.releaseTag.setText(currentItem.getTag_name());
	    }

	    if(currentItem.getPublished_at() != null) {
		    holder.releaseDate.setText(TimeHelper.formatTime(currentItem.getPublished_at(), new Locale(locale), timeFormat, mCtx));
	    }

	    if(timeFormat.equals("pretty")) {
		    holder.releaseDate.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getPublished_at()), mCtx));
	    }

        final Markwon markwon = Markwon.builder(Objects.requireNonNull(mCtx))
                .usePlugin(CorePlugin.create())
                .usePlugin(ImagesPlugin.create(plugin -> {
                    plugin.addSchemeHandler(new SchemeHandler() {
                        @NonNull
                        @Override
                        public ImageItem handle(@NonNull String raw, @NonNull Uri uri) {

                            final int resourceId = mCtx.getResources().getIdentifier(
                                    raw.substring("drawable://".length()),
                                    "drawable",
                                    mCtx.getPackageName());

                            final Drawable drawable = mCtx.getDrawable(resourceId);

                            assert drawable != null;
                            return ImageItem.withResult(drawable);
                        }

                        @NonNull
                        @Override
                        public Collection<String> supportedSchemes() {
                            return Collections.singleton("drawable");
                        }
                    });
                    plugin.placeholderProvider(drawable -> null);
                    plugin.addMediaDecoder(GifMediaDecoder.create(false));
                    plugin.addMediaDecoder(SvgMediaDecoder.create(mCtx.getResources()));
                    plugin.addMediaDecoder(SvgMediaDecoder.create());
                    plugin.defaultMediaDecoder(DefaultMediaDecoder.create(mCtx.getResources()));
                    plugin.defaultMediaDecoder(DefaultMediaDecoder.create());
                }))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                        builder
                                .codeTextColor(tinyDb.getInt("codeBlockColor"))
                                .codeBackgroundColor(tinyDb.getInt("codeBlockBackground"))
                                .linkColor(mCtx.getResources().getColor(R.color.lightBlue));
                    }
                })
                .usePlugin(TablePlugin.create(mCtx))
                .usePlugin(TaskListPlugin.create(mCtx))
                .usePlugin(HtmlPlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(LinkifyPlugin.create())
                .build();

        Spanned bodyWithMD = markwon.toMarkdown(EmojiParser.parseToUnicode(currentItem.getBody()));

        if(!currentItem.getBody().equals("")) {
            markwon.setParsedMarkdown(holder.releaseBodyContent, bodyWithMD);
        }
        else {
	        holder.releaseBodyContent.setText(R.string.noReleaseBodyContent);
        }

	    holder.downloadFrame.setOnClickListener(v -> {

		    if(holder.downloads.getVisibility() == View.GONE) {

			    holder.downloadDropdownIcon.setImageResource(R.drawable.ic_arrow_down);
			    holder.downloads.setVisibility(View.VISIBLE);

		    }
		    else {

			    holder.downloadDropdownIcon.setImageResource(R.drawable.ic_arrow_right);
			    holder.downloads.setVisibility(View.GONE);

		    }

	    });

        holder.releaseZipDownload.setText(
                Html.fromHtml("<a href='" + currentItem.getZipball_url() + "'>" + mCtx.getResources().getString(R.string.zipArchiveDownloadReleasesTab) + "</a> "));
        holder.releaseZipDownload.setMovementMethod(LinkMovementMethod.getInstance());

        holder.releaseTarDownload.setText(
                Html.fromHtml("<a href='" + currentItem.getTarball_url() + "'>" + mCtx.getResources().getString(R.string.tarArchiveDownloadReleasesTab) + "</a> "));
        holder.releaseTarDownload.setMovementMethod(LinkMovementMethod.getInstance());

	    ReleasesDownloadsAdapter adapter = new ReleasesDownloadsAdapter(currentItem.getAssets());
	    holder.downloadList.setAdapter(adapter);

    }

    @Override
    public int getItemCount() {
        return releasesList.size();
    }

}
