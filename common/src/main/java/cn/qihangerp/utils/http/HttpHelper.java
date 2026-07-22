package cn.qihangerp.utils.http;

import jakarta.servlet.ServletRequest;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * 通用http工具封装
 * 
 * @author qihang
 */
public class HttpHelper
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHelper.class);

    /**
     * post请求
     *
     * @param sendUrl 请求地址
     * @param params  请求参数
     */
    public static HttpResponse<String> doPostForJson(String sendUrl, String jsonParam) {
        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sendUrl)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(jsonParam))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            LOGGER.error("ExpressClient doPost exception:" + e);
        }
        return null;

    }

    public static String getBodyString(ServletRequest request)
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try (InputStream inputStream = request.getInputStream())
        {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = "";
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("getBodyString出现问题！");
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    LOGGER.error(ExceptionUtils.getMessage(e));
                }
            }
        }
        return sb.toString();
    }
}
