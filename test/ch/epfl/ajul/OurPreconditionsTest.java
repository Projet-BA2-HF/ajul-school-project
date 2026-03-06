package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OurPreconditionsTest {

    @Test
    void checkArgumentDoesNotThrowWhenTrue() {
        assertDoesNotThrow(() -> Preconditions.checkArgument(true));
    }

    @Test
    void checkArgumentThrowsWhenFalse() {
        assertThrows(IllegalArgumentException.class,
                () -> Preconditions.checkArgument(false));
    }

    @Test
    void checkArgumentThrowsExactType() {
        Throwable thrown = assertThrows(Throwable.class,
                () -> Preconditions.checkArgument(false));
        assertInstanceOf(IllegalArgumentException.class, thrown);
    }

    @Test
    void checkArgumentWithComputedTrue() {
        int x = 5;
        assertDoesNotThrow(() -> Preconditions.checkArgument(x > 0));
    }

    @Test
    void checkArgumentWithComputedFalse() {
        int x = -1;
        assertThrows(IllegalArgumentException.class,
                () -> Preconditions.checkArgument(x > 0));
    }
}