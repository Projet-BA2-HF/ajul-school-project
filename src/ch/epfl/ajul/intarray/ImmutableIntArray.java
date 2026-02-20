package ch.epfl.ajul.intarray;

/// An immutable integer array whose content cannot change after creation.
/// Instances can only be created through the factory method copyOf.

public final class ImmutableIntArray extends AbstractIntArray {

    /// Private constructor. Passes the given array to the superclass constructor.
    /// Use the factory method copyOf to create instances.
    ///
    /// @param tab the primitive array to store
    private ImmutableIntArray(int[] tab) {
        super(tab);
    }

    /// Returns a new immutable integer array with the same size and elements
    /// as the given primitive array. The array is copied to guarantee immutability.
    ///
    /// @param array the primitive array to copy
    /// @return a new ImmutableIntArray containing a copy of array
    public static ImmutableIntArray copyOf(int[] array) {
        return new ImmutableIntArray(array.clone());
    }

    /// Returns this array itself, since it is already immutable and no copy is needed.
    ///
    /// @return this array
    @Override
    public ImmutableIntArray immutable() {
        return this;
    }
}