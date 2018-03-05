package io.straas.android.sdk.circall.demo;

import okhttp3.ResponseBody;
import proguard.annotation.KeepClassMembers;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RoomServiceEndpoint {

    @POST("/createToken")
    Call<ResponseBody> createToken(@Body CreateTokenBody body);

    @KeepClassMembers
    class CreateTokenBody {
        private final String role;
        private final String room;
        private final String username;
        private final String type;

        CreateTokenBody(String role, String room, String username, String type) {
            this.role = role;
            this.room = room;
            this.username = username;
            this.type = type;
        }
    }
}

