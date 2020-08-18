package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import java.util.List;
import io.mikael.urlbuilder.UrlBuilder;

/**
 * Author M M Arif
 */

public class UserAccountsListDialogAdapter extends ArrayAdapter<UserAccount> {

	private final Context mCtx;
	private final List<UserAccount> userAccountsList;

	public UserAccountsListDialogAdapter(@NonNull Context mCtx, int resource, @NonNull List<UserAccount> objects) {

		super(mCtx, resource, objects);
		userAccountsList = objects;
		this.mCtx = mCtx;
	}

	@SuppressLint("ViewHolder")
	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		assert inflater != null;
		View rowView = inflater.inflate(R.layout.custom_user_accounts_list, parent, false);

		TinyDB tinyDB = new TinyDB(mCtx);

		ImageView profileImage = rowView.findViewById(R.id.profileImage);
		TextView userName = rowView.findViewById(R.id.userName);
		TextView accountUrl = rowView.findViewById(R.id.accountUrl);
		ImageView activeAccount = rowView.findViewById(R.id.activeAccount);

		UserAccount currentItem = userAccountsList.get(position);

		String url = UrlBuilder.fromString(currentItem.getInstanceUrl())
			.withPath("/")
			.toString();

		userName.setText(currentItem.getUserName());
		accountUrl.setText(url);

		if(tinyDB.getInt("currentActiveAccountId") == currentItem.getAccountId()) {
			activeAccount.setVisibility(View.VISIBLE);
		}

		PicassoService
			.getInstance(mCtx).get().load(url + "img/favicon.png").placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(profileImage);

		return rowView;
	}

}
