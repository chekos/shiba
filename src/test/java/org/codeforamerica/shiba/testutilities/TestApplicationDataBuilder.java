package org.codeforamerica.shiba.testutilities;


import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;

/**
 * Helper class for building test application data
 */
public class TestApplicationDataBuilder {

  private final ApplicationData applicationData;

  public TestApplicationDataBuilder() {
    applicationData = new ApplicationData();
  }

  public TestApplicationDataBuilder(ApplicationData applicationData) {
    this.applicationData = applicationData;
  }

  public ApplicationData build() {
    return applicationData;
  }

  public TestApplicationDataBuilder base() {
    applicationData.setId("12345");
    applicationData.setStartTimeOnce(Instant.now());
    return this;
  }

  public TestApplicationDataBuilder withApplicantPrograms(List<String> programs) {
    PageData programPage = new PageData();
    programPage.put("programs", new InputData(programs));
    applicationData.getPagesData().put("choosePrograms", programPage);
    return this;
  }

  public TestApplicationDataBuilder withPersonalInfo() {
    PageData personalInfo = new PageData();
    personalInfo.put("firstName", new InputData(List.of("Jane")));
    personalInfo.put("lastName", new InputData(List.of("Doe")));
    personalInfo.put("otherName", new InputData(List.of("")));
    personalInfo.put("dateOfBirth", new InputData(List.of("10", "04", "2020")));
    personalInfo.put("ssn", new InputData(List.of("123-45-6789")));
    personalInfo.put("sex", new InputData(List.of("FEMALE")));
    personalInfo.put("maritalStatus", new InputData(List.of("NEVER_MARRIED")));
    personalInfo.put("livedInMnWholeLife", new InputData(List.of("true")));
    applicationData.getPagesData().put("personalInfo", personalInfo);
    return this;
  }

  public TestApplicationDataBuilder withContactInfo() {
    PageData pageData = new PageData();
    pageData.put("phoneNumber", new InputData(List.of("(603) 879-1111")));
    pageData.put("email", new InputData(List.of("jane@example.com")));
    pageData.put("phoneOrEmail", new InputData(List.of("PHONE")));
    applicationData.getPagesData().put("contactInfo", pageData);
    return this;
  }

  public TestApplicationDataBuilder noPermamentAddress() {
    PageData homeAddress = new PageData();
    homeAddress.put("isHomeless", new InputData(List.of("true")));
    applicationData.getPagesData().put("homeAddress", homeAddress);
    return this;
  }

  public TestApplicationDataBuilder withHomeAddress() {
    applicationData.getPagesData().putIfAbsent("homeAddress", new PageData());
    PageData pageData = applicationData.getPagesData().get("homeAddress");
    pageData.put("streetAddress", new InputData(List.of("street")));
    pageData.put("city", new InputData(List.of("city")));
    pageData.put("state", new InputData(List.of("CA")));
    pageData.put("zipCode", new InputData(List.of("02103")));
    pageData.put("apartmentNumber", new InputData(List.of("ste 123")));
    return this;
  }

  public TestApplicationDataBuilder withEnrichedHomeAddress() {
    applicationData.getPagesData().putIfAbsent("homeAddress", new PageData());
    PageData pageData = applicationData.getPagesData().get("homeAddress");
    pageData
        .put("enrichedStreetAddress", new InputData(List.of("smarty street")));
    pageData.put("enrichedCity", new InputData(List.of("smarty city")));
    pageData.put("enrichedState", new InputData(List.of("CA")));
    pageData.put("enrichedZipCode", new InputData(List.of("02103-9999")));
    pageData.put("enrichedApartmentNumber", new InputData(List.of("apt 123")));
    return this;
  }

  public TestApplicationDataBuilder withMailingAddress() {
    applicationData.getPagesData().putIfAbsent("mailingAddress", new PageData());
    PageData pageData = applicationData.getPagesData().get("mailingAddress");
    pageData.put("streetAddress", new InputData(List.of("street")));
    pageData.put("city", new InputData(List.of("city")));
    pageData.put("state", new InputData(List.of("CA")));
    pageData.put("zipCode", new InputData(List.of("02103")));
    pageData.put("apartmentNumber", new InputData(List.of("ste 123")));
    return this;
  }

  public TestApplicationDataBuilder withEnrichedMailingAddress() {
    applicationData.getPagesData().putIfAbsent("mailingAddress", new PageData());
    PageData pageData = applicationData.getPagesData().get("mailingAddress");
    pageData.put("enrichedStreetAddress", new InputData(List.of("smarty street")));
    pageData.put("enrichedCity", new InputData(List.of("smarty city")));
    pageData.put("enrichedState", new InputData(List.of("CA")));
    pageData.put("enrichedZipCode", new InputData(List.of("02103-9999")));
    pageData.put("enrichedApartmentNumber", new InputData(List.of("apt 123")));
    return this;
  }

  public TestApplicationDataBuilder withPageData(String pageName, String input, String value) {
    return withPageData(pageName, input, List.of(value));
  }

  public TestApplicationDataBuilder withPageData(String pageName, String input,
      List<String> values) {
    PagesData pagesData = applicationData.getPagesData();
    pagesData.putIfAbsent(pageName, new PageData());
    pagesData.get(pageName).put(input, new InputData(values));
    return this;
  }

  public TestApplicationDataBuilder withSubworkflow(String pageGroup, PagesData... pagesData) {
    applicationData.setSubworkflows(
        new Subworkflows(Map.of(pageGroup, new Subworkflow(Arrays.asList(pagesData)))));
    return this;
  }

  public TestApplicationDataBuilder withSubworkflow(String pageGroup,
      PagesDataBuilder pagesDataBuilder) {
    applicationData.setSubworkflows(
        new Subworkflows(Map.of(pageGroup, new Subworkflow(List.of(pagesDataBuilder.build())))));
    return this;
  }

  public TestApplicationDataBuilder withJobs() {
    return withSubworkflow("jobs",
        new PagesDataBuilder().withNonHourlyJob("false", "1.1", "EVERY_WEEK"));
  }

  public TestApplicationDataBuilder withHouseholdMemberPrograms(List<String> programs) {
    return withSubworkflow("household", (PagesDataBuilder.build(List.of(
        new PageDataBuilder("householdMemberInfo", Map.of("programs", programs)))
    )));
  }

  public TestApplicationDataBuilder withHouseholdMember() {
    return withSubworkflow("household", PagesDataBuilder.build(List.of(
        new PageDataBuilder("householdMemberInfo",
            Map.of("firstName", List.of("Daria"),
                "lastName", List.of("Agàta"),
                "dateOfBirth", List.of("5", "6", "1978"),
                "maritalStatus", List.of("Never married"),
                "sex", List.of("Female"),
                "livedInMnWholeLife", List.of("Yes"),
                "relationship", List.of("housemate"),
                "programs", List.of("SNAP"),
                "ssn", List.of("123121234"))))
    ));
  }
}
