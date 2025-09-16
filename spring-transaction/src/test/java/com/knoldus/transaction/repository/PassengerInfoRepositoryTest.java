package com.knoldus.transaction.repository;

import com.knoldus.transaction.entity.PassengerInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PassengerInfoRepository 单元测试
 */
@DataJpaTest
@Rollback
class PassengerInfoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PassengerInfoRepository passengerInfoRepository;

    @Test
    void testSaveAndFindById() {
        // 创建测试数据
        PassengerInfo passenger = new PassengerInfo();
        passenger.setName("李四");
        passenger.setEmail("lisi@example.com");
        passenger.setSource("广州");
        passenger.setDestination("深圳");
        passenger.setTravelDate(new Date());
        passenger.setPickupTime("09:00");
        passenger.setArrivalTime("11:00");
        passenger.setFare(500.0);

        // 保存数据
        PassengerInfo saved = passengerInfoRepository.save(passenger);
        entityManager.flush();

        // 验证保存结果
        assertNotNull(saved.getPId());
        assertEquals("李四", saved.getName());
        assertEquals("lisi@example.com", saved.getEmail());

        // 通过ID查找
        Optional<PassengerInfo> found = passengerInfoRepository.findById(saved.getPId());
        assertTrue(found.isPresent());
        assertEquals("李四", found.get().getName());
        assertEquals("广州", found.get().getSource());
        assertEquals("深圳", found.get().getDestination());
    }

    @Test
    void testFindByIdNotFound() {
        // 查找不存在的ID
        Optional<PassengerInfo> notFound = passengerInfoRepository.findById(999L);
        assertFalse(notFound.isPresent());
    }

    @Test
    void testCount() {
        // 初始计数
        long initialCount = passengerInfoRepository.count();

        // 添加一条记录
        PassengerInfo passenger = new PassengerInfo();
        passenger.setName("王五");
        passenger.setEmail("wangwu@example.com");
        passenger.setSource("杭州");
        passenger.setDestination("苏州");
        passenger.setTravelDate(new Date());
        passenger.setPickupTime("14:00");
        passenger.setArrivalTime("16:30");
        passenger.setFare(300.0);

        passengerInfoRepository.save(passenger);
        entityManager.flush();

        // 验证计数增加
        assertEquals(initialCount + 1, passengerInfoRepository.count());
    }

    @Test
    void testDeleteById() {
        // 先保存一条记录
        PassengerInfo passenger = new PassengerInfo();
        passenger.setName("赵六");
        passenger.setEmail("zhaoliu@example.com");
        passenger.setSource("成都");
        passenger.setDestination("重庆");
        passenger.setTravelDate(new Date());
        passenger.setPickupTime("16:00");
        passenger.setArrivalTime("18:00");
        passenger.setFare(400.0);

        PassengerInfo saved = passengerInfoRepository.save(passenger);
        entityManager.flush();
        Long savedId = saved.getPId();

        // 验证记录存在
        assertTrue(passengerInfoRepository.findById(savedId).isPresent());

        // 删除记录
        passengerInfoRepository.deleteById(savedId);
        entityManager.flush();

        // 验证记录已删除
        assertFalse(passengerInfoRepository.findById(savedId).isPresent());
    }
}