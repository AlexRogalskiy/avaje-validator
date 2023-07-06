# Avaje Validator (site docs coming soon)

Reflection-Free pojo validation via apt source code generation. A light (~85kb + generated code) source code generation style alternative to Hibernate Validation. (code generation vs reflection)

- Annotate java classes with `@Valid` (or use `@Valid.Import` for types we "don't own" or can't annotate)
- `avaje-validator-generator` annotation processor generates Java source code to write validation classes
- Supports Avaje/Jakarta/Javax Constraint Annotations
- Group Support
- Composable Contraint Annotations
- loading and interpolating error messages (with multiple Locales) through ResourceBundles
- Getter Validation 
- Method parameter validation (via Avaje Inject AOP only)

# Quick Start

## Step 1 - Add dependencies
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-validator</artifactId>
  <version>${avaje.validator.version}</version>
</dependency>
<!-- Alternatively can use Jakarta/Javax Constraints-->
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>validator-constraints</artifactId>
  <version>${avaje.validator.version}</version>
</dependency>
```

And add avaje-validator-generator as an annotation processor.
```xml

<!-- Annotation processors -->
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-validator-generator</artifactId>
  <version>${avaje.validator.version}</version>
  <scope>provided</scope>
</dependency>
```

## Step 2 - Add `@Valid`

Add `@Valid` to the types we want to add validation.

The `avaje-validator-generator` annotation processor will generate a ValidationAdapter as Java source code
for each type annotated with `@Valid`. These will be automatically registered with `Validator`
when it is started using a service loader mechanism.

```java
@Json
public class Leyndell {
  @NotBlank
  private String street;

  @NotEmpty(message="must not be empty")
  private List<@NotBlank(message="{message.bundle.key}") String> suburb;

  @Valid
  @NotNull(groups=SomeGroup.class)
  private OtherClass otherclass;

  //add getters/setters
}
```

It also works with records:
```java
@Json
public record Address(@NotBlank String street, @NotEmpty(message="must not be empty") String suburb, @NotNull(groups=SomeGroup.class) String city) { }
```

For types we cannot annotate with `@Valid` we can place `@Valid.Import(TypeToimport.class)` on any class/package-info to generate the adapters.

## Step 3 - Use

```java
// build using defaults
Validator validator = Validator.builder().build();

Customer customer = ...;

// will throw a `ConstraintViolationException` containing all the failed constraint violations
validator.validate(customer);

// validate with explicit locale
validator.validate(customer, Locale.ENGLISH);

// validate with groups
validator.validate(customer, Locale.ENGLISH, Group1.class);
```
