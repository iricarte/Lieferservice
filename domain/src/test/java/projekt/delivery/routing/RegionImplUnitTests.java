package projekt.delivery.routing;

import projekt.ObjectUnitTests;
import projekt.base.Location;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.tudalgo.algoutils.student.Student.crash;


public class RegionImplUnitTests {

    private static ObjectUnitTests<RegionImpl> objectUnitTests;

    /*
     * TestRegion has one node:
     *       (0,0)
     *      /    \
     * (x, 1)    (x, 2)
     */
    @BeforeAll
    public static void initialize() {
        objectUnitTests = new ObjectUnitTests<>(x -> {
            RegionImpl region = new RegionImpl();
            NodeImpl node = new NodeImpl(region, "node", new Location(0, 0), Set.of(new Location(x, 1), new Location(x, 2)));
            region.putNode(node);
            return region;
        }, Object::toString);
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
    public void testNodes() {
        RegionImpl region = new RegionImpl();
    }

    @Test
    public void testEdges() {
        crash(); // TODO: H12.3 - remove if implemented
    }

    @Test
    public void getNode() {
    }

    @Test
    public void getEdge() {
    }

    @Test
    public void getNodes() {
    }

    @Test
    public void getEdges() {
    }

    @Test
    public void getDistanceCalculator() {
    }

    @Test
    public void putNode() {
    }

    @Test
    public void putEdge() {
    }
}
