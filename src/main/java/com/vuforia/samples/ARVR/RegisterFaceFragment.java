package com.vuforia.samples.ARVR;

import android.content.Intent;
import android.graphics.Bitmap;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RegisterFaceFragment extends Fragment implements View.OnClickListener{
    public static Bitmap cropBitmap = null;
    private Bitmap scaledBitmap = null;

    private ImageView imageView;
    private ImageButton nextBtn;
    private NCARProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_face, null);

        imageView = view.findViewById(R.id.register_face_imageview);
        imageView.setOnClickListener(this);
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

        String result = NCARApiRequest.detectFace(getContext(), User.getCurrentUser().getUserToken(), scaledBitmap);
        if(result == null) {
            dialog.hideProgressDialog();
            Toast.makeText(getContext(), "네트워크가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            JSONObject coordObj = new JSONObject(result);
            cropBitmap = Bitmap.createBitmap(scaledBitmap,
                    coordObj.getInt("preX"),
                    coordObj.getInt("preY"),
                    coordObj.getInt("postX") - coordObj.getInt("preX"),
                    coordObj.getInt("postY") - coordObj.getInt("preY"));

            if(NCARApiRequest.saveFace(getContext(), User.getCurrentUser().getUserToken(), cropBitmap) == -1) {
                dialog.hideProgressDialog();
                Toast.makeText(getContext(), "네트워크가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                return;
            }

            ((RegisterActivity)getActivity()).setCurrentItem(1, true);
            dialog.hideProgressDialog();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "JSON 에러가 발생하였습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
            dialog.hideProgressDialog();
        }
    }

    public void onActivityResult(Intent data) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
            float ratio = (float)bitmap.getHeight() / bitmap.getWidth();
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)imageView.getLayoutParams();
            params.height = (int)(params.width * ratio);
            imageView.setLayoutParams(params);

            scaledBitmap = Bitmap.createScaledBitmap(bitmap, params.width, params.height, true);

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
                imageClick(v);
                break;
            case R.id.register_face_next:
                moveToNext();
                break;
        }
    }
}
