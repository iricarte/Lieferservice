package projekt;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;

public class ObjectUnitTests<T> {

    private final Function<Integer, T> testObjectFactory;
    private final Function<T, String> toString;

    private T[] testObjects;
    private T[] testObjectsContentEquality;
    private T[] testObjectsReferenceEquality;

    public ObjectUnitTests(Function<Integer, T> testObjectFactory, Function<T, String> toString) {
        this.testObjectFactory = testObjectFactory;
        this.toString = toString;
    }

    public static <T> ObjectUnitTests<T> initialize100(Function<Integer, T> testObjectFactory,
                                                       Function<T, String> toString) {
        ObjectUnitTests<T> retVal = new ObjectUnitTests<>(testObjectFactory, toString);
        retVal.initialize100(100);
        return retVal;
    }

    @SuppressWarnings("unchecked")
    public void initialize100(int testObjectCount) {
        this.testObjects = (T[]) new Object[testObjectCount];
        this.testObjectsContentEquality = (T[]) new Object[testObjectCount];
        this.testObjectsReferenceEquality = (T[]) new Object[testObjectCount];

        for (int i = 0; i < testObjectCount; i++) {
            this.testObjects[i] = testObjectFactory.apply(i);
            this.testObjectsContentEquality[i] = testObjectFactory.apply(i);
            this.testObjectsReferenceEquality[i] = this.testObjects[i];
        }
    }

    public void testEquals() {
        for (int i = 0; i < this.testObjects.length; i++) {
            for (int j = 0; j < this.testObjects.length; j++) {
                if (i == j) {
                    Assertions.assertEquals(this.testObjects[i], this.testObjects[i]);
                    Assertions.assertEquals(this.testObjects[i], this.testObjectsContentEquality[i]);
                    Assertions.assertEquals(this.testObjects[i], this.testObjectsReferenceEquality[i]);
                } else {
                    Assertions.assertNotEquals(this.testObjects[i], this.testObjects[j]);
                }
            }
        }
    }

    public void testHashCode() {
        for (int i = 0; i < this.testObjects.length; i++) {
            for (int j = 0; j < this.testObjects.length; j++) {
                if (i == j) {
                    Assertions.assertEquals(this.testObjects[i].hashCode(), this.testObjects[i].hashCode());
                    Assertions.assertEquals(this.testObjects[i].hashCode(),
                                            this.testObjectsContentEquality[i].hashCode());
                    Assertions.assertEquals(this.testObjects[i].hashCode(),
                                            this.testObjectsReferenceEquality[i].hashCode());
                } else {
                    Assertions.assertNotEquals(this.testObjects[i].hashCode(), this.testObjects[j].hashCode());
                }
            }
        }
    }

    public void testToString() {
        for (T testObject : this.testObjects) {
            Assertions.assertEquals(toString.apply(testObject), testObject.toString());
        }
    }
}
