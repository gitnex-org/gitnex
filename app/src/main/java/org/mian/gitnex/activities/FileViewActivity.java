package org.mian.gitnex.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.github.chrisbanes.photoview.PhotoView;
import org.apache.commons.io.FileUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.BottomSheetFileViewerFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.highlightjs.HighlightJsView;
import org.mian.gitnex.helpers.highlightjs.models.Theme;
import org.mian.gitnex.models.Files;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
	final Context ctx = this;
	private Context appCtx;
	private ProgressBar mProgressBar;
	private byte[] imageData;
	private PDFView pdfView;
	private LinearLayout pdfViewFrame;
	private byte[] decodedPdf;
	private Boolean pdfNightMode;
	private static final int PERMISSION_REQUEST_CODE = 1;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_file_view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		final TinyDB tinyDb = new TinyDB(appCtx);
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		ImageView closeActivity = findViewById(R.id.close);
		singleFileContents = findViewById(R.id.singleFileContents);
		singleCodeContents = findViewById(R.id.singleCodeContents);
		imageView = findViewById(R.id.imageView);
		mProgressBar = findViewById(R.id.progress_bar);
		pdfView = findViewById(R.id.pdfView);
		pdfViewFrame = findViewById(R.id.pdfViewFrame);
		singleFileContentsFrame = findViewById(R.id.singleFileContentsFrame);

		String singleFileName = getIntent().getStringExtra("singleFileName");

		TextView toolbar_title = findViewById(R.id.toolbar_title);
		toolbar_title.setMovementMethod(new ScrollingMovementMethod());

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		tinyDb.putString("downloadFileContents", "");

		try {

			singleFileName = URLDecoder.decode(singleFileName, "UTF-8");
			singleFileName = singleFileName.replaceAll("//", "/");
			singleFileName = singleFileName.startsWith("/") ? singleFileName.substring(1) : singleFileName;

		}
		catch(UnsupportedEncodingException e) {

			assert singleFileName != null;
			Log.i("singleFileName", singleFileName);

		}

		toolbar_title.setText(singleFileName);

		getSingleFileContents(instanceUrl, instanceToken, repoOwner, repoName, singleFileName);

	}

	private void getSingleFileContents(String instanceUrl, String token, final String owner, String repo, final String filename) {

		final TinyDB tinyDb = new TinyDB(appCtx);

		Call<Files> call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().getSingleFileContents(token, owner, repo, filename);

		call.enqueue(new Callback<Files>() {

			@Override
			public void onResponse(@NonNull Call<Files> call, @NonNull retrofit2.Response<Files> response) {

				if(response.code() == 200) {

					AppUtil appUtil = new AppUtil();
					assert response.body() != null;

					if(!response.body().getContent().equals("")) {

						String fileExtension = FileUtils.getExtension(filename);
						mProgressBar.setVisibility(View.GONE);

						// download file meta
						tinyDb.putString("downloadFileName", filename);
						tinyDb.putString("downloadFileContents", response.body().getContent());

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

							switch(tinyDb.getInt("fileviewerSourceCodeThemeId")) {
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

							pdfNightMode = tinyDb.getBoolean("enablePdfMode");

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

					Toasty.info(ctx, ctx.getString(R.string.authorizeError));

				}
				else if(response.code() == 404) {

					Toasty.info(ctx, ctx.getString(R.string.apiNotFound));

				}
				else {

					Toasty.info(ctx, getString(R.string.labelGeneralError));

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
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		switch(id) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.genericMenu:
				BottomSheetFileViewerFragment bottomSheet = new BottomSheetFileViewerFragment();
				bottomSheet.show(getSupportFragmentManager(), "fileViewerBottomSheet");
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onButtonClicked(String text) {

		switch(text) {
			case "downloadFile":

				if(Build.VERSION.SDK_INT >= 23) {
					if(checkPermission()) {
						requestFileDownload();
					}
					else {
						requestPermission();
					}
				}
				else {
					requestFileDownload();
				}
				break;

		}

	}

	private void requestFileDownload() {

		final TinyDB tinyDb = new TinyDB(appCtx);

		if(!tinyDb.getString("downloadFileContents").isEmpty()) {

			File outputFileName = new File(tinyDb.getString("downloadFileName"));
			final File downloadFilePath = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/" + outputFileName.getName());

			byte[] pdfAsBytes = Base64.decode(tinyDb.getString("downloadFileContents"), 0);
			FileOutputStream fileOutputStream = null;

			try {

				fileOutputStream = new FileOutputStream(downloadFilePath, false);
				Objects.requireNonNull(fileOutputStream).write(pdfAsBytes);
				fileOutputStream.flush();
				fileOutputStream.close();
				Toasty.info(ctx, getString(R.string.downloadFileSaved));

			}
			catch(IOException e) {
				Log.e("errorFileDownloading", Objects.requireNonNull(e.getMessage()));
			}

		}
		else {
			Toasty.error(ctx, getString(R.string.waitLoadingDownloadFile));
		}

	}

	private boolean checkPermission() {

		int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		return result == PackageManager.PERMISSION_GRANTED;
	}

	private void requestPermission() {

		ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		switch(requestCode) {
			case PERMISSION_REQUEST_CODE:
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.i("PermissionsCheck", "Permission Granted");
				}
				else {
					Log.e("PermissionsCheck", "Permission Denied");
				}
				break;
		}
	}

	private void initCloseListener() {

		onClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				getIntent().removeExtra("singleFileName");
				finish();
			}
		};
	}

}