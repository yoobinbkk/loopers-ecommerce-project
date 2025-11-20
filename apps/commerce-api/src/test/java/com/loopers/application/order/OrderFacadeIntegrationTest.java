package com.loopers.application.order;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandStatus;
import com.loopers.domain.product.ProductStatus;
import com.loopers.interfaces.api.order.OrderDto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderFacade 통합 테스트")
@SpringBootTest
class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private com.loopers.domain.stock.StockService stockService;

    @Autowired
    private PointService pointService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private com.loopers.domain.order.OrderService orderService;

    @Autowired
    private com.loopers.domain.coupon.CouponService couponService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private User testUser;
    private Product testProduct;
    private Long testProductId;
    
    private final String testLoginId = "test34";

    @BeforeEach
    void setUp() {
        // 테스트용 User 생성
        UserInfo userInfo = UserInfo.builder()
                .loginId(testLoginId)
                .email("test@test.com")
                .birthday("1990-01-01")
                .gender(Gender.MALE)
                .build();
        userFacade.saveUser(userInfo);

        testUser = userRepository.findByLoginId(testLoginId)
                .orElseThrow(() -> new RuntimeException("User를 찾을 수 없습니다"));

        // 테스트용 Brand 생성
        Brand brand = Brand.builder()
                .name("Test Brand")
                .description("Test Description")
                .status(BrandStatus.ON_SALE)
                .isVisible(true)
                .isSellable(true)
                .build();
        Brand savedBrand = brandJpaRepository.save(brand);

        // 테스트용 Product 생성
        Product product = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(10000))
                .status(ProductStatus.ON_SALE)
                .isVisible(true)
                .isSellable(true)
                .brand(savedBrand)
                .build();
        Product savedProduct = productRepository.save(product)
                .orElseThrow(() -> new RuntimeException("Product 저장 실패"));
        testProduct = savedProduct;
        testProductId = savedProduct.getId();

        // 테스트용 Stock 생성 (재고 100개로 설정)
        // Product 생성 시 Stock이 자동으로 생성되므로, 재고를 100개로 증가
        Stock stock = stockRepository.findByProductId(testProductId)
                .orElseThrow(() -> new RuntimeException("Stock을 찾을 수 없습니다"));
        // 현재 재고를 확인하고 100개가 되도록 조정
        long currentQuantity = stock.getQuantity();
        if (currentQuantity < 100L) {
            stockService.increaseQuantity(testProductId, 100L - currentQuantity);
        }

        // 테스트용 포인트 충전 (100000원)
        pointService.charge(testLoginId, BigDecimal.valueOf(100000));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("createOrder 테스트")
    @Nested
    class CreateOrderTest {

        @DisplayName("성공 케이스: 모든 조건을 만족하는 주문 생성 성공")
        @Test
        void createOrder_withValidRequest_Success() {
            // arrange
            List<OrderDto.OrderItemRequest> items = List.of(
                    OrderDto.OrderItemRequest.builder()
                            .productId(testProductId)
                            .quantity(2)
                            .build()
            );
            OrderDto.CreateOrderRequest request = OrderDto.CreateOrderRequest.builder()
                    .items(items)
                    .couponIds(new ArrayList<>())
                    .build();

            BigDecimal expectedTotalPrice = BigDecimal.valueOf(10000).multiply(BigDecimal.valueOf(2)); // 20000
            BigDecimal expectedFinalAmount = expectedTotalPrice; // 할인 없음

            // act
            OrderInfo orderInfo = orderFacade.createOrder(testLoginId, request);

            // assert
            assertNotNull(orderInfo);
            assertAll(
                    () -> assertNotNull(orderInfo.id(), "주문 ID는 null이 아니어야 함"),
                    () -> assertEquals(0, expectedTotalPrice.compareTo(orderInfo.totalPrice()), "총 가격이 일치해야 함"),
                    () -> assertEquals(0, expectedFinalAmount.compareTo(orderInfo.finalAmount()), "최종 금액이 일치해야 함"),
                    () -> assertEquals(OrderStatus.CONFIRMED, orderInfo.orderStatus(), "주문 상태는 CONFIRMED여야 함"),
                    () -> assertEquals(testLoginId, orderInfo.userLoginId(), "사용자 로그인 ID가 일치해야 함"),
                    () -> assertEquals(1, orderInfo.orderItems().size(), "주문 상품 수가 일치해야 함")
            );

            // 재고가 차감되었는지 확인
            Stock stock = stockRepository.findByProductId(testProductId)
                    .orElseThrow(() -> new RuntimeException("Stock을 찾을 수 없습니다"));
            assertEquals(98L, stock.getQuantity(), "재고가 2개 차감되어야 함 (100 - 2 = 98)");

            // 포인트가 차감되었는지 확인
            Point point = pointService.findByUserLoginId(testLoginId)
                    .orElseThrow(() -> new RuntimeException("Point를 찾을 수 없습니다"));
            BigDecimal expectedPoint = BigDecimal.valueOf(100000).subtract(expectedFinalAmount);
            assertEquals(0, expectedPoint.compareTo(point.getAmount()), "포인트가 차감되어야 함");
        }

        @DisplayName("성공 케이스: 쿠폰 적용하여 주문 생성 성공")
        @Test
        void createOrder_withCoupon_Success() {
            // arrange
            // 쿠폰 생성
            Coupon coupon = Coupon.builder()
                    .couponType(CouponType.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(5000))
                    .user(testUser)
                    .build();
            Coupon savedCoupon = couponRepository.save(coupon)
                    .orElseThrow(() -> new RuntimeException("Coupon 저장 실패"));

            List<OrderDto.OrderItemRequest> items = List.of(
                    OrderDto.OrderItemRequest.builder()
                            .productId(testProductId)
                            .quantity(2)
                            .build()
            );
            OrderDto.CreateOrderRequest request = OrderDto.CreateOrderRequest.builder()
                    .items(items)
                    .couponIds(List.of(savedCoupon.getId()))
                    .build();

            BigDecimal expectedTotalPrice = BigDecimal.valueOf(10000).multiply(BigDecimal.valueOf(2)); // 20000
            BigDecimal expectedDiscount = BigDecimal.valueOf(5000);
            BigDecimal expectedFinalAmount = expectedTotalPrice.subtract(expectedDiscount); // 15000

            // act
            OrderInfo orderInfo = orderFacade.createOrder(testLoginId, request);

            // assert
            assertNotNull(orderInfo);
            assertAll(
                    () -> assertEquals(0, expectedTotalPrice.compareTo(orderInfo.totalPrice()), "총 가격이 일치해야 함"),
                    () -> assertEquals(0, expectedDiscount.compareTo(orderInfo.discountAmount()), "할인 금액이 일치해야 함"),
                    () -> assertEquals(0, expectedFinalAmount.compareTo(orderInfo.finalAmount()), "최종 금액이 일치해야 함")
            );

            // 쿠폰이 사용되었는지 확인
            Coupon usedCoupon = couponRepository.findById(savedCoupon.getId())
                    .orElseThrow(() -> new RuntimeException("Coupon을 찾을 수 없습니다"));
            assertTrue(usedCoupon.getIsUsed(), "쿠폰은 사용된 상태여야 함");
            assertNotNull(usedCoupon.getOrder(), "쿠폰의 order는 null이 아니어야 함");
        }

        @DisplayName("실패 케이스: 존재하지 않는 User로 주문 생성 시 NOT_FOUND 예외 발생")
        @Test
        void createOrder_withNonExistentUser_NotFound() {
            // arrange
            List<OrderDto.OrderItemRequest> items = List.of(
                    OrderDto.OrderItemRequest.builder()
                            .productId(testProductId)
                            .quantity(2)
                            .build()
            );
            OrderDto.CreateOrderRequest request = OrderDto.CreateOrderRequest.builder()
                    .items(items)
                    .couponIds(new ArrayList<>())
                    .build();

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () ->
                    orderFacade.createOrder("nonexistent", request)
            );

            assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
            assertTrue(exception.getCustomMessage().contains("[loginId = nonexistent] User를 찾을 수 없습니다"));
        }

        @DisplayName("실패 케이스: 존재하지 않는 Product로 주문 생성 시 NOT_FOUND 예외 발생")
        @Test
        void createOrder_withNonExistentProduct_NotFound() {
            // arrange
            List<OrderDto.OrderItemRequest> items = List.of(
                    OrderDto.OrderItemRequest.builder()
                            .productId(99999L)
                            .quantity(2)
                            .build()
            );
            OrderDto.CreateOrderRequest request = OrderDto.CreateOrderRequest.builder()
                    .items(items)
                    .couponIds(new ArrayList<>())
                    .build();

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () ->
                    orderFacade.createOrder(testLoginId, request)
            );

            assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
            assertTrue(exception.getCustomMessage().contains("[productId = 99999] Product를 찾을 수 없습니다"));
        }

        @DisplayName("실패 케이스: 재고 부족 시 BAD_REQUEST 예외 발생")
        @Test
        void createOrder_withInsufficientStock_BadRequest() {
            // arrange
            // 재고를 5개로 설정 (현재 재고를 확인하고 5개가 되도록 조정)
            Stock stock = stockRepository.findByProductId(testProductId)
                    .orElseThrow(() -> new RuntimeException("Stock을 찾을 수 없습니다"));
            long currentQuantity = stock.getQuantity();
            if (currentQuantity > 5L) {
                // 재고를 5개로 만들기 위해 차감
                stockService.decreaseQuantity(testProductId, currentQuantity - 5L);
            } else if (currentQuantity < 5L) {
                // 재고를 5개로 만들기 위해 증가
                stockService.increaseQuantity(testProductId, 5L - currentQuantity);
            }

            List<OrderDto.OrderItemRequest> items = List.of(
                    OrderDto.OrderItemRequest.builder()
                            .productId(testProductId)
                            .quantity(10) // 재고보다 많은 수량
                            .build()
            );
            OrderDto.CreateOrderRequest request = OrderDto.CreateOrderRequest.builder()
                    .items(items)
                    .couponIds(new ArrayList<>())
                    .build();

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () ->
                    orderFacade.createOrder(testLoginId, request)
            );

            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
            assertTrue(exception.getCustomMessage().contains("재고가 부족"));
        }

        @DisplayName("실패 케이스: 포인트 부족 시 BAD_REQUEST 예외 발생")
        @Test
        void createOrder_withInsufficientPoint_BadRequest() {
            // arrange
            // 포인트를 1000원으로 조정 (기존 포인트를 모두 차감 후 1000원 충전)
            Point currentPoint = pointService.findByUserLoginId(testLoginId)
                    .orElseThrow(() -> new RuntimeException("Point를 찾을 수 없습니다"));
            BigDecimal currentAmount = currentPoint.getAmount();
            if (currentAmount.compareTo(BigDecimal.valueOf(1000)) > 0) {
                pointService.deduct(testLoginId, currentAmount.subtract(BigDecimal.valueOf(1000)));
            } else if (currentAmount.compareTo(BigDecimal.valueOf(1000)) < 0) {
                pointService.charge(testLoginId, BigDecimal.valueOf(1000).subtract(currentAmount));
            }

            List<OrderDto.OrderItemRequest> items = List.of(
                    OrderDto.OrderItemRequest.builder()
                            .productId(testProductId)
                            .quantity(10) // 100000원 (포인트보다 많은 금액)
                            .build()
            );
            OrderDto.CreateOrderRequest request = OrderDto.CreateOrderRequest.builder()
                    .items(items)
                    .couponIds(new ArrayList<>())
                    .build();

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () ->
                    orderFacade.createOrder(testLoginId, request)
            );

            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
            assertTrue(exception.getCustomMessage().contains("포인트가 부족"));
        }

        @DisplayName("실패 케이스: 쿠폰 사용 불가 시 BAD_REQUEST 예외 발생")
        @Test
        void createOrder_withUnavailableCoupon_BadRequest() {
            // arrange
            // 쿠폰 생성
            Coupon coupon = Coupon.builder()
                    .couponType(CouponType.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(5000))
                    .user(testUser)
                    .build();
            Coupon savedCoupon = couponRepository.save(coupon)
                    .orElseThrow(() -> new RuntimeException("Coupon 저장 실패"));

            // 쿠폰을 먼저 사용 (실제 Order를 생성하여 쿠폰 사용)
            Order firstOrder = Order.builder()
                    .discountAmount(BigDecimal.ZERO)
                    .shippingFee(BigDecimal.ZERO)
                    .user(testUser)
                    .build();
            firstOrder.addOrderItem(testProduct, 1);
            
            // Order 저장
            Order savedFirstOrder = orderService.saveOrder(firstOrder)
                    .orElseThrow(() -> new RuntimeException("Order 저장 실패"));

            // CouponService를 사용하여 쿠폰 사용 (guard() 검증을 통과하도록)
            couponService.useCoupon(savedFirstOrder, savedCoupon.getId());

            // 이미 사용된 쿠폰으로 다시 주문 생성 시도
            List<OrderDto.OrderItemRequest> items = List.of(
                    OrderDto.OrderItemRequest.builder()
                            .productId(testProductId)
                            .quantity(2)
                            .build()
            );
            OrderDto.CreateOrderRequest request = OrderDto.CreateOrderRequest.builder()
                    .items(items)
                    .couponIds(List.of(savedCoupon.getId()))
                    .build();

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () ->
                    orderFacade.createOrder(testLoginId, request)
            );

            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
            assertTrue(exception.getCustomMessage().contains("쿠폰을 사용할 수 없습니다"));
        }
    }
}

