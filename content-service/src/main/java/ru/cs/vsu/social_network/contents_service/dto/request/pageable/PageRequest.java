package ru.cs.vsu.social_network.contents_service.dto.request.pageable;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    private Integer size;
    private Integer pageNumber;
    private String sortBy = "createdAt";
    private Sort.Direction direction = Sort.Direction.DESC;

    public org.springframework.data.domain.PageRequest toPageable() {
        return org.springframework.data.domain.PageRequest.of(pageNumber, size, direction, sortBy);
    }
}

