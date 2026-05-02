package classified.util.pagination;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagingRequest {
    private final int page;   // номер страницы (начинается с 0)
    private final int size;   // количество элементов на странице
    private final Sort sort;  // объект сортировки (или unsorted)

    // удобные методы
    public int getOffset() {
        return page * size;   // смещение для запроса в БД
    }

    public PagingRequest(int page, int size) {
        this(page, size, Sort.unsorted());
    }

    public PagingRequest(int page, int size, Sort sort) {
        this.page = page;
        this.size = size;
        this.sort = sort != null ? sort : Sort.unsorted();
    }
}
