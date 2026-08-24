package com.events.modules.event.specification;

import com.events.common.utils.contants.Constants;
import com.events.modules.event.dto.EventSearchFilterDto;
import com.events.modules.event.entity.Event;
import org.springframework.data.jpa.domain.Specification;

public class EventSpecification {
    public Specification<Event> withSpecification(EventSearchFilterDto filter) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.name() != null && !filter.name().isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get(Constants.NAME)), "%" + filter.name().toLowerCase() + "%"));
            }
            if (filter.status() != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get(Constants.STATUS), filter.status()));
            }
            if (filter.isPublic() != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get(Constants.IS_PUBLIC), filter.isPublic()));
            }
            if (filter.startDateFrom() != null) {
                predicates = cb.and(predicates,
                        cb.greaterThanOrEqualTo(root.get(Constants.START_DATE), filter.startDateFrom()));
            }
            if (filter.startDateTo() != null) {
                predicates = cb.and(predicates,
                        cb.lessThanOrEqualTo(root.get(Constants.END_DATE), filter.startDateTo()));
            }
            return predicates;
        };
    }
}
