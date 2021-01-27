package com.sscctv.seeeyesonvif.__Not_Used;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;

public class OnvifDiscovery {

    @SuppressLint("StaticFieldLeak")
    private class OnDiscovery extends AsyncTask<Void, Boolean, String> {

        private ProgressDialog progressDialog = new ProgressDialog(ActivityMain.getAppContext());

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("카메라 검색 중...");
            progressDialog.show();
            super.onPreExecute();
        }

        protected String doInBackground(Void... params) {
//            try {
                DiscoveryProbe probe = new DiscoveryProbe(ActivityMain.getAppContext(), null);
//                deviceList = probe.sendMessage();
//                while (tinyDB.getBoolean(KeyList.RUN_DISCOVERY_MODE)) {
//                    if (!tinyDB.getBoolean(KeyList.RUN_DISCOVERY_MODE)) {
//                        break;
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
        }

        @Override
        protected void onPostExecute(String s) {
//            for (int i = 0; i < deviceList.size(); i++) {
//                resultList.add(new DeviceDiscovery(deviceList.get(i).mOnvifVendorModel, deviceList.get(i).mOnvifIPAddress, deviceList.get(i).mOnvifPort));
//            }
//            adapterDiscoveryList.notifyDataSetChanged();
//            progressDialog.dismiss();
            super.onPostExecute(s);
        }
    }
}
