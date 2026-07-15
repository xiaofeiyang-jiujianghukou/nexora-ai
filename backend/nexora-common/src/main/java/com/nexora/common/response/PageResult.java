package com.nexora.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回体
 *
 * @param <T> 列表元素类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据列表 */
    private List<T> list;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Integer page;

    /** 每页大小 */
    private Integer size;

    /** 是否有更多 */
    private Boolean hasMore;

    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer size) {
        boolean hasMore = (long) page * size < total;
        return new PageResult<>(list, total, page, size, hasMore);
    }
}
