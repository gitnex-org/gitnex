package org.mian.gitnex.items;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.util.TinyDB;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class CommitsItems extends AbstractItem<CommitsItems, CommitsItems.ViewHolder> {

    final private Context ctx;
    private String commitTitle;
    private String commitHtmlUrl;
    private String commitCommitter;
    private Date commitDate;

    private boolean isSelectable = true;

    public CommitsItems(Context ctx) {
        this.ctx = ctx;
    }

    public CommitsItems withNewItems(String commitTitle, String commitHtmlUrl, String commitCommitter, Date commitDate) {

        this.setNewItems(commitTitle, commitHtmlUrl, commitCommitter, commitDate);
        return this;

    }

    private void setNewItems(String commitTitle, String commitHtmlUrl, String commitCommitter, Date commitDate) {

        this.commitTitle = commitTitle;
        this.commitHtmlUrl = commitHtmlUrl;
        this.commitCommitter = commitCommitter;
        this.commitDate = commitDate;

    }

    public String getCommitTitle() {
        return commitTitle;
    }

    private String getCommitHtmlUrl() {
        return commitHtmlUrl;
    }

    private String getcommitCommitter() {
        return commitCommitter;
    }

    private Date getcommitDate() {
        return commitDate;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public CommitsItems withEnabled(boolean enabled) {
        return null;
    }

    @Override
    public boolean isSelectable() {
        return isSelectable;
    }

    @Override
    public CommitsItems withSelectable(boolean selectable) {

        this.isSelectable = selectable;
        return this;

    }

    @Override
    public int getType() {
        return R.id.commitList;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.list_commits;
    }

    @NonNull
    @Override
    public CommitsItems.ViewHolder getViewHolder(@NonNull View v) {

        return new CommitsItems.ViewHolder(v);

    }

    public class ViewHolder extends FastAdapter.ViewHolder<CommitsItems> {

        final TinyDB tinyDb = new TinyDB(ctx);
        final String locale = tinyDb.getString("locale");
        final String timeFormat = tinyDb.getString("dateFormat");

        TextView commitTitleVw;
        TextView commitCommitterVw;
        TextView commitDateVw;
        TextView commitHtmlUrlVw;

        public ViewHolder(View itemView) {

            super(itemView);

            commitTitleVw = itemView.findViewById(R.id.commitTitleVw);
            commitCommitterVw = itemView.findViewById(R.id.commitCommitterVw);
            commitDateVw = itemView.findViewById(R.id.commitDateVw);
            commitHtmlUrlVw = itemView.findViewById(R.id.commitHtmlUrlVw);

        }

        @Override
        public void bindView(CommitsItems item, @NonNull List<Object> payloads) {

            commitTitleVw.setText(item.getCommitTitle());
            commitCommitterVw.setText(ctx.getString(R.string.commitCommittedBy, item.getcommitCommitter()));

            switch (timeFormat) {
                case "pretty": {
                    PrettyTime prettyTime = new PrettyTime(new Locale(locale));
                    String createdTime = prettyTime.format(item.getcommitDate());
                    commitDateVw.setText(createdTime);
                    commitDateVw.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(item.getcommitDate()), ctx));
                    break;
                }
                case "normal": {
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + ctx.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
                    String createdTime = formatter.format(item.getcommitDate());
                    commitDateVw.setText(createdTime);
                    break;
                }
                case "normal1": {
                    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + ctx.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
                    String createdTime = formatter.format(item.getcommitDate());
                    commitDateVw.setText(createdTime);
                    break;
                }
            }

            commitHtmlUrlVw.setText(Html.fromHtml("<a href='" + item.getCommitHtmlUrl() + "'>" + ctx.getResources().getString(R.string.viewInBrowser) + "</a> "));
            commitHtmlUrlVw.setMovementMethod(LinkMovementMethod.getInstance());

        }

        @Override
        public void unbindView(@NonNull CommitsItems item) {

            commitTitleVw.setText(null);
            commitCommitterVw.setText(null);
            commitDateVw.setText(null);
            commitHtmlUrlVw.setText(null);

        }

    }

}
