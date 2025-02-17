package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.TestUtils.createApplicationInput;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class UnearnedIncomeCcapPreparerTest {

  private final UnearnedIncomeCcapPreparer preparer = new UnearnedIncomeCcapPreparer();

  @Test
  public void shouldMapNoneOfTheAboveToNoForAllOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("unearnedIncomeCcap", "unearnedIncomeCcap",
            List.of("NO_UNEARNED_INCOME_CCAP_SELECTED"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        createApplicationInput("unearnedIncomeCcap", "BENEFITS", "false"),
        createApplicationInput("unearnedIncomeCcap", "INSURANCE_PAYMENTS", "false"),
        createApplicationInput("unearnedIncomeCcap", "CONTRACT_FOR_DEED", "false"),
        createApplicationInput("unearnedIncomeCcap", "TRUST_MONEY", "false"),
        createApplicationInput("unearnedIncomeCcap", "HEALTH_CARE_REIMBURSEMENT", "false"),
        createApplicationInput("unearnedIncomeCcap", "INTEREST_DIVIDENDS", "false"),
        createApplicationInput("unearnedIncomeCcap", "OTHER_SOURCES", "false")
    );
  }

  @Test
  public void shouldMapYesToSelectedOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("unearnedIncomeCcap", "unearnedIncomeCcap",
            List.of("INSURANCE_PAYMENTS", "TRUST_MONEY"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        createApplicationInput("unearnedIncomeCcap", "BENEFITS", "false"),
        createApplicationInput("unearnedIncomeCcap", "INSURANCE_PAYMENTS", "true"),
        createApplicationInput("unearnedIncomeCcap", "CONTRACT_FOR_DEED", "false"),
        createApplicationInput("unearnedIncomeCcap", "TRUST_MONEY", "true"),
        createApplicationInput("unearnedIncomeCcap", "HEALTH_CARE_REIMBURSEMENT", "false"),
        createApplicationInput("unearnedIncomeCcap", "INTEREST_DIVIDENDS", "false"),
        createApplicationInput("unearnedIncomeCcap", "OTHER_SOURCES", "false")
    );
  }

  @Test
  public void shouldReturnEmptyForMissingData() {
    ApplicationData applicationData = new ApplicationData();
    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }

}
