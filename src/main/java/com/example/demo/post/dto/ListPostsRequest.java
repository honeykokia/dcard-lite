package com.example.demo.post.dto;

import com.example.demo.post.enums.PostSort;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class ListPostsRequest {

    @Min(value = 1, message = "PAGE_INVALID")
    private Integer page = 1;

    @Min(value = 1, message = "PAGE_SIZE_INVALID")
    @Max(value = 200, message = "PAGE_SIZE_INVALID")
    private Integer pageSize = 40;

    @NotNull(message = "SORT_INVALID")
    private PostSort sort = PostSort.LATEST;
}
