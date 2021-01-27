package org.onvif.ver10.schema.nativeParcel;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.kxml2.kdom.Element;

import java.net.URLDecoder;
import java.util.logging.Logger;

public class ProbeMatch implements Parcelable {
	@NonNull
	@Override
	public String toString() {
		return super.toString();
	}

	/*****************************************************************************************
	 * private values
	 *****************************************************************************************/
	private final static String TAG = ProbeMatch.class.getSimpleName();
	private String mSrcIPAddress = null;
	/*****************************************************************************************
	 * public values
	 *****************************************************************************************/

	public String mTypes;
	public String mScopes;
	public String mXAddrs;
	public int mMetadataVersion;

	// device infomation
	public String mOnvifVendorModel = "";
	public String mOnvifIPAddress = "";
	public int mOnvifPort;
	public String mOnvifDeviceServiceXAddr = "";

	//public boolean isSBOXDevice;
	public boolean isSBOXDevice;
	public int mResult;

	/*****************************************************************************************
	 * Constructors
	 *****************************************************************************************/
	public ProbeMatch() {

	}

	public ProbeMatch(Parcel src) {
		this.mSrcIPAddress = utils.readString(src);
		this.mTypes = utils.readString(src);
		this.mScopes = utils.readString(src);
		this.mXAddrs = utils.readString(src);
		this.mMetadataVersion = utils.readInt(src);
		this.mOnvifVendorModel = utils.readString(src);
		this.mOnvifIPAddress = utils.readString(src);
		this.mOnvifPort = utils.readInt(src);
		this.mOnvifDeviceServiceXAddr = utils.readString(src);
		this.mResult = utils.readInt(src);
	}

