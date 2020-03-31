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
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Files;
import org.mian.gitnex.util.AppUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> implements Filterable {

    private List<Files> filesList;
    private Context mCtx;
    private List<Files> filesListFull;

    private FilesAdapterListener filesListener;

    public interface FilesAdapterListener {
        void onClickDir(String str);
        void onClickFile(String str);
    }

    class FilesViewHolder extends RecyclerView.ViewHolder {

        private ImageView fileTypeImage;
        private TextView fileName;
        private TextView fileType;
        private TextView fileInfo;

        private FilesViewHolder(View itemView) {

            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            fileTypeImage = itemView.findViewById(R.id.fileImage);
            fileType = itemView.findViewById(R.id.fileType);
            fileInfo = itemView.findViewById(R.id.fileInfo);

            //ImageView filesDropdownMenu = itemView.findViewById(R.id.filesDropdownMenu);

            fileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();

                    if(fileType.getText().toString().equals("file")) {
                        filesListener.onClickFile(fileName.getText().toString());
                    }
                    else if(fileType.getText().toString().equals("dir")) {
                        filesListener.onClickDir(fileName.getText().toString());
                    }
                    else {
                        Toasty.info(context, context.getString(R.string.filesGenericError));
                    }

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

    public FilesAdapter(Context mCtx, List<Files> filesListMain, FilesAdapterListener filesListener) {
        this.mCtx = mCtx;
        this.filesList = filesListMain;
        filesListFull = new ArrayList<>(filesList);
        this.filesListener = filesListener;
    }

    @NonNull
    @Override
    public FilesAdapter.FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_files, parent, false);
        return new FilesAdapter.FilesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FilesAdapter.FilesViewHolder holder, int position) {

        Files currentItem = filesList.get(position);

        holder.fileType.setText(currentItem.getType());
        holder.fileName.setText(currentItem.getName());

        if(currentItem.getType().equals("file")) {
            holder.fileTypeImage.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_file_new));
            holder.fileInfo.setVisibility(View.VISIBLE);
            holder.fileInfo.setText(AppUtil.formatFileSizeInDetail(currentItem.getSize()));
        }
        else if(currentItem.getType().equals("dir")) {
            holder.fileTypeImage.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_folder_24));
        }
        else {
            holder.fileTypeImage.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_question_mark_24));
        }

    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    @Override
    public Filter getFilter() {
        return filesFilter;
    }

    private Filter filesFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Files> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(filesListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Files item : filesListFull) {
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
            filesList.clear();
            filesList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}
