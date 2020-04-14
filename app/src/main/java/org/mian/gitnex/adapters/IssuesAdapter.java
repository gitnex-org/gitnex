package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.utils.EventHookUtil;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
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

public class IssuesAdapter extends AbstractItem<IssuesAdapter, IssuesAdapter.ViewHolder> {

	final private Context ctx;
	private String issueTitle;
	private int issueNumber;
	private String issueAssigneeAvatar;
	private Date issueCreatedTime;
	private int issueCommentsCount;
	private String userFullname;
	private String userLogin;

	private boolean isSelectable = true;

	public IssuesAdapter(Context ctx) {
		this.ctx = ctx;
	}

	public IssuesAdapter withNewItems(String issueTitle, int issueNumber, String issueAssigneeAvatar, Date issueCreatedTime, int issueCommentsCount, String userFullname, String userLogin) {

		this.setNewItems(issueTitle, issueNumber, issueAssigneeAvatar, issueCreatedTime, issueCommentsCount, userFullname, userLogin);
		return this;

	}

	private void setNewItems(String issueTitle, int issueNumber, String issueAssigneeAvatar, Date issueCreatedTime, int issueCommentsCount, String userFullname, String userLogin) {

		this.issueTitle = issueTitle;
		this.issueNumber = issueNumber;
		this.issueAssigneeAvatar = issueAssigneeAvatar;
		this.issueCreatedTime = issueCreatedTime;
		this.issueCommentsCount = issueCommentsCount;
		this.userFullname = userFullname;
		this.userLogin = userLogin;

	}

	private int getIssueNumber() {
		return issueNumber;
	}

	public String getIssueTitle() {
		return issueTitle;
	}

	private String getIssueAssigneeAvatar() {
		return issueAssigneeAvatar;
	}

	private Date getIssueCreatedTime() {
		return issueCreatedTime;
	}

	private int getIssueCommentsCount() {
		return issueCommentsCount;
	}

	private String getUserFullname() {
		return userFullname;
	}

	private String getUserLogin() {
		return userLogin;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public IssuesAdapter withEnabled(boolean enabled) {
		return null;
	}

	@Override
	public boolean isSelectable() {
		return isSelectable;
	}

	@Override
	public IssuesAdapter withSelectable(boolean selectable) {
		this.isSelectable = selectable;
		return this;
	}

	@Override
	public int getType() {
		return R.id.relativeLayoutFrameIssuesList;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.list_issues;
	}

	@NonNull
	@Override
	public IssuesAdapter.ViewHolder getViewHolder(@NonNull View v) {
		return new IssuesAdapter.ViewHolder(v);
	}

	public class ViewHolder extends FastAdapter.ViewHolder<IssuesAdapter> {

		final TinyDB tinyDb = new TinyDB(ctx);
		final String locale = tinyDb.getString("locale");
		final String timeFormat = tinyDb.getString("dateFormat");

		private TextView issueNumber;
		private ImageView issueAssigneeAvatar;
		private TextView issueTitle;
		private TextView issueCreatedTime;
		private TextView issueCommentsCount;

		public ViewHolder(View itemView) {

			super(itemView);

			issueNumber = itemView.findViewById(R.id.issueNumber);
			issueAssigneeAvatar = itemView.findViewById(R.id.assigneeAvatar);
			issueTitle = itemView.findViewById(R.id.issueTitle);
			issueCommentsCount = itemView.findViewById(R.id.issueCommentsCount);
			issueCreatedTime = itemView.findViewById(R.id.issueCreatedTime);

		}

		@Override
		public void bindView(@NonNull IssuesAdapter item, @NonNull List<Object> payloads) {

			if (!item.getUserFullname().equals("")) {
				issueAssigneeAvatar.setOnClickListener(new ClickListener(ctx.getResources().getString(R.string.issueCreator) + item.getUserFullname(), ctx));
			}
			else {
				issueAssigneeAvatar.setOnClickListener(new ClickListener(ctx.getResources().getString(R.string.issueCreator) + item.getUserLogin(), ctx));
			}

			PicassoService.getInstance(ctx).get().load(item.getIssueAssigneeAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(issueAssigneeAvatar);

			String issueNumber_ = "<font color='" + ctx.getResources().getColor(R.color.lightGray) + "'>" + ctx.getResources().getString(R.string.hash) + item.getIssueNumber() + "</font>";
			issueTitle.setText(Html.fromHtml(issueNumber_ + " " + item.getIssueTitle()));

			issueNumber.setText(String.valueOf(item.getIssueNumber()));
			issueCommentsCount.setText(String.valueOf(item.getIssueCommentsCount()));

			switch (timeFormat) {

				case "pretty": {
					PrettyTime prettyTime = new PrettyTime(new Locale(locale));
					String createdTime = prettyTime.format(item.getIssueCreatedTime());
					issueCreatedTime.setText(createdTime);
					issueCreatedTime.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(item.getIssueCreatedTime()), ctx));
					break;
				}
				case "normal": {
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + ctx.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
					String createdTime = formatter.format(item.getIssueCreatedTime());
					issueCreatedTime.setText(createdTime);
					break;
				}
				case "normal1": {
					DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + ctx.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
					String createdTime = formatter.format(item.getIssueCreatedTime());
					issueCreatedTime.setText(createdTime);
					break;
				}

			}

		}

		@Override
		public void unbindView(@NonNull IssuesAdapter item) {

			issueTitle.setText(null);
			issueCommentsCount.setText(null);
			issueCreatedTime.setText(null);

		}

	}

	public static class IssueTitleClickEvent extends ClickEventHook<IssuesAdapter> {

		@Nullable
		@Override
		public List<View> onBindMany(@NonNull RecyclerView.ViewHolder viewHolder) {

			if (viewHolder instanceof IssuesAdapter.ViewHolder) {
				return EventHookUtil.toList(((ViewHolder) viewHolder).issueTitle);
			}

			return super.onBindMany(viewHolder);

		}

		@Override
		public void onClick(View v, int position, @NonNull FastAdapter<IssuesAdapter> fastAdapter, IssuesAdapter item) {

			Context context = v.getContext();

			Intent intent = new Intent(context, IssueDetailActivity.class);
			intent.putExtra("issueNumber", item.getIssueNumber());

			TinyDB tinyDb = new TinyDB(context);
			tinyDb.putString("issueNumber", String.valueOf(item.getIssueNumber()));
			tinyDb.putString("issueType", "issue");
			context.startActivity(intent);

		}

	}

}
