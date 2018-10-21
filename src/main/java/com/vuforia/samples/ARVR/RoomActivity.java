package com.vuforia.samples.ARVR;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class RoomActivity extends AppCompatActivity implements View.OnClickListener {
    private Button testBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testBtn = findViewById(R.id.room_test_btn);
        testBtn.setOnClickListener(this);
    }

    private void enterRoom() {
        Intent intent = new Intent(RoomActivity.this, UnityPlayerActivity.class);
        intent.putExtra("port", "9899");
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.room_test_btn:
                enterRoom();
                break;
        }
    }
}