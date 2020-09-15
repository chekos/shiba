package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class CoverPageInputsMapperTest {
    private final Map<County, Map<Recipient, String>> countyInstructionsMapping = new HashMap<>();
    private final CoverPageInputsMapper coverPageInputsMapper = new CoverPageInputsMapper(countyInstructionsMapping);

    PagesData pagesData = new PagesData();
    ApplicationData applicationData = new ApplicationData();

    @BeforeEach
    void setUp() {
        applicationData.setPagesData(pagesData);
        countyInstructionsMapping.put(County.OTHER, Map.of(
                Recipient.CLIENT, "other client instructions",
                Recipient.CASEWORKER, "other caseworker instructions"));

        pagesData.put("choosePrograms", new PageData(Map.of(
                "programs", InputData.builder().value(List.of()).build())));
        pagesData.put("personalInfo", new PageData(Map.of(
                "firstName", InputData.builder().value(emptyList()).build(),
                "lastName", InputData.builder().value(emptyList()).build()))
        );
    }

    @Test
    void shouldIncludeProgramsInputWithCombinedProgramSelection() {
        pagesData.put("choosePrograms",
                new PageData(Map.of("programs", InputData.builder()
                        .value(List.of("SNAP", "CASH"))
                        .build())));
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(County.OTHER)
                .fileName("")
                .timeToComplete(null)
                .build();

        List<ApplicationInput> applicationInputs = coverPageInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "programs",
                        List.of("SNAP, CASH"),
                        ApplicationInputType.SINGLE_VALUE
                ));
    }

    @Test
    void shouldIncludeCountyInstructionsInputWithMatchingCountyInstructions() {
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(County.OLMSTED)
                .fileName("")
                .timeToComplete(null)
                .build();
        String clientCountyInstructions = "olmsted client instructions";
        String caseworkerCountyInstructions = "olmsted caseworker instructions";
        countyInstructionsMapping.put(County.OLMSTED, Map.of(
                Recipient.CLIENT, clientCountyInstructions,
                Recipient.CASEWORKER, caseworkerCountyInstructions
        ));

        List<ApplicationInput> applicationInputs = coverPageInputsMapper.map(application, Recipient.CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "countyInstructions",
                        List.of(caseworkerCountyInstructions),
                        ApplicationInputType.SINGLE_VALUE
                ));

        applicationInputs = coverPageInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "countyInstructions",
                        List.of(clientCountyInstructions),
                        ApplicationInputType.SINGLE_VALUE
                ));
    }

    @Test
    void shouldIncludeCombinedFirstNameAndLastNameInput() {
        pagesData.put(
                "personalInfo", new PageData(Map.of(
                        "firstName", InputData.builder().value(List.of("someFirstName")).build(),
                        "lastName", InputData.builder().value(List.of("someLastName")).build()))
        );
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(County.OTHER)
                .fileName("")
                .timeToComplete(null)
                .build();

        List<ApplicationInput> applicationInputs = coverPageInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput("coverPage", "fullName", List.of("someFirstName someLastName"), ApplicationInputType.SINGLE_VALUE)
        );
    }

}