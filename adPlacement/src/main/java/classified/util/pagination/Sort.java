package classified.util.pagination;

import java.util.Collections;
import java.util.List;

public class Sort {
    private final List<SortOrder> orders;

    private Sort(List<SortOrder> orders) {
        this.orders = Collections.unmodifiableList(orders);
    }

    public static Sort by(String field, Direction direction) {
        return new Sort(List.of(new SortOrder(field, direction)));
    }

    public static Sort unsorted() {
        return new Sort(List.of());
    }

    public boolean isSorted() { return !orders.isEmpty(); }

    public List<SortOrder> getOrders() { return orders; }
}