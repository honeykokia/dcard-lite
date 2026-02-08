package com.example.demo.comment;

import com.example.demo.comment.dto.CreateCommentRequest;
import com.example.demo.comment.dto.CreateCommentResponse;
import com.example.demo.comment.entity.Comment;
import com.example.demo.comment.error.CommentErrorCode;
import com.example.demo.comment.repository.CommentRepository;
import com.example.demo.comment.service.CommentService;
import com.example.demo.common.error.ErrorMessage;
import com.example.demo.common.exception.ApiException;
import com.example.demo.post.entity.Post;
import com.example.demo.post.enums.PostStatus;
import com.example.demo.post.repository.PostRepository;
import com.example.demo.user.entity.User;
import com.example.demo.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void createComment_Success() {
        // == Given ==

        long postId = 1L;

        User mockCurrentUser = new User();
        mockCurrentUser.setUserId(1L);
        mockCurrentUser.setDisplayName("Leo");
        mockCurrentUser.setRole(UserRole.USER);

        CreateCommentRequest mockRequest = new CreateCommentRequest();
        mockRequest.setBody("這是一則留言");

        Post mockPost = new Post();
        mockPost.setPostId(postId);
        mockPost.setStatus(PostStatus.ACTIVE);

        Comment savedComment = new Comment();
        savedComment.setCommentId(1L);
        savedComment.setPost(mockPost);
        savedComment.setAuthor(mockCurrentUser);
        savedComment.setBody(mockRequest.getBody());

        given(postRepository.findBasicByPostIdAndStatus(postId, PostStatus.ACTIVE)).willReturn(Optional.of(mockPost));
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // == When ==
        CreateCommentResponse response =  commentService.createComment(postId, mockCurrentUser, mockRequest);

        // == Then ==
        assertNotNull(response);
        assertEquals(1L, response.getCommentId());

        // == Verify ==
        verify(postRepository, times(1)).incrementCommentCount(postId);
        verify(postRepository).findBasicByPostIdAndStatus(postId, PostStatus.ACTIVE);
        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentArgumentCaptor.capture());
        Comment comment = commentArgumentCaptor.getValue();
        assertEquals(comment.getPost(), mockPost);
        assertEquals(comment.getAuthor(), mockCurrentUser);
        assertEquals(comment.getBody(), mockRequest.getBody());

    }

    @Test
    void createComment_PostNotFound_ThrowsException() {
        // == Given ==
        long postId = 1L;

        User mockCurrentUser = new User();
        mockCurrentUser.setUserId(1L);
        mockCurrentUser.setDisplayName("Leo");
        mockCurrentUser.setRole(UserRole.USER);

        CreateCommentRequest mockRequest = new CreateCommentRequest();
        mockRequest.setBody("這是一則留言");

        given(postRepository.findBasicByPostIdAndStatus(postId, PostStatus.ACTIVE)).willReturn(Optional.empty());

        // == When ==
        ApiException exception = assertThrows(ApiException.class, () ->{
            commentService.createComment(postId, mockCurrentUser, mockRequest);
        });

        // == Then ==
        assertEquals(ErrorMessage.NOT_FOUND.name(), exception.getMessage());
        assertEquals(CommentErrorCode.POST_NOT_FOUND, exception.getErrorCode());

        // == Vrify ==
        verify(postRepository).findBasicByPostIdAndStatus(postId, PostStatus.ACTIVE);
        verify(postRepository, never()).incrementCommentCount(postId);
        verify(commentRepository, never()).save(any());

    }
}
