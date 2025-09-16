package com.knoldus.transaction.repository;

import com.knoldus.transaction.entity.PaymentInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PaymentInfoRepository 单元测试
 */
@DataJpaTest
@Rollback
class PaymentInfoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentInfoRepository paymentInfoRepository;

    @Test
    void testSaveAndFindById() {
        // 创建测试数据
        PaymentInfo payment = new PaymentInfo();
        payment.setAccountNo("test_acc_001");
        payment.setAmount(1500.0);
        payment.setCardType("DEBIT");

        // 保存数据
        PaymentInfo saved = paymentInfoRepository.save(payment);
        entityManager.flush();

        // 验证保存结果
        assertNotNull(saved.getPaymentId());
        assertEquals("test_acc_001", saved.getAccountNo());
        assertEquals(1500.0, saved.getAmount());
        assertEquals("DEBIT", saved.getCardType());

        // 通过ID查找
        Optional<PaymentInfo> found = paymentInfoRepository.findById(saved.getPaymentId());
        assertTrue(found.isPresent());
        assertEquals("test_acc_001", found.get().getAccountNo());
        assertEquals(1500.0, found.get().getAmount());
    }

    @Test
    void testFindByIdNotFound() {
        // 查找不存在的ID
        Optional<PaymentInfo> notFound = paymentInfoRepository.findById("non_existent_id");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testSaveMultiplePayments() {
        // 保存多个支付记录
        PaymentInfo payment1 = new PaymentInfo();
        payment1.setAccountNo("acc_001");
        payment1.setAmount(800.0);
        payment1.setCardType("CREDIT");

        PaymentInfo payment2 = new PaymentInfo();
        payment2.setAccountNo("acc_002");
        payment2.setAmount(1200.0);
        payment2.setCardType("DEBIT");

        PaymentInfo saved1 = paymentInfoRepository.save(payment1);
        PaymentInfo saved2 = paymentInfoRepository.save(payment2);
        entityManager.flush();

        // 验证两条记录都保存成功
        assertNotNull(saved1.getPaymentId());
        assertNotNull(saved2.getPaymentId());
        assertNotEquals(saved1.getPaymentId(), saved2.getPaymentId());

        // 验证可以分别查找到
        assertTrue(paymentInfoRepository.findById(saved1.getPaymentId()).isPresent());
        assertTrue(paymentInfoRepository.findById(saved2.getPaymentId()).isPresent());
    }

    @Test
    void testCount() {
        // 初始计数
        long initialCount = paymentInfoRepository.count();

        // 添加一条记录
        PaymentInfo payment = new PaymentInfo();
        payment.setAccountNo("count_test_acc");
        payment.setAmount(600.0);
        payment.setCardType("CREDIT");

        paymentInfoRepository.save(payment);
        entityManager.flush();

        // 验证计数增加
        assertEquals(initialCount + 1, paymentInfoRepository.count());
    }

    @Test
    void testDeleteById() {
        // 先保存一条记录
        PaymentInfo payment = new PaymentInfo();
        payment.setAccountNo("delete_test_acc");
        payment.setAmount(900.0);
        payment.setCardType("DEBIT");

        PaymentInfo saved = paymentInfoRepository.save(payment);
        entityManager.flush();
        String savedId = saved.getPaymentId();

        // 验证记录存在
        assertTrue(paymentInfoRepository.findById(savedId).isPresent());

        // 删除记录
        paymentInfoRepository.deleteById(savedId);
        entityManager.flush();

        // 验证记录已删除
        assertFalse(paymentInfoRepository.findById(savedId).isPresent());
    }

    @Test
    void testUpdatePaymentInfo() {
        // 保存初始记录
        PaymentInfo payment = new PaymentInfo();
        payment.setAccountNo("update_test_acc");
        payment.setAmount(1000.0);
        payment.setCardType("CREDIT");

        PaymentInfo saved = paymentInfoRepository.save(payment);
        entityManager.flush();
        entityManager.clear(); // 清除持久化上下文

        // 更新记录
        Optional<PaymentInfo> found = paymentInfoRepository.findById(saved.getPaymentId());
        assertTrue(found.isPresent());
        
        PaymentInfo toUpdate = found.get();
        toUpdate.setAmount(1500.0);
        toUpdate.setCardType("DEBIT");
        
        PaymentInfo updated = paymentInfoRepository.save(toUpdate);
        entityManager.flush();

        // 验证更新结果
        assertEquals(saved.getPaymentId(), updated.getPaymentId());
        assertEquals(1500.0, updated.getAmount());
        assertEquals("DEBIT", updated.getCardType());
        assertEquals("update_test_acc", updated.getAccountNo()); // 账号不变
    }
}