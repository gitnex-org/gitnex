package org.mian.gitnex.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.models.Releases;
import java.util.List;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.OkHttpClient;
import ru.noties.markwon.AbstractMarkwonPlugin;
import ru.noties.markwon.Markwon;
import ru.noties.markwon.core.CorePlugin;
import ru.noties.markwon.core.MarkwonTheme;
import ru.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import ru.noties.markwon.ext.tables.TablePlugin;
import ru.noties.markwon.ext.tables.TableTheme;
import ru.noties.markwon.ext.tasklist.TaskListPlugin;
import ru.noties.markwon.html.HtmlPlugin;
import ru.noties.markwon.image.ImagesPlugin;
import ru.noties.markwon.image.gif.GifPlugin;
import ru.noties.markwon.image.okhttp.OkHttpImagesPlugin;

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

        private ReleasesViewHolder(View itemView) {
            super(itemView);

            releaseType = itemView.findViewById(R.id.releaseType);
            releaseTitle = itemView.findViewById(R.id.releaseTitle);
            releaseDescription = itemView.findViewById(R.id.releaseDescription);
            releaseZipDownload = itemView.findViewById(R.id.releaseZipDownload);
            releaseTarDownload = itemView.findViewById(R.id.releaseTarDownload);

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

        Releases currentItem = releasesList.get(position);

        holder.releaseTitle.setText(currentItem.getName());

        if(currentItem.isPrerelease()) {
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    //.useFont(Typeface.DEFAULT)
                    .textColor(mCtx.getResources().getColor(R.color.white))
                    .fontSize(36)
                    .width(240)
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
                    .fontSize(36)
                    .width(240)
                    .height(60)
                    .endConfig()
                    .buildRoundRect(mCtx.getResources().getString(R.string.releaseTypeStable), mCtx.getResources().getColor(R.color.releaseStable), 8);
            holder.releaseType.setImageDrawable(drawable);
        }

        final TableTheme tableTheme = TableTheme.buildWithDefaults(Objects.requireNonNull(mCtx))
                .tableBorderWidth(1)
                .tableCellPadding(0)
                .build();

        final Markwon markwon = Markwon.builder(Objects.requireNonNull(mCtx))
                .usePlugin(CorePlugin.create())
                .usePlugin(OkHttpImagesPlugin.create(new OkHttpClient()))
                .usePlugin(ImagesPlugin.createWithAssets(mCtx))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                        builder
                                .codeTextColor(Color.GREEN)
                                .codeBackgroundColor(Color.BLACK)
                                .linkColor(mCtx.getResources().getColor(R.color.lightBlue));
                    }
                })
                .usePlugin(TablePlugin.create(tableTheme))
                .usePlugin(TaskListPlugin.create(mCtx))
                .usePlugin(HtmlPlugin.create())
                .usePlugin(GifPlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .build();

        final CharSequence bodyWithMD = markwon.toMarkdown(EmojiParser.parseToUnicode(currentItem.getBody()));
        holder.releaseDescription.setText(bodyWithMD);
        holder.releaseZipDownload.setText(currentItem.getZipball_url());
        holder.releaseTarDownload.setText(currentItem.getTarball_url());

    }

    @Override
    public int getItemCount() {
        return releasesList.size();
    }

}
