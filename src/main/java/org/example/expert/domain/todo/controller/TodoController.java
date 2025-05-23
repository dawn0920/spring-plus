package org.example.expert.domain.todo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.request.TodoSearchCondition;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;


    // 할 일 저장 기능을 구현한 API(/todos)를 호출할 때, 아래와 같은 에러가 발생
    /*
    jakarta.servlet.ServletException: Request processing failed:
    org.springframework.orm.jpa.JpaSystemException: could not execute statement
    [Connection is read-only. Queries leading to data modification are not allowed]
    [insert into todos (contents,created_at,modified_at,title,user_id,weather)
    values (?,?,?,?,?,?)]
    */
    // 문제!  연결이 읽기 전용으로 되어있기 때문에 데이터 수정인 퀘리 삽입이 이루어지지 않음
    // 해결! TodoService에 @Transactional(readOnly = true) 설정을 각 메소드마다 다르게 변경
    // ex) saveTodo - @Transactional / getTodos - @Transactional(readOnly = true)
    // 추가 정보 - (readOnly = true) 를 붙이는 것이 Flush 수행을 하지 않기 때문에 SQL 검사가 줄어듬
    // -> 즉 성능 향상 + 의도 명확성이 올라간다/
    @PostMapping("/todos")
    public ResponseEntity<TodoSaveResponse> saveTodo(
            @Auth AuthUser authUser,
            @Valid @RequestBody TodoSaveRequest todoSaveRequest
    ) {
        return ResponseEntity.ok(todoService.saveTodo(authUser, todoSaveRequest));
    }


    @GetMapping("/todos")
    public ResponseEntity<Page<TodoResponse>> getTodos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String weather,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime end
            // ISO 형식 (yyyy-MM-dd'T'HH:mm:ss) 사용
    ) {
        return ResponseEntity.ok(todoService.getTodos(page, size, weather, start, end));
    }

    @GetMapping("/todos/{todoId}")
    public ResponseEntity<TodoResponse> getTodo(@PathVariable long todoId) {
        return ResponseEntity.ok(todoService.getTodo(todoId));
    }

    @GetMapping("/todos/search")
    public ResponseEntity<Page<TodoSearchResponse>> getSearchTodos(
            @ModelAttribute TodoSearchCondition todoSearchCondition,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
            ) {

        return ResponseEntity.ok(todoService.getSearchTodos(todoSearchCondition, page, size));
    }
}
