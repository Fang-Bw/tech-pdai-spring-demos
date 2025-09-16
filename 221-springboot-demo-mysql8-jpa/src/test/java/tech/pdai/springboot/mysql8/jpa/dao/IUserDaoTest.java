package tech.pdai.springboot.mysql8.jpa.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import tech.pdai.springboot.mysql8.jpa.entity.Role;
import tech.pdai.springboot.mysql8.jpa.entity.User;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IUserDao测试类
 * 
 * @author pdai
 */
@DataJpaTest
@ActiveProfiles("test")
@SpringJUnitConfig
class IUserDaoTest {

    @Resource
    private IUserDao userDao;

    @Resource
    private IRoleDao roleDao;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // 创建测试角色
        testRole = new Role();
        testRole.setName("TEST_ROLE");
        testRole.setRoleKey("test");
        testRole.setDescription("测试角色");
        testRole.setCreateTime(LocalDateTime.now());
        testRole.setUpdateTime(LocalDateTime.now());
        testRole = roleDao.save(testRole);

        // 创建测试用户
        testUser = new User();
        testUser.setUserName("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setPhoneNumber(13800138000L);
        testUser.setDescription("测试用户");
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());

        Set<Role> roles = new HashSet<>();
        roles.add(testRole);
        testUser.setRoles(roles);
    }

    @Test
    void testSaveUser() {
        // 保存用户
        User savedUser = userDao.save(testUser);
        
        // 验证保存结果
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUserName()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getRoles()).hasSize(1);
    }

    @Test
    void testFindById() {
        // 保存用户
        User savedUser = userDao.save(testUser);
        
        // 根据ID查找用户
        Optional<User> foundUser = userDao.findById(savedUser.getId());
        
        // 验证查找结果
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserName()).isEqualTo("testuser");
        assertThat(foundUser.get().getRoles()).hasSize(1);
    }

    @Test
    void testFindAll() {
        // 保存多个用户
        userDao.save(testUser);
        
        User anotherUser = new User();
        anotherUser.setUserName("anotheruser");
        anotherUser.setPassword("password456");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPhoneNumber(13900139000L);
        anotherUser.setDescription("另一个测试用户");
        anotherUser.setCreateTime(LocalDateTime.now());
        anotherUser.setUpdateTime(LocalDateTime.now());
        userDao.save(anotherUser);
        
        // 查找所有用户
        List<User> allUsers = userDao.findAll();
        
        // 验证结果
        assertThat(allUsers).hasSize(2);
    }

    @Test
    void testFindAllWithPagination() {
        // 保存多个用户
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setUserName("user" + i);
            user.setPassword("password" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPhoneNumber(13800138000L + i);
            user.setDescription("用户" + i);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userDao.save(user);
        }
        
        // 分页查询
        Pageable pageable = PageRequest.of(0, 3, Sort.by("id"));
        Page<User> userPage = userDao.findAll(pageable);
        
        // 验证分页结果
        assertThat(userPage.getContent()).hasSize(3);
        assertThat(userPage.getTotalElements()).isEqualTo(5);
        assertThat(userPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testFindAllWithSpecification() {
        // 保存测试用户
        userDao.save(testUser);
        
        User adminUser = new User();
        adminUser.setUserName("admin");
        adminUser.setPassword("adminpass");
        adminUser.setEmail("admin@example.com");
        adminUser.setPhoneNumber(13700137000L);
        adminUser.setDescription("管理员用户");
        adminUser.setCreateTime(LocalDateTime.now());
        adminUser.setUpdateTime(LocalDateTime.now());
        userDao.save(adminUser);
        
        // 使用Specification查询
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.like(root.get("userName"), "%test%");
            return predicate;
        };
        
        List<User> users = userDao.findAll(spec);
        
        // 验证结果
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUserName()).isEqualTo("testuser");
    }

    @Test
    void testUpdateUser() {
        // 保存用户
        User savedUser = userDao.save(testUser);
        
        // 更新用户信息
        savedUser.setUserName("updateduser");
        savedUser.setEmail("updated@example.com");
        savedUser.setUpdateTime(LocalDateTime.now());
        
        User updatedUser = userDao.save(savedUser);
        
        // 验证更新结果
        assertThat(updatedUser.getUserName()).isEqualTo("updateduser");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void testDeleteUser() {
        // 保存用户
        User savedUser = userDao.save(testUser);
        Long userId = savedUser.getId();
        
        // 删除用户
        userDao.deleteById(userId);
        
        // 验证删除结果
        Optional<User> deletedUser = userDao.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void testExistsById() {
        // 保存用户
        User savedUser = userDao.save(testUser);
        
        // 检查用户是否存在
        boolean exists = userDao.existsById(savedUser.getId());
        assertThat(exists).isTrue();
        
        // 检查不存在的用户
        boolean notExists = userDao.existsById(999L);
        assertThat(notExists).isFalse();
    }

    @Test
    void testCount() {
        // 初始计数
        long initialCount = userDao.count();
        
        // 保存用户
        userDao.save(testUser);
        
        // 验证计数增加
        long newCount = userDao.count();
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    @Test
    void testFindAllWithSort() {
        // 保存多个用户
        User user1 = new User();
        user1.setUserName("zuser");
        user1.setPassword("password");
        user1.setEmail("z@example.com");
        user1.setPhoneNumber(13800138001L);
        user1.setDescription("Z用户");
        user1.setCreateTime(LocalDateTime.now());
        user1.setUpdateTime(LocalDateTime.now());
        userDao.save(user1);
        
        User user2 = new User();
        user2.setUserName("auser");
        user2.setPassword("password");
        user2.setEmail("a@example.com");
        user2.setPhoneNumber(13800138002L);
        user2.setDescription("A用户");
        user2.setCreateTime(LocalDateTime.now());
        user2.setUpdateTime(LocalDateTime.now());
        userDao.save(user2);
        
        // 按用户名排序查询
        List<User> sortedUsers = userDao.findAll(Sort.by("userName"));
        
        // 验证排序结果
        assertThat(sortedUsers).hasSize(2);
        assertThat(sortedUsers.get(0).getUserName()).isEqualTo("auser");
        assertThat(sortedUsers.get(1).getUserName()).isEqualTo("zuser");
    }
}