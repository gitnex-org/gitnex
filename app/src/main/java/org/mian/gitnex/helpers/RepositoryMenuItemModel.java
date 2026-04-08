package org.mian.gitnex.helpers;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author mmarif
 */
public class RepositoryMenuItemModel implements Parcelable {

	private final String id;
	private final int labelRes;
	private final int iconRes;
	private final int backgroundAttr;
	private final int contentColorAttr;

	public RepositoryMenuItemModel(
			String id, int labelRes, int iconRes, int backgroundAttr, int contentColorAttr) {
		this.id = id;
		this.labelRes = labelRes;
		this.iconRes = iconRes;
		this.backgroundAttr = backgroundAttr;
		this.contentColorAttr = contentColorAttr;
	}

	protected RepositoryMenuItemModel(Parcel in) {
		id = in.readString();
		labelRes = in.readInt();
		iconRes = in.readInt();
		backgroundAttr = in.readInt();
		contentColorAttr = in.readInt();
	}

	public static final Creator<RepositoryMenuItemModel> CREATOR =
			new Creator<>() {
				@Override
				public RepositoryMenuItemModel createFromParcel(Parcel in) {
					return new RepositoryMenuItemModel(in);
				}

				@Override
				public RepositoryMenuItemModel[] newArray(int size) {
					return new RepositoryMenuItemModel[size];
				}
			};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeInt(labelRes);
		dest.writeInt(iconRes);
		dest.writeInt(backgroundAttr);
		dest.writeInt(contentColorAttr);
	}

	public String getId() {
		return id;
	}

	public int getLabelRes() {
		return labelRes;
	}

	public int getIconRes() {
		return iconRes;
	}

	public int getBackgroundAttr() {
		return backgroundAttr;
	}

	public int getContentColorAttr() {
		return contentColorAttr;
	}
}
