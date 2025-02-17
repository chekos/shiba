package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class HasCostForChildcarePreparerTest {

  private final HasCostForChildcarePreparer preparer = new HasCostForChildcarePreparer();

  @Test
  public void shouldMapToTrueForApplicantLivingAlone() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("goingToSchool", "goingToSchool", List.of("true"))
        .withApplicantPrograms(List.of("CCAP"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(createApplicationInput());

    applicationData = new TestApplicationDataBuilder()
        .withPageData("jobSearch", "currentlyLookingForJob", List.of("true"))
        .withApplicantPrograms(List.of("CCAP"))
        .build();

    result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(createApplicationInput());
  }

  @Test
  public void shouldMapEmptyForApplicantLivingAlone() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("goingToSchool", "goingToSchool", List.of("false"))
        .withPageData("jobSearch", "currentlyLookingForJob", List.of("false"))
        .withApplicantPrograms(List.of("CCAP"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }

  @Test
  public void shouldMapTrueForHouseholdMember() {
    Subworkflow household = createHouseholdSubworkflow("CCAP");
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("goingToSchool", "goingToSchool", List.of("true"))
        .withPageData("whoIsGoingToSchool", "whoIsGoingToSchool",
            List.of("some name " + household.get(0).getId()))
        .withApplicantPrograms(List.of("SNAP"))
        .build();
    applicationData.setSubworkflows(new Subworkflows(Map.of("household", household)));

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(createApplicationInput());
  }

  @Test
  public void shouldMapTrueForApplicantInHousehold() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("goingToSchool", "goingToSchool", List.of("true"))
        .withPageData("whoIsGoingToSchool", "whoIsGoingToSchool", List.of("some name applicant"))
        .withApplicantPrograms(List.of("CCAP"))
        .withSubworkflow("household", createHouseholdSubworkflowBuilder("SNAP"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(createApplicationInput());
  }

  @Test
  public void shouldMapEmptyForHousehold() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("goingToSchool", "goingToSchool", List.of("true"))
        .withPageData("whoIsGoingToSchool", "whoIsGoingToSchool", List.of("some name applicant"))
        .withApplicantPrograms(List.of("SNAP"))
        .withSubworkflow("household", createHouseholdSubworkflowBuilder("CCAP"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }


  @Test
  public void shouldReturnEmptyForMissingData() {
    ApplicationData applicationData = new ApplicationData();
    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }

  @NotNull
  private Subworkflow createHouseholdSubworkflow(String program) {
    return new Subworkflow(List.of(createHouseholdSubworkflowBuilder(program).build()));
  }

  @NotNull
  private PagesDataBuilder createHouseholdSubworkflowBuilder(String program) {
    return new PagesDataBuilder()
        .withPageData("householdMemberInfo",
            Map.of("firstName", "Daria",
                "lastName", "Agàta",
                "dateOfBirth", List.of("5", "6", "1978"),
                "maritalStatus", "Never married",
                "sex", "Female",
                "livedInMnWholeLife", "Yes",
                "relationship", "housemate",
                "programs", program,
                "ssn", "123121234"));
  }


  @NotNull
  private DocumentField createApplicationInput() {
    return new DocumentField(
        "ccapHasCostsForChildCare",
        "ccapHasCostsForChildCare",
        List.of("true"),
        DocumentFieldType.ENUMERATED_SINGLE_VALUE
    );
  }
}
