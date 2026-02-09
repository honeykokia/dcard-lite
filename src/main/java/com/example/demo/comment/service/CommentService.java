package com.example.demo.comment.service;

import com.example.demo.comment.dto.CreateCommentRequest;
import com.example.demo.comment.dto.CreateCommentResponse;
import com.example.demo.comment.entity.Comment;
import com.example.demo.comment.error.CommentErrorCode;
import com.example.demo.comment.repository.CommentRepository;
import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.exception.ApiException;
import com.example.demo.post.entity.Post;
import com.example.demo.post.enums.PostStatus;
import com.example.demo.post.repository.PostRepository;
import com.example.demo.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public CreateCommentResponse createComment(long postId, User currentUser, CreateCommentRequest createCommentRequest) {

        // 先更新評論數，避免在同一個 transaction 內對已載入的 Post 進行 bulk update 造成 stale entity
        int updateRows = postRepository.incrementCommentCount(postId, PostStatus.ACTIVE);

        if (updateRows == 0){
            throw new ApiException(ErrorMessage.NOT_FOUND, CommentErrorCode.POST_NOT_FOUND);
        }

        // 檢查文章是否存在且狀態為 ACTIVE
        Post post = postRepository.findBasicByPostIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, CommentErrorCode.POST_NOT_FOUND));



        // 创建评论
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(currentUser);
        comment.setBody(createCommentRequest.getBody());

        // 保存評論
        Comment savedComment = commentRepository.save(comment);

        // 返回響應
        CreateCommentResponse response = new CreateCommentResponse();
        response.setCommentId(savedComment.getCommentId());
        return response;
    }
}
