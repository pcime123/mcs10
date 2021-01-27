package com.sscctv.seeeyesonvif.__Not_Used;

import android.content.Context;
import android.util.Log;

import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.kxml2.io.KXmlParser;
import org.onvif.ver10.schema.nativeParcel.ProbeMatch;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @brief Probe메시지를 통해 Onvif디바이스를 찾고 정보를 파싱하는 클래스
 */
public class DiscoveryProbe {

    private final static String TAG = DiscoveryProbe.class.getSimpleName();
    private final static int TIMEOUT = 3000;
    private String mIPAddress = null;
    private String mProbeMessage;
    private String mProbeMessageUUID;
    private ArrayList<ProbeMatch> mProbeMatchesList = null;
    private DatagramPacket mDatagramPacket = null;
    private MulticastSocket mMultiCastSocket = null;
    private boolean mRun = true;
    private TinyDB tinyDB;

    public DiscoveryProbe(Context context, String remoteIP) {
        this.mIPAddress = remoteIP;
        this.mProbeMessageUUID = UUID.randomUUID().toString();
        this.mProbeMessage = createMessage(mProbeMessageUUID);
        this.tinyDB = new TinyDB(context);
    }


    private String createMessage(String uuid) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<Envelope xmlns:dn=\"http://www.onvif.org/ver10/network/wsdl\" xmlns=\"http://www.w3.org/2003/05/soap-envelope\">" +
                "<Header>" +
                "<wsa:MessageID xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">uuid:" +
                uuid +
                "</wsa:MessageID>" +
                "<wsa:To xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>" +
                "<wsa:Action xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action>" +
                "</Header>" +
                "<Body>" +
                "<Probe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\">" +
                "<Types>dn:NetworkVideoTransmitter</Types>" +
                "<Scopes/>" +
                "</Probe>" +
                "</Body>" +
                "</Envelope>";
    }

    public ArrayList<ProbeMatch> sendMessage() {
        mProbeMatchesList = new ArrayList<ProbeMatch>();
        try {
            mMultiCastSocket = new MulticastSocket(3702);
            if (mIPAddress == null) {
                mIPAddress = "239.255.255.250";
                mMultiCastSocket.setBroadcast(true);
            } else {
                mMultiCastSocket.setBroadcast(false);
            }

            mDatagramPacket = new DatagramPacket(mProbeMessage.getBytes(), mProbeMessage.getBytes().length);
            mDatagramPacket.setAddress(InetAddress.getByName(mIPAddress));

            mDatagramPacket.setPort(3702);
            mMultiCastSocket.setSoTimeout(TIMEOUT);
            mMultiCastSocket.setTimeToLive(64);
            mMultiCastSocket.send(mDatagramPacket);
            byte[] buf = new byte[4096];
            mDatagramPacket.setData(buf);

            while (mRun) {
                try {
                    mMultiCastSocket.receive(mDatagramPacket);
                    SoapSerializationEnvelope mEnvelope;
                    mEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
                    mEnvelope.setAddAdornments(false);
                    mEnvelope.dotNet = false;
                    XmlPullParser xp = new KXmlParser();
                    xp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                    String receiveData = new String(mDatagramPacket.getData()).trim();
                    StringReader reader = new StringReader(receiveData);
//                    Log.d(TAG, "readProbe:" + receiveData);
                    xp.setInput(reader);
                    mEnvelope.parse(xp);
                    if (mEnvelope.bodyIn instanceof SoapFault) {
                        Log.e(TAG, "error SoapFault:" + mEnvelope.bodyIn.toString());
                    } else {
                        ProbeMatch probe = new ProbeMatch(mEnvelope, mProbeMessageUUID, mIPAddress);
//						if (probe.isSBOXDevice) {
                        //if (probe.mTypes != null && probe.mTypes.contains("Device")) {
                        if (probe.mTypes != null) {
                            if (probe.mXAddrs != null) {
//                                Log.d(TAG, "get Ip: " + mIPAddress);
                                if (!isAdded(probe.mXAddrs)) {
                                    Log.d(TAG, "add Success ProbeMatchesList:" + probe.mXAddrs);
                                    mProbeMatchesList.add(probe);
                                    try {
                                        if (!mIPAddress.equals("239.255.255.250")) {
                                            close();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    Log.e(TAG, "add Failure ProbeMatchesList:" + probe.mXAddrs);
                                }
                            } else {
                                Log.e(TAG, "error Probe XAddrs:" + probe.mXAddrs);
                            }
                        } else {
                            Log.e(TAG, "error Probe Type:" + probe.mTypes);
                        }
//						} else {
//							close();
//							LOG.e(TAG, "error Device:" + probe.isSBOXDevice);
//						}
                    }
                } catch (Exception e) {
                    // e.printStackTrace();

                    Log.e(TAG, "------------ Discover Timeout ----------------");
                    close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mProbeMatchesList;
    }

    /*************************************************************************
     * isAdded
     *************************************************************************/
    private boolean isAdded(String XAddr) {
        int size = mProbeMatchesList.size();
        for (int i = 0; i < size; i++) {
            if (mProbeMatchesList.get(i).mXAddrs.equalsIgnoreCase(XAddr)) {
                return true;
            }
        }
        return false;
    }

    /*************************************************************************
     * close
     *************************************************************************/
    private void close() {
        mRun = false;
        tinyDB.putBoolean(KeyList.RUN_DISCOVERY_MODE, false);
        mDatagramPacket = null;
        if (mMultiCastSocket != null) {
            mMultiCastSocket.close();
        }
        mMultiCastSocket = null;
    }
}
