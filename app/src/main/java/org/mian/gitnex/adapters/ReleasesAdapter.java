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
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.models.Releases;
import org.mian.gitnex.util.TinyDB;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.AsyncDrawable;
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

        private ImageView releaseType;
        private TextView releaseTitle;
        private TextView releaseDescription;
        private TextView releaseDownload;
        private TextView releaseZipDownload;
        private TextView releaseTarDownload;
        private TextView releaseTag;

        private ReleasesViewHolder(View itemView) {
            super(itemView);

            releaseType = itemView.findViewById(R.id.releaseType);
            releaseTitle = itemView.findViewById(R.id.releaseTitle);
            releaseDescription = itemView.findViewById(R.id.releaseDescription);
            releaseZipDownload = itemView.findViewById(R.id.releaseZipDownload);
            releaseTarDownload = itemView.findViewById(R.id.releaseTarDownload);
            releaseTag = itemView.findViewById(R.id.releaseTag);

        }
    }

    public ReleasesAdapter(Context mCtx, List<Releases> releasesMain) {
        this.mCtx = mCtx;
        this.releasesList = releasesMain;
    }

    @NonNull
    @Override
    public ReleasesAdapter.ReleasesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.releases_list, parent, false);
        return new ReleasesAdapter.ReleasesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReleasesAdapter.ReleasesViewHolder holder, int position) {

        final TinyDB tinyDb = new TinyDB(mCtx);

        Releases currentItem = releasesList.get(position);

        holder.releaseTitle.setText(currentItem.getName());

        if(!currentItem.getTag_name().equals("")) {
            holder.releaseTag.setText(mCtx.getResources().getString(R.string.releaseTag, currentItem.getTag_name()));
        }
        else {
            holder.releaseTag.setVisibility(View.GONE);
        }

        if(currentItem.isPrerelease()) {
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    //.useFont(Typeface.DEFAULT)
                    .textColor(mCtx.getResources().getColor(R.color.white))
                    .fontSize(34)
                    .width(260)
                    .height(60)
                    .endConfig()
                    .buildRoundRect(mCtx.getResources().getString(R.string.releaseTypePre), mCtx.getResources().getColor(R.color.releasePre), 8);
            holder.releaseType.setImageDrawable(drawable);
        }
        else {
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    //.useFont(Typeface.DEFAULT)
                    .textColor(mCtx.getResources().getColor(R.color.white))
                    .fontSize(34)
                    .width(260)
                    .height(60)
                    .endConfig()
                    .buildRoundRect(mCtx.getResources().getString(R.string.releaseTypeStable), mCtx.getResources().getColor(R.color.releaseStable), 8);
            holder.releaseType.setImageDrawable(drawable);
        }

        final Markwon markwon = Markwon.builder(Objects.requireNonNull(mCtx))
                .usePlugin(CorePlugin.create())
                .usePlugin(ImagesPlugin.create(new ImagesPlugin.ImagesConfigure() {
                    @Override
                    public void configureImages(@NonNull ImagesPlugin plugin) {
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
                        plugin.placeholderProvider(new ImagesPlugin.PlaceholderProvider() {
                            @Nullable
                            @Override
                            public Drawable providePlaceholder(@NonNull AsyncDrawable drawable) {
                                return null;
                            }
                        });
                        plugin.addMediaDecoder(GifMediaDecoder.create(false));
                        plugin.addMediaDecoder(SvgMediaDecoder.create(mCtx.getResources()));
                        plugin.addMediaDecoder(SvgMediaDecoder.create());
                        plugin.defaultMediaDecoder(DefaultMediaDecoder.create(mCtx.getResources()));
                        plugin.defaultMediaDecoder(DefaultMediaDecoder.create());
                    }
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
            markwon.setParsedMarkdown(holder.releaseDescription, bodyWithMD);
        }
        else {
            holder.releaseDescription.setVisibility(View.GONE);
        }

        holder.releaseZipDownload.setText(
                Html.fromHtml("<a href='" + currentItem.getZipball_url() + "'>" + mCtx.getResources().getString(R.string.zipArchiveDownloadReleasesTab) + "</a> "));
        holder.releaseZipDownload.setMovementMethod(LinkMovementMethod.getInstance());

        holder.releaseTarDownload.setText(
                Html.fromHtml("<a href='" + currentItem.getTarball_url() + "'>" + mCtx.getResources().getString(R.string.tarArchiveDownloadReleasesTab) + "</a> "));
        holder.releaseTarDownload.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    public int getItemCount() {
        return releasesList.size();
    }

}
