package com.sscctv.seeeyesonvif.__Not_Used;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.sscctv.seeeyesonvif.Items.IPDevice;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Fragment.FragmentCallView;
import com.sscctv.seeeyesonvif.databinding.FragmentLoginBinding;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.Objects;

public class LoginFragment extends Fragment {

    private static final String TAG = LoginFragment.class.getSimpleName();
    private FragmentLoginBinding mBinding;
    private TinyDB tinyDB;
    private String deviceName, ipAddress;
    private int port;
    private InputMethodManager imm;
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.v(TAG, "onResume()");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        tinyDB = new TinyDB(getContext());
        imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            DeviceDiscovery deviceDiscovery = bundle.getParcelable("device");
            if (deviceDiscovery != null) {
                deviceName = deviceDiscovery.getName();
                ipAddress = deviceDiscovery.getIp();
                port = deviceDiscovery.getPort();

                mBinding.device.setText(deviceName);
                mBinding.ipAddress.setText(ipAddress);
                mBinding.port.setText(String.valueOf(port));
            } else {
                mBinding.device.setText("선택 안됨");
                mBinding.ipAddress.setText("");
                mBinding.port.setText("");
            }
        }

        mBinding.btnLogin.setOnClickListener(view -> {

            if (Objects.requireNonNull(imm).isActive()) {
                Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(Objects.requireNonNull(getActivity()).getCurrentFocus()).getWindowToken(), 0);
            }

//            ONVIFRequest request = new ONVIFRequest(ipAddress, port, mBinding.inputId.getText().toString(), mBinding.inputPw.getText().toString());
//            int authResult = request.createDeviceManagementAuthHeader();
//
//            if (authResult == ONVIFRequest.ERROR_SOCKET_TIMEOUT) {
//                Toast.makeText(getContext(), "로그인을 실패했습니다.", Toast.LENGTH_SHORT).show();
//                return;
//            }

//            int mediaResult = request.createMediaManagementAuthHeader();
//            if (mediaResult == ONVIFRequest.ERROR_SOCKET_TIMEOUT) {
//                Toast.makeText(getContext(), "Media 불러오기를 실패했습니다.", Toast.LENGTH_SHORT).show();
//                return;
//            }

            Toast.makeText(getContext(), "정보 가져오기 성공!", Toast.LENGTH_SHORT).show();

            IPDevice ipDevice = new IPDevice();
            ipDevice.setId(mBinding.inputId.getText().toString());
            ipDevice.setPassword(mBinding.inputPw.getText().toString());
            ipDevice.setIpAddress(mBinding.ipAddress.getText().toString());
            ipDevice.setName(mBinding.device.getText().toString());
            ipDevice.setPort(port);

            Bundle videoBundle = new Bundle(1);
            videoBundle.putParcelable("ipDevice", ipDevice);

            FragmentCallView fragment = new FragmentCallView();
            fragment.setArguments(videoBundle);

            FragmentTransaction fragmentTransaction = Objects.requireNonNull(getFragmentManager()).beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, fragment).commit();
        });

        return mBinding.getRoot();
    }
}
