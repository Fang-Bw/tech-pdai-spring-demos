package com.knoldus.transaction.service;

import com.knoldus.transaction.dto.FlightBookingRequest;
import com.knoldus.transaction.entity.PassengerInfo;
import com.knoldus.transaction.entity.PaymentInfo;
import com.knoldus.transaction.exception.InsufficientAmountException;
import com.knoldus.transaction.repository.PassengerInfoRepository;
import com.knoldus.transaction.repository.PaymentInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 专门测试Spring事务回滚机制的测试类
 */
@SpringBootTest
class TransactionRollbackTest {

    @Autowired
    private FlightBookingService flightBookingService;

    @Autowired
    private PassengerInfoRepository passengerInfoRepository;

    @Autowired
    private PaymentInfoRepository paymentInfoRepository;

    private long initialPassengerCount;
    private long initialPaymentCount;

    @BeforeEach
    void setUp() {
        // 记录测试前的数据库状态
        initialPassengerCount = passengerInfoRepository.count();
        initialPaymentCount = paymentInfoRepository.count();
    }

    /**
     * 测试当支付验证失败时，整个事务应该回滚
     * 乘客信息不应该被保存到数据库中
     */
    @Test
    void testTransactionRollbackOnPaymentFailure() {
        // 创建一个会导致支付失败的请求（金额超过账户限额）
        FlightBookingRequest request = createBookingRequest("acc1", 15000.0);

        // 验证抛出异常
        assertThrows(InsufficientAmountException.class, () -> {
            flightBookingService.bookFlightTicket(request);
        });

        // 验证事务回滚 - 数据库中的记录数应该没有变化
        assertEquals(initialPassengerCount, passengerInfoRepository.count());
        assertEquals(initialPaymentCount, paymentInfoRepository.count());
    }

    /**
     * 测试多次失败操作不会影响数据库状态
     */
    @Test
    void testMultipleFailedTransactions() {
        // 执行多次失败的预订操作
        for (int i = 0; i < 3; i++) {
            FlightBookingRequest request = createBookingRequest("acc" + (i + 1), 20000.0);
            assertThrows(InsufficientAmountException.class, () -> {
                flightBookingService.bookFlightTicket(request);
            });
        }

        // 验证数据库状态没有变化
        assertEquals(initialPassengerCount, passengerInfoRepository.count());
        assertEquals(initialPaymentCount, paymentInfoRepository.count());
    }

    /**
     * 测试成功和失败操作混合的情况
     */
    @Test
    void testMixedSuccessAndFailureTransactions() {
        // 先执行一个成功的操作
        FlightBookingRequest successRequest = createBookingRequest("acc1", 5000.0);
        assertDoesNotThrow(() -> {
            flightBookingService.bookFlightTicket(successRequest);
        });

        // 验证成功操作增加了记录
        assertEquals(initialPassengerCount + 1, passengerInfoRepository.count());
        assertEquals(initialPaymentCount + 1, paymentInfoRepository.count());

        // 再执行一个失败的操作
        FlightBookingRequest failRequest = createBookingRequest("acc2", 15000.0);
        assertThrows(InsufficientAmountException.class, () -> {
            flightBookingService.bookFlightTicket(failRequest);
        });

        // 验证失败操作没有增加记录，但成功操作的记录仍然存在
        assertEquals(initialPassengerCount + 1, passengerInfoRepository.count());
        assertEquals(initialPaymentCount + 1, paymentInfoRepository.count());
    }

    /**
     * 测试边界情况的事务处理
     */
    @Test
    void testBoundaryTransactionScenarios() {
        // 测试刚好等于限额的情况（应该成功）
        FlightBookingRequest exactLimitRequest = createBookingRequest("acc3", 5000.0);
        assertDoesNotThrow(() -> {
            flightBookingService.bookFlightTicket(exactLimitRequest);
        });
        assertEquals(initialPassengerCount + 1, passengerInfoRepository.count());

        // 测试超出限额1分钱的情况（应该失败并回滚）
        FlightBookingRequest overLimitRequest = createBookingRequest("acc4", 8000.01);
        assertThrows(InsufficientAmountException.class, () -> {
            flightBookingService.bookFlightTicket(overLimitRequest);
        });
        
        // 验证失败的操作没有增加记录
        assertEquals(initialPassengerCount + 1, passengerInfoRepository.count());
        assertEquals(initialPaymentCount + 1, paymentInfoRepository.count());
    }

    /**
     * 测试零金额和负金额的事务处理
     */
    @Test
    @Transactional
    @Rollback
    void testZeroAndNegativeAmountTransactions() {
        // 测试零金额（应该成功）
        FlightBookingRequest zeroAmountRequest = createBookingRequest("acc1", 0.0);
        assertDoesNotThrow(() -> {
            flightBookingService.bookFlightTicket(zeroAmountRequest);
        });
        assertEquals(initialPassengerCount + 1, passengerInfoRepository.count());

        // 测试负金额（应该成功，可能代表退款场景）
        FlightBookingRequest negativeAmountRequest = createBookingRequest("acc2", -100.0);
        assertDoesNotThrow(() -> {
            flightBookingService.bookFlightTicket(negativeAmountRequest);
        });
        assertEquals(initialPassengerCount + 2, passengerInfoRepository.count());
    }

    /**
     * 创建预订请求的辅助方法
     */
    private FlightBookingRequest createBookingRequest(String accountNo, double fare) {
        FlightBookingRequest request = new FlightBookingRequest();

        // 设置乘客信息
        PassengerInfo passengerInfo = new PassengerInfo();
        passengerInfo.setName("测试乘客_" + accountNo);
        passengerInfo.setEmail(accountNo + "@test.com");
        passengerInfo.setSource("测试出发地");
        passengerInfo.setDestination("测试目的地");
        passengerInfo.setTravelDate(new Date());
        passengerInfo.setPickupTime("10:00");
        passengerInfo.setArrivalTime("12:00");
        passengerInfo.setFare(fare);
        request.setPassengerInfo(passengerInfo);

        // 设置支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setAccountNo(accountNo);
        paymentInfo.setAmount(fare);
        paymentInfo.setCardType("CREDIT");
        request.setPaymentInfo(paymentInfo);

        return request;
    }
}