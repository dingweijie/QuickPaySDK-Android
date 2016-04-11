package cn.digielec.quickpaysdk.retrofit.error;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;

import retrofit.Converter;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Retrofit 2 — Simple Error Handling
 * https://futurestud.io/blog/retrofit-2-simple-error-handling
 * Created by dw on 2016/4/1.
 */

public class ErrorUtils {

    public static APIError parseError(Response<?> response, Retrofit retrofit) {
        APIError error = null;

        try {
            Converter<ResponseBody, APIError> converter = retrofit.responseConverter(APIError.class, new Annotation[0]);
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return error;
    }
}
