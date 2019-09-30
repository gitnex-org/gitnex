package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spanned;
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
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.UserMentions;
import org.mian.gitnex.models.IssueComments;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.helpers.ClickListener;
import org.ocpsoft.prettytime.PrettyTime;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
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
            Picasso.get().load(currentItem.getUser().getAvatar_url()).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.issueCommenterAvatar);
        } else {
            Picasso.get().load(currentItem.getUser().getAvatar_url()).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.issueCommenterAvatar);
        }

        String cleanIssueComments = currentItem.getBody().trim();

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

        Spanned bodyWithMD = markwon.toMarkdown(EmojiParser.parseToUnicode(cleanIssueComments));
        markwon.setParsedMarkdown(holder.issueComment, UserMentions.UserMentionsFunc(mCtx, bodyWithMD, cleanIssueComments));

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
