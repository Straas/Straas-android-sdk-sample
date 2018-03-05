package io.straas.android.sdk.circall.demo;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class RoomTokenFetcher {
    private static final String TAG = RoomTokenFetcher.class.getSimpleName();

    /*
   * create token POST body: username: user, role: presenter
   * room: basicExampleRoom, type: erizo
   */
    private static String VALUE_LICODE_ROOM_ID = "basicExampleRoom";
    private static String VALUE_LICODE_ROOM_TYPE = "erizo";

    public static class RoomConnectionParameters {
        final String roomUrl;
        final String roomId;

        RoomConnectionParameters(String roomUrl, String roomId) {
            this.roomUrl = roomUrl;
            this.roomId = roomId;
        }
    }

    static Task<String> obtainLicodeToken(RoomTokenFetcher.RoomConnectionParameters roomConnectionParameters) {
        final TaskCompletionSource<String> source = new TaskCompletionSource<>();
        RoomTokenFetcher.makeRequest(roomConnectionParameters.roomUrl,
                new RoomServiceEndpoint.CreateTokenBody("presenter", VALUE_LICODE_ROOM_ID,
                        roomConnectionParameters.roomId, VALUE_LICODE_ROOM_TYPE), new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    source.setException(new IllegalArgumentException(response.toString()));
                    return;
                }

                try {
                    source.setResult(response.body().string());
                } catch (IOException ignored) {
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                source.setException(new Exception(t));
            }
        });
        return source.getTask();
    }

    private static void makeRequest(String roomHost, RoomServiceEndpoint.CreateTokenBody body, final Callback<ResponseBody> callback) {
        Log.d(TAG, "Connecting " + roomHost + " to fetch room token...");
        RoomServiceEndpoint roomServerEndpoint = createRoomServiceEndpoint(roomHost);
        Call<ResponseBody> tokenCall = roomServerEndpoint.createToken(body);
        tokenCall.enqueue(callback);
    }

    private static RoomServiceEndpoint createRoomServiceEndpoint(String roomHost) {
        return new Retrofit.Builder()
                .baseUrl(roomHost)
                .addConverterFactory(MoshiConverterFactory.create())
                .build().create(RoomServiceEndpoint.class);
    }
}
