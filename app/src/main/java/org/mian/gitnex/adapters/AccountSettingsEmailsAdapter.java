package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import org.gitnex.tea4j.v2.models.DeleteEmailOption;
import org.gitnex.tea4j.v2.models.Email;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class AccountSettingsEmailsAdapter
		extends RecyclerView.Adapter<AccountSettingsEmailsAdapter.EmailsViewHolder> {

	private final List<Email> emailsList;
	private final Context context;

	public AccountSettingsEmailsAdapter(Context ctx, List<Email> emailsListMain) {
		this.context = ctx;
		this.emailsList = emailsListMain;
	}

	@NonNull @Override
	public EmailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_account_settings_emails, parent, false);
		return new EmailsViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull EmailsViewHolder holder, int position) {

		Email currentItem = emailsList.get(position);

		holder.userEmail.setText(currentItem.getEmail());

		LinearLayout.LayoutParams param =
				new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						.1f);

		if (currentItem.isPrimary()) {
			holder.primaryFrame.setVisibility(View.VISIBLE);
			TextDrawable drawable =
					TextDrawable.builder()
							.beginConfig()
							.textColor(
									ResourcesCompat.getColor(
											context.getResources(), R.color.colorWhite, null))
							.fontSize(36)
							.width(220)
							.height(60)
							.endConfig()
							.buildRoundRect(
									context.getResources().getString(R.string.emailTypeText),
									ResourcesCompat.getColor(
											context.getResources(),
											R.color.tooltipBackground,
											null),
									8);
			holder.emailPrimary.setImageDrawable(drawable);
			holder.deleteFrame.setVisibility(View.GONE);
			holder.primaryFrame.setLayoutParams(param);
		} else {
			holder.primaryFrame.setVisibility(View.GONE);
			holder.deleteFrame.setVisibility(View.VISIBLE);
		}

		holder.deleteFrame.setOnClickListener(
				delEmail -> deleteEmailAddress(currentItem.getEmail(), position));
	}

	@Override
	public int getItemCount() {
		return emailsList.size();
	}

	public static class EmailsViewHolder extends RecyclerView.ViewHolder {

		private final ImageView emailPrimary;
		private final TextView userEmail;
		private final LinearLayout deleteFrame;
		private final LinearLayout primaryFrame;

		private EmailsViewHolder(View itemView) {
			super(itemView);

			emailPrimary = itemView.findViewById(R.id.emailPrimary);
			userEmail = itemView.findViewById(R.id.userEmail);
			deleteFrame = itemView.findViewById(R.id.deleteFrame);
			primaryFrame = itemView.findViewById(R.id.primaryFrame);
		}
	}

	private void updateAdapter(int position) {
		emailsList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, emailsList.size());
	}

	private void deleteEmailAddress(final String emailAddress, int position) {

		MaterialAlertDialogBuilder materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		DeleteEmailOption deleteEmailOption = new DeleteEmailOption();
		deleteEmailOption.addEmailsItem(emailAddress);

		materialAlertDialogBuilder
				.setMessage(
						String.format(
								context.getString(R.string.deleteEmailPopupText), emailAddress))
				.setPositiveButton(
						R.string.menuDeleteText,
						(dialog, whichButton) ->
								RetrofitClient.getApiInterface(context)
										.userDeleteEmailWithBody(deleteEmailOption)
										.enqueue(
												new Callback<>() {

													@Override
													public void onResponse(
															@NonNull Call<Void> call,
															@NonNull Response<Void> response) {

														if (response.isSuccessful()) {
															updateAdapter(position);
															Toasty.success(
																	context,
																	context.getString(
																			R.string
																					.deleteEmailSuccess));
														} else if (response.code() == 403) {
															Toasty.error(
																	context,
																	context.getString(
																			R.string
																					.authorizeError));
														} else {
															Toasty.error(
																	context,
																	context.getString(
																			R.string.genericError));
														}
													}

													@Override
													public void onFailure(
															@NonNull Call<Void> call,
															@NonNull Throwable t) {

														Toasty.error(
																context,
																context.getString(
																		R.string.genericError));
													}
												}))
				.setNeutralButton(R.string.cancelButton, null)
				.show();
	}
}
