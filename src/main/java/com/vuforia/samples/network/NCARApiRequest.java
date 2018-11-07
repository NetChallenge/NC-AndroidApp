package com.vuforia.samples.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.vuforia.samples.model.User;
import com.vuforia.samples.mqtt.MQTTServerOpts;
import com.vuforia.samples.model.Room;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public class NCARApiRequest {
    public enum NCARApi_Err {
        SUCCESS,
        FACE_NOT_FOUND,
        AUDIO_NOT_FOUND,
        UNKNOWN_ERR,
        NETWORK_ERR,
        FILE_ERR,
        ROOM_NOT_FOUND,
        ALREADY_EXIST
    }

    //private static String baseUrl = "http://45.248.74.89:5679";
    //private static String baseUrl = "http://163.180.117.216:5679";
    private static String baseUrl = "http://192.168.0.11:5679";

    private interface NCARApi {
        @POST("checkIsRegister")
        Call<String> checkIsRegister(@Query("email") String userEmail, @Query("name") String userName);

        @POST("detectFace")
        @Multipart
        Call<ResponseBody> detectFace(@Part("token") RequestBody token, @Part MultipartBody.Part imageFile);

        @POST("saveFace")
        @Multipart
        Call<String> saveFace(@Part("email") RequestBody email, @Part("name") RequestBody name, @Part MultipartBody.Part imageFile);

        @POST("saveAudio")
        @Multipart
        Call<String> saveAudio(@Part("email") RequestBody email, @Part("name") RequestBody name, @Part MultipartBody.Part audioFile);

        @POST("getRoomInfoByEmail")
        Call<Object> getRoomInfoByEmail(@Query("email") String userEmail);

        @POST("getRoomInfoByTitle")
        Call<Object> getRoomInfoByTitle(@Query("title") String roomTitle);

        @POST("searchUser")
        Call<Object> searchUser(@Query("name") String userName);

        @POST("createRoom")
        Call<Object> createRoom(@Query("email") String email, @Query("name") String name, @Query("title") String roomTitle, @Query("users") String usersJson);

        @POST("enterRoom")
        Call<Object> enterRoom(@Query("email") String email, @Query("room_id") int room_id, @Query("name") String userName);

        @POST("leaveRoom")
        Call<Object> leaveRoom(@Query("email") String email, @Query("room_id") int room_id);
    }

    private static NCARApi getNCARApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(NCARApi.class);
    }

    public static NCARApi_Err checkIsRegister(String userEmail, String userName) {
        NCARApi api = getNCARApi();
        Call<String> checkIsRegisterCall = api.checkIsRegister(userEmail, userName);

        try {
            Response<String> response = checkIsRegisterCall.execute();
            if(response.code() != 200)
                return NCARApi_Err.UNKNOWN_ERR;

            int result = Integer.parseInt(response.body());
            switch (result) {
                case 0:
                    return NCARApi_Err.SUCCESS;
                case 1:
                    return NCARApi_Err.FACE_NOT_FOUND;
                case 2:
                    return NCARApi_Err.AUDIO_NOT_FOUND;
                default:
                    return NCARApi_Err.UNKNOWN_ERR;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return NCARApi_Err.NETWORK_ERR;
        }
    }

    public static NCARApi_Err saveFace(Context context, String userEmail, String userName, Bitmap face) {
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
        } catch (IOException e) {
            e.printStackTrace();
            return NCARApi_Err.FILE_ERR;
        }

        try {
            NCARApi api = getNCARApi();

            Call<String> uploadImageCall = api.saveFace(
                    RequestBody.create(MediaType.parse("text/plain"), userEmail),
                    RequestBody.create(MediaType.parse("text/plain"), userName),
                    MultipartBody.Part.createFormData(
                            "image",
                            file.getName(),
                            RequestBody.create(MediaType.parse("image"), file)));

            Response<String> response = uploadImageCall.execute();
            if(response.code() != 200)
                return NCARApi_Err.UNKNOWN_ERR;

            int result = Integer.parseInt(response.body());
            switch (result) {
                case 1:
                    return NCARApi_Err.SUCCESS;
                case 0:
                    return NCARApi_Err.UNKNOWN_ERR;
                default:
                    return NCARApi_Err.UNKNOWN_ERR;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return NCARApi_Err.NETWORK_ERR;
        }
    }

    public static Pair<NCARApi_Err, String> detectFace(Context context, String userToken, Bitmap face) {
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

            Response<ResponseBody> response = uploadImageCall.execute();
            if(response.code() != 200)
                return new Pair(NCARApi_Err.UNKNOWN_ERR, null);

            String result = (response.body().string());
            return new Pair(NCARApi_Err.SUCCESS, result);
        } catch (IOException e) {
            e.printStackTrace();
            return new Pair(NCARApi_Err.NETWORK_ERR, null);
        }
    }

    public static NCARApi_Err saveAudio(Context context, String userEmail, String userName, File audio) {
        try {
            NCARApi api = getNCARApi();

            Call<String> uploadAudioCall = api.saveAudio(
                    RequestBody.create(MediaType.parse("text/plain"), userEmail),
                    RequestBody.create(MediaType.parse("text/plain"), userName),
                    MultipartBody.Part.createFormData(
                            "audio",
                            audio.getName(),
                            RequestBody.create(MediaType.parse("audio"), audio)));

            Response<String> response = uploadAudioCall.execute();
            if(response.code() != 200)
                return NCARApi_Err.UNKNOWN_ERR;

            int result = Integer.parseInt(response.body());
            switch (result) {
                case 1:
                    return NCARApi_Err.SUCCESS;
                case 0:
                    return NCARApi_Err.UNKNOWN_ERR;
                default:
                    return NCARApi_Err.UNKNOWN_ERR;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return NCARApi_Err.NETWORK_ERR;
        }
    }

    public static Pair<NCARApi_Err, Room> getRoomInfoByEmail(String userEmail) {
        NCARApi api = getNCARApi();
        Call<Object> getRoomInfoByEmailCall = api.getRoomInfoByEmail(userEmail);

        try {
            Response<Object> response = getRoomInfoByEmailCall.execute();
            if(response.code() == 204)
                return new Pair(NCARApi_Err.ROOM_NOT_FOUND, null);
            if(response.code() != 200)
                return new Pair(NCARApi_Err.UNKNOWN_ERR, null);

            String jsonString = new Gson().toJson(response.body());
            JSONObject jsonObject = new JSONObject(jsonString);

            if(jsonObject.isNull("stt_id")) {
                Room room = new Room(
                        jsonObject.getInt("room_id"),
                        jsonObject.getString("room_title"),
                        new MQTTServerOpts(
                                jsonObject.getString("mqtt_ip"),
                                jsonObject.getInt("mqtt_port"),
                                jsonObject.getString("mqtt_topic")),
                        new SocketOpts(
                                jsonObject.getString("stt_ip"),
                                jsonObject.getInt("stt_port"),
                                jsonObject.getInt("stt_max_size")),
                        null);

                return new Pair(NCARApi_Err.SUCCESS, room);
            }
            else {
                Room room = new Room(
                        jsonObject.getInt("room_id"),
                        jsonObject.getString("room_title"),
                        new MQTTServerOpts(
                                jsonObject.getString("mqtt_ip"),
                                jsonObject.getInt("mqtt_port"),
                                jsonObject.getString("mqtt_topic")),
                        new SocketOpts(
                                jsonObject.getString("stt_ip"),
                                jsonObject.getInt("stt_port"),
                                jsonObject.getInt("stt_max_size")),
                        jsonObject.getString("stt_id"));

                return new Pair(NCARApi_Err.SUCCESS, room);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new Pair(NCARApi_Err.NETWORK_ERR, null);
        } catch (JSONException e) {
            e.printStackTrace();
            return new Pair(NCARApi_Err.UNKNOWN_ERR, null);
        }
    }

    public static Pair<NCARApi_Err, Room> getRoomInfoByTitle(String roomTitle) {
        NCARApi api = getNCARApi();
        Call<Object> getRoomInfoByTitleCall = api.getRoomInfoByTitle(roomTitle);

        try {
            Response<Object> response = getRoomInfoByTitleCall.execute();
            if(response.code() == 204)
                return new Pair(NCARApi_Err.ROOM_NOT_FOUND, null);
            if(response.code() != 200)
                return new Pair(NCARApi_Err.UNKNOWN_ERR, null);

            String jsonString = new Gson().toJson(response.body());
            JSONObject jsonObject = new JSONObject(jsonString);
            Room room = new Room(
                    jsonObject.getInt("room_id"),
                    jsonObject.getString("room_title"),
                    new MQTTServerOpts(
                            jsonObject.getString("mqtt_ip"),
                            jsonObject.getInt("mqtt_port"),
                            jsonObject.getString("mqtt_topic")),
                    new SocketOpts(
                            jsonObject.getString("stt_ip"),
                            jsonObject.getInt("stt_port"),
                            jsonObject.getInt("stt_max_size")),
                    jsonObject.getString("stt_id"));

            return new Pair(NCARApi_Err.SUCCESS, room);
        } catch (IOException e) {
            e.printStackTrace();
            return new Pair(NCARApi_Err.NETWORK_ERR, null);
        } catch (JSONException e) {
            e.printStackTrace();
            return new Pair(NCARApi_Err.UNKNOWN_ERR, null);
        }
    }

    public static Pair<NCARApi_Err, ArrayList<User>> searchUser(String userName) {
        try {
            NCARApi api = getNCARApi();

            Call<Object> searchUserCall = api.searchUser(userName);
            Response<Object> response = searchUserCall.execute();
            if(response.code() != 200)
                return new Pair(NCARApi_Err.UNKNOWN_ERR, null);

            String jsonString = new Gson().toJson(response.body());
            JSONArray jsonArray = new JSONArray(jsonString);

            ArrayList<User> users = new ArrayList<>();
            for(int i=0; i<jsonArray.length(); i++) {
                JSONArray jsonArray1 = jsonArray.getJSONArray(i);
                users.add(new User(jsonArray1.getString(0), jsonArray1.getString(1)));
            }
            return new Pair(NCARApi_Err.SUCCESS, users);
        } catch (IOException e) {
            e.printStackTrace();
            return new Pair(NCARApi_Err.NETWORK_ERR, null);
        } catch (JSONException e) {
            e.printStackTrace();
            return new Pair(NCARApi_Err.UNKNOWN_ERR, null);
        }

    }

    public static Pair<NCARApi_Err, Room> createRoom(String userEmail, String userName, String roomTitle, ArrayList<User> users) {
        try {
            NCARApi api = getNCARApi();

            Gson gson = new GsonBuilder().create();
            JsonArray usersJson = gson.toJsonTree(users).getAsJsonArray();
            Call<Object> uploadImageCall = api.createRoom(userEmail, userName, roomTitle, usersJson.toString());
            Response<Object> response = uploadImageCall.execute();
            if(response.code() == 204)
                return new Pair(NCARApi_Err.ALREADY_EXIST, null);
            else if(response.code() != 200)
                return new Pair(NCARApi_Err.UNKNOWN_ERR, null);

            String jsonString = new Gson().toJson(response.body());
            JSONObject jsonObject = new JSONObject(jsonString);

            Room room = new Room(
                    jsonObject.getInt("room_id"),
                    jsonObject.getString("room_title"),
                    new MQTTServerOpts(
                            jsonObject.getString("mqtt_ip"),
                            jsonObject.getInt("mqtt_port"),
                            jsonObject.getString("mqtt_topic")),
                    new SocketOpts(
                            jsonObject.getString("stt_ip"),
                            jsonObject.getInt("stt_port"),
                            jsonObject.getInt("stt_max_size")),
                    jsonObject.getString("stt_id"));

            return new Pair(NCARApi_Err.SUCCESS, room);
        } catch (IOException e) {
            e.printStackTrace();
            return new Pair(NCARApi_Err.NETWORK_ERR, null);
        } catch (JSONException e) {
            e.printStackTrace();
            return new Pair(NCARApi_Err.UNKNOWN_ERR, null);
        }
    }

    public static Pair<NCARApi_Err, Integer> enterRoom(String userEmail, int room_id, String userName) {
        try {
            NCARApi api = getNCARApi();

            Call<Object> uploadImageCall = api.enterRoom(userEmail, room_id, userName);
            Response<Object> response = uploadImageCall.execute();
            if(response.code() != 200)
                return new Pair(NCARApi_Err.UNKNOWN_ERR, null);

            String jsonString = new Gson().toJson(response.body());
            JSONObject jsonObject = new JSONObject(jsonString);

            return new Pair(NCARApi_Err.SUCCESS, jsonObject.getInt("stt_port"));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pair(NCARApi_Err.NETWORK_ERR, null);
        } catch (JSONException e) {
            e.printStackTrace();
            return new Pair(NCARApi_Err.UNKNOWN_ERR, null);
        }
    }

    public static NCARApi_Err leaveRoom(String userEmail, int room_id) {
        try {
            NCARApi api = getNCARApi();

            Call<Object> leaveRoomCall = api.leaveRoom(userEmail, room_id);
            Response<Object> response = leaveRoomCall.execute();
            if(response.code() != 200)
                return NCARApi_Err.UNKNOWN_ERR;

            return NCARApi_Err.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            return NCARApi_Err.NETWORK_ERR;
        }
    }
}
