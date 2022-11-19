//package com.example.ola.service;
//
//import com.example.ola.domain.User;
//import com.example.ola.dto.UserDto;
//import com.example.ola.dto.request.UserRequest;
//import com.example.ola.exception.OlaApplicationException;
//import com.example.ola.repository.UserRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//@Slf4j
//@Transactional
//@SpringBootTest
//class UserServiceTest {
//    @Autowired UserService userService;
//    @MockBean private UserRepository userRepository;
//
//    @Test
//    void 회원가입() throws Exception {
//        // given
//        UserRequest userRequest = new UserRequest("user1", "1q2w3e4r!!", "nickname1", "name1", 20L, "없음", "M");
//        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
//        when(userRepository.save(any()))
//                .thenReturn(User.of("user1", "1q2w3e4r!!", "nickname1", "name1", 20L, "없음", "M"));
//        // when
//        UserDto join = userService.join(userRequest);
//        // then
//        assertThat(join.getUsername()).isEqualTo("user1");
//        assertThat(join.getPassword()).isEqualTo("1q2w3e4r!!");
//        assertThat(join.getNickname()).isEqualTo("nickname1");
//        assertThat(join.getName()).isEqualTo("name1");
//        assertThat(join.getAgeRange()).isEqualTo(20);
//        assertThat(join.getHomeGym()).isEqualTo("없음");
//        assertThat(join.getGender()).isEqualTo("M");
//    }
//
//    @Test
//    void 회원가입시_중복일_경우() throws Exception {
//        UserRequest userRequest = new UserRequest("user1", "1q2w3e4r!!", "nickname1", "name1", 20L, "없음", "M");
//        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.of("user1", "1q2w3e4r!!", "nickname1", "name1", 20L, "없음", "M")));
//        assertThatThrownBy(() -> userService.join(userRequest))
//                .isInstanceOf(OlaApplicationException.class);
//    }
//
//    @Test
//    void 회원조회() throws Exception {
//        // given when
//        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.of("user1", "1q2w3e4r!!", "nickname1", "name1", 20L, "없음", "M")));
//        UserDto user1 = userService.findByUsername("user1");
//        // then
//        assertThat(user1.getUsername()).isEqualTo("user1");
//        assertThat(user1.getPassword()).isEqualTo("1q2w3e4r!!");
//        assertThat(user1.getNickname()).isEqualTo("nickname1");
//        assertThat(user1.getName()).isEqualTo("name1");
//        assertThat(user1.getAgeRange()).isEqualTo(20);
//        assertThat(user1.getHomeGym()).isEqualTo("없음");
//        assertThat(user1.getGender()).isEqualTo("M");
//    }
//
//    @Test
//    void 회원조회시_없는_경우() throws Exception {
//        // given
//        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
//        // when then
//        assertThatThrownBy(() -> userService.findByUserName("user1"))
//                .isInstanceOf(OlaApplicationException.class);
//    }
//}