package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.database.models.UserAccount;
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

	static class UserAccountsViewHolder extends RecyclerView.ViewHolder {

		private TextView accountUrl;
		private TextView userId;
		private ImageView activeAccount;
		private ImageView deleteAccount;

		private UserAccountsViewHolder(View itemView) {

			super(itemView);

			accountUrl = itemView.findViewById(R.id.accountUrl);
			userId = itemView.findViewById(R.id.userId);
			activeAccount = itemView.findViewById(R.id.activeAccount);
			deleteAccount = itemView.findViewById(R.id.deleteAccount);

			deleteAccount.setOnClickListener(itemDelete -> {
				// use later to delete an account

			});

			itemView.setOnClickListener(itemEdit -> {
				// use later to switch account

			});

		}

	}

	public UserAccountsAdapter(Context mCtx, List<UserAccount> userAccountsListMain) {

		this.mCtx = mCtx;
		this.userAccountsList = userAccountsListMain;
	}

	private void deleteAccount(int position) {

		userAccountsList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, userAccountsList.size());
		Toasty.info(mCtx, mCtx.getResources().getString(R.string.accountDeletedMessage));

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

		holder.userId.setText(String.format("@%s", currentItem.getUserName()));
		holder.accountUrl.setText(url);

		if(tinyDB.getInt("currentActiveAccountId") == currentItem.getAccountId()) {
			holder.activeAccount.setVisibility(View.VISIBLE);
		}
		else {
			holder.deleteAccount.setVisibility(View.GONE);
		}

	}

	@Override
	public int getItemCount() {
		return userAccountsList.size();
	}

}
