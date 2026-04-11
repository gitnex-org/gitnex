package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.gitnex.tea4j.v2.models.PublicKey;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListAccountSettingsSshKeysBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class AccountSettingsSSHKeysAdapter
		extends RecyclerView.Adapter<AccountSettingsSSHKeysAdapter.KeysViewHolder> {

	private final List<PublicKey> keysList;
	private final Context context;
	private final OnKeyInteractionListener listener;

	public interface OnKeyInteractionListener {
		void onDeleteKey(PublicKey key, int position);
	}

	public AccountSettingsSSHKeysAdapter(
			List<PublicKey> keysList, Context context, OnKeyInteractionListener listener) {
		this.keysList = keysList;
		this.context = context;
		this.listener = listener;
	}

	@NonNull @Override
	public KeysViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListAccountSettingsSshKeysBinding binding =
				ListAccountSettingsSshKeysBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new KeysViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull KeysViewHolder holder, int position) {
		PublicKey currentKey = keysList.get(position);
		holder.binding.keyTitle.setText(currentKey.getTitle());
		holder.binding.keyFingerprint.setText(currentKey.getKey());

		holder.binding.copyFrame.setOnClickListener(
				v -> {
					AppUtil.copyToClipboard(
							context,
							currentKey.getKey(),
							context.getString(R.string.copied_to_clipboard));
				});

		holder.binding.deleteFrame.setOnClickListener(
				v -> listener.onDeleteKey(currentKey, position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return keysList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<PublicKey> newList) {
		this.keysList.clear();
		this.keysList.addAll(newList);
		notifyDataSetChanged();
	}

	public static class KeysViewHolder extends RecyclerView.ViewHolder {
		final ListAccountSettingsSshKeysBinding binding;

		KeysViewHolder(ListAccountSettingsSshKeysBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
