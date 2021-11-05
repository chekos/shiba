package org.codeforamerica.shiba;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class FunctionalProgrammingTest {

  private final Person andrew = new Person("Andrew", "Edstrom", "Oakland", List.of("Miriam", "Coffee", "Donuts"));
  private final Person alex = new Person("Alex", "Gonzalez", "Berkeley", Collections.emptyList());
  private final Person sree = new Person("Sree", "Prasad", "Oakland", List.of("Korra"));
  private final Person britney = new Person("Britney", "Epps", "Detroit", List.of("Titus"));
  private final Person chibuisi = new Person("Chibuisi", "Enyia", "Chicago", List.of("Totes", "Fiona", "Sport", "Ghost", "Gus"));
  private final Person ben = new Person("Ben", "Calegari", "Manchester", List.of("Chester"));
  private List<Person> cfaEngineers;

  @BeforeEach
  void setUp() {
    cfaEngineers = List.of(andrew, alex, sree, britney, chibuisi, ben);
  }

  @Test
  void testGetLastNames() {
    List<String> lastNames = new ArrayList<>();
    assertThat(lastNames).containsExactly("Edstrom", "Gonzalez", "Prasad", "Epps", "Enyia", "Calegari");
  }

  @Test
  void testGetCities() {
    Set<String> currentCities = new HashSet<>();
    assertThat(currentCities).containsExactlyInAnyOrder("Oakland", "Bekeley", "Detroit", "Chicago", "Manchester");
  }

  @Test
  void testGetFirstNames() {
    List<String> firstNames = new ArrayList<>();
    assertThat(firstNames).containsExactly("Andrew", "Alex", "Sree", "Britney", "Chibuisi", "Ben");
  }

  @Test
  void testGetPetNames() {
    List<String> petNames = new ArrayList<>();
    assertThat(petNames).containsExactly("Miriam", "Coffee", "Donuts", "Korra", "Titus", "Totes", "Fiona", "Sport", "Ghost", "Gus", "Chester");
  }

  @Test
  void testGetPetNamesStartingWithC() {
    List<String> petNames = new ArrayList<>();
    assertThat(petNames).containsExactly("Coffee", "Chester");
  }

  @Test
  void testGetANameStartingWithB() {
    String firstName = "";
    assertThat(List.of("Britney", "Ben")).contains(firstName);
  }

  @Test
  void testDoAnyPetNamesHaveMoreThan6Letters() {
    boolean result = false;
    assertThat(result).isTrue();
  }

  @Test
  void testDoAllEngineersHavePets() {
    boolean result = true;
    assertThat(result).isFalse();
  }

  @Test
  void testDoAllEngineersHaveLastNamesWithMoreThan3Letters() {
    boolean result = false;
    assertThat(result).isTrue();
  }

  @Test
  void testGetPeopleByCity() {
    Map<String, List<Person>> expected = Map.of(
        "Oakland",   List.of(andrew, sree),
        "Berkeley",  List.of(alex),
        "Chicago",   List.of(chibuisi),
        "Detroit",   List.of(britney),
        "Manchester", List.of(ben)
    );

    Map<String, List<Person>> peopleByCity = new HashMap<>();

    assertThat(peopleByCity).containsAllEntriesOf(expected);
  }

  @Test
  void testGetFirstNamesByCity() {
    Map<String, List<String>> expected = Map.of(
        "Oakland",   List.of("Edstrom", "Prasad"),
        "Berkeley",  List.of("Gonzalez"),
        "Chicago",   List.of("Enyia"),
        "Detroit",   List.of("Epps"),
        "Manchester", List.of("Calegari")
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

/*
  Map<String, List<String>> cityToFirstNames = cfaEngineers.stream().collect(
        groupingBy(
            Person::getCurrentCity, // Oakland
            mapping(Person::getFirstName, toList()) // [Andrew, Sree]
        ));
    assertThat(cityToFirstNames).containsAllEntriesOf(expected);
 */