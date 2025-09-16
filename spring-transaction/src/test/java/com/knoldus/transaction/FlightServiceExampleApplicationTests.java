package com.knoldus.transaction;

import com.knoldus.transaction.dto.FlightBookingAcknowledgement;
import com.knoldus.transaction.dto.FlightBookingRequest;
import com.knoldus.transaction.entity.PassengerInfo;
import com.knoldus.transaction.entity.PaymentInfo;
import com.knoldus.transaction.exception.InsufficientAmountException;
import com.knoldus.transaction.repository.PassengerInfoRepository;
import com.knoldus.transaction.repository.PaymentInfoRepository;
import com.knoldus.transaction.service.FlightBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FlightServiceExampleApplicationTests {

	@Autowired
	private FlightBookingService flightBookingService;
	
	@Autowired
	private PassengerInfoRepository passengerInfoRepository;
	
	@Autowired
	private PaymentInfoRepository paymentInfoRepository;

	@Test
	void contextLoads() {
		// 验证Spring上下文正常加载
		assertNotNull(flightBookingService);
		assertNotNull(passengerInfoRepository);
		assertNotNull(paymentInfoRepository);
	}

	/**
	 * 测试正常的航班预订流程
	 */
	@Test
	@Transactional
	@Rollback
	void testSuccessfulFlightBooking() {
		// 准备测试数据
		FlightBookingRequest request = createValidBookingRequest("acc1", 1000.0);
		
		// 执行预订
		FlightBookingAcknowledgement result = flightBookingService.bookFlightTicket(request);
		
		// 验证结果
		assertNotNull(result);
		assertEquals("SUCCESS", result.getStatus());
		assertEquals(1000.0, result.getTotalFare());
		assertNotNull(result.getPnrNo());
		assertNotNull(result.getPassengerInfo());
		
		// 验证数据库中的数据
		Optional<PassengerInfo> savedPassenger = passengerInfoRepository.findById(result.getPassengerInfo().getPId());
		assertTrue(savedPassenger.isPresent());
		assertEquals("张三", savedPassenger.get().getName());
		assertEquals("test@example.com", savedPassenger.get().getEmail());
	}

	/**
	 * 测试余额不足的异常场景
	 */
	@Test
	@Transactional
	@Rollback
	void testInsufficientFundsException() {
		// 准备测试数据 - 使用acc1账户（余额12000），但尝试支付15000
		FlightBookingRequest request = createValidBookingRequest("acc1", 15000.0);
		
		// 验证抛出异常
		InsufficientAmountException exception = assertThrows(
			InsufficientAmountException.class,
			() -> flightBookingService.bookFlightTicket(request)
		);
		
		assertEquals("insufficient fund..!", exception.getMessage());
		
		// 验证事务回滚 - 乘客信息不应该被保存
		long passengerCount = passengerInfoRepository.count();
		long paymentCount = paymentInfoRepository.count();
		// 由于事务回滚，数据不应该被保存
	}

	/**
	 * 测试不同账户的余额验证
	 */
	@Test
	@Transactional
	@Rollback
	void testDifferentAccountBalances() {
		// 测试acc2账户（余额10000）
		FlightBookingRequest request1 = createValidBookingRequest("acc2", 8000.0);
		FlightBookingAcknowledgement result1 = flightBookingService.bookFlightTicket(request1);
		assertEquals("SUCCESS", result1.getStatus());
		
		// 测试acc3账户（余额5000）
		FlightBookingRequest request2 = createValidBookingRequest("acc3", 4000.0);
		FlightBookingAcknowledgement result2 = flightBookingService.bookFlightTicket(request2);
		assertEquals("SUCCESS", result2.getStatus());
		
		// 测试acc4账户（余额8000）超额支付
		FlightBookingRequest request3 = createValidBookingRequest("acc4", 10000.0);
		assertThrows(
			InsufficientAmountException.class,
			() -> flightBookingService.bookFlightTicket(request3)
		);
	}

	/**
	 * 测试边界条件 - 刚好等于余额的支付
	 */
	@Test
	@Transactional
	@Rollback
	void testExactBalancePayment() {
		// 使用acc3账户（余额5000），支付刚好5000
		FlightBookingRequest request = createValidBookingRequest("acc3", 5000.0);
		
		FlightBookingAcknowledgement result = flightBookingService.bookFlightTicket(request);
		
		assertNotNull(result);
		assertEquals("SUCCESS", result.getStatus());
		assertEquals(5000.0, result.getTotalFare());
	}

	/**
	 * 创建有效的预订请求
	 */
	private FlightBookingRequest createValidBookingRequest(String accountNo, double fare) {
		FlightBookingRequest request = new FlightBookingRequest();
		
		// 设置乘客信息
		PassengerInfo passengerInfo = new PassengerInfo();
		passengerInfo.setName("张三");
		passengerInfo.setEmail("test@example.com");
		passengerInfo.setSource("北京");
		passengerInfo.setDestination("上海");
		passengerInfo.setTravelDate(new Date());
		passengerInfo.setPickupTime("08:00");
		passengerInfo.setArrivalTime("10:30");
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
