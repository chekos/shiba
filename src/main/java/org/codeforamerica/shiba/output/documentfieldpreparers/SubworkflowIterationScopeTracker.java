package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Value;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.data.Iteration;

public class SubworkflowIterationScopeTracker {

  private final Map<String, List<UUID>> scopesToIterations;

  public SubworkflowIterationScopeTracker() {
    scopesToIterations = new HashMap<>();
  }

  public IterationScopeInfo getIterationScopeInfo(PageGroupConfiguration pageGroupConfiguration,
      Iteration iteration) {
    String scope = scopeForIteration(pageGroupConfiguration, iteration);
    if (scope != null) {
      if (!scopesToIterations.containsKey(scope)) {
        scopesToIterations.put(scope, new LinkedList<>());
      }
      List<UUID> iterationsInScope = scopesToIterations.get(scope);
      if (!iterationsInScope.contains(iteration.getId())) {
        iterationsInScope.add(iteration.getId());
      }
      return new IterationScopeInfo(scope, iterationsInScope.indexOf(iteration.getId()));
    } else {
      return null;
    }
  }

  private String scopeForIteration(PageGroupConfiguration pageGroupConfiguration,
      Iteration iteration) {
    Map<String, Condition> scopes = pageGroupConfiguration.getAddedScope();
    return ofNullable(scopes).flatMap(allScopes -> allScopes.entrySet().stream()
        .filter(entry -> ofNullable(iteration.getPagesData().get(entry.getValue().getPageName()))
            .map(pageData -> entry.getValue().matches(pageData, iteration.getPagesData()))
            .orElse(false))
        .findAny()
        .map(Map.Entry::getKey)).orElse(null);
  }

  @Value
  public static class IterationScopeInfo {

    String scope;
    int index;

    public IterationScopeInfo(String scope, int index) {
      this.scope = scope;
      this.index = index;
    }
  }

}
