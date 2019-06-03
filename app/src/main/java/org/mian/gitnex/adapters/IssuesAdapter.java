package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.UserMentions;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.util.TinyDB;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class IssuesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private Context context;
    private final int TYPE_LOAD = 0;
    private List<Issues> issuesList;
    private List<Issues> issuesListFull;
    private OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false, isMoreDataAvailable = true;

    public IssuesAdapter(Context context, List<Issues> issuesListMain) {

        this.context = context;
        this.issuesList = issuesListMain;
        issuesListFull = new ArrayList<>(issuesList);

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);

        if(viewType == TYPE_LOAD){
            return new IssuesHolder(inflater.inflate(R.layout.repo_detail_issues_list, parent,false));
        }
        else {
            return new LoadHolder(inflater.inflate(R.layout.row_load,parent,false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(position >= getItemCount()-1 && isMoreDataAvailable && !isLoading && loadMoreListener!=null) {

            isLoading = true;
            loadMoreListener.onLoadMore();

        }

        if(getItemViewType(position) == TYPE_LOAD) {

            ((IssuesHolder)holder).bindData(issuesList.get(position));

        }

    }

    @Override
    public int getItemViewType(int position) {

        if(issuesList.get(position).getTitle() != null) {
            return TYPE_LOAD;
        }
        else {
            return 1;
        }

    }

    @Override
    public int getItemCount() {

        return issuesList.size();

    }

    class IssuesHolder extends RecyclerView.ViewHolder {

        private TextView issueNumber;
        private ImageView issueAssigneeAvatar;
        private TextView issueTitle;
        private TextView issueDescription;
        //private ImageView issueState;
        private TextView issueCreatedTime;
        private TextView issueCommentsCount;
        private ImageView issueType;

        IssuesHolder(View itemView) {

            super(itemView);

            issueNumber = itemView.findViewById(R.id.issueNumber);
            issueAssigneeAvatar = itemView.findViewById(R.id.assigneeAvatar);
            issueTitle = itemView.findViewById(R.id.issueTitle);
            issueDescription = itemView.findViewById(R.id.issueDescription);
            issueCommentsCount = itemView.findViewById(R.id.issueCommentsCount);
            //issueState = itemView.findViewById(R.id.issueStatus);
            issueCreatedTime = itemView.findViewById(R.id.issueCreatedTime);
            issueType = itemView.findViewById(R.id.issueType);

            issueTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();
                    //Log.i("issueNumber", issueNumber.getText().toString());

                    Intent intent = new Intent(context, IssueDetailActivity.class);
                    intent.putExtra("issueNumber", issueNumber.getText());

                    TinyDB tinyDb = new TinyDB(context);
                    tinyDb.putString("issueNumber", issueNumber.getText().toString());
                    context.startActivity(intent);

                }
            });
            issueDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();
                    //Log.i("issueNumber", issueNumber.getText().toString());

                    Intent intent = new Intent(context, IssueDetailActivity.class);
                    intent.putExtra("issueNumber", issueNumber.getText());

                    TinyDB tinyDb = new TinyDB(context);
                    tinyDb.putString("issueNumber", issueNumber.getText().toString());
                    context.startActivity(intent);

                }
            });

        }

        @SuppressLint("SetTextI18n")
        void bindData(Issues issuesModel){

            final TinyDB tinyDb = new TinyDB(context);
            final String locale = tinyDb.getString("locale");
            final String timeFormat = tinyDb.getString("dateFormat");

            final Markwon markwon = Markwon.builder(Objects.requireNonNull(context))
                    .usePlugin(CorePlugin.create())
                    .usePlugin(OkHttpImagesPlugin.create(new OkHttpClient()))
                    .usePlugin(ImagesPlugin.createWithAssets(context))
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
                    .usePlugin(GifPlugin.create())
                    .usePlugin(StrikethroughPlugin.create())
                    .build();

            if (!issuesModel.getUser().getFull_name().equals("")) {
                issueAssigneeAvatar.setOnClickListener(new ClickListener(context.getResources().getString(R.string.issueCreator) + issuesModel.getUser().getFull_name(), context));
            } else {
                issueAssigneeAvatar.setOnClickListener(new ClickListener(context.getResources().getString(R.string.issueCreator) + issuesModel.getUser().getLogin(), context));
            }

            if (issuesModel.getUser().getAvatar_url() != null) {
                Picasso.get().load(issuesModel.getUser().getAvatar_url()).transform(new RoundedTransformation(100, 0)).resize(200, 200).centerCrop().into(issueAssigneeAvatar);
            } else {
                Picasso.get().load(issuesModel.getUser().getAvatar_url()).transform(new RoundedTransformation(100, 0)).resize(200, 200).centerCrop().into(issueAssigneeAvatar);
            }

            if (issuesModel.getPull_request() == null) {
                issueType.setImageResource(R.drawable.ic_issues);
                issueType.setOnClickListener(new ClickListener(context.getResources().getString(R.string.issueTypeIssue), context));
            } else {
                issueType.setImageResource(R.drawable.ic_merge);
                issueType.setOnClickListener(new ClickListener(context.getResources().getString(R.string.issueTypePullRequest), context));
            }

            issueTitle.setText(context.getResources().getString(R.string.hash) + issuesModel.getNumber() + " " + issuesModel.getTitle());
            issueNumber.setText(String.valueOf(issuesModel.getNumber()));
            issueCommentsCount.setText(String.valueOf(issuesModel.getComments()));

            if (!issuesModel.getBody().equals("")) {
                String cleanIssueDescription = issuesModel.getBody().trim();
                issueDescription.setVisibility(View.VISIBLE);
                final CharSequence bodyWithMD = markwon.toMarkdown(EmojiParser.parseToUnicode(cleanIssueDescription));
                issueDescription.setText(UserMentions.UserMentionsFunc(context, bodyWithMD, cleanIssueDescription));
            }
            else {
                issueDescription.setText("");
                issueDescription.setVisibility(View.GONE);
            }
            /*if (issuesModel.getState().equals("open")) {
                issueState.setImageResource(R.drawable.ic_issue_open);
                issueState.setOnClickListener(new ClickListener(context.getResources().getString(R.string.issueStatusTextOpen), context));
            } else {
                issueState.setImageResource(R.drawable.ic_issue_closed);
                issueState.setOnClickListener(new ClickListener(context.getResources().getString(R.string.issueStatusTextClosed), context));
            }*/

            switch (timeFormat) {
                case "pretty": {
                    PrettyTime prettyTime = new PrettyTime(new Locale(locale));
                    String createdTime = prettyTime.format(issuesModel.getCreated_at());
                    issueCreatedTime.setText(createdTime);
                    issueCreatedTime.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(issuesModel.getCreated_at()), context));
                    break;
                }
                case "normal": {
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
                    String createdTime = formatter.format(issuesModel.getCreated_at());
                    issueCreatedTime.setText(createdTime);
                    break;
                }
                case "normal1": {
                    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
                    String createdTime = formatter.format(issuesModel.getCreated_at());
                    issueCreatedTime.setText(createdTime);
                    break;
                }
            }

        }

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

    @Override
    public Filter getFilter() {
        return issuesFilter;
    }

    private Filter issuesFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Issues> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(issuesList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Issues item : issuesList) {
                    if (item.getTitle().toLowerCase().contains(filterPattern) || item.getBody().toLowerCase().contains(filterPattern)) {
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
            issuesList.clear();
            issuesList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}
