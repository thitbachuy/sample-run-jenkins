package steps;

import config.DriverUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import pages.SearchPage;

public class SearchSteps {

  private SearchPage page;

  public SearchSteps() {
    page = new SearchPage(DriverUtil.getDriver());
  }

  @When("the user enters {string} into {string} input")
  public void enterCorrect(String info, String input) {
    page.enterData(info, input);
  }

  @Given("the user can open the link {string}")
  public void the_user_can_open_string(String url) {
    page.openUrl(url);
  }

  @Given("the title page is {string}")
  public void the_title_page_is_string(String title) {
    page.verifyTitlePage(title);
  }
}
