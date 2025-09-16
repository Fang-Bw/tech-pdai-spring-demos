package tech.pdai.springboot.mysql8.mybatis.anno.integration;

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
import tech.pdai.springboot.mysql8.mybatis.anno.dao.IUserDao;
import tech.pdai.springboot.mysql8.mybatis.anno.dao.IRoleDao;
import tech.pdai.springboot.mysql8.mybatis.anno.entity.User;
import tech.pdai.springboot.mysql8.mybatis.anno.entity.Role;
import tech.pdai.springboot.mysql8.mybatis.anno.entity.query.UserQueryBean;
import tech.pdai.springboot.mysql8.mybatis.anno.entity.query.RoleQueryBean;
import tech.pdai.springboot.mysql8.mybatis.anno.service.IUserService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户管理系统集成测试
 * 测试从Controller到DAO的完整业务流程
 * 
 * @author pdai
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUserService userService;

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IRoleDao roleDao;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCompleteUserLifecycle() throws Exception {
        // 集成测试：完整的用户生命周期管理
        
        // 1. 验证初始数据存在
        List<User> initialUsers = userService.findList(new UserQueryBean());
        assertNotNull(initialUsers);
        assertTrue(initialUsers.size() >= 2);
        
        List<Role> roles = roleDao.findList(new RoleQueryBean());
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        
        // 2. 通过Web接口创建新用户
        String createResponse = mockMvc.perform(post("/user/add")
                        .param("userName", "integrationtest")
                        .param("password", "integration123")
                        .param("email", "integrationtest@example.com")
                        .param("phoneNumber", "1357924680")
                        .param("description", "集成测试用户"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data.userName").value("integrationtest"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 提取创建的用户ID
        JsonNode responseNode = objectMapper.readTree(createResponse);
        Long userId = responseNode.get("data").get("id").asLong();
        
        // 3. 验证用户在数据库中存在（DAO层验证）
        User daoUser = userDao.findById(userId);
        assertNotNull(daoUser);
        assertEquals("integrationtest", daoUser.getUserName());
        assertEquals("integrationtest@example.com", daoUser.getEmail());
        
        // 4. 通过Service层查询用户
        User serviceUser = userService.findById(userId);
        assertNotNull(serviceUser);
        assertEquals("integrationtest", serviceUser.getUserName());
        
        // 5. 通过Web接口查询用户
        mockMvc.perform(get("/user/edit/" + userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("integrationtest"))
                .andExpect(jsonPath("$.data.email").value("integrationtest@example.com"));
        
        // 6. 验证用户出现在列表中
        mockMvc.perform(get("/user/list"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.userName == 'integrationtest')]").exists());
        
        // 7. 通过Web接口更新用户
        mockMvc.perform(post("/user/add")
                        .param("id", userId.toString())
                        .param("userName", "updatedintegrationtest")
                        .param("password", "newintegration123")
                        .param("email", "updatedintegrationtest@example.com")
                        .param("phoneNumber", "9876543210")
                        .param("description", "更新后的集成测试用户"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("updatedintegrationtest"));
        
        // 8. 验证更新在各层都生效
        User updatedDaoUser = userDao.findById(userId);
        assertEquals("updatedintegrationtest", updatedDaoUser.getUserName());
        assertEquals("updatedintegrationtest@example.com", updatedDaoUser.getEmail());
        
        User updatedServiceUser = userService.findById(userId);
        assertEquals("updatedintegrationtest", updatedServiceUser.getUserName());
        
        // 9. 测试条件查询
        UserQueryBean queryBean = new UserQueryBean();
        queryBean.setUserName("updatedintegrationtest");
        List<User> queryResults = userService.findList(queryBean);
        assertFalse(queryResults.isEmpty());
        assertTrue(queryResults.stream().anyMatch(u -> u.getId().equals(userId)));
        
        // 10. 通过Web接口删除用户
        mockMvc.perform(post("/user/delete")
                        .param("userId", userId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        
        // 11. 验证删除在各层都生效
        assertNull(userDao.findById(userId));
        assertNull(userService.findById(userId));
        
        mockMvc.perform(get("/user/edit/" + userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDataConsistencyAcrossLayers() {
        // 测试数据在各层之间的一致性
        
        // 通过DAO层查询
        List<User> daoUsers = userDao.findList(new UserQueryBean());
        
        // 通过Service层查询
        List<User> serviceUsers = userService.findList(new UserQueryBean());
        
        // 验证数据一致性
        assertEquals(daoUsers.size(), serviceUsers.size());
        
        for (User daoUser : daoUsers) {
            User serviceUser = serviceUsers.stream()
                    .filter(u -> u.getId().equals(daoUser.getId()))
                    .findFirst()
                    .orElse(null);
            
            assertNotNull(serviceUser);
            assertEquals(daoUser.getUserName(), serviceUser.getUserName());
            assertEquals(daoUser.getEmail(), serviceUser.getEmail());
            assertEquals(daoUser.getPhoneNumber(), serviceUser.getPhoneNumber());
        }
    }

    @Test
    void testRoleAssociationIntegration() throws Exception {
        // 测试用户角色关联的集成功能
        
        // 1. 查询现有角色
        List<Role> roles = roleDao.findList(new RoleQueryBean());
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        
        // 2. 查询用户及其角色
        User userWithRoles = userService.findById(1L);
        assertNotNull(userWithRoles);
        assertNotNull(userWithRoles.getRoles());
        assertFalse(userWithRoles.getRoles().isEmpty());
        
        // 3. 通过Web接口验证用户角色信息
        mockMvc.perform(get("/user/edit/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.roles", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.roles[0].name").exists());
        
        // 4. 验证角色查询功能
        List<Role> userRoles = roleDao.findRoleByUserId(1L);
        assertNotNull(userRoles);
        assertFalse(userRoles.isEmpty());
        assertEquals(userWithRoles.getRoles().size(), userRoles.size());
    }

    @Test
    void testBatchOperationsIntegration() {
        // 测试批量操作的集成功能
        
        // 1. 创建多个测试用户
        User user1 = new User();
        user1.setUserName("batchtest1");
        user1.setPassword("password");
        user1.setEmail("batchtest1@example.com");
        user1.setPhoneNumber(1111111111L);
        user1.setDescription("批量测试用户1");
        userService.save(user1);
        
        User user2 = new User();
        user2.setUserName("batchtest2");
        user2.setPassword("password");
        user2.setEmail("batchtest2@example.com");
        user2.setPhoneNumber(2222222222L);
        user2.setDescription("批量测试用户2");
        userService.save(user2);
        
        // 2. 验证用户创建成功
        assertNotNull(userService.findById(user1.getId()));
        assertNotNull(userService.findById(user2.getId()));
        
        // 3. 批量删除
        Long[] userIds = {user1.getId(), user2.getId()};
        int deleteResult = userService.deleteByIds(userIds);
        assertEquals(2, deleteResult);
        
        // 4. 验证批量删除成功
        assertNull(userService.findById(user1.getId()));
        assertNull(userService.findById(user2.getId()));
        assertNull(userDao.findById(user1.getId()));
        assertNull(userDao.findById(user2.getId()));
    }

    @Test
    void testErrorHandlingIntegration() throws Exception {
        // 测试错误处理的集成功能
        
        // 1. 测试查询不存在的用户
        mockMvc.perform(get("/user/edit/99999"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
        
        assertNull(userService.findById(99999L));
        assertNull(userDao.findById(99999L));
        
        // 2. 测试删除不存在的用户
        mockMvc.perform(post("/user/delete")
                        .param("userId", "99999"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));
        
        assertEquals(0, userService.deleteById(99999L));
        assertEquals(0, userDao.deleteById(99999L));
    }

    @Test
    void testTransactionIntegration() {
        // 测试事务处理的集成功能
        
        // 获取初始用户数量
        List<User> initialUsers = userService.findList(new UserQueryBean());
        int initialCount = initialUsers.size();
        
        // 创建测试用户
        User testUser = new User();
        testUser.setUserName("transactiontest");
        testUser.setPassword("password");
        testUser.setEmail("transactiontest@example.com");
        testUser.setPhoneNumber(3333333333L);
        testUser.setDescription("事务测试用户");
        
        // 保存用户
        userService.save(testUser);
        
        // 验证用户数量增加
        List<User> afterSaveUsers = userService.findList(new UserQueryBean());
        assertEquals(initialCount + 1, afterSaveUsers.size());
        
        // 删除用户
        userService.deleteById(testUser.getId());
        
        // 验证用户数量恢复
        List<User> afterDeleteUsers = userService.findList(new UserQueryBean());
        assertEquals(initialCount, afterDeleteUsers.size());
    }

    @Test
    void testProviderMethodIntegration() throws Exception {
        // 测试Provider方法的集成功能
        
        // 1. 通过Service层调用Provider方法
        User serviceUser = userService.findById2(1L);
        assertNotNull(serviceUser);
        assertEquals(1L, serviceUser.getId());
        
        // 2. 通过Web接口调用Provider方法
        mockMvc.perform(get("/user/edit2/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userName").value("pdai"));
        
        // 3. 验证Provider方法与普通方法结果一致
        User normalUser = userService.findById(1L);
        assertEquals(serviceUser.getId(), normalUser.getId());
        assertEquals(serviceUser.getUserName(), normalUser.getUserName());
    }
}