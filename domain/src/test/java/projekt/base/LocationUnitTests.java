package projekt.base;

import projekt.ComparableUnitTests;
import projekt.ObjectUnitTests;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LocationUnitTests {

    private static ComparableUnitTests<Location> comparableUnitTests;
    private static ObjectUnitTests<Location> objectUnitTests;

    @BeforeAll
    public static void initialize() {
        Function<Integer, Location> xToLocation = x -> new Location(x, x ^ 2 - x);
        comparableUnitTests = new ComparableUnitTests<>(xToLocation);
        objectUnitTests = new ObjectUnitTests<>(xToLocation, Object::toString);
        comparableUnitTests.initialize(100);
        objectUnitTests.initialize(100);
    }

    @Test
    public void testEquals() {
        objectUnitTests.testEquals();
    }

    @Test
    public void testHashCode() {
        objectUnitTests.testHashCode();
    }

    @Test
    public void testToString() {
        objectUnitTests.testToString();
    }

    @Test
    public void testBiggerThen() {
        comparableUnitTests.testBiggerThen();
    }

    @Test
    public void testAsBigAs() {
        comparableUnitTests.testAsBigAs();
    }

    @Test
    public void testLessThen() {
        comparableUnitTests.testLessThen();
    }

}
