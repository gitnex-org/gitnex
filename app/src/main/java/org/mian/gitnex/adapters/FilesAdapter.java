package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import org.apache.commons.io.FileUtils;
import org.gitnex.tea4j.v2.models.ContentsResponse;
import org.mian.gitnex.R;
import java.util.ArrayList;
import java.util.List;

/**
 * @author M M Arif
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> implements Filterable {

	private final List<ContentsResponse> originalFiles = new ArrayList<>();
	private final List<ContentsResponse> alteredFiles = new ArrayList<>();

	private final Context context;

	private final FilesAdapterListener filesListener;
	private final Filter filesFilter = new Filter() {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			List<ContentsResponse> filteredList = new ArrayList<>();

			if(constraint == null || constraint.length() == 0) {
				filteredList.addAll(originalFiles);
			}
			else {
				String filterPattern = constraint.toString().toLowerCase().trim();

				for(ContentsResponse item : originalFiles) {
					if(item.getName().toLowerCase().contains(filterPattern) || item.getPath().toLowerCase().contains(filterPattern)) {
						filteredList.add(item);
					}
				}
			}

			FilterResults results = new FilterResults();
			results.values = filteredList;

			return results;

		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {

			alteredFiles.clear();
			alteredFiles.addAll((List) results.values);

			notifyDataSetChanged();

		}

	};

	public FilesAdapter(Context ctx, FilesAdapterListener filesListener) {

		this.context = ctx;
		this.filesListener = filesListener;
	}

	public List<ContentsResponse> getOriginalFiles() {
		return originalFiles;
	}

	public void notifyOriginalDataSetChanged() {

		alteredFiles.clear();
		alteredFiles.addAll(originalFiles);

		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public FilesAdapter.FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_files, parent, false);
		return new FilesAdapter.FilesViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull FilesAdapter.FilesViewHolder holder, int position) {

		ContentsResponse currentItem = alteredFiles.get(position);

		holder.file = currentItem;
		holder.fileName.setText(currentItem.getName());

		switch(currentItem.getType()) {

			case "file":
				holder.fileTypeIs.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_file));
				holder.fileInfo.setVisibility(View.VISIBLE);
				holder.fileInfo.setText(FileUtils.byteCountToDisplaySize(Math.toIntExact(currentItem.getSize())));
				break;

			case "dir":
				holder.fileTypeIs.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_directory));
				holder.fileInfo.setVisibility(View.GONE);
				break;

			case "submodule":
				holder.fileTypeIs.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_submodule));
				holder.fileInfo.setVisibility(View.GONE);
				break;

			case "symlink":
				holder.fileTypeIs.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_symlink));
				holder.fileInfo.setVisibility(View.GONE);
				break;

			default:
				holder.fileTypeIs.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_question));

		}
	}

	@Override
	public int getItemCount() {
		return alteredFiles.size();
	}

	@Override
	public Filter getFilter() {
		return filesFilter;
	}

	public interface FilesAdapterListener {

		void onClickFile(ContentsResponse file);

	}

	class FilesViewHolder extends RecyclerView.ViewHolder {

		private final ImageView fileTypeIs;
		private final TextView fileName;
		private final TextView fileInfo;
		private ContentsResponse file;

		private FilesViewHolder(View itemView) {

			super(itemView);

			LinearLayout fileFrame = itemView.findViewById(R.id.fileFrame);
			fileName = itemView.findViewById(R.id.fileName);
			fileTypeIs = itemView.findViewById(R.id.fileTypeIs);
			fileInfo = itemView.findViewById(R.id.fileInfo);

			fileFrame.setOnClickListener(v -> filesListener.onClickFile(file));

			//ImageView filesDropdownMenu = itemView.findViewById(R.id.filesDropdownMenu);

            /*filesDropdownMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Context context = v.getContext();
                    Context context_ = new ContextThemeWrapper(context, R.style.popupMenuStyle);

                    PopupMenu popupMenu = new PopupMenu(context_, v);
                    popupMenu.inflate(R.menu.files_dotted_list_menu);

                    Object menuHelper;
                    Class[] argTypes;
                    try {

                        Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
                        fMenuHelper.setAccessible(true);
                        menuHelper = fMenuHelper.get(popupMenu);
                        argTypes = new Class[] { boolean.class };
                        menuHelper.getClass().getDeclaredMethod("setForceShowIcon",
                                argTypes).invoke(menuHelper, true);

                    } catch (Exception e) {

                        popupMenu.show();
                        return;

                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.deleteFile:

                                    Intent intent = new Intent(context, DeleteFileActivity.class);
                                    intent.putExtra("repoFullNameForDeleteFile", fullName.getText());
                                    context.startActivity(intent);
                                    break;

                                case R.id.editFile:

                                    Intent intentW = new Intent(context, EditFileActivity.class);
                                    intentW.putExtra("repoFullNameForEditFile", fullName.getText());
                                    context.startActivity(intentW);
                                    break;

                                case R.id.openInBrowser:

                                    Intent intentOpenInBrowser = new Intent(context, OpenFileInBrowserActivity.class);
                                    intentOpenInBrowser.putExtra("fileFullNameBrowser", fullName.getText());
                                    context.startActivity(intentOpenInBrowser);
                                    break;

                            }
                            return false;
                        }
                    });

                    popupMenu.show();

                }
            });*/

		}

	}

}
