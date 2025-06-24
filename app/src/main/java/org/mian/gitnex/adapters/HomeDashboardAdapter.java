package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.helpers.TinyDB;

/**
 * @author mmarif
 */
public class HomeDashboardAdapter
		extends RecyclerView.Adapter<HomeDashboardAdapter.CategoryViewHolder> {

	private final Context context;
	private final List<CategoryData> categories;
	private final TinyDB tinyDB;
	private String username;

	public static class ItemData {
		public int iconResId;
		public String title;
		public String destinationId;

		public ItemData(int iconResId, String title, String destinationId) {
			this.iconResId = iconResId;
			this.title = title;
			this.destinationId = destinationId;
		}
	}

	public static class CategoryData {
		public String header;
		public List<ItemData> items;

		public CategoryData(String header, List<ItemData> items) {
			this.header = header;
			this.items = items;
		}
	}

	public HomeDashboardAdapter(Context context) {
		this.context = context;
		this.tinyDB = TinyDB.getInstance(context);
		this.categories = new ArrayList<>();
		this.username = tinyDB.getString("username");

		// Personal
		List<ItemData> personalItems = new ArrayList<>();
		personalItems.add(
				new ItemData(
						R.drawable.ic_trending,
						context.getString(R.string.navMostVisited),
						"nav_most_visited"));
		personalItems.add(
				new ItemData(
						R.drawable.ic_person,
						context.getString(R.string.navProfile),
						"profileActivity"));
		personalItems.add(
				new ItemData(
						R.drawable.ic_notes, context.getString(R.string.navNotes), "nav_notes"));
		categories.add(new CategoryData(context.getString(R.string.personal), personalItems));

		// Settings
		List<ItemData> settingsItems = new ArrayList<>();
		settingsItems.add(
				new ItemData(
						R.drawable.ic_account_settings,
						context.getString(R.string.navAccount),
						"nav_account_settings"));
		settingsItems.add(
				new ItemData(
						R.drawable.ic_tool,
						context.getString(R.string.navAdministration),
						"nav_administration"));
		settingsItems.add(
				new ItemData(
						R.drawable.ic_settings,
						context.getString(R.string.navSettings),
						"nav_settings"));
		categories.add(new CategoryData(context.getString(R.string.navSettings), settingsItems));
	}

	@NonNull @Override
	public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view =
				LayoutInflater.from(context)
						.inflate(R.layout.list_home_dashboard_item, parent, false);
		return new CategoryViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
		CategoryData category = categories.get(position);
		holder.header.setVisibility(View.VISIBLE);
		holder.header.setText(category.header);
		holder.categoryCard.setVisibility(View.VISIBLE);
		holder.itemContainer.setOrientation(LinearLayout.VERTICAL);
		holder.itemContainer.removeAllViews();
		for (ItemData item : category.items) {
			View itemView =
					LayoutInflater.from(context)
							.inflate(
									R.layout.list_home_dashboard_subitem,
									holder.itemContainer,
									false);
			ImageView icon = itemView.findViewById(R.id.itemIcon);
			TextView title = itemView.findViewById(R.id.itemTitle);
			icon.setImageResource(item.iconResId);
			title.setText(item.title);

			if (item.destinationId.equals("nav_administration")) {
				itemView.setVisibility(tinyDB.getBoolean("isAdmin") ? View.VISIBLE : View.GONE);
			} else {
				itemView.setVisibility(View.VISIBLE);
			}

			itemView.setOnClickListener(
					v -> {
						NavController navController = Navigation.findNavController(v);
						switch (item.destinationId) {
							case "nav_my_issues":
								navController.navigate(R.id.action_to_myIssues);
								break;
							case "nav_most_visited":
								navController.navigate(R.id.action_to_mostVisitedRepos);
								break;
							case "profileActivity":
								Intent intentProfile = new Intent(context, ProfileActivity.class);
								intentProfile.putExtra("username", username);
								context.startActivity(intentProfile);
								break;
							case "nav_notes":
								navController.navigate(R.id.action_to_notes);
								break;
							case "nav_account_settings":
								navController.navigate(R.id.action_to_accountSettings);
								break;
							case "nav_administration":
								navController.navigate(R.id.action_to_administration);
								break;
							case "nav_settings":
								navController.navigate(R.id.action_to_settings);
								break;
						}
					});
			holder.itemContainer.addView(itemView);
		}
	}

	@Override
	public int getItemCount() {
		return categories.size();
	}

	public static class CategoryViewHolder extends RecyclerView.ViewHolder {
		TextView header;
		LinearLayout itemContainer;
		com.google.android.material.card.MaterialCardView categoryCard;

		CategoryViewHolder(View itemView) {
			super(itemView);
			header = itemView.findViewById(R.id.sectionHeader);
			itemContainer = itemView.findViewById(R.id.itemContainer);
			categoryCard = itemView.findViewById(R.id.categoryCard);
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateUserInfo(String username, boolean isAdmin, String serverVersion) {
		this.username = username;
		tinyDB.putString("username", username);
		tinyDB.putBoolean("isAdmin", isAdmin);
		tinyDB.putString("serverVersion", serverVersion);
		notifyDataSetChanged();
	}
}
