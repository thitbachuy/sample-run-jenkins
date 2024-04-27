package locators;

import java.util.HashMap;
import java.util.Map;

public class BasicPageLocators {
    public static Map<String, String> createLibraryPage() {
        Map<String, String> xpathToPage = new HashMap<>();
        xpathToPage.put("my sky contract", "//h1[@class='headlineTitle']/descendant::span[contains(text(),'Vertrag')]");
        xpathToPage.put("my sky program", "//h1[@class='headlineTitle']/descendant::span[contains(text(),'Programm')]");
        xpathToPage.put("my sky configuration", "//h1[@class='headlineTitle']/descendant::span[text()='Meine Daten & Einstellungen']");
        xpathToPage.put("my sky billing", "//h1[@class='headlineTitle']/descendant::span[text()='Abrechnung']");
        xpathToPage.put("installation", "//h1[@class='headlineTitle']/descendant::span[text()='Installation']");
        xpathToPage.put("my sky shipping", "//h1[@class='headlineTitle']/descendant::span[text()='Versand & Retoure']");
        xpathToPage.put("cdp account page", "//*[@data-testid='my-account']");
        xpathToPage.put("cdp pin page", "(//div[@class='o-container'])[1]");
        xpathToPage.put("cdp login page", "//h3[contains(@class,'heading') and text()='Login']");
        xpathToPage.put("new crm facebook login", "//div[@data-test-id='block-message']//*[contains(.,'eingeloggt')]");
        xpathToPage.put("my sky services", "//h1[@class='headlineTitle']/descendant::span[contains(.,'Weitere Services')]");
        xpathToPage.put("my sky refer friend", "//h1[@class='headlineTitle']/descendant::span[contains(.,'Freundschaftswerbung')]");
        xpathToPage.put("my sky iptv cooperation", "//h1[@class='headlineTitle']/descendant::span[contains(.,'IPTV & Kooperationen')]");
        xpathToPage.put("my sky q mini", "//h1[@class='headlineTitle']/descendant::span[text()='Sky Q Mini']");
        xpathToPage.put("my sky q multiscreen", "//h2[text()='Multiscreen:']");
        xpathToPage.put("cdp hardware selection", "//div[@type='hardware']/..");
        xpathToPage.put("sky product shopping cart", "//*[text()='Warenkorb' and @class='c-stepper__name']");
        xpathToPage.put("sky registration page", "//*[@class='checkout-account-page']");
        xpathToPage.put("cdp communication settings page", "//h2[text()='Kommunikationseinstellungen']");
        xpathToPage.put("sky pin change", "//h1[text()='Sky PIN ändern']");
        xpathToPage.put("new sky pin", "//h1[text()='Neue Sky PIN']");
        xpathToPage.put("minor protection pin change", "//h1[text()='Jugendschutz-PIN ändern']");
        xpathToPage.put("new mp pin", "(//*[text()='Neue Jugendschutz-PIN'])[1]");
        xpathToPage.put("cdp receiver configuration", "//*[@role='navigation']//a[@rel='follow' and contains(@href,'produkte-receiver')]");
        xpathToPage.put("cdp add-ons", "//*[@role='navigation']//li[.//a[@rel='follow' and contains(@href,'produkte-zubuchoptionen')] and contains(@class,'is-active')]");
        xpathToPage.put("cdp registration", "//*[@role='navigation']//a[@rel='follow' and contains(@href,'konto-erstellen')]");
        xpathToPage.put("cdp checkout order", "//*[@role='navigation']//a[@rel='follow' and contains(@href,'abschliessen')]");
        xpathToPage.put("cdp product selection", "//*[@role='navigation']//a[@rel='follow' and contains(@href,'pakete')]");
        xpathToPage.put("cdp glass configuration", "//*[@id='radio-button-TV55']");
        xpathToPage.put("cdp hardware page", "//*[normalize-space()='Welche Empfangsart nutzt du?']");
        xpathToPage.put("dhl", "//h5[contains(text(),'Ihr Rückversand an Sky Deutschland Fernsehen')]");
        xpathToPage.put("cdp product setting page", "//h2[text()='Produkteinstellungen']");
        xpathToPage.put("Filme & Serien", "//li[contains(@class,'isActive')]//a[contains(text(),'Filme & Serien')]");
        xpathToPage.put("Sport", "//li[contains(@class,'isActive')]//a[contains(text(),'Sport')]");
        xpathToPage.put("privacy policy", "//*[contains(text(),'Datenschutzerklärung')]");
        xpathToPage.put("help center topic catalog", "//*[@class='headlineTitleText']//*[text()='Themen']");
        xpathToPage.put("cdp smart card installation", "//div[contains(@class,'articleAdditionalInfo')]//*[text()='Smartcard in den Sky Q Receiver einstecken']");
        xpathToPage.put("cdp product overview", "//*[text()='Eine Plattform für alles, was du liebst.']");

        return xpathToPage;
    }

    public static Map<String, String> createLibraryElement() {
        Map<String, String> xpathToElement = new HashMap<>();
        xpathToElement.put("New", "//li[@data-name='New']");
        xpathToElement.put("In Progress", "//li[@data-name='In Progress']");
        xpathToElement.put("Closed", "//li[@data-name='Closed']");

        return xpathToElement;
    }
}
