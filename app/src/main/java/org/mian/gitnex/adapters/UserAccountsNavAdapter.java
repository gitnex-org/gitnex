package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.LoginActivity;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.UrlHelper;

/**
 * @author mmarif
 */
public class UserAccountsNavAdapter
		extends RecyclerView.Adapter<UserAccountsNavAdapter.UserAccountsViewHolder> {

	private final List<UserAccount> userAccountsList;
	private final Context context;

	public UserAccountsNavAdapter(Context ctx, List<UserAccount> userAccountsListMain) {

		this.context = ctx;
		this.userAccountsList = userAccountsListMain;
	}

	@NonNull @Override
	public UserAccountsNavAdapter.UserAccountsViewHolder onCreateViewHolder(
			@NonNull ViewGroup parent, int viewType) {

		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.nav_user_accounts, parent, false);
		return new UserAccountsViewHolder(v);
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void onBindViewHolder(
			@NonNull UserAccountsNavAdapter.UserAccountsViewHolder holder, int position) {

		UserAccount currentItem = userAccountsList.get(position);

		Glide.with(context)
				.load(UrlHelper.appendPath(currentItem.getInstanceUrl(), "assets/img/favicon.png"))
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.centerCrop()
				.into(holder.userAccountAvatar);
	}

	@Override
	public int getItemCount() {

		return Math.min(userAccountsList.size(), 3);
	}

	private void customDialogUserAccountsList() {

		Dialog dialog = new Dialog(context, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
		dialog.setContentView(R.layout.custom_user_accounts_dialog);

		RecyclerView listView = dialog.findViewById(R.id.accountsList);
		Button newAccount = dialog.findViewById(R.id.newAccount);

		if (dialog.getWindow() != null) {
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		newAccount.setOnClickListener(
				item -> {
					Intent intent = new Intent(context, LoginActivity.class);
					intent.putExtra("mode", "new_account");
					context.startActivity(intent);
					dialog.dismiss();
				});

		UserAccountsAdapter arrayAdapter = new UserAccountsAdapter(context, dialog);
		listView.setLayoutManager(new LinearLayoutManager(context));
		listView.setAdapter(arrayAdapter);
		dialog.show();
	}

	public class UserAccountsViewHolder extends RecyclerView.ViewHolder {

		private final ImageView userAccountAvatar;

		private UserAccountsViewHolder(View itemView) {

			super(itemView);

			userAccountAvatar = itemView.findViewById(R.id.userAccountAvatar);

			itemView.setOnClickListener(
					item -> {
						customDialogUserAccountsList();
					});
		}
	}
}
