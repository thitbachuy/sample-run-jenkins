package pages.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.TestDataLoader;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.datatable.DataTable;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JiraApiPage extends BaseApiPage {

  private static final Logger LOG = LogManager.getLogger(JiraApiPage.class);
  private static final String BASE_JIRA_URL = TestDataLoader.getTestData(
      "https://sky.atlassian.net/rest/api/3/issue/ISSUE");
  private static final String JIRA_USERNAME = TestDataLoader.getTestData("@TD:JiraUsername");
  private static final String JIRA_TOKEN = TestDataLoader.getTestData("@TD:JiraApiToken");

  public void createUpdateJiraIssueRequestBody(DataTable dataTable) {
    String mainBody = "{\"update\":{%s}}";
    List<Map<String, String>> info = dataTable.asMaps(String.class, String.class);

    for (Map<String, String> itemRow : info) {
      String mainBodyCopy = mainBody;
      String summaryBody = "\"summary\": [%s]";
      String labelsBody = "\"labels\": [%s]";
      String innerLabelBody = "";
      String addLabelBody = "{\"add\":\"%s\"}";
      String removeLabelBody = "{\"remove\":\"%s\"}";

      String summaryValue = itemRow.get("Summary");
      String addLabelValue = itemRow.get("Label to add");
      String removeLabelValue = itemRow.get("Label to remove");

      if (summaryValue != null) {
        summaryBody = String.format(summaryBody, String.format("{\"set\" : \"%s\"}", summaryValue));
      } else {
        summaryBody = "";
      }

      labelsBody = createUpdateLabelsBody(addLabelValue, removeLabelValue, labelsBody,
          innerLabelBody, addLabelBody, removeLabelBody);

      if (summaryBody.equals("") || labelsBody.equals("")) {
        mainBodyCopy = String.format(mainBodyCopy, summaryBody + labelsBody);
      } else {
        mainBodyCopy = String.format(mainBodyCopy, summaryBody + "," + labelsBody);
      }

      RequestBody body = RequestBody.create(mainBodyCopy, MediaType.parse("application/json"));

      updateJiraIssues(body, itemRow.get("Jira ID"));
    }
  }

  public String createUpdateLabelsBody(String addLabelValue, String removeLabelValue,
      String labelsBody, String innerLabelBody, String addLabelBody, String removeLabelBody) {
    if (addLabelValue != null || removeLabelValue != null) {
      innerLabelBody = createUpdateAddLabelsBody(addLabelValue, innerLabelBody, addLabelBody);

      if (addLabelValue != null && removeLabelValue != null) {
        innerLabelBody = innerLabelBody.concat(",");
      }
      innerLabelBody = createUpdateRemoveLabelsBody(removeLabelValue, innerLabelBody,
          removeLabelBody);
      labelsBody = String.format(labelsBody, innerLabelBody);
    } else {
      labelsBody = "";
    }

    return labelsBody;
  }

  public String createUpdateAddLabelsBody(String addLabelValue, String innerLabelBody,
      String addLabelBody) {
    if (addLabelValue != null) {
      if (addLabelValue.contains(",")) {
        String[] labels = addLabelValue.split(",");
        for (String label : labels) {
          if (!label.equals(labels[0])) {
            innerLabelBody = innerLabelBody.concat(",");
          }
          innerLabelBody = innerLabelBody.concat(String.format(addLabelBody, label));
        }
      } else {
        innerLabelBody = innerLabelBody.concat(String.format(addLabelBody, addLabelValue));
      }
    }

    return innerLabelBody;
  }

  public String createUpdateRemoveLabelsBody(String removeLabelValue, String innerLabelBody,
      String removeLabelBody) {
    if (removeLabelValue != null) {
      if (removeLabelValue.contains(",")) {
        String[] labels = removeLabelValue.split(",");
        for (String label : labels) {
          if (!label.equals(labels[0])) {
            innerLabelBody = innerLabelBody.concat(",");
          }
          innerLabelBody = innerLabelBody.concat(String.format(removeLabelBody, label));
        }
      } else {
        innerLabelBody = innerLabelBody.concat(String.format(removeLabelBody, removeLabelValue));
      }
    }

    return innerLabelBody;
  }

  public void updateJiraIssues(RequestBody body, String issue) {
    String creds = Credentials.basic(JIRA_USERNAME, JIRA_TOKEN);
    String url = BASE_JIRA_URL.replace("ISSUE", issue);
    Request request = new Request.Builder().url(url).method("PUT", body)
        .header("Authorization", creds).build();
    OkHttpClient client = new OkHttpClient().newBuilder().build();
    Response response = null;
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
      String responseBodyRAW = response.body().string();
      response.close();
      ObjectMapper objectMapper = new ObjectMapper();
      rootNode = objectMapper.readTree(responseBodyRAW);
    } catch (Exception ex) {
      LOG.error(ex.getMessage());
      throw new CucumberException("Failed to parse response body.");
    }
    Integer responseCode = response.code();
    JsonNode responseBody = rootNode;
    response.close();
    LOG.info("received response code for updating jira id {} : \"{}\"", issue, responseCode);
    LOG.info("response body: {}", responseBody);
    Assert.assertEquals(responseCode.intValue(), 204);

  }
}
