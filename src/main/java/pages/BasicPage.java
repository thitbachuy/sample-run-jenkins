package pages;

import config.BasePage;
import config.DriverUtil;
import config.TestDataLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;
import steps.Hook;

public class BasicPage extends BasePage {

  public BasicPage(RemoteWebDriver driver) {
    super(driver);
  }

  private static final Logger LOG = LogManager.getLogger(BasicPage.class);

  public void openURL(String url) {
    String extractedUrl = loadUrl(url);
    DriverUtil.threadLocalActiveBrowsers.get().get("current").get(extractedUrl);
  }

  public String loadUrl(String url) {
    if (!url.startsWith("http://") || !url.startsWith("https://")) {
      url = TestDataLoader.getTestData(url);
    }
    return url;
  }

  public void switchTab(String tab) {
    tab = TestDataLoader.getTestData(tab);
    clickOrEvaluateAndClick(
      String.format("//a[@title='%s' and contains(@class,'label-action')]", tab));
  }

  public void closeCurrentPage() {
    threadLocalDriverBasePage.get().close();
  }

  //Save the key-value in test data runtime
  public void saveKeyWithValue(String key, String value) {
    if (value.contains(" ") && value.toLowerCase().contains("@td:")) {
      StringBuilder valueBuilder = new StringBuilder();
      String[] valueArray = value.split(" ");
      for (String val : valueArray) {
        valueBuilder.append(TestDataLoader.getTestData(val)).append(" ");
      }
      value = valueBuilder.toString().strip();
    }
    if ("Trensport_cancellation_period_mein_abo".equals(key)
      || "Trensport_end_of_commitment_mein_abo".equals(key)) {
      String[] dateValue = TestDataLoader.getTestData(value).split("-");
      value = String.format("%s.%s.%s", dateValue[2], dateValue[1], dateValue[0]);
    }
    TestDataLoader.setTestData(key, TestDataLoader.getTestData(value));
  }

  public void switchDriverToTab(String pageTitle) {
    String currentWindow = threadLocalDriverBasePage.get().getWindowHandle();
    for (String winHandle : threadLocalDriverBasePage.get().getWindowHandles()) {
      if (threadLocalDriverBasePage.get().switchTo().window(winHandle).getTitle()
        .equalsIgnoreCase(pageTitle)) {
        break;
      } else {
        threadLocalDriverBasePage.get().switchTo().window(currentWindow);
      }
    }
  }

  //Open new browser with same type from -Dbrowser and assign to an alias to handle
  public void openNewBrowserWithAlias(String browserAlias) {
    String testBrowser = Hook.browser;
    DriverUtil.initializeBrowserWithSessionAlias(testBrowser, browserAlias);
  }

}
