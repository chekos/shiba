package org.codeforamerica.shiba;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.codeforamerica.shiba.FormData.getFormDataFrom;

@Data
public class PageConfiguration {
    public List<FormInput> inputs = List.of();
    private String pageTitle;
    private String headerKey;
    private String headerHelpMessageKey;
    private String nextPage;
    private String previousPage;
    private PageDatasource datasource;
    private Condition skipCondition;

    @SuppressWarnings("unused")
    public boolean hasHeader() {
        return this.headerKey != null;
    }

    public List<FormInput> getFlattenedInputs() {
        return this.inputs.stream()
                .flatMap(formInput -> Stream.concat(Stream.of(formInput), formInput.followUps.stream()))
                .collect(Collectors.toList());
    }

    boolean isStaticPage() {
        return this.inputs.isEmpty();
    }

    boolean shouldSkip(PagesData pagesData) {
        if (this.datasource == null || this.skipCondition == null) {
            return false;
        }
        return this.skipCondition.appliesTo(getFormDataFrom(datasource, pagesData));
    }

    String getAdjacentPageName(boolean isBackwards) {
        return isBackwards ? previousPage : nextPage;
    }
}
