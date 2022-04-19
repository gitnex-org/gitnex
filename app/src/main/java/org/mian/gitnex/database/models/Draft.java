package org.mian.gitnex.database.models;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import static androidx.room.ForeignKey.CASCADE;

/**
 * @author M M Arif
 */

@Entity(tableName = "Drafts", foreignKeys = @ForeignKey(entity = Repository.class, parentColumns = "repositoryId", childColumns = "draftRepositoryId", onDelete = CASCADE), indices = {@Index("draftRepositoryId")})
public class Draft implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private int draftId;

	private int draftRepositoryId;
	private int draftAccountId;
	private int issueId;
	private String draftText;
	@Nullable
	private String draftType;
	@Nullable
	private String commentId;
	@Nullable
	private String issueType;

	public int getDraftId() {

		return draftId;
	}

	public void setDraftId(int draftId) {

		this.draftId = draftId;
	}

	public int getDraftRepositoryId() {

		return draftRepositoryId;
	}

	public void setDraftRepositoryId(int draftRepositoryId) {

		this.draftRepositoryId = draftRepositoryId;
	}

	public int getDraftAccountId() {

		return draftAccountId;
	}

	public void setDraftAccountId(int draftAccountId) {

		this.draftAccountId = draftAccountId;
	}

	public int getIssueId() {

		return issueId;
	}

	public void setIssueId(int issueId) {

		this.issueId = issueId;
	}

	public String getDraftText() {

		return draftText;
	}

	public void setDraftText(String draftText) {

		this.draftText = draftText;
	}

	@Nullable
	public String getDraftType() {

		return draftType;
	}

	public void setDraftType(@Nullable String draftType) {

		this.draftType = draftType;
	}

	@Nullable
	public String getCommentId() {

		return commentId;
	}

	public void setCommentId(@Nullable String commentId) {

		this.commentId = commentId;
	}

	@Nullable
	public String getIssueType() {

		return issueType;
	}

	public void setIssueType(@Nullable String issueType) {

		this.issueType = issueType;
	}

}
