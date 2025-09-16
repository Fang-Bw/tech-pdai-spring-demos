package tech.pdai.springboot.mysql8.jpa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.pdai.springboot.mysql8.jpa.entity.Role;
import tech.pdai.springboot.mysql8.jpa.entity.User;
import tech.pdai.springboot.mysql8.jpa.entity.query.UserQueryBean;
import tech.pdai.springboot.mysql8.jpa.service.IUserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController集成测试
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ADMIN");
        testRole.setRoleKey("admin");
        testRole.setDescription("管理员");
        testRole.setCreateTime(LocalDateTime.now());
        testRole.setUpdateTime(LocalDateTime.now());

        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setPhoneNumber(13800138000L);
        testUser.setDescription("测试用户");
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
        testUser.setRoles(new HashSet<>(Arrays.asList(testRole)));
    }

    @Test
    void testAddNewUser() throws Exception {
        // 模拟新用户添加
        when(userService.exists(any())).thenReturn(false);
        when(userService.find(1L)).thenReturn(testUser);

        mockMvc.perform(post("/user/add")
                .param("id", "1")
                .param("userName", "testuser")
                .param("password", "password123")
                .param("email", "test@example.com")
                .param("phoneNumber", "13800138000")
                .param("description", "测试用户")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data.userName").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void testUpdateExistingUser() throws Exception {
        // 模拟用户更新
        when(userService.exists(1L)).thenReturn(true);
        when(userService.find(1L)).thenReturn(testUser);

        mockMvc.perform(post("/user/add")
                .param("id", "1")
                .param("userName", "updateduser")
                .param("password", "newpassword123")
                .param("email", "updated@example.com")
                .param("phoneNumber", "13900139000")
                .param("description", "更新的测试用户")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data.userName").value("testuser"));
    }

    @Test
    void testEditUser() throws Exception {
        // 测试获取单个用户
        when(userService.find(1L)).thenReturn(testUser);

        mockMvc.perform(get("/user/edit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userName").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.phoneNumber").value(13800138000L));
    }

    @Test
    void testEditUserNotFound() throws Exception {
        // 测试用户不存在的情况
        when(userService.find(999L)).thenReturn(null);

        mockMvc.perform(get("/user/edit/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testListUsers() throws Exception {
        // 创建分页数据
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userService.findPage(any(UserQueryBean.class), any(PageRequest.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/user/list")
                .param("pageSize", "10")
                .param("pageNumber", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].userName").value("testuser"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.number").value(0));
    }

    @Test
    void testListUsersWithPagination() throws Exception {
        // 测试分页参数
        User user2 = new User();
        user2.setId(2L);
        user2.setUserName("testuser2");
        user2.setEmail("test2@example.com");

        List<User> users = Arrays.asList(testUser, user2);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(1, 5), 10);

        when(userService.findPage(any(UserQueryBean.class), eq(PageRequest.of(1, 5))))
                .thenReturn(userPage);

        mockMvc.perform(get("/user/list")
                .param("pageSize", "5")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isNotEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(10))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.number").value(1));
    }

    @Test
    void testListUsersEmptyResult() throws Exception {
        // 测试空结果
        Page<User> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        when(userService.findPage(any(UserQueryBean.class), any(PageRequest.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/user/list")
                .param("pageSize", "10")
                .param("pageNumber", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void testAddUserWithInvalidParameters() throws Exception {
        // 测试无效参数
        mockMvc.perform(post("/user/add")
                .param("userName", "")
                .param("email", "invalid-email")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk()); // 注意：这里可能需要根据实际的验证逻辑调整
    }

    @Test
    void testListUsersWithInvalidPagination() throws Exception {
        // 测试无效的分页参数
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser), PageRequest.of(0, 10), 1);

        when(userService.findPage(any(UserQueryBean.class), any(PageRequest.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/user/list")
                .param("pageSize", "-1")
                .param("pageNumber", "-1"))
                .andExpect(status().isOk()); // 注意：这里可能需要根据实际的验证逻辑调整
    }
}