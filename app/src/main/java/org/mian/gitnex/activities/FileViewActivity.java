package org.mian.gitnex.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.github.chrisbanes.photoview.PhotoView;
import org.apache.commons.io.FileUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityFileViewBinding;
import org.mian.gitnex.fragments.BottomSheetFileViewerFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.highlightjs.HighlightJsView;
import org.mian.gitnex.helpers.highlightjs.models.Theme;
import org.mian.gitnex.models.Files;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class FileViewActivity extends BaseActivity implements BottomSheetFileViewerFragment.BottomSheetListener {

	private View.OnClickListener onClickListener;
	private TextView singleFileContents;
	private LinearLayout singleFileContentsFrame;
	private HighlightJsView singleCodeContents;
	private PhotoView imageView;
	private ProgressBar mProgressBar;
	private byte[] imageData;
	private PDFView pdfView;
	private LinearLayout pdfViewFrame;
	private byte[] decodedPdf;
	private Boolean pdfNightMode;
	private String singleFileName;
	private String fileSha;
	private AppUtil appUtil;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_file_view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appUtil = new AppUtil();

		ActivityFileViewBinding activityFileViewBinding = ActivityFileViewBinding.inflate(getLayoutInflater());

		Toolbar toolbar = activityFileViewBinding.toolbar;
		setSupportActionBar(toolbar);

		String repoFullName = tinyDB.getString("repoFullName");
		String repoBranch = tinyDB.getString("repoBranch");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String loginUid = tinyDB.getString("loginUid");
		final String instanceToken = "token " + tinyDB.getString(loginUid + "-token");

		tinyDB.putBoolean("enableMarkdownInFileView", false);

		ImageView closeActivity = activityFileViewBinding.close;
		singleFileContents = activityFileViewBinding.singleFileContents;
		singleCodeContents = activityFileViewBinding.singleCodeContents;
		imageView = activityFileViewBinding.imageView;
		mProgressBar = activityFileViewBinding.progressBar;
		pdfView = activityFileViewBinding.pdfView;
		pdfViewFrame = activityFileViewBinding.pdfViewFrame;
		singleFileContentsFrame = activityFileViewBinding.singleFileContentsFrame;

		singleFileName = getIntent().getStringExtra("singleFileName");

		TextView toolbar_title = activityFileViewBinding.toolbarTitle;
		toolbar_title.setMovementMethod(new ScrollingMovementMethod());

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		tinyDB.putString("downloadFileContents", "");

		try {

			singleFileName = URLDecoder.decode(singleFileName, "UTF-8");
			singleFileName = singleFileName.replaceAll("//", "/");
			singleFileName = singleFileName.startsWith("/") ? singleFileName.substring(1) : singleFileName;
		}
		catch(UnsupportedEncodingException e) {

			Log.i("singleFileName", singleFileName);
		}

		toolbar_title.setText(singleFileName);

		getSingleFileContents(instanceToken, repoOwner, repoName, singleFileName, repoBranch);
	}

	@Override
	public void onResume() {

		super.onResume();

		String repoFullName = tinyDB.getString("repoFullName");
		String repoBranch = tinyDB.getString("repoBranch");
		String[] parts = repoFullName.split("/");
		String repoOwner = parts[0];
		String repoName = parts[1];
		String loginUid = tinyDB.getString("loginUid");
		String instanceToken = "token " + tinyDB.getString(loginUid + "-token");

		if(tinyDB.getBoolean("fileModified")) {

			getSingleFileContents(instanceToken, repoOwner, repoName, singleFileName, repoBranch);
			tinyDB.putBoolean("fileModified", false);
		}
	}


	private void getSingleFileContents(String token, final String owner, String repo, final String filename, String ref) {

		Call<Files> call = RetrofitClient.getApiInterface(ctx).getSingleFileContents(token, owner, repo, filename, ref);

		call.enqueue(new Callback<Files>() {

			@Override
			public void onResponse(@NonNull Call<Files> call, @NonNull retrofit2.Response<Files> response) {

				if(response.code() == 200) {

					assert response.body() != null;

					if(!response.body().getContent().equals("")) {

						String fileExtension = FileUtils.getExtension(filename);
						mProgressBar.setVisibility(View.GONE);

						fileSha = response.body().getSha();

						// download file meta
						tinyDB.putString("downloadFileName", filename);
						tinyDB.putString("downloadFileContents", response.body().getContent());

						if(appUtil.imageExtension(fileExtension)) { // file is image

							singleFileContentsFrame.setVisibility(View.GONE);
							singleCodeContents.setVisibility(View.GONE);
							pdfViewFrame.setVisibility(View.GONE);
							imageView.setVisibility(View.VISIBLE);

							imageData = Base64.decode(response.body().getContent(), Base64.DEFAULT);
							Drawable imageDrawable = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
							imageView.setImageDrawable(imageDrawable);
						}
						else if(appUtil.sourceCodeExtension(fileExtension)) { // file is sourcecode

							imageView.setVisibility(View.GONE);
							singleFileContentsFrame.setVisibility(View.GONE);
							pdfViewFrame.setVisibility(View.GONE);
							singleCodeContents.setVisibility(View.VISIBLE);

							switch(tinyDB.getInt("fileviewerSourceCodeThemeId")) {

								case 1:

									singleCodeContents.setTheme(Theme.ARDUINO_LIGHT);
									break;
								case 2:

									singleCodeContents.setTheme(Theme.GITHUB);
									break;
								case 3:

									singleCodeContents.setTheme(Theme.FAR);
									break;
								case 4:

									singleCodeContents.setTheme(Theme.IR_BLACK);
									break;
								case 5:

									singleCodeContents.setTheme(Theme.ANDROID_STUDIO);
									break;
								default:

									singleCodeContents.setTheme(Theme.MONOKAI_SUBLIME);
							}

							singleCodeContents.setSource(appUtil.decodeBase64(response.body().getContent()));
						}
						else if(appUtil.pdfExtension(fileExtension)) { // file is pdf

							imageView.setVisibility(View.GONE);
							singleFileContentsFrame.setVisibility(View.GONE);
							singleCodeContents.setVisibility(View.GONE);
							pdfViewFrame.setVisibility(View.VISIBLE);

							pdfNightMode = tinyDB.getBoolean("enablePdfMode");

							decodedPdf = Base64.decode(response.body().getContent(), Base64.DEFAULT);
							pdfView.fromBytes(decodedPdf).enableSwipe(true).swipeHorizontal(false).enableDoubletap(true).defaultPage(0).enableAnnotationRendering(false).password(null).scrollHandle(null).enableAntialiasing(true).spacing(0).autoSpacing(true).pageFitPolicy(FitPolicy.WIDTH).fitEachPage(true).pageSnap(false).pageFling(true).nightMode(pdfNightMode).load();

						}
						else if(appUtil.excludeFilesInFileViewerExtension(fileExtension)) { // files need to be excluded

							imageView.setVisibility(View.GONE);
							singleCodeContents.setVisibility(View.GONE);
							pdfViewFrame.setVisibility(View.GONE);
							singleFileContentsFrame.setVisibility(View.VISIBLE);

							singleFileContents.setText(getResources().getString(R.string.excludeFilesInFileviewer));
							singleFileContents.setGravity(Gravity.CENTER);
							singleFileContents.setTypeface(null, Typeface.BOLD);
						}
						else { // file type not known - plain text view

							imageView.setVisibility(View.GONE);
							singleCodeContents.setVisibility(View.GONE);
							pdfViewFrame.setVisibility(View.GONE);
							singleFileContentsFrame.setVisibility(View.VISIBLE);

							singleFileContents.setText(appUtil.decodeBase64(response.body().getContent()));
						}
					}
					else {

						singleFileContents.setText("");
						mProgressBar.setVisibility(View.GONE);
					}
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle), getResources().getString(R.string.alertDialogTokenRevokedMessage), getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
				}
				else if(response.code() == 403) {

					Toasty.error(ctx, ctx.getString(R.string.authorizeError));
				}
				else if(response.code() == 404) {

					Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
				}
				else {

					Toasty.error(ctx, getString(R.string.labelGeneralError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<Files> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.generic_nav_dotted_menu, menu);
		inflater.inflate(R.menu.files_view_menu, menu);

		String fileExtension = FileUtils.getExtension(singleFileName);

		if(!fileExtension.equalsIgnoreCase("md")) {

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
			bottomSheet.show(getSupportFragmentManager(), "fileViewerBottomSheet");
			return true;
		}
		else if(id == R.id.markdown) {

			new Markdown(ctx, appUtil.decodeBase64(tinyDB.getString("downloadFileContents")), singleFileContents);

			if(!tinyDB.getBoolean("enableMarkdownInFileView")) {

				singleCodeContents.setVisibility(View.GONE);
				singleFileContentsFrame.setVisibility(View.VISIBLE);
				singleFileContents.setVisibility(View.VISIBLE);
				tinyDB.putBoolean("enableMarkdownInFileView", true);
			}
			else {

				singleCodeContents.setVisibility(View.VISIBLE);
				singleFileContentsFrame.setVisibility(View.GONE);
				singleFileContents.setVisibility(View.GONE);
				singleCodeContents.setSource(appUtil.decodeBase64(tinyDB.getString("downloadFileContents")));
				tinyDB.putBoolean("enableMarkdownInFileView", false);
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

			String fileExtension = FileUtils.getExtension(singleFileName);
			String data = appUtil.decodeBase64(tinyDB.getString("downloadFileContents"));
			Intent intent = new Intent(ctx, CreateFileActivity.class);
			intent.putExtra("fileAction", 1);
			intent.putExtra("filePath", singleFileName);
			intent.putExtra("fileSha", fileSha);

			if(!appUtil.imageExtension(fileExtension)) {

				intent.putExtra("fileContents", data);
			}
			else {

				intent.putExtra("fileContents", "");
			}

			ctx.startActivity(intent);
		}

		if("editFile".equals(text)) {

			String fileExtension = FileUtils.getExtension(singleFileName);
			String data = appUtil.decodeBase64(tinyDB.getString("downloadFileContents"));
			Intent intent = new Intent(ctx, CreateFileActivity.class);
			intent.putExtra("fileAction", 2);
			intent.putExtra("filePath", singleFileName);
			intent.putExtra("fileSha", fileSha);

			if(!appUtil.imageExtension(fileExtension)) {

				intent.putExtra("fileContents", data);
			}
			else {

				intent.putExtra("fileContents", "");
			}

			ctx.startActivity(intent);
		}

	}

	private void requestFileDownload() {

		if(!tinyDB.getString("downloadFileContents").isEmpty()) {

			File outputFileName = new File(tinyDB.getString("downloadFileName"));

			Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			intent.putExtra(Intent.EXTRA_TITLE, outputFileName.getName());

			fileDownloadActivityResultLauncher.launch(intent);
		}
		else {

			Toasty.warning(ctx, getString(R.string.waitLoadingDownloadFile));
		}
	}

	ActivityResultLauncher<Intent> fileDownloadActivityResultLauncher = registerForActivityResult(
		new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

		@Override
		public void onActivityResult(ActivityResult result) {

			if (result.getResultCode() == Activity.RESULT_OK) {

				Intent data = result.getData();

				try {

					assert data != null;
					Uri uri = data.getData();

					assert uri != null;
					OutputStream outputStream = getContentResolver().openOutputStream(uri);

					byte[] dataAsBytes = Base64.decode(tinyDB.getString("downloadFileContents"), 0);

					assert outputStream != null;
					outputStream.write(dataAsBytes);
					outputStream.close();

					Toasty.success(ctx, getString(R.string.downloadFileSaved));

				}
				catch(IOException e) {

					Log.e("errorFileDownloading", Objects.requireNonNull(e.getMessage()));
				}
			}
		}
	});

	private void initCloseListener() {

		onClickListener = view -> {

			getIntent().removeExtra("singleFileName");
			finish();
		};
	}

}
