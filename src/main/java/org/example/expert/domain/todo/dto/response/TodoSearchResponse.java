package org.example.expert.domain.todo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TodoSearchResponse {
    private final String title;
    private final long commentCount;
    private final long managerCount;
}
