---
title: Product API Sequence Diagram
---
sequenceDiagram
autonumber
actor Client
participant C as ProductController
participant F as ProductFacade
participant S as ProductService
participant R as ProductRepository

    %% 상품 목록 조회
    Client->>+C: GET /api/v1/products (ProductDto.SearchRequest req)
    Note left of C: Product 목록 조회 API 호출 (검색 조건 포함)
    Note left of C: Query Parameters<br/>--------------------<br/>"categoryId":123 or null<br/>"brandId": 123 or null,<br/>"sort": "latest",<br/>"pageIndex": 0,<br/>"pageSize": 20<br/>---------------------

    C->>+F: getProductList(ProductInfo info)
    F->>+S: getProductList(Product product)

    S->>+R: findProductsCustom(Predicate predicate, Pageable pageable)

    Note right of S: QueryDSL 의 Predicate(BooleanBuilder) 를 활용하여<br/>상품 카테고리와 브랜드 조건으로 List<Product> 를 가져오게 한다.
    Note right of S: Pageable 객체를 활용해서 페이지 관련 데이터를 받아 Response data 에 담는다.
    
    R-->>-S: Optional<List<Product>>

    alt Product 목록이 있는 경우
        S->>S: List<Product> 객체 반환
    else Product 목록이 없는 경우
        S->>S: List<Product> empty 객체 반환
    end
    
    S->>S: List<Product> -> List<ProductInfo>
    S-->>-F: List<ProductInfo>
    F-->>-C: List<ProductInfo>

    C-->>-Client: 200 OK (List<ProductDto.InfoResponse> JSON 응답)
    Note left of C: "meta": { "result": "SUCCESS", "errorCode": null, "message": null },<br/>"data": {<br/>"content": [<br/>{<br/>"id": 10,<br/>"brand": { "id": 1, "name": "Loopers" },<br/>"name": "에센셜 코튼 티셔츠",<br/>"description": "부드러운 촉감의 데일리 티셔츠",<br/>"status": "ACTIVE",<br/>"price": 19900,<br/>"isVisible": true,<br/> "isSellable": true,<br/>"stockQuantity": 42,<br/>"likesCount": 128<br/>},<br/>{<br/>"id": 11,<br/>"brand": { "id": 1, "name": "Loopers" },<br/>"name": "라이트 웨이트 조거 팬츠",<br/>"description": "가벼운 착용감의 조거 팬츠",<br/>"status": "ACTIVE",<br/>"price": 39900,<br/>"isVisible": true,<br/>"isSellable": true,<br/>"stockQuantity": 17,<br/>"likesCount": 64<br/>}<br/>],<br/>"page": {<br/>"page": 0,<br/>"size": 20,<br/>"totalElements": 312,<br/>"totalPages": 16,<br/>"sort": "latest"<br/>}

    %% 상품 상세 조회
    Client->>+C: GET /api/v1/products/{productId}
    Note left of C: Product 상세 조회 API 호출
    Note left of C: Path Variable<br/>--------------------<br/>"productId":123<br/>---------------------

    C->>+F: getProductDetail(Long productId)
    F->>+S: getProductDetail(Long productId)
    S->>+R: findById(Long productId)
    
    R-->>-S: Optional<Product>

    rect rgb(20,20,0)
        alt Product가 존재하지 않는 경우
            S-->>Client: NullPointerException(message)
            Note left of C: "meta": { "result": "FAIL",<br/>"errorCode": NOT_FOUND,<br/>"message": "상품을 찾지 못했습니다." },<br/>"data": {}
        end
    end
    
    S->>S: Product -> ProductDetailInfo
    S-->>-F: ProductDetailInfo
    F-->>-C: ProductDetailInfo

    C-->>-Client: 200 OK (ProductDto.DetailResponse JSON 응답)
    Note left of C: "meta": { "result": "SUCCESS", "errorCode": null, "message": null },<br/>"data": {<br/> "id": 10,<br/>"brand": {<br/>"id": 1,<br/>"name": "Loopers",<br/>"status": "ACTIVE"<br/>},<br/>"name": "에센셜 코튼 티셔츠",<br/>"description": "부드러운 촉감의 데일리 티셔츠",<br/>"status": "ACTIVE",<br/>"price": 19900,<br/>"isVisible": true,<br/>"isSellable": true,<br/>"stockQuantity": 42,<br/>"likesCount": 128,<br/>"liked": false<br/>}
