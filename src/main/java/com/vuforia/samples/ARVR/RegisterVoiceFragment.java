package com.vuforia.samples.ARVR;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.vuforia.samples.network.NCARApiRequest;

import java.io.File;

import info.kimjihyok.ripplelibrary.Rate;
import info.kimjihyok.ripplelibrary.VoiceRippleView;
import info.kimjihyok.ripplelibrary.renderer.Renderer;
import info.kimjihyok.ripplelibrary.renderer.TimerCircleRippleRenderer;

public class RegisterVoiceFragment extends Fragment implements View.OnClickListener {
    private VoiceRippleView rippleView;
    private ImageButton completeBtn;
    private NCARProgressDialog dialog;
    private File audioFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_voice, null);

        rippleView = view.findViewById(R.id.register_voice_rippleview);
        rippleView.setRecordDrawable(ContextCompat.getDrawable(getContext(), R.drawable.record), ContextCompat.getDrawable(getContext(), R.drawable.recording));
        rippleView.setIconSize(30);
        rippleView.setOnClickListener(this);

        Renderer currentRenderer = new TimerCircleRippleRenderer(getDefaultRipplePaint(), getDefaultRippleBackgroundPaint(), getButtonPaint(), getArcPaint(), 10000.0, 0.0);
        if (currentRenderer instanceof TimerCircleRippleRenderer) {
            ((TimerCircleRippleRenderer) currentRenderer).setStrokeWidth(20);
        }
        rippleView.setRenderer(currentRenderer);

        rippleView.setRippleColor(ContextCompat.getColor(getContext(), R.color.blue_grey_500));
        rippleView.setRippleSampleRate(Rate.LOW);
        rippleView.setRippleDecayRate(Rate.HIGH);
        rippleView.setBackgroundRippleRatio(1.4);

        audioFile = new File(Environment.getExternalStorageDirectory()+"/audio.aac");
        rippleView.setMediaRecorder(new MediaRecorder());
        rippleView.setOutputFile(audioFile.getAbsolutePath());
        rippleView.setAudioSource(MediaRecorder.AudioSource.MIC);
        rippleView.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        rippleView.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        completeBtn = view.findViewById(R.id.register_voice_complete);
        completeBtn.setOnClickListener(this);
        completeBtn.setVisibility(View.GONE);
        view.findViewById(R.id.register_voice_back).setOnClickListener(this);

        dialog = new NCARProgressDialog(getContext());
        return view;
    }

    private Paint getArcPaint() {
        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(), R.color.temp_color));
        paint.setStrokeWidth(20);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }
    private Paint getDefaultRipplePaint() {
        Paint ripplePaint = new Paint();
        ripplePaint.setStyle(Paint.Style.FILL);
        ripplePaint.setColor(ContextCompat.getColor(getContext(), R.color.blue_grey_500));
        ripplePaint.setAntiAlias(true);

        return ripplePaint;
    }

    private Paint getDefaultRippleBackgroundPaint() {
        Paint rippleBackgroundPaint = new Paint();
        rippleBackgroundPaint.setStyle(Paint.Style.FILL);
        rippleBackgroundPaint.setColor((ContextCompat.getColor(getContext(), R.color.blue_grey_500) & 0x00FFFFFF) | 0x40000000);
        rippleBackgroundPaint.setAntiAlias(true);

        return rippleBackgroundPaint;
    }

    private Paint getButtonPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    private void recording() {
        if(rippleView.isRecording()) {
            rippleView.stopRecording();
            completeBtn.setVisibility(View.VISIBLE);
        }
        else {
            rippleView.startRecording();
            completeBtn.setVisibility(View.GONE);
        }
    }

    private void back() {
        ((RegisterActivity)getActivity()).setCurrentItem(0, true);
    }

    private void complete() {
        dialog.showProgressDialog();
        if(audioFile.exists()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NCARApiRequest.NCARApi_Err result = NCARApiRequest.saveAudio(getContext(), User.getCurrentUser().getUserEmail(), audioFile);
                    switch(result) {
                        case SUCCESS:
                            startActivity(new Intent(getActivity(), RoomActivity.class));
                            getActivity().finish();
                            break;
                        case NETWORK_ERR:
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), R.string.ncar_network_err, Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case UNKNOWN_ERR:
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), R.string.ncar_unknown_err, Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                    }
                    dialog.hideProgressDialog();
                }
            }).start();
        }
        else {
            dialog.hideProgressDialog();
            Toast.makeText(getContext(), R.string.ncar_audio_not_found_err, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        rippleView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rippleView.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.register_voice_rippleview:
                recording();
                break;
            case R.id.register_voice_back:
                back();
                break;
            case R.id.register_voice_complete:
                complete();
                break;
        }
    }
}
