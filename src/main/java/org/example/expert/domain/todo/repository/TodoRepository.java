package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);

    @Query(value = "SELECT t FROM Todo t LEFT JOIN FETCH t.user u " +
            "WHERE (:weather IS NULL OR t.weather = :weather) " +
            "AND (:start IS NULL OR t.modifiedAt >= :start) " +
            "AND (:end IS NULL OR t.modifiedAt <= :end) " +
            "ORDER BY t.modifiedAt DESC",
            countQuery = "SELECT COUNT(t) FROM Todo t " +
                    "WHERE (:weather IS NULL OR t.weather = :weather) " +
                    "AND (:start IS NULL OR t.modifiedAt >= :start) " +
                    "AND (:end IS NULL OR t.modifiedAt <= :end)")
    Page<Todo> searchTodos(
            @Param("weather") String weather,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);
    // LEFT JOIN FETCH t.user u - 유저 정보까지 한 번에 가지고옴
    // :weather IS NULL OR t.weather = :weather - 시작일이 없으면 조건 무시
    // :weather -> 메서드에서 넘겨주는 파라미터 값
    //  countQuery = "SELECT COUNT(t) FROM Todo t WHERE ..."
    // - Page<Todo>로 받을 때 JPA는 본문 쿼리로 데이터를 가져오가 + countQuery 전체 데이터 개수 세기 두가지 작업을 함
}
