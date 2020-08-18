package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.fragments.UserAccountsFragment;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import io.mikael.urlbuilder.UrlBuilder;

/**
 * Author M M Arif
 */

public class UserAccountsNavAdapter extends RecyclerView.Adapter<UserAccountsNavAdapter.UserAccountsViewHolder> {

	private static DrawerLayout drawer;
	private List<UserAccount> userAccountsList;
	private Context mCtx;
	private TextView toolbarTitle;

	public UserAccountsNavAdapter(Context mCtx, List<UserAccount> userAccountsListMain, DrawerLayout drawerLayout, TextView toolbarTitle) {

		this.mCtx = mCtx;
		this.userAccountsList = userAccountsListMain;
		drawer = drawerLayout;
		this.toolbarTitle = toolbarTitle;
	}

	class UserAccountsViewHolder extends RecyclerView.ViewHolder {

		private ImageView userAccountAvatar;

		private UserAccountsViewHolder(View itemView) {

			super(itemView);

			userAccountAvatar = itemView.findViewById(R.id.userAccountAvatar);

			itemView.setOnClickListener(item -> {

				customDialogUserAccountsList(userAccountsList);
				drawer.closeDrawers();
			});

		}

	}

	@NonNull
	@Override
	public UserAccountsNavAdapter.UserAccountsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_user_accounts, parent, false);
		return new UserAccountsViewHolder(v);
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void onBindViewHolder(@NonNull UserAccountsNavAdapter.UserAccountsViewHolder holder, int position) {

		UserAccount currentItem = userAccountsList.get(position);

		String url = UrlBuilder.fromString(currentItem.getInstanceUrl())
			.withPath("/")
			.toString();

		PicassoService
			.getInstance(mCtx).get().load(url + "img/favicon.png").placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.userAccountAvatar);
	}

	@Override
	public int getItemCount() {

		return userAccountsList.size();
	}

	private void customDialogUserAccountsList(List<UserAccount> allAccountsList) {

		TinyDB tinyDB = new TinyDB(mCtx);
		Dialog dialog = new Dialog(mCtx);
		dialog.setContentView(R.layout.custom_user_accounts_dialog);

		ListView listView = dialog.findViewById(R.id.accountsList);
		TextView manageAccounts = dialog.findViewById(R.id.manageAccounts);

		if (dialog.getWindow() != null) {
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		manageAccounts.setOnClickListener(item -> {

			toolbarTitle.setText(mCtx.getResources().getString(R.string.pageTitleUserAccounts));
			AppCompatActivity activity = (AppCompatActivity) mCtx;
			activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UserAccountsFragment()).commit();
			dialog.dismiss();
		});

		UserAccountsListDialogAdapter arrayAdapter = new UserAccountsListDialogAdapter(mCtx, R.layout.custom_user_accounts_list, allAccountsList);
		listView.setAdapter(arrayAdapter);

		listView.setOnItemClickListener((adapterView, view, which, l) -> {

			String accountNameSwitch = allAccountsList.get(which).getAccountName();
			UserAccountsApi userAccountsApi = new UserAccountsApi(mCtx);
			UserAccount userAccount = userAccountsApi.getAccountData(accountNameSwitch);

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
				dialog.dismiss();
			}
		});
		dialog.show();
	}

}
