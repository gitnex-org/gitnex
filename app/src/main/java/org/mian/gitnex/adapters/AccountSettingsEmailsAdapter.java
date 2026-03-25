package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.gitnex.tea4j.v2.models.Email;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListAccountSettingsEmailsBinding;
import org.mian.gitnex.helpers.AvatarGenerator;

/**
 * @author mmarif
 */
public class AccountSettingsEmailsAdapter
		extends RecyclerView.Adapter<AccountSettingsEmailsAdapter.EmailsViewHolder> {

	private final List<Email> emailsList;
	private final Context context;
	private final OnEmailInteractionListener listener;

	public interface OnEmailInteractionListener {
		void onDeleteEmail(Email email, int position);
	}

	public AccountSettingsEmailsAdapter(
			List<Email> emailsList, Context ctx, OnEmailInteractionListener listener) {
		this.context = ctx;
		this.emailsList = emailsList;
		this.listener = listener;
	}

	@NonNull @Override
	public EmailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListAccountSettingsEmailsBinding binding =
				ListAccountSettingsEmailsBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new EmailsViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull EmailsViewHolder holder, int position) {
		Email currentItem = emailsList.get(position);
		holder.binding.userEmail.setText(currentItem.getEmail());

		if (currentItem.isPrimary()) {
			holder.binding.primaryFrame.setVisibility(View.VISIBLE);
			holder.binding.deleteFrame.setVisibility(View.GONE);

			int labelColor =
					getThemeColor(com.google.android.material.R.attr.colorPrimaryContainer);
			String labelText = context.getString(R.string.emailTypeText);

			holder.binding.emailPrimary.setImageDrawable(
					AvatarGenerator.getLabelDrawable(context, labelText, labelColor, 24));
		} else {
			holder.binding.primaryFrame.setVisibility(View.GONE);
			holder.binding.deleteFrame.setVisibility(View.VISIBLE);
			holder.binding.deleteFrame.setOnClickListener(
					v -> listener.onDeleteEmail(currentItem, position));
		}

		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	private int getThemeColor(int attr) {
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(attr, typedValue, true);
		return typedValue.data;
	}

	@Override
	public int getItemCount() {
		return emailsList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Email> newList) {
		this.emailsList.clear();
		this.emailsList.addAll(newList);
		notifyDataSetChanged();
	}

	public static class EmailsViewHolder extends RecyclerView.ViewHolder {
		final ListAccountSettingsEmailsBinding binding;

		private EmailsViewHolder(ListAccountSettingsEmailsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
