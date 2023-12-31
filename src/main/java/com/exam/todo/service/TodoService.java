package com.exam.todo.service;

import com.exam.todo.model.TodoEntity;
import com.exam.todo.persistence.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository repository;

    public List<TodoEntity> create(final TodoEntity entity) {
        validate(entity);

        repository.save(entity);

        log.info("Entity Id : {} is saved", entity.getId());

        return repository.findByUserId(entity.getUserId());
    }

    public List<TodoEntity> retrieve(final String userId) {
        return repository.findByUserId(userId);
    }

    public List<TodoEntity> update(final TodoEntity entity) {
        // (1) 저장할 엔티티가 유요한지 확인
        validate(entity);

        // (2) 넘겨받은 엔티티 id를 이용해 TodoEntity를 취득(존재하지 않는 엔티티는 업데이트할 수 없기 때문)
        final Optional<TodoEntity> original = repository.findById(entity.getId());

        original.ifPresent(todo -> {
            // (3) 반환된 TodoEntity가 존재하면 값을 새 entity 값으로 변경
            todo.setTitle(entity.getTitle());
            todo.setDone(entity.isDone());

            // (4) 데이터베이스에 새 값을 저장
            repository.save(todo);
        });

        return retrieve(entity.getUserId());
    }

    public List<TodoEntity> delete(final TodoEntity entity) {
        // (1) 저장할 엔티티가 유요한지 확인
        validate(entity);

        try {
            // (2) 엔티티 삭제
            repository.delete(entity);
        } catch (Exception e) {
            // (3) exception 발생시 id와 exception을 로깅
            log.error("error deleting entity ", entity.getId(), e);

            // (4) 컨트롤러로 exception을 보냄(데이터베이스 내부 로직을 캡슐화하려면 e를 리턴하지 않고 새 exception 오브젝트를 리턴)
            throw new RuntimeException("error deleting entity " + entity.getId());
        }

        // (5) 새 Todo리스트를 가져와 리턴
        return retrieve(entity.getUserId());
    }

    private void validate(final TodoEntity entity) {
        if (entity == null) {
            log.warn("Entity cannot be null");
            throw new RuntimeException("Entity cannot be null");
        }

        if (entity.getUserId() == null) {
            log.warn("Unknown user");
            throw new RuntimeException("Unknown user");
        }
    }

    public String testService() {
        TodoEntity entity = TodoEntity.builder()
                .title("My first todo item")
                .build();

        repository.save(entity);

        TodoEntity savedEntity = repository.findById(entity.getId()).get();
        return savedEntity.getTitle();
    }

}
