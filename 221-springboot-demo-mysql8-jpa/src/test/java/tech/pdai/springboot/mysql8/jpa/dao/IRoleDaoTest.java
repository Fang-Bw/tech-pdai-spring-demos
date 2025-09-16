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

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IRoleDao测试类
 * 
 * @author pdai
 */
@DataJpaTest
@ActiveProfiles("test")
@SpringJUnitConfig
class IRoleDaoTest {

    @Resource
    private IRoleDao roleDao;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setName("TEST_ROLE");
        testRole.setRoleKey("test");
        testRole.setDescription("测试角色");
        testRole.setCreateTime(LocalDateTime.now());
        testRole.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void testSaveRole() {
        // 保存角色
        Role savedRole = roleDao.save(testRole);
        
        // 验证保存结果
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo("TEST_ROLE");
        assertThat(savedRole.getRoleKey()).isEqualTo("test");
        assertThat(savedRole.getDescription()).isEqualTo("测试角色");
    }

    @Test
    void testFindById() {
        // 保存角色
        Role savedRole = roleDao.save(testRole);
        
        // 根据ID查找角色
        Optional<Role> foundRole = roleDao.findById(savedRole.getId());
        
        // 验证查找结果
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("TEST_ROLE");
        assertThat(foundRole.get().getRoleKey()).isEqualTo("test");
    }

    @Test
    void testFindAll() {
        // 保存多个角色
        roleDao.save(testRole);
        
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setRoleKey("admin");
        adminRole.setDescription("管理员角色");
        adminRole.setCreateTime(LocalDateTime.now());
        adminRole.setUpdateTime(LocalDateTime.now());
        roleDao.save(adminRole);
        
        // 查找所有角色
        List<Role> allRoles = roleDao.findAll();
        
        // 验证结果
        assertThat(allRoles).hasSize(2);
    }

    @Test
    void testFindAllWithPagination() {
        // 保存多个角色
        for (int i = 0; i < 5; i++) {
            Role role = new Role();
            role.setName("ROLE_" + i);
            role.setRoleKey("role" + i);
            role.setDescription("角色" + i);
            role.setCreateTime(LocalDateTime.now());
            role.setUpdateTime(LocalDateTime.now());
            roleDao.save(role);
        }
        
        // 分页查询
        Pageable pageable = PageRequest.of(0, 3, Sort.by("id"));
        Page<Role> rolePage = roleDao.findAll(pageable);
        
        // 验证分页结果
        assertThat(rolePage.getContent()).hasSize(3);
        assertThat(rolePage.getTotalElements()).isEqualTo(5);
        assertThat(rolePage.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testFindAllWithSpecification() {
        // 保存测试角色
        roleDao.save(testRole);
        
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setRoleKey("admin");
        adminRole.setDescription("管理员角色");
        adminRole.setCreateTime(LocalDateTime.now());
        adminRole.setUpdateTime(LocalDateTime.now());
        roleDao.save(adminRole);
        
        // 使用Specification查询
        Specification<Role> spec = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.like(root.get("name"), "%TEST%");
            return predicate;
        };
        
        List<Role> roles = roleDao.findAll(spec);
        
        // 验证结果
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getName()).isEqualTo("TEST_ROLE");
    }

    @Test
    void testUpdateRole() {
        // 保存角色
        Role savedRole = roleDao.save(testRole);
        
        // 更新角色信息
        savedRole.setName("UPDATED_ROLE");
        savedRole.setDescription("更新后的角色");
        savedRole.setUpdateTime(LocalDateTime.now());
        
        Role updatedRole = roleDao.save(savedRole);
        
        // 验证更新结果
        assertThat(updatedRole.getName()).isEqualTo("UPDATED_ROLE");
        assertThat(updatedRole.getDescription()).isEqualTo("更新后的角色");
    }

    @Test
    void testDeleteRole() {
        // 保存角色
        Role savedRole = roleDao.save(testRole);
        Long roleId = savedRole.getId();
        
        // 删除角色
        roleDao.deleteById(roleId);
        
        // 验证删除结果
        Optional<Role> deletedRole = roleDao.findById(roleId);
        assertThat(deletedRole).isEmpty();
    }

    @Test
    void testExistsById() {
        // 保存角色
        Role savedRole = roleDao.save(testRole);
        
        // 检查角色是否存在
        boolean exists = roleDao.existsById(savedRole.getId());
        assertThat(exists).isTrue();
        
        // 检查不存在的角色
        boolean notExists = roleDao.existsById(999L);
        assertThat(notExists).isFalse();
    }

    @Test
    void testCount() {
        // 初始计数
        long initialCount = roleDao.count();
        
        // 保存角色
        roleDao.save(testRole);
        
        // 验证计数增加
        long newCount = roleDao.count();
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    @Test
    void testFindAllWithSort() {
        // 保存多个角色
        Role roleZ = new Role();
        roleZ.setName("Z_ROLE");
        roleZ.setRoleKey("zrole");
        roleZ.setDescription("Z角色");
        roleZ.setCreateTime(LocalDateTime.now());
        roleZ.setUpdateTime(LocalDateTime.now());
        roleDao.save(roleZ);
        
        Role roleA = new Role();
        roleA.setName("A_ROLE");
        roleA.setRoleKey("arole");
        roleA.setDescription("A角色");
        roleA.setCreateTime(LocalDateTime.now());
        roleA.setUpdateTime(LocalDateTime.now());
        roleDao.save(roleA);
        
        // 按角色名排序查询
        List<Role> sortedRoles = roleDao.findAll(Sort.by("name"));
        
        // 验证排序结果
        assertThat(sortedRoles).hasSize(2);
        assertThat(sortedRoles.get(0).getName()).isEqualTo("A_ROLE");
        assertThat(sortedRoles.get(1).getName()).isEqualTo("Z_ROLE");
    }

    @Test
    void testFindByRoleKey() {
        // 保存角色
        roleDao.save(testRole);
        
        // 使用Specification根据roleKey查询
        Specification<Role> spec = (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("roleKey"), "test");
        };
        
        List<Role> roles = roleDao.findAll(spec);
        
        // 验证结果
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getRoleKey()).isEqualTo("test");
    }

    @Test
    void testBatchSave() {
        // 批量保存角色
        Role role1 = new Role();
        role1.setName("BATCH_ROLE_1");
        role1.setRoleKey("batch1");
        role1.setDescription("批量角色1");
        role1.setCreateTime(LocalDateTime.now());
        role1.setUpdateTime(LocalDateTime.now());
        
        Role role2 = new Role();
        role2.setName("BATCH_ROLE_2");
        role2.setRoleKey("batch2");
        role2.setDescription("批量角色2");
        role2.setCreateTime(LocalDateTime.now());
        role2.setUpdateTime(LocalDateTime.now());
        
        List<Role> rolesToSave = Arrays.asList(role1, role2);
        List<Role> savedRoles = roleDao.saveAll(rolesToSave);
        
        // 验证批量保存结果
        assertThat(savedRoles).hasSize(2);
        assertThat(savedRoles.get(0).getId()).isNotNull();
        assertThat(savedRoles.get(1).getId()).isNotNull();
    }
}