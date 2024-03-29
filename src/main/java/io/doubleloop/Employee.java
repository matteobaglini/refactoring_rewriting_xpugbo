package io.doubleloop;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Employee {
  private LocalDate date;
  private String lastName;
  private String firstName;
  private String email;

  public Employee(String firstName, String lastName, String birthDate, String email) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.date = LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    this.email = email;
  }

  public static Employee parse(String value) {
    final var parts = value.split(", ");
    return new Employee(parts[1], parts[0], parts[2], parts[3]);
  }

  public String toLine() {
    return String.format(
        "%s, %s, %s, %s",
        lastName, firstName, date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")), email);
  }

  public String getEmail() {
    return email;
  }

  public String getFirstName() {
    return firstName;
  }

  public boolean isBirthday(LocalDate now) {
    return date.getMonth() == now.getMonth() && date.getDayOfMonth() == now.getDayOfMonth();
  }

  @Override
  public String toString() {
    return "Employee{"
        + "date=" + date
        + ", lastName='" + lastName + '\''
        + ", firstName='" + firstName + '\''
        + ", email='" + email + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Employee employee = (Employee) o;
    return Objects.equals(date, employee.date)
        && Objects.equals(lastName, employee.lastName)
        && Objects.equals(firstName, employee.firstName)
        && Objects.equals(email, employee.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(date, lastName, firstName, email);
  }
}
