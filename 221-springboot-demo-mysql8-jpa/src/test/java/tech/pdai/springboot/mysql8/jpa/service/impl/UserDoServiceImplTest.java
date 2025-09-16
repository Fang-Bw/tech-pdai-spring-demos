package tech.pdai.springboot.mysql8.jpa.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import tech.pdai.springboot.mysql8.jpa.dao.IUserDao;
import tech.pdai.springboot.mysql8.jpa.entity.Role;
import tech.pdai.springboot.mysql8.jpa.entity.User;
import tech.pdai.springboot.mysql8.jpa.entity.query.UserQueryBean;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UserDoServiceImpl测试类
 * 
 * @author pdai
 */
@ExtendWith(MockitoExtension.class)
class UserDoServiceImplTest {

    @Mock
    private IUserDao userDao;

    @InjectMocks
    private UserDoServiceImpl userService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // 创建测试角色
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("TEST_ROLE");
        testRole.setRoleKey("test");
        testRole.setDescription("测试角色");
        testRole.setCreateTime(LocalDateTime.now());
        testRole.setUpdateTime(LocalDateTime.now());

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
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
    void testGetBaseDao() {
        // 测试getBaseDao方法
        assertThat(userService.getBaseDao()).isEqualTo(userDao);
    }

    @Test
    void testFindById() {
        // Mock DAO行为
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        // 调用服务方法
        User foundUser = userService.find(1L);

        // 验证结果
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(1L);
        assertThat(foundUser.getUserName()).isEqualTo("testuser");

        // 验证DAO方法被调用
        verify(userDao, times(1)).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        // Mock DAO行为 - 用户不存在
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        // 调用服务方法
        User foundUser = userService.find(999L);

        // 验证结果
        assertThat(foundUser).isNull();

        // 验证DAO方法被调用
        verify(userDao, times(1)).findById(999L);
    }

    @Test
    void testFindAll() {
        // 准备测试数据
        List<User> users = Arrays.asList(testUser);
        when(userDao.findAll()).thenReturn(users);

        // 调用服务方法
        List<User> foundUsers = userService.findAll();

        // 验证结果
        assertThat(foundUsers).hasSize(1);
        assertThat(foundUsers.get(0).getUserName()).isEqualTo("testuser");

        // 验证DAO方法被调用
        verify(userDao, times(1)).findAll();
    }

    @Test
    void testSaveUser() {
        // Mock DAO行为
        when(userDao.save(testUser)).thenReturn(testUser);

        // 调用服务方法
        userService.save(testUser);

        // 验证DAO方法被调用
        verify(userDao, times(1)).save(testUser);
    }

    @Test
    void testUpdateUser() {
        // Mock DAO行为
        when(userDao.save(testUser)).thenReturn(testUser);

        // 调用服务方法
        User updatedUser = userService.update(testUser);

        // 验证结果
        assertThat(updatedUser).isEqualTo(testUser);

        // 验证DAO方法被调用
        verify(userDao, times(1)).save(testUser);
    }

    @Test
    void testDeleteUser() {
        // 调用服务方法
        userService.delete(1L);

        // 验证DAO方法被调用
        verify(userDao, times(1)).deleteById(1L);
    }

    @Test
    void testExistsUser() {
        // Mock DAO行为
        when(userDao.existsById(1L)).thenReturn(true);

        // 调用服务方法
        boolean exists = userService.exists(1L);

        // 验证结果
        assertThat(exists).isTrue();

        // 验证DAO方法被调用
        verify(userDao, times(1)).existsById(1L);
    }

    @Test
    void testCountUsers() {
        // Mock DAO行为
        when(userDao.count()).thenReturn(5L);

        // 调用服务方法
        long count = userService.count();

        // 验证结果
        assertThat(count).isEqualTo(5L);

        // 验证DAO方法被调用
        verify(userDao, times(1)).count();
    }

    @Test
    void testFindPage() {
        // 准备测试数据
        UserQueryBean queryBean = UserQueryBean.builder()
                .name("test")
                .description("测试")
                .build();

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageRequest, 1);

        // Mock DAO行为
        when(userDao.findAll(any(Specification.class), eq(pageRequest))).thenReturn(userPage);

        // 调用服务方法
        Page<User> result = userService.findPage(queryBean, pageRequest);

        // 验证结果
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUserName()).isEqualTo("testuser");

        // 验证DAO方法被调用
        verify(userDao, times(1)).findAll(any(Specification.class), eq(pageRequest));
    }

    @Test
    void testFindPageWithEmptyQuery() {
        // 准备测试数据 - 空查询条件
        UserQueryBean queryBean = UserQueryBean.builder().build();
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageRequest, 1);

        // Mock DAO行为
        when(userDao.findAll(any(Specification.class), eq(pageRequest))).thenReturn(userPage);

        // 调用服务方法
        Page<User> result = userService.findPage(queryBean, pageRequest);

        // 验证结果
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);

        // 验证DAO方法被调用
        verify(userDao, times(1)).findAll(any(Specification.class), eq(pageRequest));
    }

    @Test
    void testSaveMultipleUsers() {
        // 准备测试数据
        User user2 = new User();
        user2.setId(2L);
        user2.setUserName("testuser2");
        user2.setPassword("password456");
        user2.setEmail("test2@example.com");
        user2.setPhoneNumber(13900139000L);
        user2.setDescription("测试用户2");
        user2.setCreateTime(LocalDateTime.now());
        user2.setUpdateTime(LocalDateTime.now());

        List<User> users = Arrays.asList(testUser, user2);

        // Mock DAO行为
        when(userDao.saveAll(users)).thenReturn(users);

        // 调用服务方法
        userService.save(users);

        // 验证DAO方法被调用
        verify(userDao, times(1)).saveAll(users);
    }

    @Test
    void testDeleteMultipleUsers() {
        // 准备测试数据
        List<Long> ids = Arrays.asList(1L, 2L);

        // 调用服务方法
        userService.deleteByIds(ids);

        // 验证DAO方法被调用
        verify(userDao, times(1)).deleteAllById(ids);
    }

    @Test
    void testFlush() {
        // 调用服务方法
        userService.flush();

        // 验证DAO方法被调用
        verify(userDao, times(1)).flush();
    }

    @Test
    void testFindListByIds() {
        // 准备测试数据
        List<Long> ids = Arrays.asList(1L, 2L);
        List<User> users = Arrays.asList(testUser);

        // Mock DAO行为
        when(userDao.findAllById(ids)).thenReturn(users);

        // 调用服务方法
        List<User> foundUsers = userService.findList(ids);

        // 验证结果
        assertThat(foundUsers).hasSize(1);
        assertThat(foundUsers.get(0).getUserName()).isEqualTo("testuser");

        // 验证DAO方法被调用
        verify(userDao, times(1)).findAllById(ids);
    }
}