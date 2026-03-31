package ch.epfl.ajul.gamestate.packed;

/// Contains static methods for manipulating sets of integers packed into {@code int} values.
/// A packed integer set represents a subset of {0, …, 31}, where bit {@code i} is 1
/// if and only if integer {@code i} belongs to the set.
///
/// @author Hedi Fourati
/// @author Fahd Ben Ismail
public final class PkIntSet32 {

    /// The packed representation of the empty set.
    public static final int EMPTY = 0;

    /// Returns {@code true} iff the packed integer set {@code pkIntSet32} contains {@code i}.
    ///
    /// @param pkIntSet32 the packed integer set
    /// @param i          the integer to test for membership, between 0 and 31 (inclusive)
    /// @return {@code true} iff {@code i} belongs to {@code pkIntSet32}
    public static boolean contains(int pkIntSet32, int i) {
        //assert 0 <= i && i < Integer.SIZE;
        return (pkIntSet32 & (1 << i)) != 0;
    }

    /// Returns {@code true} iff the packed integer set {@code pkIntSet32a} contains every
    /// element of the packed integer set {@code pkIntSet32b}.
    ///
    /// @param pkIntSet32a the packed integer set to test as the superset
    /// @param pkIntSet32b the packed integer set whose elements must all be present
    /// @return {@code true} iff {@code pkIntSet32a} is a superset of {@code pkIntSet32b}
    public static boolean containsAll(int pkIntSet32a, int pkIntSet32b) {
        return (pkIntSet32a & pkIntSet32b) == pkIntSet32b;
    }

    /// Returns a packed integer set identical to {@code pkIntSet32} but also containing {@code i}.
    ///
    /// @param pkIntSet32 the packed integer set
    /// @param i          the integer to add, between 0 and 31 (inclusive)
    /// @return {@code pkIntSet32} with {@code i} added
    public static int add(int pkIntSet32, int i) {
        //assert 0 <= i && i < Integer.SIZE;
        return pkIntSet32 | (1 << i);
    }

    /// Returns a packed integer set identical to {@code pkIntSet32} but not containing {@code i}.
    ///
    /// @param pkIntSet32 the packed integer set
    /// @param i          the integer to remove, between 0 and 31 (inclusive)
    /// @return {@code pkIntSet32} with {@code i} removed
    public static int remove(int pkIntSet32, int i) {
        //assert 0 <= i && i < Integer.SIZE;
        return pkIntSet32 & ~(1 << i);
    }
}
