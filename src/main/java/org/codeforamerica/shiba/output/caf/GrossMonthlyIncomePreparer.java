package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WHOSE_JOB_IS_IT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparer;
import org.codeforamerica.shiba.output.documentfieldpreparers.SubworkflowIterationScopeTracker;
import org.codeforamerica.shiba.output.documentfieldpreparers.SubworkflowIterationScopeTracker.IterationScopeInfo;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class GrossMonthlyIncomePreparer implements DocumentFieldPreparer {

  private final ApplicationConfiguration applicationConfiguration;
  private final GrossMonthlyIncomeParser grossMonthlyIncomeParser;

  public GrossMonthlyIncomePreparer(GrossMonthlyIncomeParser grossMonthlyIncomeParser,
      ApplicationConfiguration applicationConfiguration) {
    this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
    this.applicationConfiguration = applicationConfiguration;
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    PageGroupConfiguration pageGroupConfiguration = applicationConfiguration.getPageGroups()
        .get("jobs");
    return grossMonthlyIncomeParser.parse(application.getApplicationData()).stream()
        .filter(jobIncomeInformation -> {
          // Only want applicant info for certain-pops
          PagesData pagesData = jobIncomeInformation.getIteration().getPagesData();
          return document != Document.CERTAIN_POPS ||
                 getFirstValue(pagesData, WHOSE_JOB_IS_IT).contains("applicant");
        })
        .flatMap(jobIncomeInformation -> {

          String pageName = "employee";
          String inputName = "grossMonthlyIncome";
          List<DocumentField> inputs = new ArrayList<>();
          inputs.add(new DocumentField(
              pageName,
              inputName,
              String.valueOf(jobIncomeInformation.grossMonthlyIncome()),
              SINGLE_VALUE,
              jobIncomeInformation.getIndexInJobsSubworkflow()));

          IterationScopeInfo scopeInfo = scopeTracker
              .getIterationScopeInfo(pageGroupConfiguration, jobIncomeInformation.getIteration());
          if (scopeInfo != null) {
            inputs.add(new DocumentField(
                scopeInfo.getScope() + "_" + pageName,
                inputName,
                String.valueOf(jobIncomeInformation.grossMonthlyIncome()),
                SINGLE_VALUE,
                scopeInfo.getIndex()
            ));
          }

          return inputs.stream();
        })
        .collect(Collectors.toList());
  }

}

