package config;

/*
    All basic functions are stored in here like enter text, click element, select from Dropdown etc.
    If you have additional basic functions please enter in here as all Page classes are extended from BasePage
*/

import io.cucumber.core.exception.CucumberException;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import modal.Directions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

public class BasePage {

  private static final Logger LOG = LogManager.getLogger(BasePage.class);
  private static final int WAIT_TIMEOUT = 60;
  public static ThreadLocal<RemoteWebDriver> threadLocalDriverBasePage = new ThreadLocal<>();
  private static final Random r = new Random();
  private WebElement e;

  public BasePage(RemoteWebDriver driver) {
    threadLocalDriverBasePage.set(
      DriverUtil.threadLocalActiveBrowsers.get().getOrDefault("current", driver));
  }

  public void enterText(String text, String locatorTextField, String textField) {
    text = TestDataLoader.getTestData(text);
    WebElement textFieldElem = verifyVisibilityOfElement(textField, locatorTextField);

    if (!textFieldElem.isDisplayed()) {
      throw new CucumberException("TextField " + textField + " not found!");
    }
  }


  public void clickWebElement(String locatorWebElement, String webElement) {
    verifyVisibilityOfElement(webElement, locatorWebElement).click();
    LOG.info("clicked Button {}", webElement);
  }

