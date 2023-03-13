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
        Function<Integer, Location> xToLocation = x -> new Location(x, x * 2);
        comparableUnitTests = ComparableUnitTests.initialize100(xToLocation);
        objectUnitTests = ObjectUnitTests.initialize100(xToLocation, Object::toString);
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
