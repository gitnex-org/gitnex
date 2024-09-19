package org.mian.gitnex.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.gitnex.tea4j.v2.models.PublicKey;
import org.mian.gitnex.R;

/**
 * @author M M Arif
 */
public class SSHKeysAdapter extends RecyclerView.Adapter<SSHKeysAdapter.KeysViewHolder> {

	private final List<PublicKey> keysList;

	public SSHKeysAdapter(List<PublicKey> keysListMain) {
		this.keysList = keysListMain;
	}

	@NonNull @Override
	public SSHKeysAdapter.KeysViewHolder onCreateViewHolder(
			@NonNull ViewGroup parent, int viewType) {
		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_account_settings_ssh_keys, parent, false);
		return new SSHKeysAdapter.KeysViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull SSHKeysAdapter.KeysViewHolder holder, int position) {

		PublicKey currentItem = keysList.get(position);

		holder.keyName.setText(currentItem.getTitle());
		holder.key.setText(currentItem.getKey());
	}

	@Override
	public int getItemCount() {
		return keysList.size();
	}

	public static class KeysViewHolder extends RecyclerView.ViewHolder {

		private final TextView keyName;
		private final TextView key;

		private KeysViewHolder(View itemView) {
			super(itemView);

			keyName = itemView.findViewById(R.id.keyName);
			key = itemView.findViewById(R.id.key);
		}
	}
}
