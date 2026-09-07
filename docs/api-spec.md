# API 명세

이 프로젝트가 외부에 노출하는 API는 아래 1개뿐이다. 공급사 A/B를 병렬로 조회하고, 서로 다른 응답
형식을 표준 모델로 통합해 반환한다.

## GET /api/v1/stays/search

주어진 체크인/체크아웃 기간과 인원수로 두 공급사의 숙소를 함께 검색한다.

### Query Parameters

| 이름       | 타입        | 필수 | 기본값 | 설명                                      |
|------------|-------------|:----:|:------:|-------------------------------------------|
| `checkIn`  | date (ISO-8601, `yyyy-MM-dd`) | O | -    | 체크인 날짜                              |
| `checkOut` | date (ISO-8601, `yyyy-MM-dd`) | O | -    | 체크아웃 날짜. `checkIn`보다 뒤여야 함  |
| `adults`   | int         |  O   |   -    | 성인 인원수                              |
| `children` | int         |  X   |   0    | 아동 인원수                              |

요청 예시:

```
GET /api/v1/stays/search?checkIn=2026-09-01&checkOut=2026-09-04&adults=2&children=0
```

### 응답 (200 OK)

```json
{
  "offers": [
    {
      "hotelId": 3,
      "hotelName": "Riverside Hotel Seoul",
      "roomId": 3,
      "roomName": "Deluxe Twin Room",
      "maxOccupancy": 2,
      "breakfastIncluded": true,
      "availableRooms": 1,
      "totalPrice": 452000,
      "currency": "KRW",
      "sourceSupplier": "B"
    },
    {
      "hotelId": 1,
      "hotelName": "Riverside Hotel Seoul",
      "roomId": 1,
      "roomName": "Deluxe Twin",
      "maxOccupancy": 2,
      "breakfastIncluded": false,
      "availableRooms": 1,
      "totalPrice": 429000,
      "currency": "KRW",
      "sourceSupplier": "A"
    },
    {
      "hotelId": 2,
      "hotelName": "Namsan Garden Stay",
      "roomId": 2,
      "roomName": "Standard Double",
      "maxOccupancy": 2,
      "breakfastIncluded": false,
      "availableRooms": 0,
      "totalPrice": 302500,
      "currency": "KRW",
      "sourceSupplier": "A"
    }
  ],
  "failedSuppliers": []
}
```

실제 실행 중인 서버(main app 8080, mock supplier 9090)에 위 요청 예시를 그대로 curl로 호출해 확인한
응답이다. `hotelId`/`roomId`는 공급사가 아니라 이 서버가 매핑해 부여한 내부 식별자이고, 같은
(공급사, 공급사 코드) 조합은 항상 같은 값으로 돌아온다.

#### 필드 설명

| 필드                | 타입    | 설명                                                                 |
|---------------------|---------|----------------------------------------------------------------------|
| `offers`            | array   | 조건에 맞는 객실 상품 목록                                          |
| `offers[].hotelId`  | long    | 내부 숙소 식별자                                                     |
| `offers[].hotelName`| string  | 숙소명 (공급사 응답에서 그대로 가져옴)                               |
| `offers[].roomId`   | long    | 내부 객실 식별자                                                     |
| `offers[].roomName` | string  | 객실명 (공급사 응답에서 그대로 가져옴)                               |
| `offers[].maxOccupancy` | int | 객실 1개당 최대 수용 인원                                           |
| `offers[].breakfastIncluded` | boolean | 조식 포함 여부                                                |
| `offers[].availableRooms` | int | 예약 가능 객실 수. 연박 시 기간 내 날짜별 재고의 최솟값. 재고가 없어도 상품은 응답에 포함되고 이 값이 `0`으로 노출됨(아래 참고) |
| `offers[].totalPrice` | long  | 세금 포함, 숙박 기간 전체 총액                                       |
| `offers[].currency` | string  | 통화 코드 (`KRW`)                                                    |
| `offers[].sourceSupplier` | string | 이 상품을 제공한 공급사 코드(`A` 또는 `B`)                        |
| `failedSuppliers`   | array   | 이번 요청에서 조회에 실패한 공급사 코드 목록. 비어 있으면 전부 성공 |

#### 부분 실패 시 응답 (200 OK, 공급사 A 장애)

```json
{
  "offers": [
    {
      "hotelId": 3,
      "hotelName": "Riverside Hotel Seoul",
      "roomId": 3,
      "roomName": "Deluxe Twin Room",
      "maxOccupancy": 2,
      "breakfastIncluded": true,
      "availableRooms": 1,
      "totalPrice": 452000,
      "currency": "KRW",
      "sourceSupplier": "B"
    }
  ],
  "failedSuppliers": ["A"]
}
```

공급사 하나가 응답하지 않거나 오류를 반환해도 HTTP 상태는 여전히 `200`이다. 실패한 공급사는
`failedSuppliers`에 이름이 남고, 그 공급사의 상품만 `offers`에서 빠진다. 살아있는 공급사의 결과는
그대로 반환된다. 두 공급사가 모두 실패하면 `offers`는 빈 배열, `failedSuppliers`에 `["A", "B"]`가
담긴 채로 역시 `200`이 반환된다 — 클라이언트가 항상 파싱 가능한 하나의 응답 형태만 다루면 되도록,
공급사 장애를 HTTP 오류로 노출하지 않는다.

### 오류 응답

이 API가 `400`을 반환하는 경로는 두 가지이며, 각각 응답 형식이 다르다.

**1. 필수 파라미터 누락 / 타입 불일치** — Spring MVC 자체 검증(예: `checkIn`을 아예 안 보냄)에서
걸리는 경우로, Spring Boot 기본 오류 포맷 그대로 내려간다.

```
GET /api/v1/stays/search?checkIn=2026-09-01&adults=2
```

```json
{
  "timestamp": "2026-09-07T11:30:30.156Z",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/v1/stays/search"
}
```

**2. `checkOut`이 `checkIn`보다 앞서거나 같은 경우** — 이 서비스의 도메인 검증
(`AvailabilityQueryRequest`의 compact constructor)에서 걸리는 경우로, `GlobalExceptionHandler`가
잡아 아래 형식으로 응답한다.

```
GET /api/v1/stays/search?checkIn=2026-09-04&checkOut=2026-09-01&adults=2
```

```json
{
  "error": "INVALID_PARAMETER",
  "message": "checkOut must be after checkIn"
}
```

두 경로 모두 상태 코드는 `400`이지만 바디 형식이 다르다는 점에 유의한다. Spring 자체 검증은 요청이
컨트롤러 메서드에 도달하기도 전에 걸러지기 때문에 `GlobalExceptionHandler`를 거치지 않는다.
