package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import io.mikael.urlbuilder.UrlBuilder;

/**
 * Author M M Arif
 */

public class UserAccountsAdapter extends RecyclerView.Adapter<UserAccountsAdapter.UserAccountsViewHolder> {

	private final List<UserAccount> userAccountsList;
	private final Context context;
	private TinyDB tinyDB;
	private final Dialog dialog;

	class UserAccountsViewHolder extends RecyclerView.ViewHolder {

		private int accountId;
		private String accountName;

		private final TextView accountUrl;
		private final TextView userId;
		private final ImageView activeAccount;
		private final ImageView deleteAccount;
		private final ImageView repoAvatar;

		private UserAccountsViewHolder(View itemView) {

			super(itemView);

			accountUrl = itemView.findViewById(R.id.accountUrl);
			userId = itemView.findViewById(R.id.userId);
			activeAccount = itemView.findViewById(R.id.activeAccount);
			deleteAccount = itemView.findViewById(R.id.deleteAccount);
			repoAvatar = itemView.findViewById(R.id.repoAvatar);

			deleteAccount.setOnClickListener(itemDelete -> {

				new AlertDialog.Builder(context)
					.setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_delete))
					.setTitle(context.getResources().getString(R.string.removeAccountPopupTitle))
					.setMessage(context.getResources().getString(R.string.removeAccountPopupMessage))
					.setPositiveButton(context.getResources().getString(R.string.removeButton), (dialog, which) -> {

						updateLayoutByPosition(getAdapterPosition());
						UserAccountsApi userAccountsApi = BaseApi.getInstance(context, UserAccountsApi.class);
						assert userAccountsApi != null;
						userAccountsApi.deleteAccount(Integer.parseInt(String.valueOf(accountId)));
					}).setNeutralButton(context.getResources().getString(R.string.cancelButton), null)
					.show();
			});

			itemView.setOnClickListener(switchAccount -> {

				UserAccountsApi userAccountsApi = BaseApi.getInstance(context, UserAccountsApi.class);
				assert userAccountsApi != null;
				UserAccount userAccount = userAccountsApi.getAccountByName(accountName);

				if(AppUtil.switchToAccount(context, userAccount)) {

					String url = UrlBuilder.fromString(userAccount.getInstanceUrl())
						.withPath("/")
						.toString();

					Toasty.success(context,  context.getResources().getString(R.string.switchAccountSuccess, userAccount.getUserName(), url));
					((Activity) context).recreate();
					dialog.dismiss();

				}
			});

		}

	}

	public UserAccountsAdapter(Context ctx, List<UserAccount> userAccountsListMain, Dialog dialog) {
		this.dialog = dialog;
		this.context = ctx;
		this.userAccountsList = userAccountsListMain;
	}

	private void updateLayoutByPosition(int position) {

		userAccountsList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, userAccountsList.size());
		Toasty.success(context, context.getResources().getString(R.string.accountDeletedMessage));
	}

	@NonNull
	@Override
	public UserAccountsAdapter.UserAccountsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_user_accounts, parent, false);
		return new UserAccountsViewHolder(v);
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void onBindViewHolder(@NonNull UserAccountsAdapter.UserAccountsViewHolder holder, int position) {

		UserAccount currentItem = userAccountsList.get(position);
		tinyDB = TinyDB.getInstance(context);

		String url = UrlBuilder.fromString(currentItem.getInstanceUrl())
			.withPath("/")
			.toString();

		holder.accountId = currentItem.getAccountId();
		holder.accountName = currentItem.getAccountName();

		holder.userId.setText(currentItem.getUserName());
		holder.accountUrl.setText(url);

		int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

		PicassoService.getInstance(context).get()
			.load(url + "assets/img/favicon.png")
			.placeholder(R.drawable.loader_animated)
			.transform(new RoundedTransformation(imgRadius, 0))
			.resize(120, 120)
			.centerCrop()
			.into(holder.repoAvatar);

		if(tinyDB.getInt("currentActiveAccountId") == currentItem.getAccountId()) {
			holder.activeAccount.setVisibility(View.VISIBLE);
		}
		else {
			holder.deleteAccount.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public int getItemCount() {
		return userAccountsList.size();
	}

}
