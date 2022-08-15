package org.mian.gitnex.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.io.FilenameUtils;
import org.gitnex.tea4j.v2.models.ContentsResponse;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityFileViewBinding;
import org.mian.gitnex.fragments.BottomSheetFileViewerFragment;
import org.mian.gitnex.helpers.*;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.notifications.Notifications;
import org.mian.gitnex.structs.BottomSheetListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class FileViewActivity extends BaseActivity implements BottomSheetListener {

	private ActivityFileViewBinding binding;
	private ContentsResponse file;
	private RepositoryContext repository;
	ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

		if(result.getResultCode() == Activity.RESULT_OK) {

			assert result.getData() != null;

			try {

				OutputStream outputStream = getContentResolver().openOutputStream(result.getData().getData());

				NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, ctx.getPackageName()).setContentTitle(getString(R.string.fileViewerNotificationTitleStarted))
					.setContentText(getString(R.string.fileViewerNotificationDescriptionStarted, file.getName())).setSmallIcon(R.drawable.gitnex_transparent).setPriority(NotificationCompat.PRIORITY_LOW)
					.setChannelId(Constants.downloadNotificationChannelId).setProgress(100, 0, false).setOngoing(true);

				int notificationId = Notifications.uniqueNotificationId(ctx);

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(notificationId, builder.build());

				Thread thread = new Thread(() -> {

					try {

						Call<ResponseBody> call = RetrofitClient.getWebInterface(ctx).getFileContents(repository.getOwner(), repository.getName(), repository.getBranchRef(), file.getPath());

						Response<ResponseBody> response = call.execute();

						assert response.body() != null;

						AppUtil.copyProgress(response.body().byteStream(), outputStream, file.getSize(), progress -> {
							builder.setProgress(100, progress, false);
							notificationManager.notify(notificationId, builder.build());
						});

						builder.setContentTitle(getString(R.string.fileViewerNotificationTitleFinished)).setContentText(getString(R.string.fileViewerNotificationDescriptionFinished, file.getName()));

					}
					catch(IOException ignored) {

						builder.setContentTitle(getString(R.string.fileViewerNotificationTitleFailed)).setContentText(getString(R.string.fileViewerNotificationDescriptionFailed, file.getName()));

					}
					finally {

						builder.setProgress(0, 0, false).setOngoing(false);

						notificationManager.notify(notificationId, builder.build());

					}
				});

				thread.start();

			}
			catch(IOException ignored) {
			}
		}

	});
	private boolean renderMd = false;
	private boolean processable = false;
	public ActivityResultLauncher<Intent> editFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		if(result.getResultCode() == 200) {
			assert result.getData() != null;
			if(result.getData().getBooleanExtra("fileModified", false)) {
				switch(result.getData().getIntExtra("fileAction", CreateFileActivity.FILE_ACTION_EDIT)) {
					case CreateFileActivity.FILE_ACTION_CREATE:
					case CreateFileActivity.FILE_ACTION_EDIT:
						getSingleFileContents(repository.getOwner(), repository.getName(), file.getPath(), repository.getBranchRef());
						break;
					default:
						finish();
				}
			}
		}
	});

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityFileViewBinding.inflate(getLayoutInflater());
		repository = RepositoryContext.fromIntent(getIntent());

		setContentView(binding.getRoot());
		setSupportActionBar(binding.toolbar);

		file = (ContentsResponse) getIntent().getSerializableExtra("file");

		binding.close.setOnClickListener(view -> finish());

		binding.toolbarTitle.setMovementMethod(new ScrollingMovementMethod());
		binding.toolbarTitle.setText(file.getPath());

		getSingleFileContents(repository.getOwner(), repository.getName(), file.getPath(), repository.getBranchRef());
	}

	private void getSingleFileContents(final String owner, String repo, final String filename, String ref) {

		Thread thread = new Thread(() -> {

			Call<ResponseBody> call = RetrofitClient.getWebInterface(ctx).getFileContents(owner, repo, ref, filename);

			try {

				Response<ResponseBody> response = call.execute();

				if(response.code() == 200) {

					ResponseBody responseBody = response.body();

					if(responseBody != null) {

						runOnUiThread(() -> binding.progressBar.setVisibility(View.GONE));
						String fileExtension = FilenameUtils.getExtension(filename);

						switch(AppUtil.getFileType(fileExtension)) {

							case IMAGE:

								// See https://developer.android.com/guide/topics/media/media-formats#core
								if(Arrays.asList("bmp", "gif", "jpg", "jpeg", "png", "webp", "heic", "heif").contains(fileExtension.toLowerCase())) {

									byte[] pictureBytes = responseBody.bytes();

									Bitmap image = Images.scaleImage(pictureBytes, 1920);
									processable = image != null;
									if(processable) {
										runOnUiThread(() -> {
											binding.contents.setVisibility(View.GONE);
											binding.markdownFrame.setVisibility(View.GONE);

											binding.photoView.setVisibility(View.VISIBLE);
											binding.photoView.setImageBitmap(image);
										});
									}
								}
								break;

							case UNKNOWN:
							case TEXT:

								if(file.getSize() > Constants.maximumFileViewerSize) {
									break;
								}

								processable = true;
								String text = responseBody.string();

								runOnUiThread(() -> {
									binding.photoView.setVisibility(View.GONE);

									binding.contents.setContent(text, fileExtension);

									if(renderMd) {
										Markdown.render(ctx, EmojiParser.parseToUnicode(text), binding.markdown, repository);

										binding.contents.setVisibility(View.GONE);
										binding.markdownFrame.setVisibility(View.VISIBLE);
									}
									else {
										binding.markdownFrame.setVisibility(View.GONE);
										binding.contents.setVisibility(View.VISIBLE);
									}
								});
								break;

						}

						if(!processable) { // While the file could still be non-binary,
							// it's better we don't show it (to prevent any crashes and/or unwanted behavior) and let the user download it instead.
							responseBody.close();

							runOnUiThread(() -> {
								binding.photoView.setVisibility(View.GONE);
								binding.contents.setVisibility(View.GONE);

								binding.markdownFrame.setVisibility(View.VISIBLE);
								binding.markdown.setVisibility(View.GONE);
								binding.markdownTv.setVisibility(View.VISIBLE);
								binding.markdownTv.setText(getString(R.string.excludeFilesInFileViewer));
								binding.markdownTv.setGravity(Gravity.CENTER);
								binding.markdownTv.setTypeface(null, Typeface.BOLD);
							});
						}
					}
					else {

						runOnUiThread(() -> {
							binding.markdownTv.setText("");
							binding.progressBar.setVisibility(View.GONE);
						});
					}
				}
				else {

					switch(response.code()) {

						case 401:
							runOnUiThread(() -> AlertDialogs.authorizationTokenRevokedDialog(ctx));
							break;

						case 403:
							runOnUiThread(() -> Toasty.error(ctx, ctx.getString(R.string.authorizeError)));
							break;

						case 404:
							runOnUiThread(() -> Toasty.warning(ctx, ctx.getString(R.string.apiNotFound)));
							break;

						default:
							runOnUiThread(() -> Toasty.error(ctx, getString(R.string.genericError)));

					}
				}
			}
			catch(IOException ignored) {
			}

		});

		thread.start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.generic_nav_dotted_menu, menu);
		inflater.inflate(R.menu.markdown_switcher, menu);

		if(!FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("md")) {

			menu.getItem(0).setVisible(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if(id == android.R.id.home) {

			finish();
			return true;

		}
		else if(id == R.id.genericMenu) {

			BottomSheetFileViewerFragment bottomSheet = new BottomSheetFileViewerFragment();
			Bundle opts = repository.getBundle();
			opts.putBoolean("editable", processable);
			bottomSheet.setArguments(opts);
			bottomSheet.show(getSupportFragmentManager(), "fileViewerBottomSheet");
			return true;

		}
		else if(id == R.id.markdown) {

			if(!renderMd) {
				if(binding.markdown.getAdapter() == null) {
					Markdown.render(ctx, EmojiParser.parseToUnicode(binding.contents.getContent()), binding.markdown, repository);
				}

				binding.contents.setVisibility(View.GONE);
				binding.markdownFrame.setVisibility(View.VISIBLE);

				renderMd = true;
			}
			else {
				binding.markdownFrame.setVisibility(View.GONE);
				binding.contents.setVisibility(View.VISIBLE);

				renderMd = false;
			}

			return true;

		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onButtonClicked(String text) {

		if("downloadFile".equals(text)) {
			requestFileDownload();
		}

		if("deleteFile".equals(text)) {
			Intent intent = repository.getIntent(ctx, CreateFileActivity.class);
			intent.putExtra("fileAction", CreateFileActivity.FILE_ACTION_DELETE);
			intent.putExtra("filePath", file.getPath());
			intent.putExtra("fileSha", file.getSha());

			editFileLauncher.launch(intent);
		}

		if("editFile".equals(text)) {

			if(binding.contents.getContent() != null && !binding.contents.getContent().isEmpty()) {

				Intent intent = repository.getIntent(ctx, CreateFileActivity.class);

				intent.putExtra("fileAction", CreateFileActivity.FILE_ACTION_EDIT);
				intent.putExtra("filePath", file.getPath());
				intent.putExtra("fileSha", file.getSha());
				intent.putExtra("fileContents", binding.contents.getContent());

				editFileLauncher.launch(intent);

			}
			else {
				Toasty.error(ctx, getString(R.string.fileTypeCannotBeEdited));
			}
		}
	}

	private void requestFileDownload() {

		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_TITLE, file.getName());
		intent.setType("*/*");

		activityResultLauncher.launch(intent);

	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}

}
