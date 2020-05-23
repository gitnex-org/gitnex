package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.MilestoneActions;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.models.Milestones;
import org.mian.gitnex.util.TinyDB;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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

public class MilestonesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private final int TYPE_LOAD = 0;
    private List<Milestones> dataList;
    private OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;
    private String TAG = StaticGlobalVariables.tagMilestonesAdapter;

    public MilestonesAdapter(Context context, List<Milestones> dataListMain) {

        this.context = context;
        this.dataList = dataListMain;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);

        if(viewType == TYPE_LOAD) {
            return new MilestonesAdapter.DataHolder(inflater.inflate(R.layout.list_milestones, parent, false));
        }
        else {
            return new MilestonesAdapter.LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {

            isLoading = true;
            loadMoreListener.onLoadMore();

        }

        if(getItemViewType(position) == TYPE_LOAD) {

            ((MilestonesAdapter.DataHolder) holder).bindData(dataList.get(position));

        }

    }

    class DataHolder extends RecyclerView.ViewHolder {

        private TextView milestoneId;
        private TextView msTitle;
        private TextView msDescription;
        private TextView msOpenIssues;
        private TextView msClosedIssues;
        private TextView msDueDate;
        private ImageView msStatus;
        private ProgressBar msProgress;
        private TextView milestoneStatus;

        DataHolder(View itemView) {

            super(itemView);

            milestoneId = itemView.findViewById(R.id.milestoneId);
            msTitle = itemView.findViewById(R.id.milestoneTitle);
            msStatus = itemView.findViewById(R.id.milestoneState);
            msDescription = itemView.findViewById(R.id.milestoneDescription);
            msOpenIssues = itemView.findViewById(R.id.milestoneIssuesOpen);
            msClosedIssues = itemView.findViewById(R.id.milestoneIssuesClosed);
            msDueDate = itemView.findViewById(R.id.milestoneDueDate);
            msProgress = itemView.findViewById(R.id.milestoneProgress);
            ImageView milestonesMenu = itemView.findViewById(R.id.milestonesMenu);
            milestoneStatus = itemView.findViewById(R.id.milestoneStatus);

            milestonesMenu.setOnClickListener(v -> {

                Context ctx = v.getContext();
                int milestoneId_ = Integer.parseInt(milestoneId.getText().toString());

                @SuppressLint("InflateParams") View view = LayoutInflater.from(ctx).inflate(R.layout.bottom_sheet_milestones_in_list, null);

                TextView closeMilestone = view.findViewById(R.id.closeMilestone);
                TextView openMilestone = view.findViewById(R.id.openMilestone);

                BottomSheetDialog dialog = new BottomSheetDialog(ctx);
                dialog.setContentView(view);
                dialog.show();

                if(milestoneStatus.getText().toString().equals("open")) {

                    closeMilestone.setVisibility(View.VISIBLE);
                    openMilestone.setVisibility(View.GONE);

                }
                else {

                    closeMilestone.setVisibility(View.GONE);
                    openMilestone.setVisibility(View.VISIBLE);

                }

                closeMilestone.setOnClickListener(v12 -> {

                    MilestoneActions.closeMilestone(ctx, milestoneId_);
                    dialog.dismiss();
                    updateAdapter(getAdapterPosition());

                });

                openMilestone.setOnClickListener(v12 -> {

                    MilestoneActions.openMilestone(ctx, milestoneId_);
                    dialog.dismiss();
                    updateAdapter(getAdapterPosition());

                });

            });

        }

        @SuppressLint("SetTextI18n")
        void bindData(Milestones dataModel) {

            final TinyDB tinyDb = new TinyDB(context);
            final String locale = tinyDb.getString("locale");
            final String timeFormat = tinyDb.getString("dateFormat");

            milestoneId.setText(String.valueOf(dataModel.getId()));
            milestoneStatus.setText(dataModel.getState());

            final Markwon markwon = Markwon.builder(Objects.requireNonNull(context))
                    .usePlugin(CorePlugin.create())
                    .usePlugin(ImagesPlugin.create(plugin -> {
                        plugin.addSchemeHandler(new SchemeHandler() {
                            @NonNull
                            @Override
                            public ImageItem handle(@NonNull String raw, @NonNull Uri uri) {

                                final int resourceId = context.getResources().getIdentifier(
                                        raw.substring("drawable://".length()),
                                        "drawable",
                                        context.getPackageName());

                                final Drawable drawable = context.getDrawable(resourceId);

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
                        plugin.addMediaDecoder(SvgMediaDecoder.create(context.getResources()));
                        plugin.addMediaDecoder(SvgMediaDecoder.create());
                        plugin.defaultMediaDecoder(DefaultMediaDecoder.create(context.getResources()));
                        plugin.defaultMediaDecoder(DefaultMediaDecoder.create());
                    }))
                    .usePlugin(new AbstractMarkwonPlugin() {
                        @Override
                        public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                            builder
                                    .codeTextColor(tinyDb.getInt("codeBlockColor"))
                                    .codeBackgroundColor(tinyDb.getInt("codeBlockBackground"))
                                    .linkColor(context.getResources().getColor(R.color.lightBlue));
                        }
                    })
                    .usePlugin(TablePlugin.create(context))
                    .usePlugin(TaskListPlugin.create(context))
                    .usePlugin(HtmlPlugin.create())
                    .usePlugin(StrikethroughPlugin.create())
                    .usePlugin(LinkifyPlugin.create())
                    .build();

            Spanned msTitle_ = markwon.toMarkdown(dataModel.getTitle());
            markwon.setParsedMarkdown(msTitle, msTitle_);

            if(dataModel.getState().equals("open")) {

                @SuppressLint("ResourceType") int color = Color.parseColor(context.getResources().getString(R.color.releaseStable));
                TextDrawable drawable = TextDrawable.builder()
                        .beginConfig()
                        //.useFont(Typeface.DEFAULT)
                        .textColor(context.getResources().getColor(R.color.white))
                        .fontSize(30)
                        .toUpperCase()
                        .width(120)
                        .height(60)
                        .endConfig()
                        .buildRoundRect("open", color, 8);

                msStatus.setImageDrawable(drawable);

            }
            else if(dataModel.getState().equals("closed")) {

                @SuppressLint("ResourceType") int color = Color.parseColor(context.getResources().getString(R.color.colorRed));
                TextDrawable drawable = TextDrawable.builder()
                        .beginConfig()
                        //.useFont(Typeface.DEFAULT)
                        .textColor(context.getResources().getColor(R.color.white))
                        .fontSize(30)
                        .toUpperCase()
                        .width(140)
                        .height(60)
                        .endConfig()
                        .buildRoundRect("closed", color, 8);

                msStatus.setImageDrawable(drawable);

            }

            if (!dataModel.getDescription().equals("")) {
                final CharSequence bodyWithMD = markwon.toMarkdown(EmojiParser.parseToUnicode(dataModel.getDescription()));
                msDescription.setText(bodyWithMD);
            }
            else {
                msDescription.setText("");
            }

            msOpenIssues.setText(String.valueOf(dataModel.getOpen_issues()));
            msOpenIssues.setOnClickListener(new ClickListener(context.getResources().getString(R.string.milestoneOpenIssues, dataModel.getOpen_issues()), context));

            msClosedIssues.setText(String.valueOf(dataModel.getClosed_issues()));
            msClosedIssues.setOnClickListener(new ClickListener(context.getResources().getString(R.string.milestoneClosedIssues, dataModel.getClosed_issues()), context));

            if ((dataModel.getOpen_issues() + dataModel.getClosed_issues()) > 0) {

                if (dataModel.getOpen_issues() == 0) {
                    msProgress.setProgress(100);
                    msProgress.setOnClickListener(new ClickListener(context.getResources().getString(R.string.milestoneCompletion, 100), context));
                }
                else {
                    int msCompletion = 100 * dataModel.getClosed_issues() / (dataModel.getOpen_issues() + dataModel.getClosed_issues());
                    msProgress.setOnClickListener(new ClickListener(context.getResources().getString(R.string.milestoneCompletion, msCompletion), context));
                    msProgress.setProgress(msCompletion);
                }

            }
            else {
                msProgress.setProgress(0);
                msProgress.setOnClickListener(new ClickListener(context.getResources().getString(R.string.milestoneCompletion, 0), context));
            }

            if(dataModel.getDue_on() != null) {

                if (timeFormat.equals("normal") || timeFormat.equals("pretty")) {

                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", new Locale(locale));
                    Date date = null;
                    try {
                        date = formatter.parse(dataModel.getDue_on());
                    }
                    catch (ParseException e) {
                        Log.e(TAG, e.toString());
                    }
                    assert date != null;
                    String dueDate = formatter.format(date);

                    if(date.before(new Date())) {
                        msDueDate.setTextColor(context.getResources().getColor(R.color.darkRed));
                    }

                    msDueDate.setText(dueDate);
                    msDueDate.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToast(dataModel.getDue_on()), context));

                }
                else if (timeFormat.equals("normal1")) {

                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", new Locale(locale));
                    Date date1 = null;
                    try {
                        date1 = formatter.parse(dataModel.getDue_on());
                    }
                    catch (ParseException e) {
                        Log.e(TAG, e.toString());
                    }
                    assert date1 != null;
                    String dueDate = formatter.format(date1);
                    msDueDate.setText(dueDate);

                }

            }
            else {
                msDueDate.setText("");
            }

        }

    }

    private void updateAdapter(int position) {

        dataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, dataList.size());

    }

    @Override
    public int getItemViewType(int position) {

        if(dataList.get(position).getTitle() != null) {
            return TYPE_LOAD;
        }
        else {
            return 1;
        }

    }

    @Override
    public int getItemCount() {

        return dataList.size();

    }

    static class LoadHolder extends RecyclerView.ViewHolder {

        LoadHolder(View itemView) {

            super(itemView);
        }

    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {

        isMoreDataAvailable = moreDataAvailable;

    }

    public void notifyDataChanged() {

        notifyDataSetChanged();
        isLoading = false;

    }

    public interface OnLoadMoreListener {

        void onLoadMore();

    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {

        this.loadMoreListener = loadMoreListener;

    }

    public void updateList(List<Milestones> list) {

        dataList = list;
        notifyDataSetChanged();
    }

}
