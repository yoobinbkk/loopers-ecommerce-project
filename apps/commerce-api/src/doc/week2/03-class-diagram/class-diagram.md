---
title: E-Commerce Class Diagram
---
classDiagram
    class User {
        Long id
        String loginId
        String email
        String birthday
        Enum gender
        Point point
    }
    class Point {
        Long id
        User user
        Long amount
        Enum grade
    }
    class Product {
        Long id
        ProductCategory productCategory
        Stock stock;
        Brand brand
        String name
        String description
        Enum status
        Long price
        Boolean isVisible
        Boolean isSellable
    }
    class ProductCategory {
        Long id
        Long parentId
        List<Product> products
        String name
        String description
        Boolean isVisible
        Boolean isSellable
    }
    class Stock {
        Long id
        Product product
        Long quantity
    }
    class Brand {
        Long id
        List<Product> products
        String name
        String description
        Enum status
        Boolean isVisible
        Boolean isSellable
    }
    class Like {
        Long id
        User user
        Product product
    }
    class Order {
        Long id
        User user
        List<OrderItem> orderItems
        Enum status
        Long finalAmount
        Long totalPrice
        Long discountAmount
        Long shippingFee
    }
    class OrderItem {
        Long id
        Order order
        Product product
        Integer quantity
        Long unitPrice
        Long totalAmount
    }
    class Payment {
        Long id
        Order order
        String pgTransactionId
        Enum method
        Long amount
        Enum status
    }
    class PageDto {
        List<T> elements
        Integer index
        Integer number
        Integer size
        Integer totalElements
        Integer totalPages
        String sort
    }

    User <-- Point
    User <-- Like
    User <-- Order
    Product <-- Like
    Product <-- Stock
    Product <-- OrderItem
    Brand <-- Product
    ProductCategory <-- Product
    Order <-- OrderItem
    Order <-- Payment
    