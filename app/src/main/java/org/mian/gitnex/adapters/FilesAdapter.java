package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import org.apache.commons.io.FileUtils;
import org.gitnex.tea4j.models.Files;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> implements Filterable {

	private final List<Files> originalFiles = new ArrayList<>();
	private final List<Files> alteredFiles = new ArrayList<>();

    private final Context mCtx;

    private final FilesAdapterListener filesListener;

    public interface FilesAdapterListener {

        void onClickDir(String str);
        void onClickFile(String str);
    }

	class FilesViewHolder extends RecyclerView.ViewHolder {

    	private String fileType;

        private final ImageView fileTypeIs;
        private final TextView fileName;
        private final TextView fileInfo;

        private FilesViewHolder(View itemView) {

            super(itemView);

            fileName = itemView.findViewById(R.id.fileName);
	        fileTypeIs = itemView.findViewById(R.id.fileTypeIs);
            fileInfo = itemView.findViewById(R.id.fileInfo);

            //ImageView filesDropdownMenu = itemView.findViewById(R.id.filesDropdownMenu);

            fileName.setOnClickListener(v -> {

                Context context = v.getContext();

                if(fileType.equals("file")) {
                    filesListener.onClickFile(fileName.getText().toString());
                }
                else if(fileType.equals("dir")) {
                    filesListener.onClickDir(fileName.getText().toString());
                }
                else {
                    Toasty.warning(context, context.getString(R.string.filesGenericError));
                }

            });


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

    public FilesAdapter(Context mCtx, FilesAdapterListener filesListener) {

        this.mCtx = mCtx;
        this.filesListener = filesListener;

    }

	public List<Files> getOriginalFiles() {
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

        Files currentItem = alteredFiles.get(position);

        holder.fileType = currentItem.getType();
        holder.fileName.setText(currentItem.getName());

        if(currentItem.getType().equals("file")) {

            holder.fileTypeIs.setImageDrawable(AppCompatResources.getDrawable(mCtx, R.drawable.ic_file));
            holder.fileInfo.setVisibility(View.VISIBLE);
            holder.fileInfo.setText(FileUtils.byteCountToDisplaySize(currentItem.getSize()));
        }
        else if(currentItem.getType().equals("dir")) {

	        holder.fileTypeIs.setImageDrawable(AppCompatResources.getDrawable(mCtx, R.drawable.ic_directory));
	        holder.fileInfo.setVisibility(View.GONE);
        }
        else {

	        holder.fileTypeIs.setImageDrawable(AppCompatResources.getDrawable(mCtx, R.drawable.ic_question));
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

    private final Filter filesFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<Files> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(originalFiles);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Files item : originalFiles) {
                    if (item.getName().toLowerCase().contains(filterPattern) || item.getPath().toLowerCase().contains(filterPattern)) {
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

}
