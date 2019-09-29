package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.models.Milestones;
import org.mian.gitnex.util.TinyDB;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

public class MilestonesAdapter extends RecyclerView.Adapter<MilestonesAdapter.MilestonesViewHolder> implements Filterable {

    private List<Milestones> milestonesList;
    private Context mCtx;
    private List<Milestones> milestonesListFull;

    static class MilestonesViewHolder extends RecyclerView.ViewHolder {

        private TextView msTitle;
        private TextView msDescription;
        private TextView msOpenIssues;
        private TextView msClosedIssues;
        private TextView msDueDate;
        private ImageView msStatus;
        private ProgressBar msProgress;

        private MilestonesViewHolder(View itemView) {
            super(itemView);

            msTitle = itemView.findViewById(R.id.milestoneTitle);
            msStatus = itemView.findViewById(R.id.milestoneState);
            msDescription = itemView.findViewById(R.id.milestoneDescription);
            msOpenIssues = itemView.findViewById(R.id.milestoneIssuesOpen);
            msClosedIssues = itemView.findViewById(R.id.milestoneIssuesClosed);
            msDueDate = itemView.findViewById(R.id.milestoneDueDate);
            msProgress = itemView.findViewById(R.id.milestoneProgress);

            /*issueTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();
                    Log.i("issueNumber", issueNumber.getText().toString());

                    Intent intent = new Intent(context, IssueDetailActivity.class);
                    intent.putExtra("issueNumber", issueNumber.getText());

                    TinyDB tinyDb = new TinyDB(context);
                    tinyDb.putString("issueNumber", issueNumber.getText().toString());
                    context.startActivity(intent);

                }
            });*/
        }
    }

    public MilestonesAdapter(Context mCtx, List<Milestones> milestonesMain) {
        this.mCtx = mCtx;
        this.milestonesList = milestonesMain;
        milestonesListFull = new ArrayList<>(milestonesList);
    }

    @NonNull
    @Override
    public MilestonesAdapter.MilestonesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.milestones_list, parent, false);
        return new MilestonesAdapter.MilestonesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MilestonesAdapter.MilestonesViewHolder holder, int position) {

        final TinyDB tinyDb = new TinyDB(mCtx);
        final String locale = tinyDb.getString("locale");
        final String timeFormat = tinyDb.getString("dateFormat");

        Milestones currentItem = milestonesList.get(position);

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
                .usePlugin(ImagesPlugin.create(new ImagesPlugin.ImagesConfigure() {
                    @Override
                    public void configureImages(@NonNull ImagesPlugin plugin) {
                        plugin.placeholderProvider(new ImagesPlugin.PlaceholderProvider() {
                            @Nullable
                            @Override
                            public Drawable providePlaceholder(@NonNull AsyncDrawable drawable) {
                                return null;
                            }
                        });
                    }
                }))
                .usePlugin(TablePlugin.create(mCtx))
                .usePlugin(TaskListPlugin.create(mCtx))
                .usePlugin(HtmlPlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(LinkifyPlugin.create())
                .build();

        Spanned msTitle = markwon.toMarkdown(currentItem.getTitle());
        markwon.setParsedMarkdown(holder.msTitle, msTitle);
        //holder.msStatus.setText(currentItem.getState());

        if(currentItem.getState().equals("open")) {

            @SuppressLint("ResourceType") int color = Color.parseColor(mCtx.getResources().getString(R.color.releaseStable));
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    //.useFont(Typeface.DEFAULT)
                    .textColor(mCtx.getResources().getColor(R.color.white))
                    .fontSize(30)
                    .toUpperCase()
                    .width(120)
                    .height(60)
                    .endConfig()
                    .buildRoundRect("open", color, 8);

            holder.msStatus.setImageDrawable(drawable);

        }
        else if(currentItem.getState().equals("closed")) {

            @SuppressLint("ResourceType") int color = Color.parseColor(mCtx.getResources().getString(R.color.colorRed));
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    //.useFont(Typeface.DEFAULT)
                    .textColor(mCtx.getResources().getColor(R.color.white))
                    .fontSize(30)
                    .toUpperCase()
                    .width(140)
                    .height(60)
                    .endConfig()
                    .buildRoundRect("closed", color, 8);

            holder.msStatus.setImageDrawable(drawable);

        }

        if (!currentItem.getDescription().equals("")) {
            final CharSequence bodyWithMD = markwon.toMarkdown(EmojiParser.parseToUnicode(currentItem.getDescription()));
            holder.msDescription.setText(bodyWithMD);
        }
        else {
            holder.msDescription.setVisibility(View.GONE);
        }

        holder.msOpenIssues.setText(String.valueOf(currentItem.getOpen_issues()));
        holder.msOpenIssues.setOnClickListener(new ClickListener(mCtx.getResources().getString(R.string.milestoneOpenIssues, currentItem.getOpen_issues()), mCtx));

        holder.msClosedIssues.setText(String.valueOf(currentItem.getClosed_issues()));
        holder.msClosedIssues.setOnClickListener(new ClickListener(mCtx.getResources().getString(R.string.milestoneClosedIssues, currentItem.getClosed_issues()), mCtx));

        if ((currentItem.getOpen_issues() + currentItem.getClosed_issues()) > 0) {
            if (currentItem.getOpen_issues() == 0) {
                holder.msProgress.setProgress(100);
            } else {
                holder.msProgress.setProgress(100*currentItem.getClosed_issues()/(currentItem.getOpen_issues() + currentItem.getClosed_issues()));
            }
        } else {
            holder.msProgress.setProgress(0);
        }

        if(currentItem.getDue_on() != null) {

            if (timeFormat.equals("normal") || timeFormat.equals("pretty")) {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", new Locale(locale));
                Date date = null;
                try {
                    date = formatter.parse(currentItem.getDue_on());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String dueDate = formatter.format(date);
                assert date != null;
                if(date.before(new Date())) {
                    holder.msDueDate.setTextColor(mCtx.getResources().getColor(R.color.darkRed));
                }

                holder.msDueDate.setText(dueDate);
                holder.msDueDate.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToast(currentItem.getDue_on()), mCtx));

            } else if (timeFormat.equals("normal1")) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", new Locale(locale));
                Date date1 = null;
                try {
                    date1 = formatter.parse(currentItem.getDue_on());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String dueDate = formatter.format(date1);
                holder.msDueDate.setText(dueDate);
            }

        }
        else {
            holder.msDueDate.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return milestonesList.size();
    }

    @Override
    public Filter getFilter() {
        return milestoneFilter;
    }

    private Filter milestoneFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Milestones> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(milestonesListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Milestones item : milestonesListFull) {
                    if (item.getTitle().toLowerCase().contains(filterPattern) || item.getDescription().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            milestonesList.clear();
            milestonesList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}
