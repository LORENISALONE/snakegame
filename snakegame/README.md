# Snake Game với JavaFX và MySQL

Một game Snake hiện đại được phát triển bằng JavaFX với khả năng lưu điểm vào MySQL database.

## Tính năng

- 🎮 **3 chế độ chơi**: Dễ, Trung bình, Khó
- 🏆 **Hệ thống điểm số**: Lưu điểm vào MySQL database
- 📊 **Bảng xếp hạng**: Xem top người chơi theo độ khó
- 🎨 **Thiết kế đẹp**: Giao diện hiện đại với bo góc
- ⌨️ **Điều khiển**: Sử dụng phím mũi tên
- ⏸️ **Tạm dừng**: Nhấn SPACE để tạm dừng/tiếp tục

## Yêu cầu hệ thống

- Java 11 hoặc cao hơn
- MySQL 8.0 hoặc cao hơn
- Maven 3.6 hoặc cao hơn

## Cài đặt

### 1. Cài đặt MySQL

Tạo database và user:

```sql
CREATE DATABASE snake_game;
CREATE USER 'snake_user'@'localhost' IDENTIFIED BY 'snake_password';
GRANT ALL PRIVILEGES ON snake_game.* TO 'snake_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Import database schema

```bash
mysql -u root -p snake_game < database/schema.sql
```

### 3. Cấu hình database

Chỉnh sửa file `src/main/java/com/snakegame/database/DatabaseManager.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/snake_game?useSSL=false&serverTimezone=UTC";
private static final String USERNAME = "snake_user";
private static final String PASSWORD = "snake_password";
```

### 4. Build và chạy

```bash
# Build project
mvn clean compile

# Chạy game
mvn javafx:run
```

## Cách chơi

1. **Khởi động game**: Chọn độ khó từ menu chính
2. **Điều khiển**: Sử dụng phím mũi tên để di chuyển
3. **Mục tiêu**: Ăn thức ăn (hình tròn đỏ) để tăng điểm
4. **Tránh**: Không chạm vào tường hoặc thân rắn
5. **Tạm dừng**: Nhấn SPACE để tạm dừng/tiếp tục
6. **Lưu điểm**: Nhập tên và lưu điểm sau khi game over

## Cấu trúc dự án

```
snakegame/
├── src/main/java/com/snakegame/
│   ├── Main.java                          # Entry point
│   ├── controller/                        # Controllers
│   │   ├── MenuController.java
│   │   ├── GameController.java
│   │   ├── GameOverController.java
│   │   └── HighScoreController.java
│   ├── model/                            # Models
│   │   ├── Player.java
│   │   └── GameBoard.java
│   └── database/                         # Database
│       └── DatabaseManager.java
├── src/main/resources/
│   ├── fxml/                            # FXML files
│   │   ├── Menu.fxml
│   │   ├── Game.fxml
│   │   ├── GameOver.fxml
│   │   └── HighScore.fxml
│   └── css/                             # Styles
│       └── style.css
├── database/
│   └── schema.sql                        # Database schema
└── pom.xml                              # Maven configuration
```

## Độ khó

- **Dễ**: Tốc độ 200ms/khung hình
- **Trung bình**: Tốc độ 150ms/khung hình  
- **Khó**: Tốc độ 100ms/khung hình

## Tính năng kỹ thuật

- **JavaFX**: Giao diện người dùng hiện đại
- **MySQL**: Lưu trữ điểm số và thông tin người chơi
- **Maven**: Quản lý dependencies
- **FXML**: Tách biệt logic và giao diện
- **CSS**: Styling với bo góc và hiệu ứng

## Troubleshooting

### Lỗi kết nối database
- Kiểm tra MySQL đang chạy
- Xác nhận thông tin kết nối trong DatabaseManager
- Đảm bảo database và user đã được tạo

### Lỗi JavaFX
- Đảm bảo Java 11+ được cài đặt
- Kiểm tra module path nếu sử dụng Java 9+

## Đóng góp

Mọi đóng góp đều được chào đón! Hãy tạo issue hoặc pull request.

## License

MIT License
