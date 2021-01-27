package com.sscctv.seeeyesonvif.__Not_Used;


import android.util.Log;

import com.sscctv.seeeyesonvif.Interfaces.CallbackDialogListener;

import org.ksoap2.SoapFault;
import org.onvif.ver10.device.wsdl.DeviceService;
import org.onvif.ver10.device.wsdl.get.GetCapabilities;
import org.onvif.ver10.device.wsdl.get.GetCapabilitiesResponse;
import org.onvif.ver10.device.wsdl.get.GetDeviceInformation;
import org.onvif.ver10.device.wsdl.get.GetDeviceInformationResponse;
import org.onvif.ver10.device.wsdl.get.GetNetworkInterfaces;
import org.onvif.ver10.device.wsdl.get.GetNetworkInterfacesResponse;
import org.onvif.ver10.device.wsdl.set.SetNetworkInterfaces;
import org.onvif.ver10.device.wsdl.set.SetNetworkInterfacesResponse;
import org.onvif.ver10.media.wsdl.MediaService;
import org.onvif.ver10.media.wsdl.get.GetProfiles;
import org.onvif.ver10.media.wsdl.get.GetProfilesResponse;
import org.onvif.ver10.media.wsdl.get.GetSnapshotUri;
import org.onvif.ver10.media.wsdl.get.GetStreamUri;
import org.onvif.ver10.media.wsdl.get.GetStreamUriResponse;
import org.onvif.ver10.media.wsdl.get.GetVideoEncoderConfigurationOptions;
import org.onvif.ver10.media.wsdl.get.GetVideoEncoderConfigurationOptionsResponse;
import org.onvif.ver10.schema.StreamSetup;
import org.onvif.ver10.schema.StreamType;
import org.onvif.ver10.schema.Transport;
import org.onvif.ver10.schema.TransportProtocol;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Locale;

public class ONVIFRequest {
    private static final String TAG = ONVIFRequest.class.getSimpleName();

    public static final int SUCCESS = 0;
    public static final int ERROR_SOAP_FAULT = -1;
    public static final int ERROR_SOAP_AUTH = -2;
    public static final int ERROR_SOCKET_TIMEOUT = -3;
    public static final int ERROR_IOEXCEPTION = -4;
    public static final int ERROR_XMLPULLPARSEREXCEPTION = -5;
    public static final int ERROR_UNKNOWN_IPADDRESS = -6;
    public static final int ERROR_UNKNOWN = -7;
    public static final int ERROR_INVALID_STREAM_URI = -8;
    public static final int ERROR_INVALID_SNAPSHOT_URI = -9;

    private String mIPAddress;
    private int mPort;
    private String mID;
    private String mPassword;
    private String mDeviceServiceUri;
    private String mMediaServiceUri;
    private boolean mRun;
    private int mResult;
    private Object request;
    private DeviceService mDeviceService;
    public GetCapabilitiesResponse mGetCapabilitiesResponse;
    public SetNetworkInterfacesResponse mSetNetworkResponse;
    public GetNetworkInterfacesResponse mGetNetworkResponse;

    private MediaService mMediaService;
    public GetProfilesResponse mGetProfilesResponse;
    public GetDeviceInformationResponse mGetDeviceInformation;
    public GetVideoEncoderConfigurationOptionsResponse mGetVideoEncoderConfiguration;

    public ArrayList<GetStreamUriResponse> mGetStreamUriResponses;

    public ONVIFRequest(String ip, int port, String id, String pw) {
        this.mIPAddress = ip;
        this.mPort = port;
        this.mID = id;
        this.mPassword = pw;

        mDeviceServiceUri = String.format(Locale.getDefault(), "http://%s:%d/onvif/device_service", mIPAddress, mPort);
        mMediaServiceUri = String.format(Locale.getDefault(), "http://%s:%d/onvif/media_service", mIPAddress, mPort);
    }

