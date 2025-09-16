package tech.pdai.springboot.mysql8.jpa.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Role实体类单元测试
 * 
 * @author pdai
 */
@DataJpaTest
@ActiveProfiles("test")
@SpringJUnitConfig
class RoleTest {

    @Resource
    private TestEntityManager entityManager;

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setName("USER");
        role.setRoleKey("user");
        role.setDescription("普通用户角色");
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void testRoleEntityMapping() {
        // 保存角色
        Role savedRole = entityManager.persistAndFlush(role);
        
        // 验证角色基本属性
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo("USER");
        assertThat(savedRole.getRoleKey()).isEqualTo("user");
        assertThat(savedRole.getDescription()).isEqualTo("普通用户角色");
        assertThat(savedRole.getCreateTime()).isNotNull();
        assertThat(savedRole.getUpdateTime()).isNotNull();
    }

    @Test
    void testRoleValidation() {
        // 测试必填字段
        Role invalidRole = new Role();
        
        // 验证角色名不能为空的情况
        assertThat(invalidRole.getName()).isNull();
        assertThat(invalidRole.getRoleKey()).isNull();
    }

    @Test
    void testRoleToString() {
        // 测试toString方法
        String roleString = role.toString();
        assertThat(roleString).contains("USER");
        assertThat(roleString).contains("user");
        assertThat(roleString).contains("普通用户角色");
    }

    @Test
    void testRoleUpdate() {
        // 保存角色
        Role savedRole = entityManager.persistAndFlush(role);
        Long roleId = savedRole.getId();
        
        // 更新角色信息
        savedRole.setName("UPDATED_USER");
        savedRole.setDescription("更新后的用户角色");
        savedRole.setUpdateTime(LocalDateTime.now());
        
        Role updatedRole = entityManager.persistAndFlush(savedRole);
        
        // 验证更新结果
        assertThat(updatedRole.getId()).isEqualTo(roleId);
        assertThat(updatedRole.getName()).isEqualTo("UPDATED_USER");
        assertThat(updatedRole.getDescription()).isEqualTo("更新后的用户角色");
    }

    @Test
    void testRoleFind() {
        // 保存角色
        Role savedRole = entityManager.persistAndFlush(role);
        Long roleId = savedRole.getId();
        
        // 清除持久化上下文
        entityManager.clear();
        
        // 重新查询角色
        Role foundRole = entityManager.find(Role.class, roleId);
        
        // 验证查询结果
        assertThat(foundRole).isNotNull();
        assertThat(foundRole.getId()).isEqualTo(roleId);
        assertThat(foundRole.getName()).isEqualTo("USER");
        assertThat(foundRole.getRoleKey()).isEqualTo("user");
    }
}