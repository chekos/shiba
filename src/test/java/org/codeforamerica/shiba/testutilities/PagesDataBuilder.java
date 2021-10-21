package org.codeforamerica.shiba.testutilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;

public class PagesDataBuilder {

  private final List<PageDataBuilder> pageDataBuilders = new ArrayList<>();

  /**
   * Creates PagesData from list of (pageName, pageData) pairs. If there are multiple pairs with the
   * same "pageName", they will be merged into one page.
   * <p>
   * <p>
   * Ex. {@code build([ (person, {firstName: ABC}), (person, {lastName: XYZ})]}
   * <p>
   * will create pagesData with {@code {person: {firstName: ABC, lastName: XYZ}}}
   *
   * @param pageDataBuilders list of string,Map pairs corresponding to pageName and pageData
   * @return resulting merged pagesData
   */
  public static PagesData build(List<PageDataBuilder> pageDataBuilders) {
    PagesData result = new PagesData();
    pageDataBuilders.forEach(page -> {
      result.putIfAbsent(page.getPageName(), new PageData());
      PageData pageData = result.get(page.getPageName());
      page.getPageDataMap().forEach(
          (inputName, value) -> pageData.put(inputName, new InputData(value)));
    });
    return result;
  }

  public PagesData build() {
    return build(pageDataBuilders);
  }

  public PagesDataBuilder withPageData(String pageName, String inputName, String value) {
    return withPageData(pageName, Map.of(inputName, List.of(value)));
  }

  public PagesDataBuilder withPageData(String pageName, String inputName, List<String> value) {
    return withPageData(pageName, Map.of(inputName, value));
  }

  public PagesDataBuilder withPageData(String pageName, Map<String, List<String>> pageData) {
    pageDataBuilders.add(new PageDataBuilder(pageName, pageData));
    return this;
  }

  public PagesDataBuilder withHourlyJob(String isSelfEmployed, String wage, String hours) {
    return withPageData("selfEmployment", "selfEmployment", isSelfEmployed)
        .withPageData("paidByTheHour", "paidByTheHour", "true")
        .withPageData("hourlyWage", "hourlyWage", wage)
        .withPageData("hoursAWeek", "hoursAWeek", hours);
  }

  public PagesDataBuilder withNonHourlyJob(String isSelfEmployed, String wage, String payPeriod) {
    return withPageData("selfEmployment", "selfEmployment", isSelfEmployed)
        .withPageData("paidByTheHour", "paidByTheHour", "false")
        .withPageData("payPeriod", "payPeriod", payPeriod)
        .withPageData("incomePerPayPeriod", "incomePerPayPeriod", wage);
  }
}
