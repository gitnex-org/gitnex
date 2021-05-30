package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;
import org.gitnex.tea4j.models.CronTasks;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class AdminCronTasksAdapter extends RecyclerView.Adapter<AdminCronTasksAdapter.CronTasksViewHolder> {

	private final List<CronTasks> tasksList;
	private static TinyDB tinyDb;

	static class CronTasksViewHolder extends RecyclerView.ViewHolder {

		private CronTasks cronTasks;

		private final TextView taskName;

		private CronTasksViewHolder(View itemView) {

			super(itemView);
			Context ctx = itemView.getContext();

			final Locale locale = ctx.getResources().getConfiguration().locale;
			final String timeFormat = tinyDb.getString("dateFormat");

			ImageView runTask = itemView.findViewById(R.id.runTask);
			taskName = itemView.findViewById(R.id.taskName);
			LinearLayout cronTasksInfo = itemView.findViewById(R.id.cronTasksInfo);
			LinearLayout cronTasksRun = itemView.findViewById(R.id.cronTasksRun);

			cronTasksInfo.setOnClickListener(taskInfo -> {

				String nextRun = "";
				String lastRun = "";

				if(cronTasks.getNext() != null) {
					nextRun = TimeHelper.formatTime(cronTasks.getNext(), locale, timeFormat, ctx);
				}
				if(cronTasks.getPrev() != null) {
					lastRun = TimeHelper.formatTime(cronTasks.getPrev(), locale, timeFormat, ctx);
				}

				View view = LayoutInflater.from(ctx).inflate(R.layout.layout_cron_task_info, null);

				TextView taskScheduleContent = view.findViewById(R.id.taskScheduleContent);
				TextView nextRunContent = view.findViewById(R.id.nextRunContent);
				TextView lastRunContent = view.findViewById(R.id.lastRunContent);
				TextView execTimeContent = view.findViewById(R.id.execTimeContent);

				taskScheduleContent.setText(cronTasks.getSchedule());
				nextRunContent.setText(nextRun);
				lastRunContent.setText(lastRun);
				execTimeContent.setText(String.valueOf(cronTasks.getExec_times()));

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);

				alertDialog.setTitle(StringUtils.capitalize(cronTasks.getName().replace("_", " ")));
				alertDialog.setView(view);
				alertDialog.setPositiveButton(ctx.getString(R.string.close), null);
				alertDialog.create().show();

			});

			cronTasksRun.setOnClickListener(taskInfo -> {

				runCronTask(ctx, cronTasks.getName());
			});
		}
	}

	public AdminCronTasksAdapter(Context ctx, List<CronTasks> tasksListMain) {

		tinyDb = TinyDB.getInstance(ctx);
		this.tasksList = tasksListMain;
	}

	@NonNull
	@Override
	public AdminCronTasksAdapter.CronTasksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_admin_cron_tasks, parent, false);
		return new AdminCronTasksAdapter.CronTasksViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull AdminCronTasksAdapter.CronTasksViewHolder holder, int position) {

		CronTasks currentItem = tasksList.get(position);

		holder.cronTasks = currentItem;
		holder.taskName.setText(StringUtils.capitalize(currentItem.getName().replace("_", " ")));
	}

	private static void runCronTask(final Context ctx, final String taskName) {

		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		Call<JsonElement> call = RetrofitClient
			.getApiInterface(ctx)
			.adminRunCronTask(instanceToken, taskName);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				switch(response.code()) {

					case 204:
						Toasty.success(ctx, ctx.getString(R.string.adminCronTaskSuccessMsg, taskName));
						break;

					case 401:
						AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getString(R.string.alertDialogTokenRevokedTitle),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
						break;

					case 403:
						Toasty.error(ctx, ctx.getString(R.string.authorizeError));
						break;

					case 404:
						Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
						break;

					default:
						Toasty.error(ctx, ctx.getString(R.string.genericError));

				}
			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	@Override
	public int getItemCount() {
		return tasksList.size();
	}
}