    public int createDeviceManagementAuthHeader() {
        return sendONVIFRequest(OnvifRequest.createDeviceManagementAuthHeader, null);
    }

    public int createMediaManagementAuthHeader() {
        return sendONVIFRequest(OnvifRequest.createMediaServiceAuthHeader, null);
    }

    public int getCapabilities() {
        return sendONVIFRequest(OnvifRequest.GetCapabilities, null);
    }

    public int getProfiles() {
        return sendONVIFRequest(OnvifRequest.GetProfiles, null);
    }

    public int getSnapShotUri() {
        return sendONVIFRequest(OnvifRequest.GetSnapshotUri, null);
    }

    public int getStreamUri(String profileToken) {

        return sendONVIFRequest(OnvifRequest.GetStreamUri, profileToken);
    }

    public void getAllStreamUri() {
        for(int i = 0; i< mGetProfilesResponse.mProfiles.size(); i++) {
            getStreamUri(mGetProfilesResponse.mProfiles.get(i).mToken);
        }
    }

    public int setNetworkInterface(String newIP) {
        return sendONVIFRequest(OnvifRequest.SetNetworkInterface, newIP);
    }

    public int getNetworkInterface() {
        return sendONVIFRequest(OnvifRequest.GetNetworkInterface, null);
    }

    public int getDeviceInformation() {
        return sendONVIFRequest(OnvifRequest.GetDeviceInformation, null);
    }


    private int sendONVIFRequest(int type, Object obj) {
        mRun = true;
        new OnvifRequest(type, obj, new CallbackDialogListener<Integer>() {
            @Override
            public void callDialogListener(Integer value) {
                mResult = value;
                mRun = false;
            }
        }).start();

        while (mRun) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mResult;
    }

    /**********************************************************************************************
     * OnvifRequest Thread
     **********************************************************************************************/
    private class OnvifRequest extends Thread {
        public final static int createDeviceManagementAuthHeader = 0x00100;
        public final static int GetCapabilities = 0x00101;
        public final static int GetDeviceInformation = 0x00102;
        public final static int createMediaServiceAuthHeader = 0x00200;
        public final static int GetProfiles = 0x00201;
        public final static int GetVideoEncoderConfigurationOptions = 0x00202;
        public final static int SetVideoEncoderConfiguration = 0x00203;
        public final static int GetStreamUri = 0x00204;
        public final static int GetSnapshotUri = 0x00205;
        public final static int SetNetworkInterface = 0x00300;
        public final static int GetNetworkInterface = 0x00400;

        private int mType;
        private Object mObject;
        private CallbackDialogListener<Integer> mCallback;

        public void sendCallback(int value) {
            if (mCallback != null) {
                mCallback.callDialogListener(value);
            }
        }

        public OnvifRequest(int request_type, Object obj, CallbackDialogListener<Integer> l) {
            this.mType = request_type;
            this.mObject = obj;
            this.mCallback = l;
        }

