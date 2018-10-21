package com.vuforia.samples.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public class NCARApiRequest {
    private static String baseUrl = "http://163.180.117.216:5679";

    private interface NCARApi {
        @GET("checkIsRegister")
        Call<String> checkIsRegister(@Query("email") String userEmail);

        @POST("detectFace")
        @Multipart
        Call<ResponseBody> detectFace(@Part("token") RequestBody token, @Part MultipartBody.Part imageFile);

        @POST("saveFace")
        @Multipart
        Call<String> saveFace(@Part("email") RequestBody email, @Part MultipartBody.Part imageFile);

        @POST("saveAudio")
        @Multipart
        Call<String> saveAudio(@Part("email") RequestBody email, @Part MultipartBody.Part audioFile);
    }

    private static NCARApi getNCARApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(NCARApi.class);
    }

    public static int checkIsRegister(String userEmail) {
        NCARApi api = getNCARApi();
        Call<String> checkIsRegisterCall = api.checkIsRegister(userEmail);

        try {
            Response<String> response = checkIsRegisterCall.execute();
            int result = Integer.parseInt(response.body());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int saveFace(Context context, String userEmail, Bitmap face) {
        File file = new File(context.getCacheDir(), userEmail);
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

            Call<String> uploadImageCall = api.saveFace(
                    RequestBody.create(MediaType.parse("text/plain"), userEmail),
                    MultipartBody.Part.createFormData(
                            "image",
                            file.getName(),
                            RequestBody.create(MediaType.parse("image"), file)));

            int result = Integer.parseInt(uploadImageCall.execute().body());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String detectFace(Context context, String userToken, Bitmap face) {
        File file = new File(context.getCacheDir(), "tempimage");
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

            Call<ResponseBody> uploadImageCall = api.detectFace(
                    RequestBody.create(MediaType.parse("text/plain"), userToken),
                    MultipartBody.Part.createFormData(
                            "image",
                            file.getName(),
                            RequestBody.create(MediaType.parse("image"), file)));

            ResponseBody jsonBody = uploadImageCall.execute().body();
            return jsonBody.string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int saveAudio(Context context, String userEmail, File audio) {
        try {
            NCARApi api = getNCARApi();

            Call<String> uploadAudioCall = api.saveAudio(
                    RequestBody.create(MediaType.parse("text/plain"), userEmail),
                    MultipartBody.Part.createFormData(
                            "audio",
                            audio.getName(),
                            RequestBody.create(MediaType.parse("audio"), audio)));

            int result = Integer.parseInt(uploadAudioCall.execute().body());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
