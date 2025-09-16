package com.knoldus.transaction.utils;

import com.knoldus.transaction.exception.InsufficientAmountException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PaymentUtils 单元测试
 */
class PaymentUtilsTest {

    @Test
    void testValidateCreditLimitSuccess() {
        // 测试所有有效的账户和金额组合
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc1", 12000.0));
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc1", 5000.0));
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc2", 10000.0));
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc3", 5000.0));
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc4", 8000.0));
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc5", 7000.0));
    }

    @ParameterizedTest
    @CsvSource({
        "acc1, 12001.0",  // 超过acc1的12000限额
        "acc2, 10001.0",  // 超过acc2的10000限额
        "acc3, 5001.0",   // 超过acc3的5000限额
        "acc4, 8001.0",   // 超过acc4的8000限额
        "acc5, 7001.0"    // 超过acc5的7000限额
    })
    void testValidateCreditLimitExceedsLimit(String accountNo, double amount) {
        InsufficientAmountException exception = assertThrows(
            InsufficientAmountException.class,
            () -> PaymentUtils.validateCreditLimit(accountNo, amount)
        );
        assertEquals("insufficient fund..!", exception.getMessage());
    }

    @Test
    void testValidateCreditLimitExactLimit() {
        // 测试刚好等于限额的情况
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc1", 12000.0));
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc2", 10000.0));
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc3", 5000.0));
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc4", 8000.0));
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc5", 7000.0));
    }

    @Test
    void testValidateCreditLimitUnknownAccount() {
        // 测试未知账户
        InsufficientAmountException exception = assertThrows(
            InsufficientAmountException.class,
            () -> PaymentUtils.validateCreditLimit("unknown_acc", 1000.0)
        );
        assertEquals("insufficient fund..!", exception.getMessage());
    }

    @Test
    void testValidateCreditLimitNullAccount() {
        // 测试null账户
        InsufficientAmountException exception = assertThrows(
            InsufficientAmountException.class,
            () -> PaymentUtils.validateCreditLimit(null, 1000.0)
        );
        assertEquals("insufficient fund..!", exception.getMessage());
    }

    @Test
    void testValidateCreditLimitEmptyAccount() {
        // 测试空字符串账户
        InsufficientAmountException exception = assertThrows(
            InsufficientAmountException.class,
            () -> PaymentUtils.validateCreditLimit("", 1000.0)
        );
        assertEquals("insufficient fund..!", exception.getMessage());
    }

    @Test
    void testValidateCreditLimitZeroAmount() {
        // 测试零金额
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc1", 0.0));
    }

    @Test
    void testValidateCreditLimitNegativeAmount() {
        // 测试负金额 - 应该通过验证（业务逻辑可能允许退款等场景）
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc1", -100.0));
    }

    @ParameterizedTest
    @CsvSource({
        "acc1, 1.0",
        "acc1, 100.0",
        "acc1, 1000.0",
        "acc2, 500.0",
        "acc3, 2500.0",
        "acc4, 4000.0",
        "acc5, 3500.0"
    })
    void testValidateCreditLimitVariousValidAmounts(String accountNo, double amount) {
        // 测试各种有效金额
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit(accountNo, amount));
    }

    @Test
    void testAccountLimitsConsistency() {
        // 验证账户限额的一致性
        // 这个测试确保PaymentUtils中的限额设置是我们期望的值
        
        // acc1: 12000 - 测试边界
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc1", 11999.99));
        assertThrows(InsufficientAmountException.class, 
            () -> PaymentUtils.validateCreditLimit("acc1", 12000.01));
        
        // acc2: 10000 - 测试边界
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc2", 9999.99));
        assertThrows(InsufficientAmountException.class, 
            () -> PaymentUtils.validateCreditLimit("acc2", 10000.01));
        
        // acc3: 5000 - 测试边界
        assertDoesNotThrow(() -> PaymentUtils.validateCreditLimit("acc3", 4999.99));
        assertThrows(InsufficientAmountException.class, 
            () -> PaymentUtils.validateCreditLimit("acc3", 5000.01));
    }
}