package org.example.expert.domain.todo.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TodoSearchCondition {
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private String nickname;
}
