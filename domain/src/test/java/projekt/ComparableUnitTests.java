package projekt;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;


public class ComparableUnitTests<T extends Comparable<? super T>> {

    private final Function<Integer, T> testObjectFactory;

    private T[] testObjects;

    public ComparableUnitTests(Function<Integer, T> testObjectFactory) {
        this.testObjectFactory = testObjectFactory;
    }

    @SuppressWarnings("unchecked")
    public void initialize(int testObjectCount) {
        this.testObjects = (T[]) new Comparable<?>[testObjectCount];
        for (int i = 0; i < testObjectCount; i++) {
            this.testObjects[i] = testObjectFactory.apply(i);
        }
    }

    public void testBiggerThen() {
        for (int i = 0; i < this.testObjects.length; i++) {
            for (int j = 0; j < i; j++) {
                Assertions.assertEquals(1, this.testObjects[i].compareTo(this.testObjects[j]));
            }
        }
    }

    @SuppressWarnings("EqualsWithItself")
    public void testAsBigAs() {
        for (T testObject : this.testObjects) {
            Assertions.assertEquals(0, testObject.compareTo(testObject));
        }
    }

    public void testLessThen() {
        for (int i = 0; i < this.testObjects.length; i++) {
            for (int j = i + 1; j < this.testObjects.length; j++) {
                Assertions.assertEquals(-1, this.testObjects[i].compareTo(this.testObjects[j]));
            }
        }
    }
}
