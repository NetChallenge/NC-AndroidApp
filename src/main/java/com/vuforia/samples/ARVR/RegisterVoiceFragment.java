package com.vuforia.samples.ARVR;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import info.kimjihyok.ripplelibrary.Rate;
import info.kimjihyok.ripplelibrary.VoiceRippleView;

public class RegisterVoiceFragment extends Fragment implements View.OnClickListener {
    private VoiceRippleView rippleView;
    private NCARProgressDialog dialog;
    private File audioFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_voice, null);

        rippleView = view.findViewById(R.id.register_voice_rippleview);
        rippleView.setRippleSampleRate(Rate.LOW);
        rippleView.setRippleDecayRate(Rate.HIGH);
        rippleView.setBackgroundRippleRatio(1.4);

        audioFile = new File(getContext().getCacheDir(), "audio");
        rippleView.setMediaRecorder(new MediaRecorder());
        rippleView.setOutputFile(audioFile.getAbsolutePath());
        rippleView.setAudioSource(MediaRecorder.AudioSource.MIC);
        rippleView.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        rippleView.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        rippleView.setOnClickListener(this);

        view.findViewById(R.id.register_voice_back).setOnClickListener(this);
        view.findViewById(R.id.register_voice_complete).setOnClickListener(this);

        dialog = new NCARProgressDialog(getContext());
        return view;
    }

    private void recording() {
        if(rippleView.isRecording())
            rippleView.stopRecording();
        else
            rippleView.startRecording();
    }

    private void back() {
        ((RegisterActivity)getActivity()).setCurrentItem(0, true);
    }

    private void complete() {
        if(audioFile.exists())
            NCARApiRequest.saveAudio(getContext(), User.getCurrentUser().getUserToken(), audioFile);
        else
            Toast.makeText(getContext(), "오디오를 녹음해주세요.", Toast.LENGTH_SHORT).show();
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
