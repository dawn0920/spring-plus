package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.request.TodoSearchCondition;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryCustomImpl implements TodoRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;

        Todo result = jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchByCondition(TodoSearchCondition searchCondition, Pageable pageable) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;
        QManager manager = QManager.manager;
        QComment comment = QComment.comment;

        // 하나씩 조립해서 만드는 Where 조건 객체 (builder.and(...)를 여러 번 호출하면 조건이 쌓임)
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (searchCondition.getTitle() != null) {
            booleanBuilder.and(todo.title.like("%" + searchCondition.getTitle() + "%"));
        }
        if (searchCondition.getStart() != null) {
            // goe -> 크거나 같음(>=) 조건을 설정하는 메서드
            booleanBuilder.and(todo.modifiedAt.goe(searchCondition.getStart()));
        }
        if (searchCondition.getEnd() != null) {
            // goe -> 크거나 같음(>=) 조건을 설정하는 메서드
            booleanBuilder.and(todo.modifiedAt.loe(searchCondition.getEnd()));
        }
        if (searchCondition.getNickname() != null) {
            booleanBuilder.and(manager.user.nickname.like("%" + searchCondition.getNickname() + "%"));
        }

        // todo 조회
        List<Todo> result = jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.managers, manager).fetchJoin()
                .leftJoin(manager.user, user).fetchJoin()
                .where(booleanBuilder)
                .orderBy(todo.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 게시글 전체 개수
        Long totalCount = jpaQueryFactory
                .select(todo.count())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(booleanBuilder)
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        // 댓글 개수 조회
        List<Tuple> commentCounts = jpaQueryFactory
                .select(comment.todo.id, comment.count())
                .from(comment)
                .groupBy(comment.todo.id)
                .fetch();

        Map<Long, Long> commentCountMap =
                commentCounts.stream()
                        .collect(Collectors.toMap(
                                tuple -> tuple.get(comment.todo.id),
                                tuple -> tuple.get(comment.count())
                        ));

        // 담당자 수 조회
        List<Tuple> managerCounts = jpaQueryFactory
                .select(manager.todo.id, manager.count())
                .from(manager)
                .groupBy(manager.todo.id)
                .fetch();

        Map<Long, Long> managerCountMap =
                managerCounts.stream()
                        .collect(Collectors.toMap(
                                tuple -> tuple.get(manager.todo.id),
                                tuple -> tuple.get(manager.count())
                        ));

        List<TodoSearchResponse> content = result.stream()
                .map(todos -> new TodoSearchResponse(
                        todos.getTitle(),
                        commentCountMap.getOrDefault(todos.getId(), 0L),
                        managerCountMap.getOrDefault(todos.getId(), 0L)
                ))
                .toList();

        return new PageImpl<>(content, pageable, total);
    }
}
