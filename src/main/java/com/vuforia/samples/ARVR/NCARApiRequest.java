package com.vuforia.samples.ARVR;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public class NCARApiRequest {
    static String baseUrl = "http://163.180.117.216:7788";

    public interface NCARApi {
        @GET("checkIsRegister/")
        Call<Integer> checkIsRegister();

        @GET("saveFace/")
        @Multipart
        Call<Boolean> saveFace(@Part("token") RequestBody token, @Part MultipartBody.Part imageFile);

        @GET("detectFace/")
        @Multipart
        Call<String> detectFace(@Part("token") RequestBody token, @Part MultipartBody.Part imageFile);

        @GET("saveAudio/")
        @Multipart
        Call<Boolean> saveAudio(@Part("token") RequestBody token, @Part MultipartBody.Part audioFile);
    }

    static NCARApi getNCARApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(NCARApi.class);
    }

    static int checkIsRegister(String userToken) {
        NCARApi api = getNCARApi();
        Call<Integer> checkIsRegisterCall = api.checkIsRegister();

        try {
            int result = checkIsRegisterCall.execute().body();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    static int saveFace(Context context, String userToken, Bitmap face) {
        File file = new File(context.getCacheDir(), userToken);
        try {
            file.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            face.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            NCARApi api = getNCARApi();

            Call<Boolean> uploadImageCall = api.saveFace(
                    RequestBody.create(MediaType.parse("text/plain"), "token"),
                    MultipartBody.Part.createFormData(
                            "image",
                            file.getName(),
                            RequestBody.create(MediaType.parse("image"), file)));

            return uploadImageCall.execute().body() ? 1 : 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    static String detectFace(Context context, String userToken, Bitmap face) {
        File file = new File(context.getCacheDir(), userToken);
        try {
            file.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            face.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            NCARApi api = getNCARApi();

            Call<String> uploadImageCall = api.detectFace(
                    RequestBody.create(MediaType.parse("text/plain"), "token"),
                    MultipartBody.Part.createFormData(
                            "image",
                            file.getName(),
                            RequestBody.create(MediaType.parse("image"), file)));

            return uploadImageCall.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static int saveAudio(Context context, String userToken, File audio) {
        try {
            NCARApi api = getNCARApi();

            Call<Boolean> uploadImageCall = api.saveAudio(
                    RequestBody.create(MediaType.parse("text/plain"), "token"),
                    MultipartBody.Part.createFormData(
                            "audio",
                            audio.getName(),
                            RequestBody.create(MediaType.parse(context.getContentResolver().getType(Uri.fromFile(audio))), audio)));

            return uploadImageCall.execute().body() ? 1 : 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
