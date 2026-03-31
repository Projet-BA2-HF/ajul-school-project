package ch.epfl.ajul.intarray;

/// A mutable integer array. An instance of this class wraps a primitive int[] array
/// without copying it. Any subsequent modification to the original array will be
/// visible through this instance.
/// Instances can only be created through the factory method wrapping.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail

public final class MutableIntArray extends AbstractIntArray {

    /// Private constructor. Passes the given array to the superclass constructor.
    /// Use the factory method wrapping to create instances.
    ///
    /// @param tab the primitive array to wrap
    private MutableIntArray(int[] tab) {
        super(tab);
    }

    /// Returns a new mutable integer array that wraps the given primitive array
    /// without copying it. Any subsequent modification to array will be reflected
    /// in the returned MutableIntArray.
    ///
    /// @param array the primitive array to wrap
    /// @return a new MutableIntArray wrapping array
    public static MutableIntArray wrapping(int[] array) {
        return new MutableIntArray(array);
    }
}