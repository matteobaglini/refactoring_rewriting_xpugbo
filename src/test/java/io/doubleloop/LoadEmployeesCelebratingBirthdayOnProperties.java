package io.doubleloop;

import static io.vavr.test.Checkable.RNG;

import io.vavr.Tuple;
import io.vavr.collection.Stream;
import io.vavr.test.Arbitrary;
import io.vavr.test.Gen;
import io.vavr.test.Property;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoadEmployeesCelebratingBirthdayOnProperties {

  private Path testDataPath;
  private final ArbitraryEmployee arbitraryEmployee = new ArbitraryEmployee();

  @BeforeEach
  void setUp() {
    testDataPath = Paths.get("test-data-props");
    final var testDataDir = testDataPath.toFile();
    testDataDir.deleteOnExit();
    testDataDir.mkdirs();
  }

  //  @Test
  void demo() throws Exception {
    final var employeeGen = arbitraryEmployee.apply(10);
    Stream.continually(() -> employeeGen.apply(RNG.get())).take(100).stdout();
  }

  @Test
  void canLoadAndParseRandomValues() throws Exception {
    Property.def("valid name")
        .forAll(arbitraryEmployee)
        .suchThat(
            employee -> {
              final var filePath = employeeFile(List.of(header(), employee.toLine()));
              new LoadEmployeesCelebratingBirthdayOn_Old(filePath, date("2024/10/08")).execute();
              return true;
            })
        .check()
        .assertIsSatisfied();
  }

  @Test
  void oracleTest() throws Exception {
    Property.def("new implementation respect old implementation")
        .forAll(arbitraryEmployee)
        .suchThat(
            employee -> {
              final var filePath = employeeFile(List.of(header(), employee.toLine()));
              final var expected =
                  new LoadEmployeesCelebratingBirthdayOn_Old(filePath, date("2024/10/08"))
                      .execute();
              final var actual =
                  new LoadEmployeesCelebratingBirthdayOn_New(filePath, date("2024/10/08"))
                      .execute();
              return actual.equals(expected);
            })
        .check()
        .assertIsSatisfied();
  }

  class ArbitraryEmployee implements Arbitrary<Employee> {
    @Override
    public Gen<Employee> apply(int value) {
      return random ->
          new Employee(
              aString().apply(value).apply(random),
              aString().apply(value).apply(random),
              aDate().apply(value).apply(random),
              anEmail().apply(value).apply(random));
    }

    private static Arbitrary<String> aString() {
      return Arbitrary.string(
          Gen.frequency(
              Tuple.of(1, Gen.choose('A', 'Z')),
              Tuple.of(1, Gen.choose('a', 'z')),
              Tuple.of(1, Gen.choose('0', '9'))));
    }

    private static Arbitrary<String> aDate() {
      return Arbitrary.localDateTime(LocalDateTime.of(1990, 10, 10, 0, 0), ChronoUnit.DAYS)
          .map(x -> x.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
    }

    private static Arbitrary<String> anEmail() {
      return aString().flatMap(x -> aString().map(y -> "%s@%s.com".formatted(x, y)));
    }
  }

  private String employeeFile(List<String> lines) throws IOException {
    final var filePath = Paths.get(testDataPath.toString(), "employee_data.txt");
    filePath.toFile().deleteOnExit();
    Files.write(filePath, lines);
    return filePath.toString();
  }

  private String header() {
    return "last_name, first_name, date_of_birth, email";
  }

  private LocalDate date(String value) {
    return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
  }
}
