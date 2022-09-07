package org.mian.gitnex.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.v2.models.CreateWikiPageOptions;
import org.gitnex.tea4j.v2.models.WikiPage;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityWikiBinding;
import org.mian.gitnex.fragments.BottomSheetWikiFragment;
import org.mian.gitnex.fragments.WikiFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.structs.BottomSheetListener;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class WikiActivity extends BaseActivity implements BottomSheetListener {

	private ActivityWikiBinding binding;
	private String pageName;
	private String action;
	private RepositoryContext repository;
	private boolean renderMd = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityWikiBinding.inflate(getLayoutInflater());
		repository = RepositoryContext.fromIntent(getIntent());

		setContentView(binding.getRoot());
		setSupportActionBar(binding.toolbar);

		if(getIntent().getStringExtra("action") != null) {
			action = getIntent().getStringExtra("action");
		}
		else {
			action = "";
		}
		pageName = getIntent().getStringExtra("pageName");
		binding.close.setOnClickListener(view -> finish());

		binding.toolbarTitle.setMovementMethod(new ScrollingMovementMethod());
		binding.toolbarTitle.setText(pageName);

		if(action.equalsIgnoreCase("edit")) {
			getWikiPageContents();
			binding.renderWiki.setVisibility(View.GONE);
			binding.wikiTitle.setText(pageName);
		}
		else if(action.equalsIgnoreCase("add")) {
			binding.renderWiki.setVisibility(View.GONE);
			binding.progressBar.setVisibility(View.GONE);
			binding.toolbarTitle.setText(R.string.createWikiPage);
			binding.createWiki.setVisibility(View.VISIBLE);
		}
		else {
			getWikiPageContents();
			binding.renderWiki.setVisibility(View.VISIBLE);
			binding.createWiki.setVisibility(View.GONE);
		}
	}

	private void processWiki() {

		String wikiTitle = binding.wikiTitle.getText() != null ? binding.wikiTitle.getText().toString() : "";
		String wikiContent = binding.wikiContent.getText() != null ? binding.wikiContent.getText().toString() : "";

		if(wikiTitle.isEmpty() || wikiContent.isEmpty()) {
			Toasty.error(ctx, getString(R.string.wikiPageNameAndContentError));
			return;
		}

		if(action.equalsIgnoreCase("edit")) {
			patchWiki(wikiTitle, wikiContent);
		}
		else if(action.equalsIgnoreCase("add")) {
			addWiki(wikiTitle, wikiContent);
		}
	}

	private void addWiki(String wikiTitle, String wikiContent) {

		CreateWikiPageOptions createWikiPageOptions = new CreateWikiPageOptions();
		createWikiPageOptions.setTitle(wikiTitle);
		createWikiPageOptions.setContentBase64(AppUtil.encodeBase64(wikiContent));

		Call<WikiPage> call = RetrofitClient.getApiInterface(ctx).repoCreateWikiPage(repository.getOwner(), repository.getName(), createWikiPageOptions);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<WikiPage> call, @NonNull retrofit2.Response<WikiPage> response) {

				if(response.isSuccessful()) {
					if(response.code() == 201) {

						Toasty.success(ctx, getString(R.string.wikiCreated));
						WikiFragment.resumeWiki = true;
						finish();
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
			}

			@Override
			public void onFailure(@NonNull Call<WikiPage> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	private void patchWiki(String wikiTitle, String wikiContent) {

		CreateWikiPageOptions createWikiPageOptions = new CreateWikiPageOptions();
		createWikiPageOptions.setTitle(wikiTitle);
		createWikiPageOptions.setContentBase64(AppUtil.encodeBase64(wikiContent));

		Call<WikiPage> call = RetrofitClient.getApiInterface(ctx).repoEditWikiPage(repository.getOwner(), repository.getName(), pageName, createWikiPageOptions);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<WikiPage> call, @NonNull retrofit2.Response<WikiPage> response) {

				if(response.isSuccessful()) {
					if(response.code() == 200) {

						Toasty.success(ctx, getString(R.string.wikiUpdated));
						WikiFragment.resumeWiki = true;
						finish();
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
			}

			@Override
			public void onFailure(@NonNull Call<WikiPage> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	private void getWikiPageContents() {

		Call<WikiPage> call = RetrofitClient.getApiInterface(ctx).repoGetWikiPage(repository.getOwner(), repository.getName(), pageName);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<WikiPage> call, @NonNull retrofit2.Response<WikiPage> response) {

				if(response.isSuccessful()) {
					if(response.code() == 200) {

						WikiPage wikiPage = response.body();

						runOnUiThread(() -> {

							assert wikiPage != null;
							String pageContents = AppUtil.decodeBase64(wikiPage.getContentBase64());
							binding.contents.setContent(pageContents, "md");

							if(renderMd) {
								Markdown.render(ctx, EmojiParser.parseToUnicode(pageContents), binding.markdown, repository);

								binding.markdownFrame.setVisibility(View.VISIBLE);
								binding.contents.setVisibility(View.GONE);
							}
							else {
								binding.markdownFrame.setVisibility(View.GONE);
								binding.contents.setVisibility(View.VISIBLE);
							}

							if(action.equalsIgnoreCase("edit")) {
								binding.wikiContent.setText(pageContents);
							}
						});
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

					binding.progressBar.setVisibility(View.GONE);
				}
			}

			@Override
			public void onFailure(@NonNull Call<WikiPage> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	private void deleteWiki(final String owner, final String repo, final String pageName) {

		MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(ctx).setTitle(String.format(ctx.getString(R.string.deleteGenericTitle), pageName))
			.setMessage(ctx.getString(R.string.deleteWikiPageMessage, pageName)).setNeutralButton(R.string.cancelButton, null)
			.setPositiveButton(R.string.menuDeleteText, (dialog, whichButton) -> RetrofitClient.getApiInterface(ctx).repoDeleteWikiPage(owner, repo, pageName).enqueue(new Callback<>() {

				@Override
				public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

					if(response.isSuccessful()) {
						Toasty.success(ctx, getString(R.string.wikiPageDeleted));
						WikiFragment.resumeWiki = true;
						finish();
					}
					else if(response.code() == 403) {
						Toasty.error(ctx, ctx.getString(R.string.authorizeError));
					}
					else {
						Toasty.error(ctx, ctx.getString(R.string.genericError));
					}
				}

				@Override
				public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}));

		materialAlertDialogBuilder.create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.markdown_switcher, menu);
		if(action.equalsIgnoreCase("edit") || action.equalsIgnoreCase("add")) {
			inflater.inflate(R.menu.save, menu);
		}
		else {
			if(repository.getPermissions().isPush()) {
				inflater.inflate(R.menu.generic_nav_dotted_menu, menu);
			}
		}

		return true;
	}

	@Override
	public void onButtonClicked(String text) {
		switch(text) {

			case "editWiki":
				Intent intent = new Intent(ctx, WikiActivity.class);
				intent.putExtra("pageName", pageName);
				intent.putExtra("action", "edit");
				intent.putExtra(RepositoryContext.INTENT_EXTRA, repository);
				ctx.startActivity(intent);
				finish();
				break;
			case "delWiki":
				deleteWiki(repository.getOwner(), repository.getName(), pageName);
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if(id == android.R.id.home) {

			finish();
			return true;
		}
		else if(id == R.id.genericMenu) {

			BottomSheetWikiFragment bottomSheet = new BottomSheetWikiFragment();
			bottomSheet.show(getSupportFragmentManager(), "wikiBottomSheet");
			return true;
		}
		else if(id == R.id.save) {

			processWiki();
			return true;
		}
		else if(id == R.id.markdown) {

			if(action.equalsIgnoreCase("edit") || action.equalsIgnoreCase("add")) {
				if(renderMd) {
					Markdown.render(ctx, EmojiParser.parseToUnicode(String.valueOf(Objects.requireNonNull(binding.contentLayout.getEditText()).getText())), binding.markdownPreview, repository);

					binding.markdownPreview.setVisibility(View.VISIBLE);
					binding.contentLayout.setVisibility(View.GONE);
					renderMd = false;
				}
				else {
					binding.markdownPreview.setVisibility(View.GONE);
					binding.contentLayout.setVisibility(View.VISIBLE);
					renderMd = true;
				}
			}
			else {

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
			}

			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

}
