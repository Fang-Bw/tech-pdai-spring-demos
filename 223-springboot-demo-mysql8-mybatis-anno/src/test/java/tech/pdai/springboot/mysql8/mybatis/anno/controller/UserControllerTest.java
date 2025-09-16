package tech.pdai.springboot.mysql8.mybatis.anno.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tech.pdai.springboot.mysql8.mybatis.anno.entity.User;
import tech.pdai.springboot.mysql8.mybatis.anno.service.IUserService;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController测试类
 * 
 * @author pdai
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testEdit() throws Exception {
        // 测试根据ID查询用户
        mockMvc.perform(get("/user/edit/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userName").value("pdai"))
                .andExpect(jsonPath("$.data.email").value("suzhou.daipeng@gmail.com"))
                .andExpect(jsonPath("$.data.phoneNumber").value(1212121213L))
                .andExpect(jsonPath("$.data.description").value("afsdfsaf"))
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.roles[0].name").value("admin"));
    }

    @Test
    void testEditNotExists() throws Exception {
        // 测试查询不存在的用户
        mockMvc.perform(get("/user/edit/999"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testEdit2() throws Exception {
        // 测试使用Provider的查询方法
        mockMvc.perform(get("/user/edit2/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userName").value("pdai"));
    }

    @Test
    void testList() throws Exception {
        // 测试查询用户列表
        mockMvc.perform(get("/user/list"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.data[?(@.userName == 'pdai')]").exists())
                .andExpect(jsonPath("$.data[?(@.userName == 'test')]").exists());
    }

    @Test
    void testListWithCondition() throws Exception {
        // 测试带条件的查询
        mockMvc.perform(get("/user/list")
                        .param("userName", "pdai"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.userName == 'pdai')]").exists());

        // 测试邮箱条件查询
        mockMvc.perform(get("/user/list")
                        .param("email", "test"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.email =~ /.*test.*/)]").exists());
    }

    @Test
    void testAddNewUser() throws Exception {
        // 测试添加新用户
        mockMvc.perform(post("/user/add")
                        .param("userName", "controllertest")
                        .param("password", "password123")
                        .param("email", "controllertest@example.com")
                        .param("phoneNumber", "9876543210")
                        .param("description", "Controller层测试用户"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.userName").value("controllertest"))
                .andExpect(jsonPath("$.data.email").value("controllertest@example.com"))
                .andExpect(jsonPath("$.data.description").value("Controller层测试用户"));
    }

    @Test
    void testUpdateUser() throws Exception {
        // 先创建一个用户用于更新测试
        User testUser = new User();
        testUser.setUserName("updatetest");
        testUser.setPassword("password");
        testUser.setEmail("updatetest@example.com");
        testUser.setPhoneNumber(1234567890L);
        testUser.setDescription("待更新的测试用户");
        userService.save(testUser);

        // 测试更新用户
        mockMvc.perform(post("/user/add")
                        .param("id", testUser.getId().toString())
                        .param("userName", "updatedcontrollertest")
                        .param("password", "newpassword")
                        .param("email", "updatedcontrollertest@example.com")
                        .param("phoneNumber", "9876543210")
                        .param("description", "Controller层更新测试用户"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.id").value(testUser.getId()))
                .andExpect(jsonPath("$.data.userName").value("updatedcontrollertest"))
                .andExpect(jsonPath("$.data.email").value("updatedcontrollertest@example.com"))
                .andExpect(jsonPath("$.data.description").value("Controller层更新测试用户"));
    }

    @Test
    void testDelete() throws Exception {
        // 先创建一个用户用于删除测试
        User testUser = new User();
        testUser.setUserName("deletetest");
        testUser.setPassword("password");
        testUser.setEmail("deletetest@example.com");
        testUser.setPhoneNumber(1111111111L);
        testUser.setDescription("待删除的测试用户");
        userService.save(testUser);

        // 测试删除用户
        mockMvc.perform(post("/user/delete")
                        .param("userId", testUser.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data").value(1));

        // 验证用户已删除 - 再次查询应该返回null
        mockMvc.perform(get("/user/edit/" + testUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testCompleteWorkflow() throws Exception {
        // 测试完整的工作流程：创建 -> 查询 -> 更新 -> 删除
        
        // 1. 创建用户
        String createResponse = mockMvc.perform(post("/user/add")
                        .param("userName", "workflowtest")
                        .param("password", "password123")
                        .param("email", "workflowtest@example.com")
                        .param("phoneNumber", "5555555555")
                        .param("description", "工作流程测试用户"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data.userName").value("workflowtest"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 从响应中提取用户ID
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseNode = mapper.readTree(createResponse);
        Long userId = responseNode.get("data").get("id").asLong();

        // 2. 查询用户
        mockMvc.perform(get("/user/edit/" + userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("workflowtest"));

        // 3. 更新用户
        mockMvc.perform(post("/user/add")
                        .param("id", userId.toString())
                        .param("userName", "updatedworkflowtest")
                        .param("password", "newpassword")
                        .param("email", "updatedworkflowtest@example.com")
                        .param("phoneNumber", "6666666666")
                        .param("description", "更新后的工作流程测试用户"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("updatedworkflowtest"));

        // 4. 删除用户
        mockMvc.perform(post("/user/delete")
                        .param("userId", userId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));

        // 5. 验证删除成功
        mockMvc.perform(get("/user/edit/" + userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testErrorHandling() throws Exception {
        // 测试错误处理 - 删除不存在的用户
        mockMvc.perform(post("/user/delete")
                        .param("userId", "999"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data").value(0)); // 删除不存在的用户返回0
    }
}