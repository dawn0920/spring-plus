package org.example.expert.domain.todo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(todoService.getTodos(page, size));
    }

    @GetMapping("/todos/{todoId}")
    public ResponseEntity<TodoResponse> getTodo(@PathVariable long todoId) {
        return ResponseEntity.ok(todoService.getTodo(todoId));
    }
}
