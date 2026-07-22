package cn.qihangerp.utils.http;

import tools.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

/**
 * @program:
 * @description
 * @author: qi
 * @create: 2024-08-28 15:20
 **/
public class OkHttpClientHelper {
    private static final OkHttpClient client = new OkHttpClient(); // 单例 OkHttpClient 实例
    private static final ObjectMapper objectMapper = new ObjectMapper();
    // 执行 GET 请求
    public static String get(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }
    public static String get(String serverUrl, Map<String, String> params, Map<String, String> headers) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().build();

        // 构造URL及其参数
        HttpUrl.Builder urlBuilder = HttpUrl.parse(serverUrl).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        // 获取查询参数部分
        HttpUrl url = urlBuilder.build();

        // 拼接成完整 URL
        String fullUrl = serverUrl + "?" + url.query();


        // 构建请求头
        Headers.Builder headersBuilder = new Headers.Builder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headersBuilder.add(entry.getKey(), entry.getValue());
        }
        Headers header = headersBuilder.build();

        // 创建 GET 请求
        Request request = new Request.Builder()
                .url(fullUrl)
                .headers(header)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("返回状态码: " + response.code() + ", message: " + response.message());
            return response.body().string();
        }
    }

    // 执行 POST 请求
    public static String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), json);
        Request request = new Request.Builder().url(url).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }

    // 执行 POST 请求
    public static String post(String url,Map<String, String> headers,String json) throws IOException {
        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), json);

        // 构建请求头
        Headers.Builder headersBuilder = new Headers.Builder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headersBuilder.add(entry.getKey(), entry.getValue());
        }
        Headers header = headersBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .headers(header)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if(response.code()==400) return "400";
            else if (response.code()==403) return "403";
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }


    public static String post(String url,Map<String, String> headers,Map<String, Object> requestBody) throws IOException {
        // 将Map转换为JSON字符串
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        // 构建请求体
        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonBody);

        // 构建请求头
        Headers.Builder headersBuilder = new Headers.Builder();
        if(headers!=null&&headers.size()>0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                headersBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        Headers header = headersBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .headers(header)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if(response.code()==400) return "400";
            else if (response.code()==403) return "403";
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }

//    /**
//    * @Description: 实名认证post
//    * @Param: [appCode, url, params]
//    * @return: java.lang.String
//    * @Author: qi
//    * @Date: 2024/9/11 10:38
//    */
//    public static String postForm(String appCode, String url, Map<String, String> params) throws IOException {
//        OkHttpClient client = new OkHttpClient.Builder().build();
//        FormBody.Builder formbuilder = new FormBody.Builder();
//        Iterator<String> it = params.keySet().iterator();
//        while (it.hasNext()) {
//            String key = it.next();
//            formbuilder.add(key, params.get(key));
//        }
//        FormBody body = formbuilder.build();
//        Request request = new Request.Builder().url(url).addHeader("Authorization", "APPCODE " + appCode).post(body).build();
//        Response response = client.newCall(request).execute();
//        System.out.println("返回状态码" + response.code() + ",message:" + response.message());
//        String result = response.body().string();
//        return result;
//    }

    /**
     * 发送 POST 请求并返回响应结果
     *
     * @param url      请求的 URL
     * @param params   表单参数
     * @param headers  HTTP 请求头
     * @return 响应结果
     * @throws IOException
     */
    public static String postForm(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().build();
        FormBody.Builder formBuilder = new FormBody.Builder();

        // 添加表单参数
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }

        FormBody body = formBuilder.build();

        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);

        // 添加 HTTP 请求头
        for (Map.Entry<String, String> header : headers.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }

        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println("返回状态码: " + response.code() + ", message: " + response.message());
            return response.body().string();
        }
    }
}
