package sk.autoops.autoops.dto;

import java.util.List;

public record PagedResponse<T>(
        List<T> items,
        int page,
        int size,
        int totalItems,
        int totalPages
) {
    public static <T> PagedResponse<T> of(List<T> source, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 20 : Math.min(size, 200);
        int totalItems = source.size();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / safeSize);
        int from = Math.min(safePage * safeSize, totalItems);
        int to = Math.min(from + safeSize, totalItems);
        List<T> slice = source.subList(from, to);
        return new PagedResponse<>(slice, safePage, safeSize, totalItems, totalPages);
    }
}
