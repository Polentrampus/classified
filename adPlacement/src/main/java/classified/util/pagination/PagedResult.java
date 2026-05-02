package classified.util.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PagedResult <T>{
    private final List<T> content;    //список элементов на текущей странице
    private final int page;
    private final int size;           //параметры запроса
    private final Long totalElements;  //общее количество записей (нужно для вычисления общего числа страниц)

                                //вычисляет, сколько всего страниц.
    public int getTotalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }
}
