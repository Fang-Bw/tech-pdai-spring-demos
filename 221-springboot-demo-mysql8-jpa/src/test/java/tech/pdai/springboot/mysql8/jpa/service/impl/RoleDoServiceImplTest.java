package tech.pdai.springboot.mysql8.jpa.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.pdai.springboot.mysql8.jpa.dao.IRoleDao;
import tech.pdai.springboot.mysql8.jpa.entity.Role;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * RoleDoServiceImpl测试类
 * 
 * @author pdai
 */
@ExtendWith(MockitoExtension.class)
class RoleDoServiceImplTest {

    @Mock
    private IRoleDao roleDao;

    @InjectMocks
    private RoleDoServiceImpl roleService;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("TEST_ROLE");
        testRole.setRoleKey("test");
        testRole.setDescription("测试角色");
        testRole.setCreateTime(LocalDateTime.now());
        testRole.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void testGetBaseDao() {
        // 测试getBaseDao方法
        assertThat(roleService.getBaseDao()).isEqualTo(roleDao);
    }

    @Test
    void testFindById() {
        // Mock DAO行为
        when(roleDao.findById(1L)).thenReturn(Optional.of(testRole));

        // 调用服务方法
        Role foundRole = roleService.find(1L);

        // 验证结果
        assertThat(foundRole).isNotNull();
        assertThat(foundRole.getId()).isEqualTo(1L);
        assertThat(foundRole.getName()).isEqualTo("TEST_ROLE");

        // 验证DAO方法被调用
        verify(roleDao, times(1)).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        // Mock DAO行为 - 角色不存在
        when(roleDao.findById(999L)).thenReturn(Optional.empty());

        // 调用服务方法
        Role foundRole = roleService.find(999L);

        // 验证结果
        assertThat(foundRole).isNull();

        // 验证DAO方法被调用
        verify(roleDao, times(1)).findById(999L);
    }

    @Test
    void testFindAll() {
        // 准备测试数据
        List<Role> roles = Arrays.asList(testRole);
        when(roleDao.findAll()).thenReturn(roles);

        // 调用服务方法
        List<Role> foundRoles = roleService.findAll();

        // 验证结果
        assertThat(foundRoles).hasSize(1);
        assertThat(foundRoles.get(0).getName()).isEqualTo("TEST_ROLE");

        // 验证DAO方法被调用
        verify(roleDao, times(1)).findAll();
    }

    @Test
    void testSaveRole() {
        // Mock DAO行为
        when(roleDao.save(testRole)).thenReturn(testRole);

        // 调用服务方法
        roleService.save(testRole);

        // 验证DAO方法被调用
        verify(roleDao, times(1)).save(testRole);
    }

    @Test
    void testUpdateRole() {
        // Mock DAO行为
        when(roleDao.save(testRole)).thenReturn(testRole);

        // 调用服务方法
        Role updatedRole = roleService.update(testRole);

        // 验证结果
        assertThat(updatedRole).isEqualTo(testRole);

        // 验证DAO方法被调用
        verify(roleDao, times(1)).save(testRole);
    }

    @Test
    void testDeleteRole() {
        // 调用服务方法
        roleService.delete(1L);

        // 验证DAO方法被调用
        verify(roleDao, times(1)).deleteById(1L);
    }

    @Test
    void testExistsRole() {
        // Mock DAO行为
        when(roleDao.existsById(1L)).thenReturn(true);

        // 调用服务方法
        boolean exists = roleService.exists(1L);

        // 验证结果
        assertThat(exists).isTrue();

        // 验证DAO方法被调用
        verify(roleDao, times(1)).existsById(1L);
    }

    @Test
    void testCountRoles() {
        // Mock DAO行为
        when(roleDao.count()).thenReturn(3L);

        // 调用服务方法
        long count = roleService.count();

        // 验证结果
        assertThat(count).isEqualTo(3L);

        // 验证DAO方法被调用
        verify(roleDao, times(1)).count();
    }

    @Test
    void testSaveMultipleRoles() {
        // 准备测试数据
        Role role2 = new Role();
        role2.setId(2L);
        role2.setName("ADMIN_ROLE");
        role2.setRoleKey("admin");
        role2.setDescription("管理员角色");
        role2.setCreateTime(LocalDateTime.now());
        role2.setUpdateTime(LocalDateTime.now());

        List<Role> roles = Arrays.asList(testRole, role2);

        // Mock DAO行为
        when(roleDao.saveAll(roles)).thenReturn(roles);

        // 调用服务方法
        roleService.save(roles);

        // 验证DAO方法被调用
        verify(roleDao, times(1)).saveAll(roles);
    }

    @Test
    void testDeleteMultipleRoles() {
        // 准备测试数据
        List<Long> ids = Arrays.asList(1L, 2L);

        // 调用服务方法
        roleService.deleteByIds(ids);

        // 验证DAO方法被调用
        verify(roleDao, times(1)).deleteAllById(ids);
    }

    @Test
    void testFlush() {
        // 调用服务方法
        roleService.flush();

        // 验证DAO方法被调用
        verify(roleDao, times(1)).flush();
    }

    @Test
    void testFindListByIds() {
        // 准备测试数据
        List<Long> ids = Arrays.asList(1L, 2L);
        List<Role> roles = Arrays.asList(testRole);

        // Mock DAO行为
        when(roleDao.findAllById(ids)).thenReturn(roles);

        // 调用服务方法
        List<Role> foundRoles = roleService.findList(ids);

        // 验证结果
        assertThat(foundRoles).hasSize(1);
        assertThat(foundRoles.get(0).getName()).isEqualTo("TEST_ROLE");

        // 验证DAO方法被调用
        verify(roleDao, times(1)).findAllById(ids);
    }

    @Test
    void testDeleteRoleEntity() {
        // 调用服务方法
        roleService.delete(testRole);

        // 验证DAO方法被调用
        verify(roleDao, times(1)).delete(testRole);
    }

    @Test
    void testDeleteAllRoles() {
        // 调用服务方法
        roleService.deleteAll();

        // 验证DAO方法被调用
        verify(roleDao, times(1)).deleteAll();
    }
}