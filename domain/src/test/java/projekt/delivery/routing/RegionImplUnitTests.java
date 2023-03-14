package projekt.delivery.routing;

import projekt.ObjectUnitTests;
import projekt.base.Location;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RegionImplUnitTests {

    private static final Location testLocation0_0 = new Location(0, 0);
    private static final Location testLocation1_1 = new Location(1, 1);
    private static ObjectUnitTests<RegionImpl> objectUnitTests;
    private static RegionImpl testRegion;

    @BeforeAll
    public static void initialize() {
        objectUnitTests = ObjectUnitTests.initialize100(RegionImplUnitTests::testRegion, Object::toString);
    }

    /*
     * TestRegion has two nodes:
     *       (0,0)
     *      /
     * (1, 1)   [2, 2] Location, no connecting Edges
     */
    @NotNull
    private static RegionImpl testRegion(Integer x) {
        RegionImpl region = new RegionImpl();
        region.putNode(new NodeImpl(region, "node", testLocation0_0, Set.of(testLocation1_1, new Location(x * 2, 2))));
        region.putNode(new NodeImpl(region, "node2", testLocation1_1, Set.of(testLocation0_0, new Location(x * 2, 2))));
        region.putEdge(new EdgeImpl(region, "edge", testLocation0_0, testLocation1_1, 10));
        return region;
    }

    @BeforeEach
    public void setUp() {
        testRegion = testRegion(10);
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
        Assertions.assertNotNull(testRegion.getNode(testLocation0_0));

        Location location = new Location(2, 2);
        testRegion.putNode(new NodeImpl(testRegion, "node3", testLocation1_1, Set.of(location)));
        Region.Node node = testRegion.getNode(testLocation1_1);
        Assertions.assertNotNull(node);
    }

    @Test
    public void testNodes_Exceptions() {
        NodeImpl invalidNode = new NodeImpl(testRegion, "node3", testLocation0_0, Set.of(testLocation1_1));
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> new RegionImpl().putNode(invalidNode),
                                "Node " + invalidNode + " has incorrect region");
    }

    @Test
    public void testEdges() {
        Region.Edge edge = testRegion.getEdge(testLocation0_0, testLocation1_1);
        Assertions.assertNotNull(edge);
    }

    @Test
    public void testEdges_Exceptions() {
        EdgeImpl invalidEdge = new EdgeImpl(testRegion, "edge", testLocation0_0, testLocation1_1, 10);
        EdgeImpl invalidEdgeNodeA = new EdgeImpl(testRegion, "edge2", new Location(-1, -1), testLocation0_0, 10);
        EdgeImpl invalidEdgeNodeB = new EdgeImpl(testRegion, "edge3", testLocation0_0, new Location(3, -3), 10);
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> new RegionImpl().putEdge(invalidEdge),
                                "Edge " + invalidEdge + " has incorrect region");
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> testRegion.putEdge(invalidEdgeNodeA),
                                "NodeA " + invalidEdgeNodeA + " has incorrect region");
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> testRegion.putEdge(invalidEdgeNodeB),
                                "NodeB " + invalidEdgeNodeB + " has incorrect region");
    }
}
