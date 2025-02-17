package org.codeforamerica.shiba.pages.config;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;

@Data
public class FormInput {

  private FormInputType type;
  private String name;
  private String customInputFragment;
  private PromptMessage promptMessage;
  private String helpMessageKey;
  private String placeholder;
  private OptionsWithDataSource options;
  private List<FormInput> followUps = Collections.emptyList();
  private List<String> followUpValues = Collections.emptyList();
  private List<Validator> validators = Collections.emptyList();
  private Boolean readOnly = false;
  private String defaultValue;
  private Integer max;
  private Integer min;
  private Condition condition;
  private List<PageDatasource> datasources;
}
