package projekt.delivery.routing;

import projekt.ComparableUnitTests;
import projekt.ObjectUnitTests;
import projekt.base.Location;

import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NodeImplUnitTests {

    private static ComparableUnitTests<NodeImpl> comparableUnitTests;
    private static ObjectUnitTests<NodeImpl> objectUnitTests;
    private static NodeImpl nodeA;
    private static NodeImpl nodeB;
    private static NodeImpl nodeC;
    private static NodeImpl nodeD;

    private static EdgeImpl edgeAA;
    private static EdgeImpl edgeAB;
    private static EdgeImpl edgeBC;

    @BeforeAll
    public static void initialize() {
        RegionImpl region = new RegionImpl();
        Function<Integer, NodeImpl> xToNodeImpl = x -> new NodeImpl(region,
                                                                    "Node" + x,
                                                                    new Location(x, x * 2),
                                                                    Set.of());
        comparableUnitTests = ComparableUnitTests.initialize100(xToNodeImpl);
        objectUnitTests = ObjectUnitTests.initialize100(xToNodeImpl, Object::toString);
        Location locationNodeA = new Location(0, 0);
        Location locationNodeB = new Location(1, 0);
        Location locationNodeC = new Location(2, 0);
        Location locationNodeD = new Location(3, 0);
        nodeA = new NodeImpl(region, "NodeA", locationNodeA, Set.of(locationNodeA, locationNodeB));
        nodeB = new NodeImpl(region, "NodeB", locationNodeB, Set.of(locationNodeA, locationNodeC));
        nodeC = new NodeImpl(region, "NodeC", locationNodeC, Set.of(locationNodeB));
        nodeD = new NodeImpl(region, "NodeD", locationNodeD, Set.of());
        edgeAA = new EdgeImpl(region, "EdgeAA", locationNodeA, locationNodeA, 0);
        edgeAB = new EdgeImpl(region, "EdgeAB", locationNodeA, locationNodeB, 1);
        edgeBC = new EdgeImpl(region, "EdgeBC", locationNodeB, locationNodeC, 1);
        region.putNode(nodeA);
        region.putNode(nodeB);
        region.putNode(nodeC);
        region.putNode(nodeD);
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
    public void testGetEdge() {
        Assertions.assertEquals(edgeAA, nodeA.getEdge(nodeA));
        Assertions.assertEquals(edgeAB, nodeA.getEdge(nodeB));
        Assertions.assertEquals(edgeBC, nodeB.getEdge(nodeC));
        Assertions.assertNull(nodeC.getEdge(nodeD));
    }

    @Test
    public void testAdjacentNodes() {
        Assertions.assertEquals(Set.of(nodeA, nodeB), nodeA.getAdjacentNodes());
        Assertions.assertEquals(Set.of(nodeA, nodeC), nodeB.getAdjacentNodes());
        Assertions.assertEquals(Set.of(nodeB), nodeC.getAdjacentNodes());
        Assertions.assertEquals(Set.of(), nodeD.getAdjacentNodes());
    }

    @Test
    public void testAdjacentEdges() {
        Assertions.assertEquals(Set.of(edgeAA, edgeAB), nodeA.getAdjacentEdges());
        Assertions.assertEquals(Set.of(edgeAB, edgeBC), nodeB.getAdjacentEdges());
        Assertions.assertEquals(Set.of(edgeBC), nodeC.getAdjacentEdges());
        Assertions.assertEquals(Set.of(), nodeD.getAdjacentEdges());
    }
}
