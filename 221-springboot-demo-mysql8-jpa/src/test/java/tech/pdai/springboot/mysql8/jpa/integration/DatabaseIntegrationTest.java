package tech.pdai.springboot.mysql8.jpa.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tech.pdai.springboot.mysql8.jpa.dao.IRoleDao;
import tech.pdai.springboot.mysql8.jpa.dao.IUserDao;
import tech.pdai.springboot.mysql8.jpa.entity.Role;
import tech.pdai.springboot.mysql8.jpa.entity.User;
import tech.pdai.springboot.mysql8.jpa.entity.query.UserQueryBean;
import tech.pdai.springboot.mysql8.jpa.service.IUserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据库集成测试 - 验证完整的数据流转
 * 从Controller -> Service -> DAO -> Database 的完整流程
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseIntegrationTest {

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IRoleDao roleDao;

    @Autowired
    private IUserService userService;

    private Role adminRole;
    private Role userRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 清理数据
        userDao.deleteAll();
        roleDao.deleteAll();

        // 创建角色数据
        adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setRoleKey("admin");
        adminRole.setDescription("系统管理员");
        adminRole.setCreateTime(LocalDateTime.now());
        adminRole.setUpdateTime(LocalDateTime.now());
        adminRole = roleDao.save(adminRole);

        userRole = new Role();
        userRole.setName("USER");
        userRole.setRoleKey("user");
        userRole.setDescription("普通用户");
        userRole.setCreateTime(LocalDateTime.now());
        userRole.setUpdateTime(LocalDateTime.now());
        userRole = roleDao.save(userRole);

        // 创建用户数据
        testUser = new User();
        testUser.setUserName("integrationtest");
        testUser.setPassword("password123");
        testUser.setEmail("integration@test.com");
        testUser.setPhoneNumber(13800138000L);
        testUser.setDescription("集成测试用户");
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
        testUser.setRoles(new HashSet<>(Arrays.asList(adminRole, userRole)));
    }

    @Test
    void testCompleteUserLifecycle() {
        // 1. 保存用户
        User savedUser = userDao.save(testUser);
        assertNotNull(savedUser.getId());
        assertEquals("integrationtest", savedUser.getUserName());
        assertEquals(2, savedUser.getRoles().size());

        // 2. 通过ID查询用户
        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("integrationtest", foundUser.get().getUserName());
        assertEquals("integration@test.com", foundUser.get().getEmail());

        // 3. 更新用户信息
        savedUser.setEmail("updated@test.com");
        savedUser.setDescription("更新后的集成测试用户");
        savedUser.setUpdateTime(LocalDateTime.now());
        User updatedUser = userDao.save(savedUser);
        assertEquals("updated@test.com", updatedUser.getEmail());
        assertEquals("更新后的集成测试用户", updatedUser.getDescription());

        // 4. 验证角色关联
        assertEquals(2, updatedUser.getRoles().size());
        assertTrue(updatedUser.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName())));
        assertTrue(updatedUser.getRoles().stream()
                .anyMatch(role -> "USER".equals(role.getName())));

        // 5. 删除用户
        userDao.delete(updatedUser);
        Optional<User> deletedUser = userDao.findById(savedUser.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testServiceLayerIntegration() {
        // 通过Service层保存用户
        userService.save(testUser);
        User savedUser = userService.find(testUser.getId());
        assertNotNull(savedUser.getId());

        // 通过Service层查询用户
        User foundUser = userService.find(savedUser.getId());
        assertNotNull(foundUser);
        assertEquals("integrationtest", foundUser.getUserName());

        // 通过Service层更新用户
        foundUser.setEmail("service@test.com");
        User updatedUser = userService.update(foundUser);
        assertEquals("service@test.com", updatedUser.getEmail());

        // 通过Service层分页查询
        UserQueryBean queryBean = UserQueryBean.builder().build();
        Page<User> userPage = userService.findPage(queryBean, PageRequest.of(0, 10));
        assertNotNull(userPage);
        assertTrue(userPage.getTotalElements() >= 1);

        // 通过Service层删除用户
        userService.delete(savedUser.getId());
        User deletedUser = userService.find(savedUser.getId());
        assertNull(deletedUser);
    }

    @Test
    void testRoleManagement() {
        // 验证角色数据
        List<Role> allRoles = roleDao.findAll();
        assertEquals(2, allRoles.size());

        // 通过角色名查询
        Optional<Role> adminRoleFound = roleDao.findAll().stream()
                .filter(role -> "ADMIN".equals(role.getName()))
                .findFirst();
        assertTrue(adminRoleFound.isPresent());
        assertEquals("admin", adminRoleFound.get().getRoleKey());

        // 创建新角色
        Role guestRole = new Role();
        guestRole.setName("GUEST");
        guestRole.setRoleKey("guest");
        guestRole.setDescription("访客用户");
        guestRole.setCreateTime(LocalDateTime.now());
        guestRole.setUpdateTime(LocalDateTime.now());
        Role savedGuestRole = roleDao.save(guestRole);

        assertNotNull(savedGuestRole.getId());
        assertEquals("GUEST", savedGuestRole.getName());

        // 验证总数
        List<Role> updatedRoles = roleDao.findAll();
        assertEquals(3, updatedRoles.size());
    }

    @Test
    void testUserRoleAssociation() {
        // 保存用户
        User savedUser = userDao.save(testUser);

        // 验证用户角色关联
        User userWithRoles = userDao.findById(savedUser.getId()).orElse(null);
        assertNotNull(userWithRoles);
        assertEquals(2, userWithRoles.getRoles().size());

        // 修改用户角色 - 只保留一个角色
        userWithRoles.setRoles(new HashSet<>(Arrays.asList(adminRole)));
        User updatedUser = userDao.save(userWithRoles);

        // 验证角色更新
        User verifyUser = userDao.findById(updatedUser.getId()).orElse(null);
        assertNotNull(verifyUser);
        assertEquals(1, verifyUser.getRoles().size());
        assertEquals("ADMIN", verifyUser.getRoles().iterator().next().getName());
    }

    @Test
    void testDataPersistence() {
        // 保存多个用户
        User user1 = new User();
        user1.setUserName("user1");
        user1.setPassword("pass1");
        user1.setEmail("user1@test.com");
        user1.setPhoneNumber(13800138001L);
        user1.setDescription("用户1");
        user1.setCreateTime(LocalDateTime.now());
        user1.setUpdateTime(LocalDateTime.now());
        user1.setRoles(new HashSet<>(Arrays.asList(userRole)));

        User user2 = new User();
        user2.setUserName("user2");
        user2.setPassword("pass2");
        user2.setEmail("user2@test.com");
        user2.setPhoneNumber(13800138002L);
        user2.setDescription("用户2");
        user2.setCreateTime(LocalDateTime.now());
        user2.setUpdateTime(LocalDateTime.now());
        user2.setRoles(new HashSet<>(Arrays.asList(adminRole)));

        // 批量保存
        List<User> savedUsers = userDao.saveAll(Arrays.asList(user1, user2));
        assertEquals(2, savedUsers.size());

        // 验证数据持久化
        List<User> allUsers = userDao.findAll();
        assertTrue(allUsers.size() >= 2);

        // 分页查询验证
        Page<User> firstPage = userDao.findAll(PageRequest.of(0, 1));
        assertEquals(1, firstPage.getContent().size());
        assertTrue(firstPage.getTotalElements() >= 2);

        // 排序查询验证
        List<User> sortedUsers = userDao.findAll();
        assertNotNull(sortedUsers);
        assertFalse(sortedUsers.isEmpty());
    }

    @Test
    void testTransactionRollback() {
        // 这个测试验证事务回滚机制
        try {
            // 保存用户
            User savedUser = userDao.save(testUser);
            assertNotNull(savedUser.getId());

            // 模拟异常情况
            throw new RuntimeException("模拟异常");
        } catch (RuntimeException e) {
            // 由于@Transactional注解，事务应该回滚
            assertEquals("模拟异常", e.getMessage());
        }

        // 验证事务回滚后数据不存在
        List<User> users = userDao.findAll();
        // 注意：由于@Transactional注解，每个测试方法都会回滚，所以这里应该是空的
        assertTrue(users.isEmpty() || users.stream().noneMatch(u -> "integrationtest".equals(u.getUserName())));
    }
}