package projekt.delivery.routing;

import projekt.ComparableUnitTests;
import projekt.ObjectUnitTests;
import projekt.base.Location;

import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EdgeImplUnitTests {

    private static ComparableUnitTests<EdgeImpl> comparableUnitTests;
    private static ObjectUnitTests<EdgeImpl> objectUnitTests;
    private static NodeImpl nodeA;
    private static NodeImpl nodeB;
    private static NodeImpl nodeC;

    private static EdgeImpl edgeAA;
    private static EdgeImpl edgeAB;
    private static EdgeImpl edgeBC;

    @BeforeAll
    public static void initialize() {
        RegionImpl region = new RegionImpl();
        Function<Integer, EdgeImpl> xToEdgeImpl = x -> new EdgeImpl(region,
                                                                    "Edge",
                                                                    new Location(x, x * 2),
                                                                    new Location(x, x * 2 + x),
                                                                    10);
        comparableUnitTests = ComparableUnitTests.initialize100(xToEdgeImpl);
        objectUnitTests = ObjectUnitTests.initialize100(xToEdgeImpl, Object::toString);
        for (int x = 0; x < 100; x++) {
            EdgeImpl edge = xToEdgeImpl.apply(x);
            region.putNode(new NodeImpl(region, "Node1_" + x, edge.getLocationA(), Set.of()));
            region.putNode(new NodeImpl(region, "Node2_" + x, edge.getLocationB(), Set.of()));
            region.putEdge(edge);
        }
        Location locationNodeA = new Location(0, 0);
        Location locationNodeB = new Location(1, 0);
        Location locationNodeC = new Location(2, 0);
        nodeA = new NodeImpl(region, "NodeA", locationNodeA, Set.of(locationNodeA, locationNodeB));
        nodeB = new NodeImpl(region, "NodeB", locationNodeB, Set.of(locationNodeA, locationNodeC));
        nodeC = new NodeImpl(region, "NodeC", locationNodeC, Set.of(locationNodeB));
        edgeAA = new EdgeImpl(region, "EdgeAA", locationNodeA, locationNodeA, 0);
        edgeAB = new EdgeImpl(region, "EdgeAB", locationNodeA, locationNodeB, 1);
        edgeBC = new EdgeImpl(region, "EdgeBC", locationNodeB, locationNodeC, 1);
        region.putNode(nodeA);
        region.putNode(nodeB);
        region.putNode(nodeC);
        region.putEdge(edgeAA);
        region.putEdge(edgeAB);
        region.putEdge(edgeBC);
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

    @Test
    public void testGetNode() {
        Assertions.assertEquals(nodeA, edgeAA.getNodeA());
        Assertions.assertEquals(nodeA, edgeAA.getNodeB());
        Assertions.assertEquals(nodeA, edgeAB.getNodeA());
        Assertions.assertEquals(nodeB, edgeAB.getNodeB());
        Assertions.assertEquals(nodeB, edgeBC.getNodeA());
        Assertions.assertEquals(nodeC, edgeBC.getNodeB());
    }
}
