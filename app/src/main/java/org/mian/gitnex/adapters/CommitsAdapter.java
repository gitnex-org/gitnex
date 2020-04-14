package org.mian.gitnex.adapters;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class CommitsAdapter extends AbstractItem<CommitsAdapter, CommitsAdapter.ViewHolder> {

    final private Context ctx;
    private String commitTitle;
    private String commitHtmlUrl;
    private String commitCommitter;
    private Date commitDate;

    private boolean isSelectable = true;

    public CommitsAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public CommitsAdapter withNewItems(String commitTitle, String commitHtmlUrl, String commitCommitter, Date commitDate) {

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
    public CommitsAdapter withEnabled(boolean enabled) {
        return null;
    }

    @Override
    public boolean isSelectable() {
        return isSelectable;
    }

    @Override
    public CommitsAdapter withSelectable(boolean selectable) {

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
    public CommitsAdapter.ViewHolder getViewHolder(@NonNull View v) {

        return new CommitsAdapter.ViewHolder(v);

    }

    public class ViewHolder extends FastAdapter.ViewHolder<CommitsAdapter> {

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
        public void bindView(CommitsAdapter item, @NonNull List<Object> payloads) {

            commitTitleVw.setText(item.getCommitTitle());
            commitCommitterVw.setText(ctx.getString(R.string.commitCommittedBy, item.getcommitCommitter()));

            commitDateVw.setText(TimeHelper.formatTime(item.getcommitDate(), new Locale(locale), timeFormat, ctx));

            if(timeFormat.equals("pretty")) {
                commitDateVw.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(item.getcommitDate()), ctx));
            }

            commitHtmlUrlVw.setText(Html.fromHtml("<a href='" + item.getCommitHtmlUrl() + "'>" + ctx.getResources().getString(R.string.viewInBrowser) + "</a> "));
            commitHtmlUrlVw.setMovementMethod(LinkMovementMethod.getInstance());

        }

        @Override
        public void unbindView(@NonNull CommitsAdapter item) {

            commitTitleVw.setText(null);
            commitCommitterVw.setText(null);
            commitDateVw.setText(null);
            commitHtmlUrlVw.setText(null);

        }

    }

}
