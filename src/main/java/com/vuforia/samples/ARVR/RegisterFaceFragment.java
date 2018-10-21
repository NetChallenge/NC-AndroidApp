package com.vuforia.samples.ARVR;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.vuforia.samples.network.NCARApiRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class RegisterFaceFragment extends Fragment implements View.OnClickListener{
    //public static Bitmap cropBitmap = null;
    private Bitmap scaledBitmap = null;

    private ImageView imageView;
    private ImageView iconView;
    private ImageButton nextBtn;
    private NCARProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_face, null);

        imageView = view.findViewById(R.id.register_face_imageview);
        imageView.setOnClickListener(this);
        imageView.setVisibility(View.GONE);
        iconView = view.findViewById(R.id.register_face_imageview_icon);
        iconView.setOnClickListener(this);
        nextBtn = view.findViewById(R.id.register_face_next);
        nextBtn.setVisibility(View.GONE);
        nextBtn.setOnClickListener(this);

        dialog = new NCARProgressDialog(getContext());
        return view;
    }

    public void imageClick(View v) {
        if(scaledBitmap == null) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            getActivity().startActivityForResult(Intent.createChooser(intent, "Select Picture"), RegisterActivity.SELECT_PICTURE);
        }
        else {
            scaledBitmap = null;
        }
    }

    public void moveToNext() {
        dialog.showProgressDialog();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = NCARApiRequest.detectFace(getContext(), User.getCurrentUser().getUserToken(), scaledBitmap);
                if(result == null) {
                    dialog.hideProgressDialog();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "네트워크가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                else if(result.length() == 0) {
                    dialog.hideProgressDialog();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "얼굴을 찾을 수 없습니다. 다시 촬영해 주세요.", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                try {
                    JSONArray coordObj = new JSONArray(result).getJSONArray(0);
                    /*
                    cropBitmap = Bitmap.createBitmap(scaledBitmap,
                            coordObj.getInt(0),
                            coordObj.getInt(1),
                            coordObj.getInt("postX") - coordObj.getInt("preX"),
                            coordObj.getInt("postY") - coordObj.getInt("preY"));
                    */
                    if(NCARApiRequest.saveFace(getContext(), User.getCurrentUser().getUserEmail(), scaledBitmap) == -1) {
                        dialog.hideProgressDialog();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "네트워크가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }

                    ((RegisterActivity)getActivity()).setCurrentItem(1, true);
                    dialog.hideProgressDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                    dialog.hideProgressDialog();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "JSON 에러가 발생하였습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void onActivityResult(Intent data) {
        try {
            imageView.setVisibility(View.VISIBLE);
            iconView.setVisibility(View.GONE);

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
            float ratio = (float)bitmap.getHeight() / bitmap.getWidth();
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)imageView.getLayoutParams();
            params.height = (int)(params.width * ratio);
            imageView.setLayoutParams(params);

            Matrix matrix = new Matrix();
            matrix.postRotate(270);
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, params.width, params.height, true);
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

            imageView.setImageBitmap(scaledBitmap);
            nextBtn.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_face_imageview:
            case R.id.register_face_imageview_icon:
                imageClick(v);
                break;
            case R.id.register_face_next:
                moveToNext();
                break;
        }
    }
}
