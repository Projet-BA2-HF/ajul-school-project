package ch.epfl.ajul;

/// Provides a utility method for checking method argument preconditions.
/// If a precondition is violated, an {@link IllegalArgumentException} is thrown.
public final class Preconditions {

    /// Checks that the given condition holds.
    /// Does nothing if {@code shouldBeTrue} is true; throws
    /// {@link IllegalArgumentException} otherwise.
    ///
    /// @param shouldBeTrue the condition that must hold
    /// @throws IllegalArgumentException if {@code shouldBeTrue} is false
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}