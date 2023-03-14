package projekt.delivery.routing;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

/**
 * A {@link PathCalculator} that is based on another {@link PathCalculator} and caches its result
 * for later use.
 */
public class CachedPathCalculator implements PathCalculator {

    private final PathCalculator delegate;
    private final Map<Region.Node, Map<Region.Node, Deque<Region.Node>>> cache = new HashMap<>();
    private final int size;
    private final Set<Region.Node> accessOrder;

    /**
     * Creates a new {@link CachedPathCalculator}.<p>
     * The size of the cache will be set to 1024
     *
     * @param delegate The {@link PathCalculator} this {@link CachedPathCalculator} uses to
     *                 calculate the paths.
     */
    public CachedPathCalculator(PathCalculator delegate) {
        this(delegate, 1024);
    }

    /**
     * Creates a new {@link CachedPathCalculator}.
     *
     * @param delegate The {@link PathCalculator} this {@link CachedPathCalculator} uses to
     *                 calculate the paths.
     * @param size     The size of the cache.
     */
    public CachedPathCalculator(PathCalculator delegate, int size) {
        this.delegate = delegate;
        this.size = size;
        this.accessOrder = new LinkedHashSet<>(size);
    }

    @Override
    public Deque<Region.Node> getPath(Region.Node start, Region.Node end) {
        return getAllPathsTo(end).get(start);
    }

    @Override
    public Map<Region.Node, Deque<Region.Node>> getAllPathsTo(Region.Node end) {
        @Nullable Map<Region.Node, Deque<Region.Node>> path = cache.get(end);
        if (path != null) {
            return copyPath(path);
        }

        path = delegate.getAllPathsTo(end);

        // Limit cache size
        if (accessOrder.size() >= size) {
            Iterator<Region.Node> iterator = accessOrder.iterator();
            cache.remove(iterator.next());
            iterator.remove();
        }

        // Update access order if the element already exists
        accessOrder.remove(end);
        accessOrder.add(end);
        cache.put(end, path);

        return copyPath(path);
    }

    private Map<Region.Node, Deque<Region.Node>> copyPath(Map<Region.Node, Deque<Region.Node>> path) {
        return path.entrySet()
                   .stream()
                   .map(entry -> Map.entry(entry.getKey(), new LinkedList<>(entry.getValue())))
                   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public PathCalculator getDelegate() {
        return delegate;
    }
}
