package steps;

import config.DriverUtil;
import config.FilesUtils;
import config.TestDataLoader;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import pages.BasicPage;
import steps.Hook;

public class BasicSteps {

  private BasicPage basicPage;


  public BasicSteps() {
    basicPage = new BasicPage(DriverUtil.getDriver());
  }

  @Given("the user can open {string}")
  public void the_user_can_open_string(String url) {
    basicPage.openURL(url);
  }

  @Given("the current url is {string}")
  public void the_current_url_is_string(String url) {
    basicPage.verifyCurrentUrl(url);
  }

  @When("the user refresh current page")
  public void the_user_refresh_current_page() {
    basicPage.refresh();
  }

  @When("the user switch to salesforce tab {string}")
  public void the_user_switch_to_salesforce_tab_string(String tab) {
    basicPage.switchTab(tab);
  }

//  @Then("the user open browser {string}")
//  public void openBrowser(String browserName) {
//    DriverUtil.openBrowser(browserName);
//  }
//
//  @Then("the user open browser {string} with incognito")
//  public void openBrowserWithIncognito(String browserName) {
//    DriverUtil.openBrowserWithIncognito(browserName);
//  }
//
//  @Then("open browser {string} with incognito and proxy")
//  public void openBrowserWithIncognitoAndProxy(String browserName) {
//    DriverUtil.openBrowserWithProxy(browserName);
//  }
//
//  @Given("when browser is configured for proxy and open {string}")
//  public void openBrowserWithProxy(String url) {
//    String browser = Hook.browser;
//    DriverUtil.openBrowserWithProxy(browser);
//    basicPage.openURL(basicPage.loadUrl(url));
//  }

  @Given("the user switches to browser {string}")
  public void the_user_switches_to_browser_string(String browserName) {
    DriverUtil.switchToBrowser(TestDataLoader.getTestData(browserName));
  }

  @Then("the user closes browser {string}")
  public void the_user_close_browser(String browserName) {
    DriverUtil.closeBrowser(browserName);
  }

  @Then("the user sees text {string}")
  public void the_user_sees_text(String text) {
    basicPage.currentPageHasText(text);

  }

  @And("the user closes current page")
  public void the_user_closes_current_page() {
    basicPage.closeCurrentPage();
  }

  @Then("the user waits {string} minutes on current page")
  public void the_user_waits_string_minutes_on_current_page(String time) {

    basicPage.waitFor(Integer.parseInt(time)).minutes();
  }


  @And("the user saves {string} with value {string}")
  public void the_user_saves_string_with_value_string(String key, String value) {

    basicPage.saveKeyWithValue(key, value);
  }

  @Then("the user waits for {string} {string} on current page")
  public void the_user_waits_for_string_string_on_current_page(String timeout, String timeUnit) {

    switch (timeUnit) {
      case "milliseconds":
        basicPage.waitFor(Integer.parseInt(timeout)).milliseconds();
        break;
      case "seconds":
        basicPage.waitFor(Integer.parseInt(timeout)).seconds();
        break;
      case "minutes":
        basicPage.waitFor(Integer.parseInt(timeout)).minutes();
        break;
      case "days":
        basicPage.waitFor(Integer.parseInt(timeout)).days();
        break;
      default:
        throw new CucumberException(
          "Invalid time unit. The time unit should be the following: {'milliseconds', 'seconds', 'minutes', 'days'}");
    }
  }

  @And("the user switches focus to tab {string}")
  public void the_user_switches_focus_to_new_tab(String pageTitle) {

    basicPage.switchDriverToTab(pageTitle);
  }

  @Given("the user opens new browser with alias {string}")
  public void the_user_opens_new_browser_with_alias_string(String browserAlias) {

    basicPage.openNewBrowserWithAlias(browserAlias);
  }

  @And("the user zooms browser to {string}")
  public void the_user_zooms_browser_to_string(String percent) {

    basicPage.zoomInOut(percent);
  }

  @Given("the user removes the name field from tags node in the generated cucumber json file")
  public void the_user_removes_the_name_field_from_tags_node_in_the_generated_cucumber_json_file() {
    new FilesUtils().updateCucumberJsonFile();
  }

  @Given("the user opens {string} browser with session alias {string}")
  public void the_user_opens_string_browser_with_session_alias_string(String browserName,
    String alias) {

    DriverUtil.initializeBrowserWithSessionAlias(browserName, alias);
  }

  @Given("set the accept cookies popup value to {string}")
  public void set_the_accept_cookies_popup_value_to_string(String isCookieAccepted) {
    Hook.threadLocalCookieAccepted.set(Boolean.valueOf(isCookieAccepted));
  }
}
