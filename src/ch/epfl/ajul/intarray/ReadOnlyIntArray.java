package ch.epfl.ajul.intarray;

/// A read-only array of integers.
/// Elements can be read but not modified.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail

public interface ReadOnlyIntArray {

    /// Returns the number of elements in this array.
    ///
    /// @return the size of this array
    int size();

    /// Returns the element at the given index in this array.
    ///
    /// @param i the index of the element to return
    /// @return the element at index i
    /// @throws IndexOutOfBoundsException if i is negative or greater than or equal to the size of the array
    int get(int i);

    /// Returns an immutable integer array with the same size and elements as this array.
    ///
    /// @return an immutable copy of this array
    ImmutableIntArray immutable();

    /// Returns a new primitive int[] array with the same size and elements as this array.
    ///
    /// @return a copy of this array as a primitive int[] array
    int[] toArray();
}
