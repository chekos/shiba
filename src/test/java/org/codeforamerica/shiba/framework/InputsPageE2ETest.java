package org.codeforamerica.shiba.framework;

import org.codeforamerica.shiba.testutilities.AbstractExistingStartTimePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"pagesConfig=pages-config/test-input.yaml"})
@Tag("framework")
public class InputsPageE2ETest extends AbstractExistingStartTimePageTest {
    final String radioOption1 = "radio option 1";
    final String radioOption2 = "option-2";
    final String checkboxOption1 = "checkbox option 1";
    final String checkboxOption2 = "checkbox option 2";
    final String noneCheckboxOption = "none checkbox option";
    final String selectOption1 = "select option 1";
    final String selectOption2 = "select option 2";
    final String followUpTrue = "YEP";
    final String followUpFalse = "NOPE";
    final String followUpUncertain = "UNSURE";
    final String promptMessage = "prompt message";
    final String helpMessage = "help message";
    final String optionHelpMessage = "option help message";
    final String placeholder = "optional input";

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.ENGLISH, "firstPageTitle");
        staticMessageSource.addMessage("next-page-title", Locale.ENGLISH, "nextPageTitle");
        staticMessageSource.addMessage("radio-option-1", Locale.ENGLISH, radioOption1);
        staticMessageSource.addMessage("radio-option-2", Locale.ENGLISH, radioOption2);
        staticMessageSource.addMessage("checkbox-option-1", Locale.ENGLISH, checkboxOption1);
        staticMessageSource.addMessage("checkbox-option-2", Locale.ENGLISH, checkboxOption2);
        staticMessageSource.addMessage("none-checkbox-option", Locale.ENGLISH, noneCheckboxOption);
        staticMessageSource.addMessage("select-option-1", Locale.ENGLISH, selectOption1);
        staticMessageSource.addMessage("select-option-2", Locale.ENGLISH, selectOption2);
        staticMessageSource.addMessage("follow-up-true", Locale.ENGLISH, followUpTrue);
        staticMessageSource.addMessage("follow-up-false", Locale.ENGLISH, followUpFalse);
        staticMessageSource.addMessage("follow-up-uncertain", Locale.ENGLISH, followUpUncertain);
        staticMessageSource.addMessage("prompt-message-key", Locale.ENGLISH, promptMessage);
        staticMessageSource.addMessage("help-message-key", Locale.ENGLISH, helpMessage);
        staticMessageSource.addMessage("option-help-key", Locale.ENGLISH, optionHelpMessage);
        staticMessageSource.addMessage("general.optional", Locale.ENGLISH, placeholder);
        staticMessageSource.addMessage("general.month", Locale.ENGLISH, "month");
        staticMessageSource.addMessage("general.day", Locale.ENGLISH, "day");
        staticMessageSource.addMessage("general.year", Locale.ENGLISH, "year");
    }

    @Test
    void shouldNotBeAbleToChangeValueInUneditableInputs() {
        // TODO can we move this assertion into another test?
        driver.navigate().to(baseUrl + "/pages/firstPage");
        WebElement uneditableInput = driver.findElement(By.cssSelector("input[name='uneditableInput[]']"));

        uneditableInput.sendKeys("new value");

        assertThat(uneditableInput.getAttribute("value")).isEqualTo("default value");
    }

    @Test
    void shouldKeepUneditableInputsAfterNavigation() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.tagName("button")).click();

        assertThat(driver.getTitle()).isEqualTo("nextPageTitle");

        driver.findElement(By.partialLinkText("Go Back")).click();

        assertThat(driver.getTitle()).isEqualTo("firstPageTitle");
        assertThat(driver.findElement(By.cssSelector(String.format("input[name='%s[]']", "uneditableInput"))).getAttribute("value")).contains("default value");
    }

    @Test
    void shouldUncheckAnyOtherCheckedBoxesWhenNoneCheckboxIsSelected() {
        driver.navigate().to(baseUrl + "/pages/firstPage");

        testPage.enter("checkboxInput", List.of(checkboxOption1, checkboxOption2));
        testPage.enter("checkboxInput", noneCheckboxOption);

        assertThat(testPage.getCheckboxValues("checkboxInput")).containsOnly(noneCheckboxOption);
    }

    @Test
    void shouldUncheckNoneCheckboxWhenAnyOtherCheckboxIsSelected() {
        driver.navigate().to(baseUrl + "/pages/firstPage");

        testPage.enter("checkboxInput", noneCheckboxOption);
        testPage.enter("checkboxInput", checkboxOption1);

        assertThat(testPage.getCheckboxValues("checkboxInput")).containsOnly(checkboxOption1);
    }

    @Nested
    class FollowUps {
        @ParameterizedTest
        @ValueSource(strings = {
                "radioInputWithFollowUps",
                "checkboxInputWithFollowUps",
        })
        void shouldNotDisplayFollowUpQuestionsWhenFollowUpValueIsNotSelected(String inputName) {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            testPage.enter(inputName, followUpTrue);

            assertThat(driver.findElement(By.cssSelector(String.format("input[name='%s-followUpTextInput[]']", inputName))).isDisplayed()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "radioInputWithFollowUps",
                "checkboxInputWithFollowUps",
        })
        void shouldDisplayFollowUpQuestionsWhenFollowUpValueIsSelected(String inputName) {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            testPage.enter(inputName, followUpFalse);

            assertThat(driver.findElement(By.cssSelector(String.format("input[name='%s-followUpTextInput[]']", inputName))).isDisplayed()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "radioInputWithFollowUps",
                "checkboxInputWithFollowUps",
        })
        void shouldPreserveAnswerToFollowUpQuestions(String inputName) {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            testPage.enter(inputName, followUpFalse);
            String followUpTextInputValue = "some follow up";
            String followUpInputName = String.format("%s-followUpTextInput", inputName);
            testPage.enter(followUpInputName, followUpTextInputValue);

            testPage.clickContinue();
            testPage.goBack();

            assertThat(driver.findElement(By.cssSelector(String.format("input[name='%s-followUpTextInput[]']", inputName))).isDisplayed()).isTrue();
            assertThat(testPage.getInputValue(followUpInputName)).isEqualTo(followUpTextInputValue);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "radioInputWithFollowUps",
                "checkboxInputWithFollowUps",
        })
        void shouldDisplayFollowUpQuestionsWhenAnyFollowUpValueIsSelected(String inputName) {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            testPage.enter(inputName, followUpUncertain);

            assertThat(driver.findElement(By.cssSelector(String.format("input[name='%s-followUpTextInput[]']", inputName))).isDisplayed()).isTrue();
        }

        @Test
        void shouldContinueDisplayingFollowUpQuestionsWhenAFollowUpValueIsStillSelected() {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            testPage.enter("checkboxInputWithFollowUps", List.of(followUpFalse, followUpUncertain));
            testPage.enter("checkboxInputWithFollowUps", followUpUncertain);

            assertThat(driver.findElement(By.cssSelector("input[name='checkboxInputWithFollowUps-followUpTextInput[]']")).isDisplayed()).isTrue();
        }
    }
}