# Functionally Java

This repo is a sandbox for playing with functional concepts in Java.  There is (or will be) a good amount of inspiration/duplication from Scala and other functional languages rewritten in Java both to explore their implementation and ways of exposing them as APIs in the Java ecosystem.

## Exception handling with `Try<T>`

The `Try<T>` interface represents the possible results of a computation.  There are two concrete classes that implement this interface representing the two possible states:

* `Success<T>` - the computation has completed successfully with a result
* `Failure<T>` - the computation has failed due to an exception

The `Try<T>` interface exposes a number of methods which allow you to chain off of the result of the computation applying the results of lambda functions to create new `Try<T>` instances representing their computation.  Every single one of these composition methods internally handle exceptions that are thrown and will result in a `Failure<T>` instead of propagating the exception back up to the caller.  Through the use of `Try<T>` it is possible to avoid the use of normal exception handling in Java via `try`/`catch` and instead compose failable logic functionally.

#### Examples:

1. Creating a new `Try<T>` for a known result:
    ```java
    Try<String> succeeded = Success.of("value");
    Try<String> failed = Failure.of(new Exception("failure"));
    ```
2. Create a new `Try<T>` from executing a function or expression:
    ```java
    public Try<Integer> divide(int dividend, int divisor) {
        // note, no exception will be thrown
        return Try.from(() -> dividend / divisor);
    }
    ```
3. Getting the result of a `Try<T>`
    ```java
    Try<Integer> quotient = divide(x, y);

    // will throw if quotient is Failure
    int result1 = quotient.get();
 
    // will return passed default value if quotient is Failure
    int result2 = quotient.getOrElse(0);

    // will return Optional.empty if quotient is Success
    Optional<Exception> exception = quotient.getException();
 
    // will return Optional of result if quotient is Success; otherwise Optional.empty
    Optional<Integer> optional = quotient.toOptional();
    ```
3. Testing if a `Try<T>` is successful or failed:
    ```java
    Try<Integer> quotient = divide(x, y);
 
    if (quotient.isSuccess()) {
        System.out.println("The division was successful.");
    } else if (quotient.isFailure()) {
        System.err.println("The division had failed.");
    }
 
    // executes the function only if Success
    quotient.ifSuccess(result -> {
        System.out.printf("The result of the division was %d.%n", result);
    });
 
    // executes the function only if Failure
    quotient.ifFailure(exception -> {
        System.err.printf("The division failed because of: %s.%n", exception.getMessage());
    });
    ```
4. Composing `Try<T>` by applying a function to the successful result
    ```java
    Try<Integer> quotient = divide(x, y);

    // applies the function to the result if quotient is Success
    // if quotient is Failure then doubled will also be Failure
    Try<Integer> doubled = quotient.map(result -> result * 2);
 
    // takes two Try<T>s and applies the function to their results if both are Success
    Try<Integer> added = doubled.combineMap(quotient, (x, y) -> x + y);
 
    // Integer.parseInt will throw so parsed will be assigned to Failure
    Try<Integer> parsed = Success.of("foo")
            .map(result -> Integer.parseInt(result));
    ``` 
5. Recovering from `Failure<T>`
   ```java
   Try<Integer> quotient = divide(x, y);

   // will return an alternate Try<T> if quotient is Failure
   Try<Integer> recovered1 = quotient.orElse(Success.of(0));

   // applies the function to the exception if quotient is Failure
   Try<Integer> recovered2 = quotient.recover(exception -> {
       System.err.printf("Oops, the calculation failed: %s.%n", exception.getMessage());
       return someOtherCalculation();
   });
   ```
6. Composing `Try<T>` when either Success or Failure (folding)
   ```java
   Try<Integer> quotient = divide(x, y);

   Try<String> message = quotient.fold(
        exception -> String.format("Oops, the calculation failed: %s.", exception),
        result -> String.format("The result of the calculation is %d.", result)
   );
   ```