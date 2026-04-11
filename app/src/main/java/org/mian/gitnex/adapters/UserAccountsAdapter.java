package org.mian.gitnex.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import io.mikael.urlbuilder.UrlBuilder;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.NotificationCount;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ListUserAccountsBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UrlHelper;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class UserAccountsAdapter
		extends RecyclerView.Adapter<UserAccountsAdapter.UserAccountsViewHolder> {

	private final List<UserAccount> userAccountsList;
	private final Context context;
	private final Dialog dialog;
	private final TinyDB tinyDB;

	public UserAccountsAdapter(Context ctx, Dialog dialog) {
		this.dialog = dialog;
		this.context = ctx;
		this.userAccountsList =
				Objects.requireNonNull(BaseApi.getInstance(context, UserAccountsApi.class))
						.usersAccounts();
		this.tinyDB = TinyDB.getInstance(context);
	}

	@NonNull @Override
	public UserAccountsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListUserAccountsBinding binding =
				ListUserAccountsBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new UserAccountsViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull UserAccountsViewHolder holder, int position) {

		UserAccount currentItem = userAccountsList.get(position);
		int currentActiveId = tinyDB.getInt("currentActiveAccountId", -1);
		boolean isActive = currentActiveId == currentItem.getAccountId();
		String instanceUrl = UrlHelper.getCleanUrlForDisplay(currentItem.getInstanceUrl());

		holder.binding.userId.setText(currentItem.getUserName());
		holder.binding.accountUrl.setText(instanceUrl);

		Glide.with(context)
				.load(UrlHelper.appendPath(currentItem.getInstanceUrl(), "assets/img/favicon.png"))
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.error(R.drawable.ic_server)
				.centerCrop()
				.into(holder.binding.repoAvatar);

		if (isActive) {
			holder.binding.activeAccount.setVisibility(View.VISIBLE);
			holder.binding.deleteAccount.setVisibility(View.GONE);
		} else {
			holder.binding.activeAccount.setVisibility(View.GONE);
			holder.binding.deleteAccount.setVisibility(View.VISIBLE);
		}

		holder.binding
				.getRoot()
				.setOnLongClickListener(
						v -> {
							AppUtil.copyToClipboard(
									context,
									instanceUrl,
									context.getString(R.string.copyIssueUrlToastMsg));
							return true;
						});

		holder.binding.getRoot().updateAppearance(position, getItemCount());

		holder.binding.deleteAccount.setOnClickListener(
				v -> showDeleteConfirmation(currentItem, holder.getBindingAdapterPosition()));

		holder.binding.getRoot().setOnClickListener(v -> handleAccountSwitch(currentItem));
	}

	private void handleAccountSwitch(UserAccount userAccount) {
		if (tinyDB.getInt("currentActiveAccountId") != userAccount.getAccountId()) {
			if (AppUtil.switchToAccount(context, userAccount)) {
				String cleanUrl =
						UrlBuilder.fromString(userAccount.getInstanceUrl())
								.withPath("/")
								.toString();
				Toasty.show(
						context,
						context.getString(
								R.string.switchAccountSuccess,
								userAccount.getUserName(),
								cleanUrl));

				getNotificationsCount();

				if (context instanceof Activity activity) {
					activity.recreate();
				}
				dialog.dismiss();
			}
		}
	}

	private void showDeleteConfirmation(UserAccount account, int position) {
		new MaterialAlertDialogBuilder(context)
				.setTitle(R.string.removeAccountPopupTitle)
				.setMessage(R.string.removeAccountPopupMessage)
				.setNeutralButton(R.string.cancelButton, null)
				.setPositiveButton(
						R.string.removeButton,
						(d, which) -> {
							UserAccountsApi api =
									BaseApi.getInstance(context, UserAccountsApi.class);
							if (api != null) {
								api.deleteAccount(account.getAccountId());
								userAccountsList.remove(position);
								notifyItemRemoved(position);
								notifyItemRangeChanged(position, userAccountsList.size());
								Toasty.show(
										context, context.getString(R.string.accountDeletedMessage));
							}
						})
				.show();
	}

	@Override
	public int getItemCount() {
		return userAccountsList.size();
	}

	private void getNotificationsCount() {
		Call<NotificationCount> call = RetrofitClient.getApiInterface(context).notifyNewAvailable();
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<NotificationCount> call,
							@NonNull retrofit2.Response<NotificationCount> response) {
						if (response.isSuccessful() && response.body() != null) {
							long count = response.body().getNew();
							if (count > 0) {
								String toastMsg =
										context.getResources()
												.getQuantityString(
														R.plurals.youHaveNewNotifications,
														(int) count,
														(int) count);
								new Handler()
										.postDelayed(() -> Toasty.show(context, toastMsg), 5000);
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<NotificationCount> call, @NonNull Throwable t) {}
				});
	}

	public static class UserAccountsViewHolder extends RecyclerView.ViewHolder {
		private final ListUserAccountsBinding binding;

		public UserAccountsViewHolder(@NonNull ListUserAccountsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