  public WebElement waitForVisibilityOfElementLocated(By by) {
    WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(),
      Duration.ofSeconds(WAIT_TIMEOUT));
    return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
  }

  public Boolean waitForInvisibilityOfElementLocated(By by, int timeout) {
    WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(),
      Duration.ofSeconds(timeout));
    return wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
  }

  public WebElement waitForPresenceOfElementLocated(By by) {
    WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(),
      Duration.ofSeconds(WAIT_TIMEOUT));
    return wait.until(ExpectedConditions.presenceOfElementLocated(by));
  }

  public List<WebElement> waitForPresenceOfElementsLocated(By by) {
    WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(60));
    return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
  }

  public void waitForPageLoaded() {
    WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(50));
    wait.until(wd -> ((threadLocalDriverBasePage.get()).executeScript("return document.readyState")
      .equals("complete")));
    int count = 0;
    if ((boolean) (threadLocalDriverBasePage.get()).executeScript(
      "return window.jQuery != undefined")) {
      while (!(boolean) (threadLocalDriverBasePage.get()).executeScript(
        "return jQuery.active == 0")) {
        waitFor(1).seconds();
        if (count > 400) {
          break;
        }
        count++;
      }
    }
  }


  public void executeJs(String js) {
    threadLocalDriverBasePage.get().executeScript(js);
  }

  public void executeJs(String js, WebElement e) {
    threadLocalDriverBasePage.get().executeScript(js, e);
  }

  public void scrollTo(WebElement webElement) {
    try {
      executeJs("arguments[0].scrollIntoView(false);", webElement);
    } catch (Exception e) {
      waitFor(5).seconds();
      executeJs("arguments[0].scrollIntoView(false);", webElement);
    }
  }

  public ScrollBuilder scrollInsideElement(String elementXpath) {
    return new ScrollBuilder(elementXpath);
  }

  public class ScrollBuilder {

    private final String elementXpath;
    private Directions direction;
    private String script;
    private int loopCount = 1;

    public ScrollBuilder(String elementXpath) {
      this.elementXpath = elementXpath;
    }

    public ScrollBuilder to(Directions direction) {
      this.direction = direction;
      switch (direction) {
        case TOP:
          this.script = "arguments[0].scrollTo(0, 0);";
          break;
        case BOTTOM:
          this.script = "arguments[0].scrollTo(0, arguments[0].scrollHeight);";
          break;
        default:
          LOG.info("Directions supported available are: {}, {}", Directions.TOP, Directions.BOTTOM);
          LOG.info("Other directions will be developed in need. Thanks for using!");
          throw new CucumberException(
            String.format("Direction is not supported in this version: [%s]", direction));
      }
      return this;
    }

    public ScrollBuilder withLoop(int loopCount) {
      this.loopCount = loopCount;
      return this;
    }

    public void perform() {
      Assert.assertTrue(this.loopCount > 0,
        String.format("Wrong number of loop: [%s]. Please pass the integer value greater than 0!",
          this.loopCount));
      for (int loop = 0; loop < this.loopCount; loop++) {
        executeJs(script, waitForVisibilityOfElementLocated(By.xpath(this.elementXpath)));
        LOG.info("User scrolls to {} the {} time(s)", this.direction, loop + 1);
        waitFor(1).seconds();
      }
    }
  }

  public void scrollElementToCenter(WebElement e) {
    String scrollElementIntoMiddle =
      "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
        + "var elementTop = arguments[0].getBoundingClientRect().top;"
        + "window.scrollBy(0, elementTop-(viewPortHeight/2));";

    threadLocalDriverBasePage.get().executeScript(scrollElementIntoMiddle, e);
    waitFor(500).milliseconds();
  }

  public void waitForSpinnerCDP(int waitTimeForSpinner) {
    String spinnerXpath = "//*[contains(@data-testid,'spinner')]|//*[contains(@class,'is-loading')]";
    try {
      waitForVisibilityOfElementLocated(By.xpath(spinnerXpath), waitTimeForSpinner);
      LOG.info("Loading Spinner is visible. Wait until Spinner disappears...");
      waitForInvisibilityOfElementLocated(By.xpath(spinnerXpath), 300);
      LOG.info("Loading Spinner is invisible");
    } catch (Exception ignored) {
      LOG.info("Loading Spinner has not appeared");
    }
  }

  public void waitForSpinnerSF(int waitTimeForSpinner) {
    String spinnerXpath = "(//div[contains(@class,'loadingSpinner slds-spinner_container') or contains(@class,'slds-spinner_large') or contains(@class,' slds-spinner_medium') or contains(@class,'  forceInlineSpinner')])[last()]";
    try {
      waitForVisibilityOfElementLocated(By.xpath(spinnerXpath), waitTimeForSpinner);
      LOG.info("Loading Spinner is visible. Wait until Spinner disappears...");
      waitForInvisibilityOfElementLocated(By.xpath(spinnerXpath), 300);
      LOG.info("Loading Spinner is invisible");
    } catch (Exception ignored) {
      LOG.info("Loading Spinner has not appeared");
    }
  }

  public void currentPageHasText(String text) {
    if (text.toUpperCase().startsWith("@TD:")) {
      text = TestDataLoader.getTestData(text);
    }
    String selector = String.format("(//*[text()='%s' or contains(text(), '%s')])[1]", text, text);
    try {
      waitForVisibilityOfElementLocated(By.xpath(selector), 120);
    } catch (Exception e) {
      scrollToBottom(100);
      waitForVisibilityOfElementLocated(By.xpath(selector));
    }
    LOG.info("Current page contains an element with text \"{}\".", text);
  }

  protected String getString(String text) {
    if (text.startsWith("@TD:")) {
      text = TestDataLoader.getTestData(text);
    }
    return String.format("//*[text()='%s' or contains(text(), '%s')]|//p[contains(.,'%s')]", text,
      text, text);
  }

  public void evaluateXpathAndClick(String xpath) {
    WebElement scrollElement = verifyVisibilityOfElement(xpath, xpath, 30);
    scrollElementToCenter(scrollElement);
    executeJs("arguments[0].click();", scrollElement);
  }

  public void clickOrEvaluateAndClick(String selector) {
    LOG.info("Element locator is {}", selector);
    try {
      clickWebElement(selector, selector);
    } catch (Exception | AssertionError e) {
      // To avoid changing of selector this solution may help...
      LOG.info("Found exception: {}", e.getMessage());
      evaluateXpathAndClick(selector);
      LOG.info("Clicked on evaluated xpath: \"{}\".", selector);
    }
  }

  public void verifyCurrentUrl(String expectedUrl) {
    waitForPageLoaded();
    expectedUrl = TestDataLoader.getTestData(expectedUrl);
    String actual = threadLocalDriverBasePage.get().getCurrentUrl();
    Assert.assertEquals(
      String.format("Current Url: [%s] is not expected [%s]", actual, expectedUrl), expectedUrl,
      actual);
  }

  public String getTextOfElement(By element) {
    return waitForVisibilityOfElementLocated(element).getText();
  }

  public WaitBuilder waitFor(int duration) {
    return new WaitBuilder(duration);
  }

  public class WaitBuilder {

    private final int duration;

    public WaitBuilder(int duration) {
      this.duration = duration;
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

    public void days() {
      try {
        TimeUnit.DAYS.sleep(duration);
        LOG.info("Wait for {} day(s)", duration);
      } catch (InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt();
      }
    }
  }

  public WebElement waitForVisibilityOfElementLocated(By by, int timeout) {
    WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(),
      Duration.ofSeconds(timeout));
    return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
  }

  public List<WebElement> waitForVisibilityOfAllElementsLocated(By by, int timeout) {
    WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(),
      Duration.ofSeconds(timeout));
    return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
  }

  public void scrollToBottom(int offset) {
    Long documentHeightBeforeScroll = getDocumentScrollHeight();
    Long documentHeightAfterScroll;
    int scrollHeight = 0;
    do {
      int scrollTimes = (int) (documentHeightBeforeScroll / offset);
      for (int i = 1; i <= scrollTimes; i++) {
        if (i == scrollTimes) {
          documentHeightBeforeScroll = getDocumentScrollHeight();
        }
        executeJs(String.format("window.scrollTo(0, %s);", (i * offset) + scrollHeight));
        waitFor(1).seconds();
        LOG.info("Scroll down by {}", (i * offset) + scrollHeight);
      }
      scrollHeight += scrollTimes * offset;
      documentHeightAfterScroll = getDocumentScrollHeight();
    } while (!documentHeightBeforeScroll.equals(documentHeightAfterScroll));
  }

  public Long getDocumentScrollHeight() {
    return (Long) threadLocalDriverBasePage.get()
      .executeScript("return document.documentElement.scrollHeight");
  }

  public void refresh() {
    threadLocalDriverBasePage.get().navigate().refresh();
    try {
      // wait for the alert to exist, then handle it and continue with refresh
      WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(),
        Duration.ofSeconds(5));
      wait.until(ExpectedConditions.alertIsPresent());
      threadLocalDriverBasePage.get().switchTo().alert().accept();
      threadLocalDriverBasePage.get().switchTo().parentFrame();
      waitForPageLoaded();
      waitForSpinnerSF(1);
    } catch (Exception ignored) {
    }
  }

  //Verify visibility Of element
  public WebElement verifyVisibilityOfElement(String element, String elementXpath) {
    waitForPageLoaded();
    WebElement webElement;
    try {
      webElement = waitForVisibilityOfElementLocated(By.xpath(elementXpath), 30);
      LOG.info("Current page has element {}", element);
    } catch (TimeoutException e) {
      LOG.info("Element {} not visible. Scroll to bottom and verify again", element);
      scrollToBottom(200);
      webElement = waitForVisibilityOfElementLocated(By.xpath(elementXpath), 30);
      LOG.info("Current page has element {}", element);
    } catch (Exception e) {
      throw new CucumberException("Can not find the element after " + 60 + " seconds");
    }
    scrollTo(webElement);
    return webElement;
  }

  //Verify visibility Of element with dynamic timeout
  public WebElement verifyVisibilityOfElement(String element, String elementXpath, int timeout) {
    WebElement webElement;
    try {
      webElement = waitForVisibilityOfElementLocated(By.xpath(elementXpath), timeout);
      LOG.info("Current page has element {}", element);
    } catch (TimeoutException e) {
      LOG.info("Element {} not visible. Scroll to bottom and verify again", element);
      scrollToBottom(200);
      webElement = waitForVisibilityOfElementLocated(By.xpath(elementXpath), 10);
      LOG.info("Current page has element {}", element);
    } catch (Exception e) {
      throw new CucumberException("Can not find the element after " + (timeout + 10) + "seconds");
    }
    scrollTo(webElement);
    return webElement;
  }

  public void zoomInOut(String percent) {
    executeJs(String.format("document.body.style.zoom = '%s'", percent));
    LOG.info("Zoomed {} ", percent);
  }

  public void setIndexOfColumnsAreDisplayedInTable() {
    try {
      waitForVisibilityOfElementLocated(
        By.xpath(TestDataLoader.getTestData("@TD:listColumnLocator")), 5);
    } catch (TimeoutException e) {
      if (Boolean.parseBoolean(TestDataLoader.getTestData("@TD:isTableLocatedInsideElement"))) {
        scrollInsideElement(
          "//flexipage-record-home-scrollable-column[contains(@id,'middleColumn')]").to(
          Directions.BOTTOM).withLoop(2).perform();
      } else {
        scrollToBottom(200);
      }
    }
    int index = 0;
    List<WebElement> allTableColumns = waitForPresenceOfElementsLocated(
      By.xpath(TestDataLoader.getTestData("@TD:listColumnLocator")));
    for (WebElement element : allTableColumns) {
      index++;
      LOG.info("Index of column {} is {}", element.getText(), index);
      TestDataLoader.setTestData(element.getText() + "_columnIndex", String.valueOf(index));
    }
  }


  public int getIndexOfRowInTableHasValue(String rowValue) {
    rowValue = TestDataLoader.getTestData(rowValue);
    int index = 0;
    List<WebElement> allTableRows = waitForPresenceOfElementsLocated(
      By.xpath(TestDataLoader.getTestData("@TD:listRowLocator")));
    TestDataLoader.setTestData("isRowValuePresent", "false");
    for (WebElement element : allTableRows) {
      index++;
      if (element.getText().startsWith(rowValue)) {
        LOG.info("Index of row having value '{}' is '{}'", rowValue, index);
        TestDataLoader.setTestData("@TD:rowIndex", String.valueOf(index));
        TestDataLoader.setTestData("isRowValuePresent", "true");
        break;
      }
    }
    return index;
  }

  public String getCellValue(int columnIndex, int rowIndex, String expectedResult) {
    String result;
    String baseSelector = String.format(TestDataLoader.getTestData("@TD:cellValueLocator"),
      rowIndex, columnIndex);
    if (expectedResult.equalsIgnoreCase("checked") || expectedResult.equalsIgnoreCase(
      "unchecked")) {
      if (isCheckboxChecked(
        String.format(TestDataLoader.getTestData("@TD:cellValueLocator"), rowIndex, columnIndex),
        TestDataLoader.getTestData("@TD:checkboxTagAttribute"),
        TestDataLoader.getTestData("@TD:checkboxAttributeActiveValue"),
        TestDataLoader.getTestData("@TD:checkboxAttributeInactiveValue"))) {
        LOG.info("Cell value is: checked");
        return "checked";
      } else {
        LOG.info("Cell value is: unchecked");
        return "unchecked";
      }
    }
    if (expectedResult.equalsIgnoreCase("empty")) {
      result = waitForPresenceOfElementLocated(
        By.xpath(String.format(baseSelector, rowIndex, columnIndex))).getAttribute("innerText");
    } else {
      result = threadLocalDriverBasePage.get()
        .findElement(By.xpath(String.format(baseSelector, rowIndex, columnIndex)))
        .getAttribute("innerText");
    }
    LOG.info("Cell value is: \"{}\"", result);
    if (expectedResult.equalsIgnoreCase("empty") || expectedResult.equalsIgnoreCase("not empty")) {
      String cellValue = result.replaceAll("(?m)^\\s+$", "");
      if (cellValue.isEmpty()) {
        return "empty";
      } else {
        return "not empty";
      }
    }
    return result;
  }

  public boolean isCheckboxChecked(String locator, String tagAttribute, String activeValue,
    String inactiveValue) {
    WebElement element = verifyVisibilityOfElement(locator, locator);
    if (tagAttribute.isBlank() && activeValue.isBlank() && inactiveValue.isBlank()) {
      return element.isSelected();
    } else {
      Assert.assertNotNull("Class Attribute can not be null", tagAttribute);
      String actualAttributeValue = element.getAttribute(tagAttribute);
      if (activeValue != null && inactiveValue == null) {
        return (actualAttributeValue.equalsIgnoreCase(activeValue));
      } else if (activeValue == null && inactiveValue != null) {
        return (!actualAttributeValue.equalsIgnoreCase(inactiveValue));
      } else if (activeValue != null) {
        return actualAttributeValue.contains(activeValue) && !actualAttributeValue.contains(
          inactiveValue);
      } else {
        throw new CucumberException(
          "Active value and inactive value can not be null at the same time");
      }
    }
  }

}