	public ProbeMatch(SoapSerializationEnvelope obj, String uuid, String srcIPAddress) {
		this.mSrcIPAddress = srcIPAddress;
		if (mSrcIPAddress.equals("239.255.255.250"))
			this.mSrcIPAddress = null;

		if (obj != null) {
			// ================================================
			// HEADER
			// ================================================
			Element[] header = (Element[]) obj.headerIn;
			int size = header.length;
			for (int i = 0; i < size; i++) {
//				Log.d(TAG, "Header getName: " + header[i].getName());
				switch (header[i].getName()) {
					case "MessageID":
//						Log.i(TAG, "Message ID: " + header[i].getText(0));
						// mHeaderMessageID = header[i].getText(0).toString();
						break;
					case "RelatesTo":
//						Log.w(TAG, "RelatesTo: " + header[i].getText(0));
						String RelatesToUUID = header[i].getText(0);
						if (!RelatesToUUID.contains(uuid)) {
							return;
						}
						break;
					case "To":
//						Log.i(TAG, "To: " + header[i].getText(0));
						// Log.i(TAG, "HEADER To:" + (header[i].getText(0).toString()));
						break;
					case "Action":
//						Log.w(TAG, "Action: " + header[i].getText(0));
						// Log.i(TAG, "HEADER Action:" + (header[i].getText(0).toString()));
						break;
					case "AppSequence":
//						Log.i(TAG, "AppSequence: " + header[i].getText(0));
						// Log.i(TAG, "HEADER AppSequence:" + (header[i].getText(0).toString()));
						break;
				}
			}

			// ================================================
			// BODY
			// ================================================
			SoapObject body = (SoapObject) obj.bodyIn;
			size = body.getPropertyCount();
			for (int i = 0; i < size; i++) {
				PropertyInfo ProbeMatch = new PropertyInfo();
				body.getPropertyInfo(i, ProbeMatch);
				if (ProbeMatch.getName().equals("ProbeMatch")) {
					SoapObject childObj = (SoapObject) ProbeMatch.getValue();
					int psize = childObj.getPropertyCount();
					for (int p = 0; p < psize; p++) {
						PropertyInfo childProperty = new PropertyInfo();
						childObj.getPropertyInfo(p, childProperty);
						switch (childProperty.getName()) {
							case "EndpointReference":
								try {
//									Log.d(TAG, "BODY EndpointReference:" + childProperty.getValue().toString());
									if (childProperty.getValue().toString().contains("sboxnvr")) {
										isSBOXDevice = true;
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								break;
							case "Types":
								mTypes = childProperty.getValue().toString();
//								Log.d(TAG, "BODY Types:" + mTypes);
								break;
							case "Scopes":
								mScopes = childProperty.getValue().toString();
//								Log.d(TAG, "BODY Scopes :" + mScopes);
								break;
							case "XAddrs":
								mXAddrs = childProperty.getValue().toString();
//								Log.d(TAG, "BODY XAddrs:" + mXAddrs);
								break;
							case "MetadataVersion":
								try {
									mMetadataVersion = Integer.parseInt(childProperty.getValue().toString());
								} catch (Exception e) {
									e.printStackTrace();
								}
//								Log.d(TAG, "BODY MetadataVersion:" + mMetadataVersion);
								break;
						}
					}
				}
			}
			setParsingData();
		}
	}

	private void setParsingData() {
		if (mScopes != null) {
			String[] Scopes = mScopes.split("\\s+");
			for (String scope : Scopes) {
				if (scope.contains("onvif://www.onvif.org/name/")) {
					try {
						mOnvifVendorModel = URLDecoder.decode(scope.replaceAll("onvif://www.onvif.org/name/", ""), "UTF-8");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (mXAddrs != null) {
			String[] Xaddr = mXAddrs.split("\\s+");
			int ipAddressCount = Xaddr.length;
//			Log.d(TAG, "ipAddressCount1 :" + ipAddressCount + ":" + mXAddrs);
			if (mXAddrs.contains("169.254")) {
				for (int x = 0; x < ipAddressCount; x++) {
					if (checkAvailableIPAddress(Xaddr[x]) == null) {
						mXAddrs = mXAddrs.replace(Xaddr[x], "");
					}
				}
				Xaddr = mXAddrs.split("\\s+");
				ipAddressCount = Xaddr.length;
			}
//			Log.d(TAG, "ipAddressCount2 :" + ipAddressCount + ":" + mXAddrs);
			// IP주소가 2개 이상인 경우.(zero configure 제외)
			// IP주소 중Local Network와 동일 한 대역의 IP주소 사용 함.
			// 단 사용자가 직접 추가 한 IP주소가 있는 경우(mSrcIPAddress)
			// 해당 IP를 사용 할 수도 있음.

			if (ipAddressCount >= 2) {
				for (int x = 0; x < ipAddressCount; x++) {
					String[] temp = Xaddr[x].split("/");
					// IP주소의 유효성 체크(IPv4, zero configuration)
					String ip = checkAvailableIPAddress(Xaddr[x]);
					if (ip != null) {
						if(!ip.equals("0.0.0.0")){
							setOnvifIPInfo(ip, temp, Xaddr, x);
						} else {
							Log.e(TAG, "IP Error: "+ ip );
						}
					}
				}
			}
			// IP주소가 1개 인 경우 해당 IP주소가 Zero Configuration이 아니면
			// 그대로 사용 함.
			else if (ipAddressCount == 1) {
				String ip = checkAvailableIPAddress(Xaddr[0]);
				if (ip != null) {
					String temp[] = Xaddr[0].split("/");
					setOnvifIPInfo(ip, temp, Xaddr, 0);
				}
			}
		}
	}

	private void setOnvifIPInfo(String ip, String[] temp, String[] Xaddr, int x) {
		mOnvifIPAddress = ip;
		try {
			mOnvifPort = Integer.parseInt(temp[2].split(":")[1]);
		} catch (Exception e) {
			mOnvifPort = 80;
		}
		int tempSize = temp.length;

		for (int t = 3; t < tempSize; t++) {
			if (t == (tempSize - 1)) {
				mOnvifDeviceServiceXAddr += temp[t];
			} else {
				mOnvifDeviceServiceXAddr += temp[t] + "/";
			}
		}
		mXAddrs = Xaddr[x];

		if (mSrcIPAddress != null) {
			mOnvifIPAddress = mSrcIPAddress;
		}

		Log.d(TAG, "mOnvifIPAddress:" + mOnvifIPAddress + " mOnvifPort:" + mOnvifPort + " :" + mOnvifDeviceServiceXAddr + ":" + mXAddrs);

	}

	/****************************************************************************************************
	 * checkAvailableIPAddress : 해당 IP가 제대로 된 IP주소 인지 확인(zero config 제외시킴)
	 ****************************************************************************************************/
	private String checkAvailableIPAddress(String ipv4) {
		try {
			String temp[] = ipv4.split("/");
			String ip = temp[2].split(":")[0];
			if (ipv4.contains("169.254") || !utils.isIPv4Address(ip)) {
				return null;
			}
			return ip;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/****************************************************************************************************
	 * Parcelable
	 ****************************************************************************************************/

	/*****************************************************************************************
	 * @Override describeContents
	 *****************************************************************************************/
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*****************************************************************************************
	 * @Override writeToParcel
	 *****************************************************************************************/
	@Override
	public void writeToParcel(Parcel dst, int flags) {
		utils.writeString(dst, mSrcIPAddress);
		utils.writeString(dst, mTypes);
		utils.writeString(dst, mScopes);
		utils.writeString(dst, mXAddrs);
		utils.writeInt(dst, mMetadataVersion);
		utils.writeString(dst, mOnvifVendorModel);
		utils.writeString(dst, mOnvifIPAddress);
		utils.writeInt(dst, mOnvifPort);
		utils.writeString(dst, mOnvifDeviceServiceXAddr);
		utils.writeInt(dst, mResult);
	}

	/*****************************************************************************************
	 * CREATOR
	 *****************************************************************************************/
	public static final Creator<ProbeMatch> CREATOR = new Creator<ProbeMatch>() {
		public ProbeMatch createFromParcel(Parcel in) {
			return new ProbeMatch(in);
		}

		public ProbeMatch[] newArray(int size) {
			return new ProbeMatch[size];
		}
	};

}
