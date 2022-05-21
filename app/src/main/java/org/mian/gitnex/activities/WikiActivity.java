package org.mian.gitnex.activities;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.v2.models.WikiPage;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityWikiBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */

public class WikiActivity extends BaseActivity {

	private ActivityWikiBinding binding;
	private String pageName;
	private RepositoryContext repository;
	private boolean renderMd = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityWikiBinding.inflate(getLayoutInflater());
		repository = RepositoryContext.fromIntent(getIntent());

		setContentView(binding.getRoot());
		setSupportActionBar(binding.toolbar);

		pageName = getIntent().getStringExtra("pageName");
		binding.close.setOnClickListener(view -> finish());

		binding.toolbarTitle.setMovementMethod(new ScrollingMovementMethod());
		binding.toolbarTitle.setText(pageName);

		getWikiPageContents();
	}

	private void getWikiPageContents() {

		Call<WikiPage> call = RetrofitClient
			.getApiInterface(ctx)
			.repoGetWikiPage(repository.getOwner(), repository.getName(), pageName);

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_menu, menu);
		inflater.inflate(R.menu.files_view_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if(id == android.R.id.home) {

			finish();
			return true;
		}
		else if(id == R.id.edit) {

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
}
