package com.example.demoapp.demoapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanFragment extends Fragment {

    public static final String FRAGMENT_TAG = "ScanFragment";

    // TODO: Rename and change types of parameters
    private TextView scanResult;
    private Button okButton;
    private CountDownTimer timer;

    public ScanFragment() {
    }

    private Context context;
    public static ScanFragment newInstance() {
        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        scanResult = (TextView) getView().findViewById(R.id.scan_result_textView);
        okButton = (Button)getView().findViewById(R.id.ok_button);
        IntentIntegrator.forSupportFragment(ScanFragment.this).initiateScan();
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                IntentIntegrator.forSupportFragment(ScanFragment.this).initiateScan();
            }
        });
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
            } else {
                Log.d("MainActivity", "Scanned" + result.getContents());
                scanResult.setText(result.getContents());
                // add code here
                //getTimeLeftInmillis(result.getContents())
                timer = new CountDownTimer(50000, 1000) {
                    int counter = 1;

                    @Override
                    public void onTick(long millisUntilFinished) {
                        scanResult.setText("Time Left: "+ String.valueOf(millisUntilFinished/1000));
                        counter++;
                    }

                    @Override
                    public void onFinish() {
                        scanResult.setText("EXPIRED");
                    }
                }.start();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    int getTimeLeft(String id){
        return 0;
    }
}
