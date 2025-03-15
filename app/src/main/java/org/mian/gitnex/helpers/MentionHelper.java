package org.mian.gitnex.helpers;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.InlineResponse2001;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class MentionHelper {

	private final PopupWindow mentionPopup = new PopupWindow();
	private final MentionAdapter mentionAdapter;
	private final List<User> mentionSuggestions = new ArrayList<>();
	private final Context context;
	private final EditText editText;

	public MentionHelper(Context context, EditText editText) {

		this.context = context;
		this.editText = editText;

		RecyclerView mentionRecyclerView = new RecyclerView(context);
		mentionRecyclerView.setLayoutManager(new LinearLayoutManager(context));

		mentionAdapter =
				new MentionAdapter(
						context,
						mentionSuggestions,
						user -> {
							String mention = "@" + user.getLogin();
							int cursorPos = editText.getSelectionStart();
							String text = editText.getText().toString();
							int mentionStart = text.lastIndexOf("@", cursorPos - 1);
							if (mentionStart != -1) {
								editText.getText().replace(mentionStart, cursorPos, mention + " ");
								mentionPopup.dismiss();
							}
						});
		mentionRecyclerView.setAdapter(mentionAdapter);

		int paddingDp = 4;
		int sidePaddingDp = 12;
		int paddingPx = (int) (paddingDp * context.getResources().getDisplayMetrics().density);
		int sidePaddingPx =
				(int) (sidePaddingDp * context.getResources().getDisplayMetrics().density);
		mentionRecyclerView.setPadding(sidePaddingPx, paddingPx, sidePaddingPx, paddingPx);

		mentionPopup.setContentView(mentionRecyclerView);
		mentionPopup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		mentionPopup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		mentionPopup.setFocusable(false);
		mentionPopup.setBackgroundDrawable(
				ContextCompat.getDrawable(context, R.drawable.shape_round_corners));
		mentionPopup.setElevation(8f);
	}

	public void setup() {

		editText.addTextChangedListener(
				new TextWatcher() {
					@Override
					public void beforeTextChanged(
							CharSequence s, int start, int count, int after) {}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						handleMentionInput(s);
					}

					@Override
					public void afterTextChanged(Editable s) {}
				});
	}

	public void dismissPopup() {
		if (mentionPopup.isShowing()) {
			mentionPopup.dismiss();
		}
	}

	private void handleMentionInput(CharSequence text) {

		int cursorPos = editText.getSelectionStart();

		if (cursorPos > 0) {

			String beforeCursor = text.subSequence(0, cursorPos).toString();
			int atIndex = beforeCursor.lastIndexOf("@");

			if (atIndex != -1
					&& (cursorPos == atIndex + 1
							|| !beforeCursor.substring(atIndex).contains(" "))) {

				String query = beforeCursor.substring(atIndex + 1);
				if (!query.isEmpty()) {
					fetchUserSuggestions(query);
				} else {
					int oldSize = mentionSuggestions.size();
					mentionSuggestions.clear();
					if (oldSize > 0) {
						mentionAdapter.notifyItemRangeRemoved(0, oldSize);
					}
					mentionPopup.dismiss();
				}
			} else {
				int oldSize = mentionSuggestions.size();
				mentionSuggestions.clear();
				if (oldSize > 0) {
					mentionAdapter.notifyItemRangeRemoved(0, oldSize);
				}
				mentionPopup.dismiss();
			}
		} else {
			int oldSize = mentionSuggestions.size();
			mentionSuggestions.clear();
			if (oldSize > 0) {
				mentionAdapter.notifyItemRangeRemoved(0, oldSize);
			}
			mentionPopup.dismiss();
		}
	}

	private void fetchUserSuggestions(String query) {

		Call<InlineResponse2001> call =
				RetrofitClient.getApiInterface(context).userSearch(query, null, 1, 4);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<InlineResponse2001> call,
							@NonNull Response<InlineResponse2001> response) {

						if (response.isSuccessful()
								&& response.body() != null
								&& response.body().isOk()) {
							int oldSize = mentionSuggestions.size();
							mentionSuggestions.clear();
							if (oldSize > 0) {
								mentionAdapter.notifyItemRangeRemoved(0, oldSize);
							}
							List<User> newData = response.body().getData();
							if (newData != null && !newData.isEmpty()) {
								mentionSuggestions.addAll(
										newData.subList(0, Math.min(newData.size(), 4)));
								mentionAdapter.notifyItemRangeInserted(
										0, mentionSuggestions.size());
								showMentionPopup();
							} else {
								mentionPopup.dismiss();
							}
						} else {
							int oldSize = mentionSuggestions.size();
							mentionSuggestions.clear();
							if (oldSize > 0) {
								mentionAdapter.notifyItemRangeRemoved(0, oldSize);
							}
							mentionPopup.dismiss();
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<InlineResponse2001> call, @NonNull Throwable t) {

						int oldSize = mentionSuggestions.size();
						mentionSuggestions.clear();
						if (oldSize > 0) {
							mentionAdapter.notifyItemRangeRemoved(0, oldSize);
						}
						mentionPopup.dismiss();
						Log.e("MentionHelper", "Failed to fetch users: " + t.getMessage());
					}
				});
	}

	private void showMentionPopup() {

		int[] location = new int[2];
		editText.getLocationOnScreen(location);
		int x = location[0];
		int y = location[1] - 24;

		int popupWidth = editText.getWidth();
		int popupHeight = calculatePopupHeight();

		if (mentionPopup.isShowing()) {
			mentionPopup.dismiss();
		}

		mentionPopup.setWidth(popupWidth);
		mentionPopup.setHeight(popupHeight);
		mentionPopup.showAtLocation(editText, Gravity.NO_GRAVITY, x, y - popupHeight);
	}

	private int calculatePopupHeight() {

		int itemHeightDp = 48;
		int paddingDp = 4;
		int itemHeightPx =
				(int) (itemHeightDp * context.getResources().getDisplayMetrics().density);
		int paddingPx = (int) (paddingDp * context.getResources().getDisplayMetrics().density);

		int contentHeightPx = mentionSuggestions.size() * itemHeightPx;
		int totalPaddingPx = paddingPx * 2;
		int popupHeight = contentHeightPx + totalPaddingPx;

		int maxHeightPx = 4 * itemHeightPx + totalPaddingPx;
		popupHeight = Math.min(popupHeight, maxHeightPx);

		if (mentionSuggestions.isEmpty()) {
			popupHeight = itemHeightPx + totalPaddingPx;
		}

		return popupHeight;
	}

	private static class MentionAdapter
			extends RecyclerView.Adapter<MentionAdapter.MentionViewHolder> {
		private final Context context;
		private final List<User> users;
		private final OnUserClickListener listener;

		public MentionAdapter(Context context, List<User> users, OnUserClickListener listener) {
			this.context = context;
			this.users = users;
			this.listener = listener;
		}

		@NonNull @Override
		public MentionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view =
					LayoutInflater.from(context)
							.inflate(R.layout.list_users_mention, parent, false);
			return new MentionViewHolder(view);
		}

		@Override
		public void onBindViewHolder(MentionViewHolder holder, int position) {

			User user = users.get(position);
			String displayName =
					(user.getFullName() != null && !user.getFullName().isEmpty())
							? user.getFullName() + " (" + user.getLogin() + ")"
							: user.getLogin();
			holder.username.setText(displayName);

			Glide.with(context)
					.load(user.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(holder.avatar);

			holder.itemView.setBackground(null);
			holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
		}

		@Override
		public int getItemCount() {
			return users.size();
		}

		static class MentionViewHolder extends RecyclerView.ViewHolder {
			ImageView avatar;
			TextView username;

			MentionViewHolder(View itemView) {
				super(itemView);
				avatar = itemView.findViewById(R.id.avatar);
				username = itemView.findViewById(R.id.username);
			}
		}

		interface OnUserClickListener {
			void onUserClick(User user);
		}
	}
}
