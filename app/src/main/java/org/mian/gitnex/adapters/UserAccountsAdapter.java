package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
import org.mian.gitnex.activities.AddNewAccountActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UrlHelper;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
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

	private void updateLayoutByPosition(int position) {

		userAccountsList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, userAccountsList.size());
		Toasty.success(context, context.getResources().getString(R.string.accountDeletedMessage));
	}

	private void getNotificationsCount() {

		Call<NotificationCount> call = RetrofitClient.getApiInterface(context).notifyNewAvailable();

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<NotificationCount> call,
							@NonNull retrofit2.Response<NotificationCount> response) {

						NotificationCount notificationCount = response.body();

						if (response.code() == 200) {

							assert notificationCount != null;
							if (notificationCount.getNew() > 0) {
								String toastMsg =
										context.getResources()
												.getQuantityString(
														R.plurals.youHaveNewNotifications,
														Math.toIntExact(notificationCount.getNew()),
														Math.toIntExact(
																notificationCount.getNew()));
								new Handler()
										.postDelayed(() -> Toasty.info(context, toastMsg), 5000);
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<NotificationCount> call, @NonNull Throwable t) {}
				});
	}

	@NonNull @Override
	public UserAccountsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_user_accounts, parent, false);
		return new UserAccountsViewHolder(v);
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void onBindViewHolder(@NonNull UserAccountsViewHolder holder, int position) {

		UserAccount currentItem = userAccountsList.get(position);

		String url = UrlBuilder.fromString(currentItem.getInstanceUrl()).withPath("/").toString();

		holder.accountId = currentItem.getAccountId();
		holder.accountName = currentItem.getAccountName();

		holder.userId.setText(currentItem.getUserName());
		if (currentItem.isLoggedIn()) {
			holder.accountUrl.setText(url);
		} else {
			holder.accountUrl.setText(context.getString(R.string.notLoggedIn, url));
		}

		Glide.with(context)
				.load(UrlHelper.appendPath(currentItem.getInstanceUrl(), "assets/img/favicon.png"))
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.centerCrop()
				.into(holder.repoAvatar);

		if (tinyDB.getInt("currentActiveAccountId") == currentItem.getAccountId()) {
			holder.activeAccount.setVisibility(View.VISIBLE);
		} else {
			holder.deleteAccount.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public int getItemCount() {
		return userAccountsList.size();
	}

	public class UserAccountsViewHolder extends RecyclerView.ViewHolder {

		private final TextView accountUrl;
		private final TextView userId;
		private final ImageView activeAccount;
		private final ImageView deleteAccount;
		private final ImageView repoAvatar;
		private int accountId;
		private String accountName;

		private UserAccountsViewHolder(View itemView) {

			super(itemView);

			accountUrl = itemView.findViewById(R.id.accountUrl);
			userId = itemView.findViewById(R.id.userId);
			activeAccount = itemView.findViewById(R.id.activeAccount);
			deleteAccount = itemView.findViewById(R.id.deleteAccount);
			repoAvatar = itemView.findViewById(R.id.repoAvatar);

			deleteAccount.setOnClickListener(
					itemDelete -> {
						MaterialAlertDialogBuilder materialAlertDialogBuilder =
								new MaterialAlertDialogBuilder(context)
										.setTitle(
												context.getResources()
														.getString(
																R.string.removeAccountPopupTitle))
										.setMessage(
												context.getResources()
														.getString(
																R.string.removeAccountPopupMessage))
										.setNeutralButton(
												context.getResources()
														.getString(R.string.cancelButton),
												null)
										.setPositiveButton(
												context.getResources()
														.getString(R.string.removeButton),
												(dialog, which) -> {
													updateLayoutByPosition(
															getBindingAdapterPosition());
													UserAccountsApi userAccountsApi =
															BaseApi.getInstance(
																	context, UserAccountsApi.class);
													assert userAccountsApi != null;
													userAccountsApi.deleteAccount(
															Integer.parseInt(
																	String.valueOf(accountId)));
												});

						materialAlertDialogBuilder.create().show();
					});

			itemView.setOnClickListener(
					switchAccount -> {
						UserAccountsApi userAccountsApi =
								BaseApi.getInstance(context, UserAccountsApi.class);
						assert userAccountsApi != null;
						UserAccount userAccount = userAccountsApi.getAccountByName(accountName);

						if (!userAccount.isLoggedIn()) {
							UrlBuilder url =
									UrlBuilder.fromString(userAccount.getInstanceUrl())
											.withPath("/");

							String host;
							if (url.scheme.equals("http")) {
								if (url.port == 80 || url.port == 0) {
									host = url.hostName;
								} else {
									host = url.hostName + ":" + url.port;
								}
							} else {
								if (url.port == 443 || url.port == 0) {
									host = url.hostName;
								} else {
									host = url.hostName + ":" + url.port;
								}
							}

							Toasty.warning(context, context.getString(R.string.logInAgain));
							dialog.dismiss();

							Intent i = new Intent(context, AddNewAccountActivity.class);
							i.putExtra("instanceUrl", host);
							i.putExtra("scheme", url.scheme);
							i.putExtra("token", userAccount.getToken());
							context.startActivity(i);
							return;
						}

						if (tinyDB.getInt("currentActiveAccountId") != userAccount.getAccountId()) {
							if (AppUtil.switchToAccount(context, userAccount)) {

								String url =
										UrlBuilder.fromString(userAccount.getInstanceUrl())
												.withPath("/")
												.toString();

								Toasty.success(
										context,
										context.getResources()
												.getString(
														R.string.switchAccountSuccess,
														userAccount.getUserName(),
														url));
								getNotificationsCount();
								((Activity) context).recreate();
								dialog.dismiss();
							}
						}
					});
		}
	}
}
