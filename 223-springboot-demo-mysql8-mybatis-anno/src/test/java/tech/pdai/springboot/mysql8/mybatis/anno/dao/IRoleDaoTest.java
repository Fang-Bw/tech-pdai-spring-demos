package tech.pdai.springboot.mysql8.mybatis.anno.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tech.pdai.springboot.mysql8.mybatis.anno.entity.Role;
import tech.pdai.springboot.mysql8.mybatis.anno.entity.query.RoleQueryBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RoleDao测试类
 * 
 * @author pdai
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IRoleDaoTest {

    @Autowired
    private IRoleDao roleDao;

    @Test
    void testFindList() {
        // 测试查询角色列表
        RoleQueryBean queryBean = new RoleQueryBean();
        List<Role> roles = roleDao.findList(queryBean);
        
        assertNotNull(roles);
        assertTrue(roles.size() >= 2); // 至少有测试数据中的2个角色
        
        // 验证角色数据
        Role adminRole = roles.stream()
                .filter(r -> "admin".equals(r.getName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(adminRole);
        assertEquals("admin", adminRole.getName());
        assertEquals("admin", adminRole.getRoleKey());
        assertEquals("admin", adminRole.getDescription());
        
        Role userRole = roles.stream()
                .filter(r -> "user".equals(r.getName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(userRole);
        assertEquals("user", userRole.getName());
        assertEquals("user", userRole.getRoleKey());
        assertEquals("普通用户", userRole.getDescription());
    }

    @Test
    void testFindRoleByUserId() {
        // 测试根据用户ID查找角色
        List<Role> roles = roleDao.findRoleByUserId(1L);
        
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        
        // 用户ID为1的用户应该有admin角色
        Role adminRole = roles.stream()
                .filter(r -> "admin".equals(r.getName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(adminRole);
        assertEquals("admin", adminRole.getName());
        assertEquals("admin", adminRole.getRoleKey());
    }

    @Test
    void testFindRoleByUserIdForUser() {
        // 测试根据用户ID查找角色 - 普通用户
        List<Role> roles = roleDao.findRoleByUserId(2L);
        
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        
        // 用户ID为2的用户应该有user角色
        Role userRole = roles.stream()
                .filter(r -> "user".equals(r.getName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(userRole);
        assertEquals("user", userRole.getName());
        assertEquals("user", userRole.getRoleKey());
        assertEquals("普通用户", userRole.getDescription());
    }

    @Test
    void testFindRoleByUserIdNotExists() {
        // 测试查找不存在用户的角色
        List<Role> roles = roleDao.findRoleByUserId(999L);
        
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }
}