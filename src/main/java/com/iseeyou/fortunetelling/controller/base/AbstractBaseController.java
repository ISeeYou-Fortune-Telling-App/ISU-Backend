package com.iseeyou.fortunetelling.controller.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public abstract class AbstractBaseController {

    @Autowired
    protected ResponseFactory responseFactory;

    public static Pageable createPageable(int page, int limit, String sortType, String sortBy) {
        Sort.Direction direction = (sortType != null && sortType.equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        Sort sort = Sort.by(direction, sortField);

        int pageNumber = page - 1;
        int offset = (page - 1) * limit;
        pageNumber = offset / limit;
        return PageRequest.of(pageNumber, limit, sort);
    }

    // Other common methods for controllers can be added here...
}
