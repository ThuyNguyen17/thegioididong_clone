# Hướng dẫn chạy ứng dụng TheGioiDiDong

## Cách 1: Chạy từ IDE (Khuyến nghị)

### IntelliJ IDEA
1. Mở project trong IntelliJ IDEA
2. Tìm file `Demo1Application.java` trong `src/main/java/com/example/demo1/`
3. Click chuột phải vào file → **Run 'Demo1Application'**
4. Chờ ứng dụng start (xem log trong console)
5. Mở browser: `http://localhost:8080`

### Eclipse / STS
1. Import project as Maven project
2. Tìm file `Demo1Application.java`
3. Right click → **Run As** → **Spring Boot App**
4. Mở browser: `http://localhost:8080`

---

## Cách 2: Chạy từ Command Line (nếu có Maven)

```bash
cd c:\Users\DELL\Downloads\demo1

# Nếu có Maven global
mvn clean install -DskipTests
mvn spring-boot:run

# Hoặc dùng Maven wrapper (nếu internet ổn định)
.\mvnw.cmd clean spring-boot:run
```

---

## Cách 3: Chạy file JAR trực tiếp

Nếu đã build thành công, chạy:
```bash
cd c:\Users\DELL\Downloads\demo1\target
java -jar demo1-0.0.1-SNAPSHOT.jar
```

---

## QUAN TRỌNG: Chuẩn bị Database trước khi chạy

### Bước 1: Start Laragon MySQL
1. Mở Laragon
2. Click **Start All**
3. Đảm bảo MySQL đang chạy (icon xanh)

### Bước 2: Chạy Migration Script
1. Mở **phpMyAdmin** từ Laragon (hoặc HeidiSQL)
2. Chọn database `WebBanHang`
3. Mở tab **SQL**
4. Copy nội dung từ file `src/main/resources/db/migration.sql`
5. **SỬA dòng đầu tiên**: Thay `USE your_database_name;` thành `USE WebBanHang;`
6. Click **Go** để chạy script

### Bước 3: Verify Database
Chạy query để kiểm tra:
```sql
SELECT * FROM products;
SELECT * FROM categories;
```

Bạn sẽ thấy 8 sản phẩm và 5 categories mẫu.

---

## Kiểm tra ứng dụng đã chạy

Sau khi start Spring Boot, bạn sẽ thấy log:
```
Started Demo1Application in X.XXX seconds
```

Mở browser và test:
- Homepage: `http://localhost:8080`
- API Products: `http://localhost:8080/api/products`
- API Categories: `http://localhost:8080/api/categories`

---

## Troubleshooting

### Lỗi: Port 8080 already in use
Có app khác đang dùng port 8080. Thêm vào `application.properties`:
```properties
server.port=8081
```

### Lỗi: Cannot connect to database
- Kiểm tra Laragon MySQL đã start
- Verify database name là `WebBanHang`
- Kiểm tra username/password trong `application.properties`

### Lỗi: Table 'products' doesn't exist
- Chạy migration script như hướng dẫn ở trên
- Hoặc để Spring Boot tự tạo table (đã config `spring.jpa.hibernate.ddl-auto=update`)

---

## Các tính năng đã implement

✅ **Header & Navigation**
- Logo TheGioiDiDong
- Search bar
- Category navigation (load từ database)

✅ **Banner Khuyến Mãi**
- Gradient đỏ-cam-vàng
- Fireworks animation
- Đèn lồng Tết

✅ **Flash Sale**
- Countdown timer tự động
- Badge giảm giá

✅ **Product Grid**
- Load sản phẩm từ database
- Hiển thị giá, discount, rating
- Hover effects
- Filter theo category
- Search functionality

✅ **REST API**
- `/api/products` - Tất cả sản phẩm
- `/api/categories` - Tất cả danh mục
- `/api/products/search?q=keyword` - Tìm kiếm
- `/api/products/category/{id}` - Lọc theo danh mục

---

## Tiếp theo bạn có thể làm

1. **Thêm hình ảnh thật** cho sản phẩm (thay emoji)
2. **Product detail page** khi click vào sản phẩm
3. **Shopping cart** với localStorage hoặc session
4. **Admin page** để quản lý sản phẩm (đã có sẵn `/products`)
5. **User authentication** với Spring Security
6. **Payment integration** với VNPay/MoMo

Chúc bạn thành công! 🎉
