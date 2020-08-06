package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import io.mikael.urlbuilder.UrlBuilder;

/**
 * Author M M Arif
 */

public class UserAccountsAdapter extends RecyclerView.Adapter<UserAccountsAdapter.UserAccountsViewHolder> {

	private List<UserAccount> userAccountsList;
	private Context mCtx;
	private TinyDB tinyDB;

	class UserAccountsViewHolder extends RecyclerView.ViewHolder {

		private TextView accountUrl;
		private TextView userId;
		private ImageView activeAccount;
		private ImageView deleteAccount;
		private ImageView repoAvatar;
		private TextView accountId;
		private TextView accountName;

		private UserAccountsViewHolder(View itemView) {

			super(itemView);

			accountUrl = itemView.findViewById(R.id.accountUrl);
			userId = itemView.findViewById(R.id.userId);
			activeAccount = itemView.findViewById(R.id.activeAccount);
			deleteAccount = itemView.findViewById(R.id.deleteAccount);
			repoAvatar = itemView.findViewById(R.id.repoAvatar);
			accountId = itemView.findViewById(R.id.accountId);
			accountName = itemView.findViewById(R.id.accountName);

			deleteAccount.setOnClickListener(itemDelete -> {

				new AlertDialog.Builder(mCtx)
					.setIcon(mCtx.getDrawable(R.drawable.ic_delete))
					.setTitle(mCtx.getResources().getString(R.string.removeAccountPopupTitle))
					.setMessage(mCtx.getResources().getString(R.string.removeAccountPopupMessage))
					.setPositiveButton(mCtx.getResources().getString(R.string.removeButton), (dialog, which) -> {

						updateLayoutByPosition(getAdapterPosition());
						UserAccountsApi userAccountsApi = new UserAccountsApi(mCtx);
						userAccountsApi.deleteAccount(Integer.parseInt(accountId.getText().toString()));
					}).setNeutralButton(mCtx.getResources().getString(R.string.cancelButton), null)
					.show();

			});

			itemView.setOnClickListener(itemEdit -> {

				String accountNameSwitch = accountName.getText().toString();
				UserAccountsApi userAccountsApi = new UserAccountsApi(mCtx);
				UserAccount userAccount = userAccountsApi.getAccountData(accountNameSwitch);

				Log.e("userAccount", userAccount.getInstanceUrl());

				if(tinyDB.getInt("currentActiveAccountId") != userAccount.getAccountId()) {

					String url = UrlBuilder.fromString(userAccount.getInstanceUrl())
						.withPath("/")
						.toString();

					tinyDB.putString("loginUid", userAccount.getUserName());
					tinyDB.putString("userLogin", userAccount.getUserName());
					tinyDB.putString(userAccount.getUserName() + "-token", userAccount.getToken());
					tinyDB.putString("instanceUrl", userAccount.getInstanceUrl());
					tinyDB.putInt("currentActiveAccountId", userAccount.getAccountId());

					Toasty.success(mCtx,  mCtx.getResources().getString(R.string.switchAccountSuccess, userAccount.getUserName(), url));
					((Activity) mCtx).recreate();
				}

			});

		}

	}

	public UserAccountsAdapter(Context mCtx, List<UserAccount> userAccountsListMain) {

		this.mCtx = mCtx;
		this.userAccountsList = userAccountsListMain;
	}

	private void updateLayoutByPosition(int position) {

		userAccountsList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, userAccountsList.size());
		Toasty.success(mCtx, mCtx.getResources().getString(R.string.accountDeletedMessage));

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
		tinyDB = new TinyDB(mCtx);

		String url = UrlBuilder.fromString(currentItem.getInstanceUrl())
			.withPath("/")
			.toString();

		holder.accountId.setText(String.valueOf(currentItem.getAccountId()));
		holder.accountName.setText(currentItem.getAccountName());
		holder.userId.setText(String.format("@%s", currentItem.getUserName()));
		holder.accountUrl.setText(url);

		PicassoService.getInstance(mCtx).get().load(url + "img/favicon.png").placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.repoAvatar);

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
