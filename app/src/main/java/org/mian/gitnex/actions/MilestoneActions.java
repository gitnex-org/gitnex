package org.mian.gitnex.actions;

import android.content.Context;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.EditMilestoneOption;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class MilestoneActions {

	private static final String TAG = "MilestoneActions : ";

	public static void closeMilestone(
			final Context ctx, int milestoneId_, RepositoryContext repository) {
		updateMilestoneState(ctx, milestoneId_, repository, "closed");
	}

	public static void openMilestone(
			final Context ctx, int milestoneId_, RepositoryContext repository) {
		updateMilestoneState(ctx, milestoneId_, repository, "open");
	}

	private static void updateMilestoneState(
			final Context ctx, int milestoneId_, RepositoryContext repository, String state) {

		EditMilestoneOption milestoneStateJson = new EditMilestoneOption();
		milestoneStateJson.setState(state);
		Call<Milestone> call =
				RetrofitClient.getApiInterface(ctx)
						.issueEditMilestone(
								repository.getOwner(),
								repository.getName(),
								String.valueOf(milestoneId_),
								milestoneStateJson);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Milestone> call,
							@NonNull retrofit2.Response<Milestone> response) {

						if (response.isSuccessful()) {

							Toasty.success(ctx, ctx.getString(R.string.milestoneStatusUpdate));

						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);

						} else {

							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Milestone> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericError));
					}
				});
	}
}
