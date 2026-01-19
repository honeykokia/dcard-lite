package com.example.demo.board.service;

import com.example.demo.board.dto.BoardItem;
import com.example.demo.board.dto.ListBoardsRequest;
import com.example.demo.board.dto.ListBoardsResponse;
import com.example.demo.board.entity.Board;
import com.example.demo.board.repository.BoardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Transactional(readOnly = true)
    public ListBoardsResponse listBoards(ListBoardsRequest request){
        int page = Math.max(0, request.getPage() - 1);
        int pageSize = request.getPageSize();
        Pageable pageable = PageRequest.of(page, pageSize);

        String keyword = request.getKeyword();
        boolean hasKeyword = keyword != null;

        Page<Board> resultPage = hasKeyword
                ? boardRepository.findByNameContainingIgnoreCase(keyword, pageable)
                : boardRepository.findAll(pageable);

        List<BoardItem> items = resultPage.getContent().stream()
                .map(board -> {
                    BoardItem item = new BoardItem();
                    item.setBoardId(board.getBoardId());
                    item.setName(board.getName());
                    item.setDescription(board.getDescription());
                    return item;
                })
                .collect(Collectors.toList());

        ListBoardsResponse response = new ListBoardsResponse();
        response.setPage(resultPage.getNumber() + 1);
        response.setPageSize(resultPage.getSize());
        response.setTotal(resultPage.getTotalElements());
        response.setItems(items);
        return response;
    }
}
