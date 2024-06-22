package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;
    private final Map<String, List<String>> queryParams;

    public Request(String method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.path = extractPathWithoutQuery(path);
        this.headers = headers;
        this.body = body;
        this.queryParams = extractQueryParams(path);
    }

    private String extractPathWithoutQuery(String path) {
        int queryStart = path.indexOf("?");
        return (queryStart != -1) ? path.substring(0, queryStart) : path;
    }

    private Map<String, List<String>> extractQueryParams(String path) {
        int queryStart = path.indexOf("?");
        if (queryStart == -1) {
            return Map.of();
        }
        String query = path.substring(queryStart + 1);
        List<NameValuePair> params = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
        return params.stream().collect(Collectors.groupingBy(NameValuePair::getName,
                Collectors.mapping(NameValuePair::getValue, Collectors.toList())));
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getQueryParam(String name) {
        List<String> values = queryParams.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    public List<String> getQueryParams(String name) {
        return queryParams.getOrDefault(name, List.of());
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }
}