package com.events.common.utils.Pagination;

import io.micrometer.common.util.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.function.Function;

public class PaginationUtils {

    public static <T> Pageable getPageable(T query,
                                           Function<T, Integer> pageGetter,
                                           Function<T, Integer> sizeGetter,
                                           Function<T, String> sortFieldGetter,
                                           Function<T, String> directionGetter) {
        Sort sortOrder;
        String sortField = sortFieldGetter.apply(query);
        String direction = directionGetter.apply(query);
        if (StringUtils.isNotBlank(sortField)) {
            if (StringUtils.isBlank(direction)) {
                sortOrder = Sort.by(sortField).ascending();
            } else {
                sortOrder = direction.equalsIgnoreCase("desc")
                        ? Sort.by(sortField).descending()
                        : Sort.by(sortField).ascending();
            }
        } else {
            sortOrder = Sort.by("id").ascending();
        }

        Integer page = pageGetter.apply(query);
        Integer size = sizeGetter.apply(query);

        return PageRequest.of(page - 1, size, sortOrder);
    }
}
