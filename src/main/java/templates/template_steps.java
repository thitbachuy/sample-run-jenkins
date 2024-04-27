package templates;

/*
    This class i used to connect the Gherkin Code to Java code.
    As Example we have the three basic Gherkin Operations Given, When, Then.
    Please do not write your java execution Code in here, as we just store the Steps in here!
 */

import config.DriverUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class template_steps {

  private template_pages pageTemplate;

  private void template_steps() {
    pageTemplate = new template_pages(DriverUtil.getDriver());
  }

  @Given("something_has_already_happened")
  public void something_has_already_happened() {
    pageTemplate.methodToDoSomething1();
  }

  @When("the user performs an action at {string}")
  public void the_user_performs_an_action_at_string(String UIElement) {
    pageTemplate.methodToDoSomething2(UIElement);
  }

  @Then("the user verifies an result at {string}")
  public void the_user_verifies_an_result_at_string(String UIElement) {
    pageTemplate.methodToDoSomething3(UIElement);
  }
}
