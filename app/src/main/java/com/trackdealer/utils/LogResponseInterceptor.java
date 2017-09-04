package com.trackdealer.utils;

import android.content.Context;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import timber.log.Timber;

/**
 * Created by grechnev-av on 25.08.2017.
 */

public class LogResponseInterceptor implements Interceptor {

    private final String TAG = "LogResponseInterceptor";
    private Context context;
    Charset UTF8 = Charset.forName("UTF-8");

    public LogResponseInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        RequestBody requestBody = request.body();
        if (requestBody != null && requestBody.contentLength() != 0) {
            Buffer bufferReq = new Buffer();
            requestBody.writeTo(bufferReq);
            Buffer bufferReqClone = bufferReq.clone();
            String str = bufferReqClone.readString(UTF8);
            Timber.d(TAG + " REQUEST " + convertToJson(str));
        }

        ResponseBody responseBody = response.body();
        if (responseBody.contentLength() != 0) {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            Buffer bufferClone = buffer.clone();
            String str = bufferClone.readString(UTF8);
            Timber.d(TAG + " RESPONSE " + convertToJson(str));
        }

        return response;
    }

    private String convertToJson(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        String space = "    ";
        for (char c : str.toCharArray()) {
            if (c == '{') {
                stringBuilder.append(c).append("\n");
                space += "    ";
                stringBuilder.append(space);
            } else if (c == ',') {
                stringBuilder.append(c).append("\n").append(space);
            } else if (c == '}') {
                space = space.substring(0, space.length() - 4);
                stringBuilder.append("\n").append(space).append(c);
            } else if (c == '[') {
                stringBuilder.append(c).append("\n");
                space += "    ";
                stringBuilder.append(space);
            } else if (c == ']') {
                space = space.substring(0, space.length() - 4);
                stringBuilder.append("\n").append(space).append(c);
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }
}
