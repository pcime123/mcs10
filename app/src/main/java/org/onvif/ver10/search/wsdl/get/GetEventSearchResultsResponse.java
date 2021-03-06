package org.onvif.ver10.search.wsdl.get;

import android.os.Parcel;
import android.os.Parcelable;

import org.onvif.ver10.schema.FindEventResult;
import org.onvif.ver10.schema.nativeParcel.utils;

import java.util.ArrayList;

public class GetEventSearchResultsResponse implements Parcelable {
	/*****************************************************************************************
	 * public defines
	 *****************************************************************************************/

	/*****************************************************************************************
	 * public values
	 *****************************************************************************************/
	public int mSearchState;
	public ArrayList<FindEventResult> mResult;

	/*****************************************************************************************
	 * Constructors
	 *****************************************************************************************/
	public GetEventSearchResultsResponse() {

	}

	public GetEventSearchResultsResponse(Parcel src) {
		mSearchState = utils.readInt(src);

		mResult = new ArrayList<FindEventResult>();
		int size = utils.readInt(src);
		for (int i = 0; i < size; i++) {
			mResult.add(new FindEventResult(src));
		}
	}

	/*****************************************************************************************
	 * @Override describeContents
	 *****************************************************************************************/
	@Override
	public int describeContents() {
		return 0;
	}

	/*****************************************************************************************
	 * @Override writeToParcel
	 *****************************************************************************************/
	@Override
	public void writeToParcel(Parcel dst, int flag) {
		utils.writeInt(dst, mSearchState);

		if (mResult != null) {
			int size = mResult.size();
			utils.writeInt(dst, size);
			for (int i = 0; i < size; i++) {
				mResult.get(i).writeToParcel(dst, flag);
			}
		} else {
			utils.writeInt(dst, 0);
		}
	}
}