package com.exam.todo.controller;

import com.exam.todo.dto.ResponseDTO;
import com.exam.todo.dto.TodoDTO;
import com.exam.todo.model.TodoEntity;
import com.exam.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService service;

    @PostMapping
    public ResponseEntity<?> createTodo(@RequestBody TodoDTO dto) {
        try {
            String temporaryUserId = "temporary-user";

            // (1) TodoEntity로 변환
            TodoEntity entity = TodoDTO.toEntity(dto);

            // (2) id를 null로 초기화(생성 당시에는 id가 없어야 하기 때문)
            entity.setId(null);

            // (3) 임시 사용자 아이디 설정(이 부분은 4장 인증과 인가에서 수정할 예정)
            entity.setUserId(temporaryUserId);

            // (4) 서비스를 이용해 TodoEntity 생성
            List<TodoEntity> entities = service.create(entity);

            // (5) 자바 스트림을 이용해 리턴된 엔티티 리스트를 TodoDTO 리스트로 변환
            List<TodoDTO> dtos = entities.stream().map(TodoDTO::new).collect(Collectors.toList());

            // (6) 변환된 TodoDTO 리스트를 이용해 ResponseDTO 초기화
            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();

            // (7) ResponseDTO 리턴
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            // (8) 혹시 예외가 있는 경우 dto 대신 error에 메시지를 넣어 리턴
            String error = e.getMessage();
            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> testTodo() {
        String str = service.testService();

        List<String> list = new ArrayList<>();
        list.add(str);

        ResponseDTO<String> response = ResponseDTO.<String>builder().data(list).build();
        return ResponseEntity.ok().body(response);
    }

}
