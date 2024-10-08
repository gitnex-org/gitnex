package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.gitnex.tea4j.v2.models.Cron;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class AdminCronTasksAdapter
		extends RecyclerView.Adapter<AdminCronTasksAdapter.CronTasksViewHolder> {

	private final List<Cron> tasksList;

	public static class CronTasksViewHolder extends RecyclerView.ViewHolder {

		private Cron cronTasks;

		private final TextView taskName;

		private CronTasksViewHolder(View itemView) {

			super(itemView);
			Context ctx = itemView.getContext();

			final Locale locale = ctx.getResources().getConfiguration().locale;

			ImageView runTask = itemView.findViewById(R.id.runTask);
			taskName = itemView.findViewById(R.id.taskName);

			taskName.setOnClickListener(
					taskInfo -> {
						String nextRun = "";
						String lastRun = "";

						if (cronTasks.getNext() != null) {
							nextRun = TimeHelper.formatTime(cronTasks.getNext(), locale);
						}
						if (cronTasks.getPrev() != null) {
							lastRun = TimeHelper.formatTime(cronTasks.getPrev(), locale);
						}

						View view =
								LayoutInflater.from(ctx)
										.inflate(R.layout.layout_cron_task_info, null);

						TextView taskScheduleContent = view.findViewById(R.id.taskScheduleContent);
						TextView nextRunContent = view.findViewById(R.id.nextRunContent);
						TextView lastRunContent = view.findViewById(R.id.lastRunContent);
						TextView execTimeContent = view.findViewById(R.id.execTimeContent);

						taskScheduleContent.setText(cronTasks.getSchedule());
						nextRunContent.setText(nextRun);
						lastRunContent.setText(lastRun);
						execTimeContent.setText(String.valueOf(cronTasks.getExecTimes()));

						MaterialAlertDialogBuilder materialAlertDialogBuilder =
								new MaterialAlertDialogBuilder(ctx)
										.setTitle(
												StringUtils.capitalize(
														cronTasks.getName().replace("_", " ")))
										.setView(view)
										.setNeutralButton(ctx.getString(R.string.close), null);

						materialAlertDialogBuilder.create().show();
					});

			runTask.setOnClickListener(taskInfo -> runCronTask(ctx, cronTasks.getName()));
		}
	}

	public AdminCronTasksAdapter(List<Cron> tasksListMain) {
		this.tasksList = tasksListMain;
	}

	@NonNull @Override
	public AdminCronTasksAdapter.CronTasksViewHolder onCreateViewHolder(
			@NonNull ViewGroup parent, int viewType) {

		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_admin_cron_tasks, parent, false);
		return new AdminCronTasksAdapter.CronTasksViewHolder(v);
	}

	@Override
	public void onBindViewHolder(
			@NonNull AdminCronTasksAdapter.CronTasksViewHolder holder, int position) {

		Cron currentItem = tasksList.get(position);

		holder.cronTasks = currentItem;
		holder.taskName.setText(StringUtils.capitalize(currentItem.getName().replace("_", " ")));
	}

	private static void runCronTask(final Context ctx, final String taskName) {

		Call<Void> call = RetrofitClient.getApiInterface(ctx).adminCronRun(taskName);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						switch (response.code()) {
							case 204:
								Toasty.success(
										ctx,
										ctx.getString(R.string.adminCronTaskSuccessMsg, taskName));
								break;

							case 401:
								AlertDialogs.authorizationTokenRevokedDialog(ctx);
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
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	@Override
	public int getItemCount() {
		return tasksList.size();
	}
}
