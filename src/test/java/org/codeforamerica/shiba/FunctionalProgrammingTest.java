package org.codeforamerica.shiba;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class FunctionalProgrammingTest {

  private List<Person> cfaEngineers;

  @BeforeEach
  void setUp() {
    Person andrew = new Person("Andrew", "Edstrom", "Oakland", List.of("Miriam", "Coffee", "Donuts"));
    Person alex = new Person("Alex", "Gonzalez", "Berkeley", Collections.emptyList());
    Person sree = new Person("Sree", "Prasad", "Oakland", List.of("Korra"));
    Person britney = new Person("Britney", "Epps", "Detroit", List.of("Titus"));
    Person chibuisi = new Person("Chibuisi", "Enyia", "Chicago", List.of());
    Person ben = new Person("Ben", "Calegari", "Manchester", List.of("Chester"));
    cfaEngineers = List.of(andrew, alex, sree, britney, chibuisi, ben);
  }

  @Test
  void testGetLastNames() {
    List<String> lastNames = new ArrayList<>();
    assertThat(lastNames).containsExactly("Edstrom", "Gonzalez", "Prasad", "Epps", "Enyia",
        "Calegari");
  }

  @Test
  void testGetCities() {
    Set<String> currentCities = new HashSet<>();
    assertThat(currentCities).containsExactlyInAnyOrder("Oakland", "Bekeley", "Detroit", "Chicago", "Manchester");
  }

  @Test
  void testGetPetNames() {
    List<String> petNames = new ArrayList<>();
    assertThat(petNames).containsExactly("Miriam", "Coffee", "Donuts", "Korra", "Titus", "Chester");
  }

  @Test
  void testGetPetNamesStartingWithC() {
    List<String> petNames = new ArrayList<>();
    assertThat(petNames).containsExactly("Coffee", "Chester");
  }

  @Test
  void testGetFirstNamesByCity() {
    Map<String, List<String>> expected = Map.of(
        "Oakland", List.of("Andrew", "Sree"),
        "Berkeley", List.of("Alex"),
        "Chicago", List.of("Chibuisi"),
        "Detroit", List.of("Britney"),
        "Manchester", List.of("Ben")
    );

    Map<String, List<String>> cityToFirstNames = new HashMap<>();
    assertThat(cityToFirstNames).containsAllEntriesOf(expected);
  }






}

@Data
@AllArgsConstructor
class Person {

  private String firstName;
  private String lastName;
  private String currentCity;
  private List<String> pets;
}
