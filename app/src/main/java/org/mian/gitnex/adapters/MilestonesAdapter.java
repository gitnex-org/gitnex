package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

        private MilestonesViewHolder(View itemView) {
            super(itemView);

            msTitle = itemView.findViewById(R.id.milestoneTitle);
            msStatus = itemView.findViewById(R.id.milestoneState);
            msDescription = itemView.findViewById(R.id.milestoneDescription);
            msOpenIssues = itemView.findViewById(R.id.milestoneIssuesOpen);
            msClosedIssues = itemView.findViewById(R.id.milestoneIssuesClosed);
            msDueDate = itemView.findViewById(R.id.milestoneDueDate);

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
                .usePlugin(OkHttpImagesPlugin.create(new OkHttpClient()))
                .usePlugin(ImagesPlugin.createWithAssets(mCtx))
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
                .usePlugin(GifPlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .build();

        holder.msTitle.setText(currentItem.getTitle());
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
