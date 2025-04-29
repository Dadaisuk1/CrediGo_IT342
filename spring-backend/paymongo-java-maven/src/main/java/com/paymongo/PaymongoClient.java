package com.paymongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymongo.exceptions.AuthenticationException;
import com.paymongo.exceptions.InvalidRequestException;
import com.paymongo.exceptions.ResourceNotFoundException;
import com.paymongo.exceptions.StandardException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PaymongoClient
 */
public class PaymongoClient {
  public static ApiResource execute_request(String method, Object payload, String path)
      throws URISyntaxException, IOException, InterruptedException, StandardException {
    String URI = String.format("%s/%s/%s", Paymongo.API_BASE_URL, Paymongo.API_VERSION, path);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = build_request(method, payload, URI);
    HttpResponse<String> http_response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Parse JSON
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    // Format JSON to Java Object
    Map<String, Object> formatted_response = mapper.readValue(
      http_response.body(),
      new TypeReference<Map<String, Object>>() {
    });

    if (!isSuccessful(http_response)) {
      handleError(http_response, formatted_response);
    }

    ApiResource response = new ApiResource((Map<String, Object>) formatted_response);

    return response;
  }

  private static HttpRequest build_request(String method, Object params, String uri) throws URISyntaxException {
    if (method == "GET" && params != null) {
      uri = appendUrlParams(params, uri);
    }

    // By default HttpRequest request method is GET
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
        .uri(new URI(uri))
        .headers("Authorization", getBasicAuthenticationHeader(Paymongo.api_key));

    // Format params as string
    if (params != null) {
      ObjectMapper mapper = new ObjectMapper();
      try {
        params = mapper.writeValueAsString(params);
      } catch (JsonProcessingException e) {
        System.out.println(e.getMessage());
      }
    }

    String formatted_params = null;

    switch (method) {
      case "DELETE":
        requestBuilder.DELETE();
        break;

      case "POST":
        formatted_params = String.format("{ \"data\": { \"attributes\": %s } }", params);
        requestBuilder.POST(BodyPublishers.ofString(formatted_params));
        requestBuilder.header("Content-Type", "application/json");
        break;

      case "PUT":
        formatted_params = String.format("{ \"data\": { \"attributes\": %s } }", params);
        requestBuilder.PUT(BodyPublishers.ofString(formatted_params));
        requestBuilder.header("Content-Type", "application/json");
        break;
    }

    HttpRequest request = requestBuilder.build();
    return request;
  }

  private static final String appendUrlParams(Object params, String uri) {
    String url_params = ((Map<String, Object>) params).entrySet()
      .stream()
      .map(entry -> entry.getKey() + "=" + entry.getValue())
      .collect(Collectors.joining("&"));
    
    return uri + "?" + url_params;
  }

  private static final String getBasicAuthenticationHeader(String apiKey) {
    return "Basic " + Base64.getEncoder().encodeToString(apiKey.getBytes());
  }

  private static void handleError(HttpResponse<String> http_response, Map<String, Object> formatted_response) throws StandardException {
    int status_code = http_response.statusCode();

    switch (status_code) {
      case 400:
        throw new InvalidRequestException(formatted_response);
      case 401:
        throw new AuthenticationException(formatted_response);
      case 404:
        throw new ResourceNotFoundException(formatted_response);
      default:
        throw new StandardException(formatted_response);
    }
  }

  private static final boolean isSuccessful(HttpResponse<String> http_response) {
    return http_response.statusCode() == 200;
  }
}
