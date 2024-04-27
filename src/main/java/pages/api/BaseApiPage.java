package pages.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.TestDataLoader;
import io.cucumber.core.exception.CucumberException;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import twitter4j.JSONObject;

public class BaseApiPage {

  private static final Logger LOG = LogManager.getLogger(BaseApiPage.class);
  public Integer responseCode;
  public JsonNode responseBody;

  //Base wait for api call
  public WaitTimeBuilder waitFor(int timeout) {
    return new WaitTimeBuilder(timeout);
  }

  public static class WaitTimeBuilder {

    private final int duration;

    private WaitTimeBuilder(int timeout) {
      this.duration = timeout;
    }

    public void seconds() {
      try {
        TimeUnit.SECONDS.sleep(duration);
        LOG.info("Wait for {} seconds", duration);
      } catch (InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt();
      }
    }

    public void minutes() {
      try {
        TimeUnit.MINUTES.sleep(duration);
        LOG.info("Wait for {} minutes", duration);
      } catch (InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt();
      }
    }

    public void milliseconds() {
      try {
        TimeUnit.MILLISECONDS.sleep(duration);
        LOG.info("Wait for {} milliseconds", duration);
      } catch (InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt();
      }
    }
  }

  public RequestBody getBodyJSON(String fileName) {
    String bodyString = getBodyString(fileName);
    MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
    LOG.info("send body: {}", bodyString);
    return RequestBody.create(bodyString, mediaType);
  }

  public String getBodyString(String fileName) {
    LOG.info("Loading: \"src/main/java/testdata/apibody/{}.json\"", fileName);
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> testMap = null;
    try {
      testMap = mapper.readValue(new File("src/main/java/testdata/apibody/" + fileName + ".json"),
          new TypeReference<Map<String, String>>() {
          });
    } catch (Exception e) {
      e.printStackTrace();
    }
    assert testMap != null;
    testMap = testDataHandling(testMap);
    JSONObject testObj = new JSONObject();
    Objects.requireNonNull(testMap).forEach(testObj::append);
    return testObj.toString().replaceAll("[\\[\\]]", "").replace("\"true\"", String.valueOf(true))
        .replace("\"false\"", String.valueOf(false));
  }

  public Map<String, String> testDataHandling(Map<String, String> map) {
    map.replaceAll((k, v) -> TestDataLoader.getTestData(v));
    map.forEach((k, v) -> {
          if (!v.equalsIgnoreCase("true") && !v.equalsIgnoreCase("false") && !v.equalsIgnoreCase(
              "yes")) {
            TestDataLoader.setTestData("API_" + k.replaceAll("[_]*[c]", "").replaceAll("[_]", ""), v);
          }
        }
    );
    return map;
  }

  public void sendRequest(Request request) {
    OkHttpClient client = new OkHttpClient().newBuilder().build();
    Response response;
    try {
      LOG.info("Sent request information: {}", request);
      response = client.newCall(request).execute();
    } catch (Exception ex) {
      LOG.info("Api call is not successful. The error is {}", ex.getMessage());
      throw new IllegalArgumentException();
    }
    assert Objects.requireNonNull(response).body() != null;
    JsonNode rootNode;
    try {
      assert response.body() != null;
      String responseBodyRAW = Objects.requireNonNull(response.body()).string();
      response.close();
      ObjectMapper objectMapper = new ObjectMapper();
      rootNode = objectMapper.readTree(responseBodyRAW);
    } catch (Exception ex) {
      LOG.error(ex.getMessage());
      throw new CucumberException("Failed to parse response body.");
    }
    responseCode = response.code();
    responseBody = rootNode;
    response.close();
    LOG.info("received response code: \"{}\"", responseCode);
  }

  void sendRequest(Request request, int interval, int attempt) {
    int attempts = 0;
    do {
      try {
        sendRequest(request);
        break;
      } catch (RuntimeException e) {
        LOG.info("Api call not successful, retry after {} seconds", interval);
        waitFor(interval).seconds();
        attempts++;
      }
    } while (attempts < attempt);
    if (attempts >= attempt) {
      throw new CucumberException("Fail to call api after " + interval * attempt / 60 + " minutes");
    }
  }
}
