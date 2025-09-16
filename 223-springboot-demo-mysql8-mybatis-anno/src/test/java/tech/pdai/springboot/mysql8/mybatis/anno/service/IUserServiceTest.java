package tech.pdai.springboot.mysql8.mybatis.anno.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tech.pdai.springboot.mysql8.mybatis.anno.entity.User;
import tech.pdai.springboot.mysql8.mybatis.anno.entity.query.UserQueryBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService测试类
 * 
 * @author pdai
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IUserServiceTest {

    @Autowired
    private IUserService userService;

    @Test
    void testFindById() {
        // 测试根据ID查找用户
        User user = userService.findById(1L);
        
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("pdai", user.getUserName());
        assertEquals("suzhou.daipeng@gmail.com", user.getEmail());
        assertEquals(1212121213L, user.getPhoneNumber());
        assertEquals("afsdfsaf", user.getDescription());
        
        // 验证关联的角色信息
        assertNotNull(user.getRoles());
        assertFalse(user.getRoles().isEmpty());
        assertEquals("admin", user.getRoles().get(0).getName());
    }

    @Test
    void testFindByIdNotExists() {
        // 测试查找不存在的用户
        User user = userService.findById(999L);
        assertNull(user);
    }

    @Test
    void testFindList() {
        // 测试查询用户列表
        UserQueryBean queryBean = new UserQueryBean();
        List<User> users = userService.findList(queryBean);
        
        assertNotNull(users);
        assertTrue(users.size() >= 2); // 至少有测试数据中的2个用户
        
        // 验证用户数据
        User pdaiUser = users.stream()
                .filter(u -> "pdai".equals(u.getUserName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(pdaiUser);
        assertEquals("suzhou.daipeng@gmail.com", pdaiUser.getEmail());
        
        User testUser = users.stream()
                .filter(u -> "test".equals(u.getUserName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(testUser);
        assertEquals("test@example.com", testUser.getEmail());
    }

    @Test
    void testFindListWithCondition() {
        // 测试带条件的查询
        UserQueryBean queryBean = new UserQueryBean();
        queryBean.setUserName("pdai");
        
        List<User> users = userService.findList(queryBean);
        
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(users.stream().anyMatch(u -> "pdai".equals(u.getUserName())));
        
        // 测试邮箱条件查询
        UserQueryBean emailQueryBean = new UserQueryBean();
        emailQueryBean.setEmail("test");
        
        List<User> emailUsers = userService.findList(emailQueryBean);
        assertNotNull(emailUsers);
        assertTrue(emailUsers.stream().anyMatch(u -> u.getEmail().contains("test")));
    }

    @Test
    void testSave() {
        // 测试保存用户
        User newUser = new User();
        newUser.setUserName("servicetest");
        newUser.setPassword("password123");
        newUser.setEmail("servicetest@example.com");
        newUser.setPhoneNumber(9876543210L);
        newUser.setDescription("Service层测试用户");
        
        int result = userService.save(newUser);
        
        assertEquals(1, result);
        assertNotNull(newUser.getId());
        assertTrue(newUser.getId() > 0);
        
        // 验证保存的数据
        User savedUser = userService.findById(newUser.getId());
        assertNotNull(savedUser);
        assertEquals("servicetest", savedUser.getUserName());
        assertEquals("servicetest@example.com", savedUser.getEmail());
        assertEquals("Service层测试用户", savedUser.getDescription());
    }

    @Test
    void testUpdate() {
        // 测试更新用户
        User user = userService.findById(1L);
        assertNotNull(user);
        
        String originalDescription = user.getDescription();
        user.setUserName("updated_service_pdai");
        user.setDescription("Service层更新测试");
        user.setEmail("updated@example.com");
        
        int result = userService.update(user);
        assertEquals(1, result);
        
        // 验证更新结果
        User updatedUser = userService.findById(1L);
        assertNotNull(updatedUser);
        assertEquals("updated_service_pdai", updatedUser.getUserName());
        assertEquals("Service层更新测试", updatedUser.getDescription());
        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    @Test
    void testUpdatePassword() {
        // 测试更新密码
        User user = new User();
        user.setId(1L);
        user.setPassword("newservicepassword");
        
        int result = userService.updatePassword(user);
        assertEquals(1, result);
        
        // 验证密码更新
        User updatedUser = userService.findById(1L);
        assertNotNull(updatedUser);
        assertEquals("newservicepassword", updatedUser.getPassword());
    }

    @Test
    void testDeleteById() {
        // 先创建一个测试用户
        User testUser = new User();
        testUser.setUserName("servicetodelete");
        testUser.setPassword("password");
        testUser.setEmail("servicetodelete@example.com");
        testUser.setPhoneNumber(1111111111L);
        testUser.setDescription("待删除的测试用户");
        
        userService.save(testUser);
        Long userId = testUser.getId();
        
        // 验证用户存在
        User savedUser = userService.findById(userId);
        assertNotNull(savedUser);
        
        // 删除用户
        int result = userService.deleteById(userId);
        assertEquals(1, result);
        
        // 验证用户已删除
        User deletedUser = userService.findById(userId);
        assertNull(deletedUser);
    }

    @Test
    void testDeleteByIds() {
        // 先创建两个测试用户
        User user1 = new User();
        user1.setUserName("servicetodelete1");
        user1.setPassword("password");
        user1.setEmail("servicetodelete1@example.com");
        user1.setPhoneNumber(1111111111L);
        user1.setDescription("批量删除测试用户1");
        userService.save(user1);
        
        User user2 = new User();
        user2.setUserName("servicetodelete2");
        user2.setPassword("password");
        user2.setEmail("servicetodelete2@example.com");
        user2.setPhoneNumber(2222222222L);
        user2.setDescription("批量删除测试用户2");
        userService.save(user2);
        
        Long[] userIds = {user1.getId(), user2.getId()};
        
        // 批量删除
        int result = userService.deleteByIds(userIds);
        assertEquals(2, result);
        
        // 验证用户已删除
        assertNull(userService.findById(user1.getId()));
        assertNull(userService.findById(user2.getId()));
    }

    @Test
    void testFindById2() {
        // 测试使用Provider的查询方法
        User user = userService.findById2(1L);
        
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("pdai", user.getUserName());
    }

    @Test
    void testBusinessLogic() {
        // 测试业务逻辑：创建用户后立即查询验证
        User newUser = new User();
        newUser.setUserName("businesstest");
        newUser.setPassword("password123");
        newUser.setEmail("businesstest@example.com");
        newUser.setPhoneNumber(5555555555L);
        newUser.setDescription("业务逻辑测试");
        
        // 保存用户
        int saveResult = userService.save(newUser);
        assertEquals(1, saveResult);
        
        // 立即查询验证
        User savedUser = userService.findById(newUser.getId());
        assertNotNull(savedUser);
        assertEquals("businesstest", savedUser.getUserName());
        
        // 更新用户信息
        savedUser.setDescription("业务逻辑测试 - 已更新");
        int updateResult = userService.update(savedUser);
        assertEquals(1, updateResult);
        
        // 再次查询验证更新
        User updatedUser = userService.findById(savedUser.getId());
        assertNotNull(updatedUser);
        assertEquals("业务逻辑测试 - 已更新", updatedUser.getDescription());
        
        // 最后删除测试用户
        int deleteResult = userService.deleteById(updatedUser.getId());
        assertEquals(1, deleteResult);
        
        // 验证删除成功
        User deletedUser = userService.findById(updatedUser.getId());
        assertNull(deletedUser);
    }
}