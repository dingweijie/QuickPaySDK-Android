package cn.digielec.quickpaysdk.retrofit.convertors;


import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit.Converter;

/**
 * 针对TypeString的注解类型进行处理
 * https://github.com/mutsinghua/Retrofit2.0FullExample
 * Created by dw on 2016/3/31.
 */
public class StringConverterFactory extends Converter.Factory {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream");//八进制文件的传输流

    /**
     * 响应
     *
     * @param type
     * @param annotations
     * @return
     */
    @Override
    public Converter<ResponseBody, ?> fromResponseBody(Type type, Annotation[] annotations) {
        if (!(type instanceof Class<?>)) {
            return null;
        }

        for (Annotation annotation : annotations) {
            if (annotation instanceof TypeString) {
                return new StringResponseConverter();
            }
        }

        return null;
    }

    /**
     * 请求
     *
     * @param type
     * @param annotations
     * @return
     */
    @Override
    public Converter<?, RequestBody> toRequestBody(Type type, Annotation[] annotations) {
        if (!(type instanceof Class<?>)) {
            return null;
        }
        for (Annotation annotation : annotations) {
            if (annotation instanceof TypeString) {
                return new StringRequestConverter();
            }
        }
        return null;
    }

    public static class StringResponseConverter implements Converter<ResponseBody, String> {

        @Override
        public String convert(ResponseBody value) throws IOException {
            return value.string();
        }
    }

    public static class StringRequestConverter implements Converter<String, RequestBody> {
        @Override
        public RequestBody convert(String value) throws IOException {
            return RequestBody.create(MEDIA_TYPE, value);
        }
    }
}