        @Override
        public void run() {
            int result = 0;
            try {
                switch (mType) {
                    case createDeviceManagementAuthHeader:
                        Log.d(TAG, "createDeviceManagementAuthHeader");
                        mDeviceService = null;
                        mDeviceService = new DeviceService(mDeviceServiceUri);
                        mDeviceService.createAuthHeader(mID, mPassword);
                        break;

                    case GetCapabilities:
                        request = new GetCapabilities();
                        mGetCapabilitiesResponse = mDeviceService.getCapabilities((org.onvif.ver10.device.wsdl.get.GetCapabilities) request);
                        break;

                    case GetDeviceInformation:
                        request = new GetDeviceInformation();
                        mGetDeviceInformation = mDeviceService.getDeviceInformation((org.onvif.ver10.device.wsdl.get.GetDeviceInformation) request);
                        break;

                    case GetNetworkInterface:
                        GetNetworkInterfaces getNetworkRequest = new GetNetworkInterfaces();
                        mGetNetworkResponse = mDeviceService.getNetworkInterface(getNetworkRequest);
                        break;

                    case SetNetworkInterface:
                        SetNetworkInterfaces setNetworkRequest = new SetNetworkInterfaces();
                        //아래 값 수정 필요!!
                        setNetworkRequest.setInterfaceToken("eth0"); //필요없는 듯
                        setNetworkRequest.setNetworkInterfacesEnabled(true);
                        setNetworkRequest.setIPv4ManualAddress((String) mObject);
                        setNetworkRequest.setIPv4DHCP(false);
                        mSetNetworkResponse = mDeviceService.setNetworkInterface(setNetworkRequest);
                        break;

                    case createMediaServiceAuthHeader:
                        Log.d(TAG, "createMediaServiceAuthHeader");
                        mMediaService = null;
                        if(mGetCapabilitiesResponse == null) {
                            Log.e(TAG, "Capabilities Null--");
                            break;
                        }
                        mMediaService = new MediaService(mGetCapabilitiesResponse.mCapabilities.mMediaXAddr);
                        mMediaService.createAuthHeader(mID, mPassword);
                        mMediaService.setWsUsernameToken(mDeviceService.getWsUsernameToken());
                        break;

                    case GetProfiles:
                        Log.d(TAG, "GetProfiles");
                        request = new GetProfiles();
                        mGetProfilesResponse = mMediaService.getProfiles((org.onvif.ver10.media.wsdl.get.GetProfiles) request);
                        mGetStreamUriResponses = null;
                        break;

                    case GetStreamUri:
                        request = new GetStreamUri();
                        StreamSetup streamsetup = new StreamSetup();
                        streamsetup.mStreamType = StreamType.STREAM_TYPE_UNICAST;
                        Transport transport = new Transport();
                        transport.mTransportProtocol = TransportProtocol.TRANSPORT_PROTOCOL_UDP;
                        streamsetup.mTransport = transport;
                        ((org.onvif.ver10.media.wsdl.get.GetStreamUri) request).setStreamSetup(streamsetup, (String) mObject);
                        if (mGetStreamUriResponses == null) {
                            mGetStreamUriResponses = new ArrayList<>();
                        }
                        mGetStreamUriResponses.add(mMediaService.getStreamUri((org.onvif.ver10.media.wsdl.get.GetStreamUri) request));
                        Log.d(TAG, "mGetStreamUriResponses.add: " + mMediaService.getStreamUri((org.onvif.ver10.media.wsdl.get.GetStreamUri) request) + " Size: " + mGetStreamUriResponses.size());

                        break;

                    case GetVideoEncoderConfigurationOptions:
                        request = new GetVideoEncoderConfigurationOptions();
                        mGetVideoEncoderConfiguration = mMediaService.getVideoEncoderConfigurationOptions((org.onvif.ver10.media.wsdl.get.GetVideoEncoderConfigurationOptions) request);
                        break;

                    case SetVideoEncoderConfiguration:
                        break;

                    case GetSnapshotUri:
                        request = new GetSnapshotUri();
                        mMediaService.getSnapshotUri((org.onvif.ver10.media.wsdl.get.GetSnapshotUri) request);
                        break;

                    default:
                        Log.e(TAG, "unknown onvif requst message:" + mType);
                        result = -100;
                        break;
                }

                result = SUCCESS;
            } catch (SoapFault e) {
                e.printStackTrace();
                result = ERROR_SOAP_FAULT;
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                result = ERROR_SOCKET_TIMEOUT;
            } catch (IOException e) {
                e.printStackTrace();
                if (e.toString().contains("auth")) {
                    result = ERROR_SOAP_AUTH;
                } else if (e.toString().contains("password")) {
                    result = ERROR_SOAP_AUTH;
                } else if (e.toString().contains("400")) {
                    result = ERROR_SOAP_AUTH;
                } else {
                    result = ERROR_IOEXCEPTION;
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                result = ERROR_XMLPULLPARSEREXCEPTION;
            }
            sendCallback(result);
            super.run();
        }
    }
}
