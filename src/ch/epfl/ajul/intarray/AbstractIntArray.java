package ch.epfl.ajul.intarray;

import java.util.Arrays;

import static ch.epfl.ajul.intarray.ImmutableIntArray.copyOf;

/// Abstract base class for integer array implementations.
/// Provides concrete implementations of all methods declared in ReadOnlyIntArray,
/// as well as an override of toString.

public abstract class AbstractIntArray implements ReadOnlyIntArray {

    /// The primitive array storing the elements of this integer array.
    private int[] tab;

    /// Constructs an AbstractIntArray wrapping the given primitive array.
    /// Intended to be called by subclasses only.
    ///
    /// @param tab the primitive array to store
    protected AbstractIntArray(int[] tab) {
        this.tab = tab;
    }

    /// Returns the number of elements in this array.
    ///
    /// @return the size of this array
    @Override
    public int size() {
        return tab.length;
    }

    /// Returns the element at the given index in this array.
    ///
    /// @param i the index of the element to return
    /// @return the element at index i
    /// @throws IndexOutOfBoundsException if i is negative or greater than or equal to the size of the array
    @Override
    public int get(int i) {
        if (i < 0 || i >= tab.length) {
            throw new IndexOutOfBoundsException(
                    "Index " + i + " is out of bounds for length " + tab.length);
        }
        return tab[i];
    }

    /// Returns an immutable integer array with the same size and elements as this array.
    ///
    /// @return an immutable copy of this array
    @Override
    public ImmutableIntArray immutable() {
        return copyOf(tab);
    }

    /// Returns a new primitive int[] array with the same size and elements as this array.
    ///
    /// @return a copy of this array as a primitive int[] array
    @Override
    public int[] toArray() {
        return tab.clone();
    }

    /// Returns a string representation of this array,
    /// in the same format as Arrays.toString(int[]).
    ///
    /// @return a string representation of the elements in this array
    @Override
    public String toString() {
        return Arrays.toString(tab);
    }
}