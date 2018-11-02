package com.vuforia.samples.ARVR;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.vuforia.samples.model.Room;
import com.vuforia.samples.model.User;
import com.vuforia.samples.network.NCARApiRequest;

import java.util.ArrayList;

public class RoomActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int SEARCH=1;

    private FloatingActionMenu fabMenu;
    private FloatingActionButton createBtn;
    private FloatingActionButton participateBtn;
    private ImageView imageNotExistImage;
    private TextView imageNotExistText;
    private ImageButton roomEnterBtn;
    private NCARProgressDialog dialog;
    private String roomTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        fabMenu = findViewById(R.id.room_fab_menu);
        createBtn = fabMenu.findViewById(R.id.room_create_btn);
        createBtn.setOnClickListener(this);
        participateBtn = fabMenu.findViewById(R.id.room_participate_btn);
        participateBtn.setOnClickListener(this);
        imageNotExistImage = findViewById(R.id.room_not_exist_image);
        imageNotExistText = findViewById(R.id.room_not_exist_text);
        roomEnterBtn = findViewById(R.id.room_enter_btn);
        roomEnterBtn.setOnClickListener(this);
        dialog = new NCARProgressDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dialog.showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Pair<NCARApiRequest.NCARApi_Err, Room> result = NCARApiRequest.getRoomInfoByEmail(User.currentUser.getUserEmail());
                switch (result.first) {
                    case SUCCESS:
                        User.currentUser.setCurrentRoom(result.second);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                roomEnterBtn.setVisibility(View.VISIBLE);
                            }
                        });

                        break;
                    case ROOM_NOT_FOUND:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageNotExistImage.setVisibility(View.VISIBLE);
                                imageNotExistText.setVisibility(View.VISIBLE);
                            }
                        });
                        break;
                    case NETWORK_ERR:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RoomActivity.this, R.string.ncar_network_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case UNKNOWN_ERR:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RoomActivity.this, R.string.ncar_unknown_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                }
                dialog.hideProgressDialog();
            }
        }).start();
    }

    private void createRoom(final String roomTitle, final ArrayList<User> users) {
        dialog.showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {

                users.add(User.getCurrentUser());
                Pair<NCARApiRequest.NCARApi_Err, Room> result = NCARApiRequest.createRoom(User.getCurrentUser().getUserEmail(), User.getCurrentUser().getUserName(), roomTitle, users);
                switch(result.first) {
                    case SUCCESS:
                        User.getCurrentUser().setCurrentRoom(result.second);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageNotExistImage.setVisibility(View.GONE);
                                imageNotExistText.setVisibility(View.GONE);
                                roomEnterBtn.setVisibility(View.VISIBLE);
                            }
                        });
                        break;
                    case ALREADY_EXIST:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RoomActivity.this, R.string.ncar_room_already_exist_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case NETWORK_ERR:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RoomActivity.this, R.string.ncar_network_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case UNKNOWN_ERR:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RoomActivity.this, R.string.ncar_unknown_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                }
                dialog.hideProgressDialog();
            }
        }).start();
    }

    private void enterRoom(final Room room) {
        dialog.showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {

                Pair<NCARApiRequest.NCARApi_Err, Integer> result = NCARApiRequest.enterRoom(User.getCurrentUser().getUserEmail(), room.getRoomId());
                switch(result.first) {
                    case SUCCESS:
                        room.getSttOpts().setPort(result.second);
                        User.getCurrentUser().setCurrentRoom(room);
                        Intent intent = new Intent(RoomActivity.this, UnityPlayerActivity.class);
                        startActivity(intent);
                        break;
                    case ROOM_NOT_FOUND:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RoomActivity.this, R.string.ncar_room_not_found_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case NETWORK_ERR:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RoomActivity.this, R.string.ncar_network_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case UNKNOWN_ERR:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RoomActivity.this, R.string.ncar_unknown_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                }

                dialog.hideProgressDialog();
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.room_create_btn:
                new MaterialDialog.Builder(this)
                        .title(R.string.room_list_create_title)
                        .titleColor(Color.BLACK)
                        .positiveText(R.string.room_list_create_ok)
                        .negativeText(R.string.room_list_create_cancel)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .contentColor(Color.BLACK)
                        .backgroundColor(Color.WHITE)
                        .input(R.string.room_list_create_input_hint, R.string.room_list_create_input_prefill, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                if(input != null || input != "") {
                                    roomTitle = input.toString();
                                    Intent intent = new Intent(RoomActivity.this, SearchActivity.class);
                                    startActivityForResult(intent, SEARCH);
                                }
                                else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RoomActivity.this, "방 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).show();
                break;
            case R.id.room_participate_btn:
                /*
                new MaterialDialog.Builder(this)
                        .title(R.string.room_list_participate_title)
                        .titleColor(Color.BLACK)
                        .positiveText(R.string.room_list_create_ok)
                        .negativeText(R.string.room_list_create_cancel)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .contentColor(Color.BLACK)
                        .backgroundColor(Color.WHITE)
                        .input(R.string.room_list_create_input_hint, R.string.room_list_create_input_prefill, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                if(input != null || input != "")
                                    enterRoom(input.toString());
                                else
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RoomActivity.this, "방 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            }
                        }).show();*/
                break;

            case R.id.room_enter_btn:
                enterRoom(User.getCurrentUser().getCurrentRoom());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if(requestCode == SEARCH) {
                createRoom(roomTitle.toString(), (ArrayList<User>)data.getSerializableExtra("users"));
            }
        }
    }
}