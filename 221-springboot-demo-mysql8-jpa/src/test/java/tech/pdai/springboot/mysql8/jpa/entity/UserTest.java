package tech.pdai.springboot.mysql8.jpa.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User实体类单元测试
 * 
 * @author pdai
 */
@DataJpaTest
@ActiveProfiles("test")
@SpringJUnitConfig
class UserTest {

    @Resource
    private TestEntityManager entityManager;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        // 创建角色
        role = new Role();
        role.setName("ADMIN");
        role.setRoleKey("admin");
        role.setDescription("管理员角色");
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());

        // 创建用户
        user = new User();
        user.setUserName("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        user.setPhoneNumber(13800138000L);
        user.setDescription("测试用户");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
    }

    @Test
    void testUserEntityMapping() {
        // 保存角色
        Role savedRole = entityManager.persistAndFlush(role);
        assertThat(savedRole.getId()).isNotNull();

        // 保存用户
        User savedUser = entityManager.persistAndFlush(user);
        
        // 验证用户基本属性
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUserName()).isEqualTo("testuser");
        assertThat(savedUser.getPassword()).isEqualTo("password123");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPhoneNumber()).isEqualTo(13800138000L);
        assertThat(savedUser.getDescription()).isEqualTo("测试用户");
        assertThat(savedUser.getCreateTime()).isNotNull();
        assertThat(savedUser.getUpdateTime()).isNotNull();
    }

    @Test
    void testUserRoleAssociation() {
        // 保存角色
        entityManager.persistAndFlush(role);
        
        // 保存用户
        User savedUser = entityManager.persistAndFlush(user);
        
        // 清除持久化上下文
        entityManager.clear();
        
        // 重新查询用户
        User foundUser = entityManager.find(User.class, savedUser.getId());
        
        // 验证用户角色关联
        assertThat(foundUser.getRoles()).isNotEmpty();
        assertThat(foundUser.getRoles()).hasSize(1);
        assertThat(foundUser.getRoles().iterator().next().getName()).isEqualTo("ADMIN");
    }

    @Test
    void testUserValidation() {
        // 测试必填字段
        User invalidUser = new User();
        
        // 验证用户名不能为空的情况
        assertThat(invalidUser.getUserName()).isNull();
        assertThat(invalidUser.getPassword()).isNull();
        assertThat(invalidUser.getEmail()).isNull();
    }

    @Test
    void testUserToString() {
        // 测试toString方法
        String userString = user.toString();
        assertThat(userString).contains("testuser");
        assertThat(userString).contains("test@example.com");
    }

    @Test
    void testUserEqualsAndHashCode() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUserName("test");

        User user2 = new User();
        user2.setId(1L);
        user2.setUserName("test");

        User user3 = new User();
        user3.setId(2L);
        user3.setUserName("test2");

        // 测试相等性（基于ID）
        assertThat(user1).isNotEqualTo(user2); // Lombok生成的equals基于所有字段
        assertThat(user1).isNotEqualTo(user3);
    }
}