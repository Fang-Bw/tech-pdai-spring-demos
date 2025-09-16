package tech.pdai.springboot.mysql8.mybatis.anno.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tech.pdai.springboot.mysql8.mybatis.anno.entity.User;
import tech.pdai.springboot.mysql8.mybatis.anno.entity.query.UserQueryBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserDao测试类
 * 
 * @author pdai
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IUserDaoTest {

    @Autowired
    private IUserDao userDao;

    @Test
    void testFindById() {
        // 测试根据ID查找用户
        User user = userDao.findById(1L);
        
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("pdai", user.getUserName());
        assertEquals("suzhou.daipeng@gmail.com", user.getEmail());
        assertEquals(1212121213L, user.getPhoneNumber());
        
        // 验证关联的角色信息
        assertNotNull(user.getRoles());
        assertFalse(user.getRoles().isEmpty());
        assertEquals("admin", user.getRoles().get(0).getName());
    }

    @Test
    void testFindByIdNotExists() {
        // 测试查找不存在的用户
        User user = userDao.findById(999L);
        assertNull(user);
    }

    @Test
    void testFindList() {
        // 测试查询用户列表
        UserQueryBean queryBean = new UserQueryBean();
        List<User> users = userDao.findList(queryBean);
        
        assertNotNull(users);
        assertTrue(users.size() >= 2); // 至少有测试数据中的2个用户
    }

    @Test
    void testFindListWithCondition() {
        // 测试带条件的查询
        UserQueryBean queryBean = new UserQueryBean();
        queryBean.setUserName("pdai");
        
        List<User> users = userDao.findList(queryBean);
        
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(users.stream().anyMatch(u -> "pdai".equals(u.getUserName())));
    }

    @Test
    void testSave() {
        // 测试保存用户
        User newUser = new User();
        newUser.setUserName("newuser");
        newUser.setPassword("password123");
        newUser.setEmail("newuser@example.com");
        newUser.setPhoneNumber(9876543210L);
        newUser.setDescription("新用户");
        
        int result = userDao.save(newUser);
        
        assertEquals(1, result);
        assertNotNull(newUser.getId());
        assertTrue(newUser.getId() > 0);
        
        // 验证保存的数据
        User savedUser = userDao.findById(newUser.getId());
        assertNotNull(savedUser);
        assertEquals("newuser", savedUser.getUserName());
        assertEquals("newuser@example.com", savedUser.getEmail());
    }

    @Test
    void testUpdate() {
        // 测试更新用户
        User user = userDao.findById(1L);
        assertNotNull(user);
        
        String originalUserName = user.getUserName();
        user.setUserName("updated_pdai");
        user.setDescription("更新后的描述");
        
        int result = userDao.update(user);
        assertEquals(1, result);
        
        // 验证更新结果
        User updatedUser = userDao.findById(1L);
        assertNotNull(updatedUser);
        assertEquals("updated_pdai", updatedUser.getUserName());
        assertEquals("更新后的描述", updatedUser.getDescription());
    }

    @Test
    void testUpdatePassword() {
        // 测试更新密码
        User user = new User();
        user.setId(1L);
        user.setPassword("newpassword123");
        
        int result = userDao.updatePassword(user);
        assertEquals(1, result);
        
        // 验证密码更新（注意：实际应用中密码应该加密）
        User updatedUser = userDao.findById(1L);
        assertNotNull(updatedUser);
        assertEquals("newpassword123", updatedUser.getPassword());
    }

    @Test
    void testDeleteById() {
        // 先创建一个测试用户
        User testUser = new User();
        testUser.setUserName("todelete");
        testUser.setPassword("password");
        testUser.setEmail("todelete@example.com");
        testUser.setPhoneNumber(1111111111L);
        
        userDao.save(testUser);
        Long userId = testUser.getId();
        
        // 验证用户存在
        User savedUser = userDao.findById(userId);
        assertNotNull(savedUser);
        
        // 删除用户
        int result = userDao.deleteById(userId);
        assertEquals(1, result);
        
        // 验证用户已删除
        User deletedUser = userDao.findById(userId);
        assertNull(deletedUser);
    }

    @Test
    void testDeleteByIds() {
        // 先创建两个测试用户
        User user1 = new User();
        user1.setUserName("todelete1");
        user1.setPassword("password");
        user1.setEmail("todelete1@example.com");
        user1.setPhoneNumber(1111111111L);
        userDao.save(user1);
        
        User user2 = new User();
        user2.setUserName("todelete2");
        user2.setPassword("password");
        user2.setEmail("todelete2@example.com");
        user2.setPhoneNumber(2222222222L);
        userDao.save(user2);
        
        Long[] userIds = {user1.getId(), user2.getId()};
        
        // 批量删除
        int result = userDao.deleteByIds(userIds);
        assertEquals(2, result);
        
        // 验证用户已删除
        assertNull(userDao.findById(user1.getId()));
        assertNull(userDao.findById(user2.getId()));
    }

    @Test
    void testFindById2() {
        // 测试使用Provider的查询方法
        User user = userDao.findById2(1L);
        
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("pdai", user.getUserName());
    }
}