package ru.cs.vsu.social_network.messaging_service.dto.response.pageable;


import lombok.*;
import org.springframework.data.domain.Page;
import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    public static <T> PageResponse<T> of(PageRequest pageRequest, List<T> content, Long totalElements) {
        int pageSize = pageRequest.getSize();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isFirst = pageRequest.getPageNumber() == 0;
        boolean isLast = pageRequest.getPageNumber() >= totalPages - 1;

        return PageResponse.<T>builder()
                .content(content)
                .currentPage(pageRequest.getPageNumber())
                .totalPages(totalPages)
                .totalElements(totalElements)
                .pageSize(pageSize)
                .first(isFirst)
                .last(isLast)
                .build();
    }

    public static <T> PageResponse<T> ofAll(List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .currentPage(0)
                .totalPages(1)
                .totalElements((long) content.size())
                .pageSize(content.size())
                .first(true)
                .last(true)
                .build();
    }
}