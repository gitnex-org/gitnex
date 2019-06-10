package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ReplyToIssueActivity;
import org.mian.gitnex.helpers.UserMentions;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.models.IssueComments;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.helpers.ClickListener;
import org.ocpsoft.prettytime.PrettyTime;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
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

public class IssueCommentsAdapter extends RecyclerView.Adapter<IssueCommentsAdapter.IssueCommentViewHolder> {

    private List<IssueComments> issuesComments;
    private Context mCtx;

    static class IssueCommentViewHolder extends RecyclerView.ViewHolder {

        private TextView issueNumber;
        private TextView commendId;
        private ImageView issueCommenterAvatar;
        private TextView issueComment;
        private TextView issueCommentDate;
        private ImageView commentsOptionsMenu;
        private TextView commendBodyRaw;
        private TextView commentModified;

        private IssueCommentViewHolder(View itemView) {
            super(itemView);

            issueNumber = itemView.findViewById(R.id.issueNumber);
            commendId = itemView.findViewById(R.id.commendId);
            issueCommenterAvatar = itemView.findViewById(R.id.issueCommenterAvatar);
            issueComment = itemView.findViewById(R.id.issueComment);
            issueCommentDate = itemView.findViewById(R.id.issueCommentDate);
            commentsOptionsMenu = itemView.findViewById(R.id.commentsOptionsMenu);
            commendBodyRaw = itemView.findViewById(R.id.commendBodyRaw);
            commentModified = itemView.findViewById(R.id.commentModified);

            commentsOptionsMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Context context = v.getContext();
                    Context context_ = new ContextThemeWrapper(context, R.style.popupMenuStyle);

                    PopupMenu popupMenu = new PopupMenu(context_, v);
                    popupMenu.inflate(R.menu.issue_comment_menu);

                    Object menuHelper;
                    Class[] argTypes;
                    try {

                        Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
                        fMenuHelper.setAccessible(true);
                        menuHelper = fMenuHelper.get(popupMenu);
                        argTypes = new Class[] { boolean.class };
                        menuHelper.getClass().getDeclaredMethod("setForceShowIcon",
                                argTypes).invoke(menuHelper, true);

                    } catch (Exception e) {

                        popupMenu.show();
                        return;

                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.commentMenuEdit:

                                    Intent intent = new Intent(context, ReplyToIssueActivity.class);
                                    intent.putExtra("commentId", commendId.getText());
                                    intent.putExtra("commentAction", "edit");
                                    intent.putExtra("commentBody", commendBodyRaw.getText());
                                    context.startActivity(intent);
                                    break;

                                case R.id.commentMenuDelete:

                                    break;

                            }
                            return false;
                        }
                    });

                    popupMenu.show();

                }
            });

        }
    }

    public IssueCommentsAdapter(Context mCtx, List<IssueComments> issuesCommentsMain) {
        this.mCtx = mCtx;
        this.issuesComments = issuesCommentsMain;
    }

    @NonNull
    @Override
    public IssueCommentsAdapter.IssueCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.issue_comments, parent, false);
        return new IssueCommentsAdapter.IssueCommentViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull IssueCommentsAdapter.IssueCommentViewHolder holder, int position) {

        final TinyDB tinyDb = new TinyDB(mCtx);
        final String locale = tinyDb.getString("locale");
        final String timeFormat = tinyDb.getString("dateFormat");
        final String loginUid = tinyDb.getString("loginUid");

        IssueComments currentItem = issuesComments.get(position);

        if(!loginUid.equals(currentItem.getUser().getUsername())) {
            holder.commentsOptionsMenu.setVisibility(View.INVISIBLE);
        }
        holder.commendId.setText(String.valueOf(currentItem.getId()));
        holder.commendBodyRaw.setText(currentItem.getBody());

        if (!currentItem.getUser().getFull_name().equals("")) {
            holder.issueCommenterAvatar.setOnClickListener(new ClickListener(mCtx.getResources().getString(R.string.issueCommenter) + currentItem.getUser().getFull_name(), mCtx));
        } else {
            holder.issueCommenterAvatar.setOnClickListener(new ClickListener(mCtx.getResources().getString(R.string.issueCommenter) + currentItem.getUser().getLogin(), mCtx));
        }

        if (currentItem.getUser().getAvatar_url() != null) {
            Picasso.get().load(currentItem.getUser().getAvatar_url()).transform(new RoundedTransformation(100, 0)).resize(200, 200).centerCrop().into(holder.issueCommenterAvatar);
        } else {
            Picasso.get().load(currentItem.getUser().getAvatar_url()).transform(new RoundedTransformation(100, 0)).resize(200, 200).centerCrop().into(holder.issueCommenterAvatar);
        }

        String cleanIssueComments = currentItem.getBody().trim();

        final Markwon markwon = Markwon.builder(Objects.requireNonNull(mCtx))
                .usePlugin(CorePlugin.create())
                .usePlugin(OkHttpImagesPlugin.create(new OkHttpClient()))
                .usePlugin(ImagesPlugin.create(mCtx))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                        builder
                                .codeTextColor(tinyDb.getInt("codeBlockColor"))
                                .codeBackgroundColor(tinyDb.getInt("codeBlockBackground"));
                    }
                })
                .usePlugin(TablePlugin.create(mCtx))
                .usePlugin(TaskListPlugin.create(mCtx))
                .usePlugin(HtmlPlugin.create())
                .usePlugin(GifPlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .build();

        final CharSequence bodyWithMD = markwon.toMarkdown(EmojiParser.parseToUnicode(cleanIssueComments));
        holder.issueComment.setText(UserMentions.UserMentionsFunc(mCtx, bodyWithMD, cleanIssueComments));

        String edited;

        if(!currentItem.getUpdated_at().equals(currentItem.getCreated_at())) {
            edited = mCtx.getResources().getString(R.string.colorfulBulletSpan) + mCtx.getResources().getString(R.string.modifiedText);
            holder.commentModified.setVisibility(View.VISIBLE);
            holder.commentModified.setText(edited);
            holder.commentModified.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getUpdated_at()), mCtx));
        }
        else {
            holder.commentModified.setVisibility(View.INVISIBLE);
        }

        switch (timeFormat) {
            case "pretty": {
                PrettyTime prettyTime = new PrettyTime(new Locale(locale));
                String createdTime = prettyTime.format(currentItem.getCreated_at());
                holder.issueCommentDate.setText(createdTime);
                holder.issueCommentDate.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getCreated_at()), mCtx));
                break;
            }
            case "normal": {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + mCtx.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
                String createdTime = formatter.format(currentItem.getCreated_at());
                holder.issueCommentDate.setText(createdTime);
                break;
            }
            case "normal1": {
                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + mCtx.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
                String createdTime = formatter.format(currentItem.getCreated_at());
                holder.issueCommentDate.setText(createdTime);
                break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return issuesComments.size();
    }

}
