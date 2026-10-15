
#TECHZONE – ỨNG DỤNG MUA SẮM THIẾT BỊ CÔNG NGHỆ

TechZone là ứng dụng thương mại điện tử trên nền tảng Android, được phát triển nhằm mang đến trải nghiệm mua sắm các sản phẩm công nghệ hiện đại, thuận tiện và dễ sử dụng.
Ứng dụng hỗ trợ người dùng khám phá, tìm kiếm, xem thông tin sản phẩm, quản lý giỏ hàng, đặt hàng và theo dõi đơn hàng. Đồng thời, hệ thống cung cấp các chức năng quản trị giúp quản lý sản phẩm và hoạt động kinh doanh hiệu quả.
Dự án được xây dựng bằng **Java, Android SDK và Firebase**, sử dụng **Android Studio** làm môi trường phát triển chính.

---

#✨ 1. TÍNH NĂNG CHÍNH

👤 Quản lý tài khoản người dùng
- Đăng ký và đăng nhập tài khoản.
- Quản lý thông tin cá nhân.
- Cập nhật và chỉnh sửa thông tin người dùng.
- Quản lý các thiết lập tài khoản.

🛍️ Khám phá và tìm kiếm sản phẩm
- Hiển thị danh sách sản phẩm công nghệ.
- Giao diện trang chủ với banner quảng cáo.
- Hiển thị chương trình khuyến mãi, Flash Sale.
- Tìm kiếm và xem thông tin chi tiết sản phẩm.
- Xem giá bán, hình ảnh, mô tả và đánh giá sản phẩm.

❤️ Danh sách yêu thích (Wishlist)
- Thêm sản phẩm vào danh sách yêu thích.
- Xem và quản lý các sản phẩm đã lưu.
- Xóa sản phẩm khỏi danh sách yêu thích.

🛒 Giỏ hàng và đặt hàng
- Thêm sản phẩm vào giỏ hàng.
- Tăng hoặc giảm số lượng sản phẩm.
- Xóa sản phẩm khỏi giỏ hàng.
- Tính toán tổng giá trị đơn hàng.
- Nhập thông tin giao hàng và thực hiện đặt hàng.
- Hỗ trợ quy trình thanh toán.

📦 Quản lý đơn hàng
- Xem lịch sử đặt hàng.
- Theo dõi trạng thái xử lý và vận chuyển đơn hàng.
- Xem thông tin chi tiết từng đơn hàng.
- Nhận thông báo cập nhật liên quan đến đơn hàng.

🛡️ Quản trị hệ thống (Admin)
- Quản lý danh sách sản phẩm.
- Thêm sản phẩm mới.
- Chỉnh sửa thông tin sản phẩm.
- Xóa sản phẩm.
- Quản lý thông tin hàng hóa phục vụ hoạt động bán hàng.

---

#🛠️ 2. CÔNG NGHỆ SỬ DỤNG

Công nghệ                 - Vai trò 
Java                      - Ngôn ngữ lập trình chính 
Android SDK               - Phát triển ứng dụng Android 
XML                       - Xây dựng giao diện người dùng 
Firebase                  - Nền tảng dịch vụ backend
Android Studio            - Môi trường phát triển ứng dụng 
Gradle (Kotlin DSL)       - Quản lý thư viện và quá trình build 
Git                       - Quản lý phiên bản source code 
GitHub                    - Lưu trữ và quản lý mã nguồn 

---

#🏗️ 3. CẤU TRÚC DỰ ÁN

Cấu trúc tổng quan của project Android:

```text
techZone/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/            # Source code Java
│   │   │   ├── res/             # Tài nguyên giao diện
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── androidTest/         # Kiểm thử Android
│   │   └── test/                # Unit Test
│   │
│   ├── build.gradle.kts         # Cấu hình module app
│   └── google-services.json     # Cấu hình Firebase
│
├── gradle/                      # Gradle Wrapper
├── build.gradle.kts             # Cấu hình build project
├── settings.gradle.kts          # Cấu hình các module
├── gradle.properties            # Thuộc tính Gradle
├── gradlew
├── gradlew.bat
├── .gitignore
└── README.md
```

*Lưu ý: Đây là cấu trúc tham khảo cho dự án Android; các thư mục kiểm thử hoặc cấu hình có thể khác tùy phiên bản project.*

---

#🚀 4. HƯỚNG DẪN CÀI ĐẶT VÀ CHẠY ỨNG DỤNG

### Bước 1: Chuẩn bị môi trường

Cài đặt các công cụ cần thiết:

- Android Studio.
- Android SDK phù hợp với cấu hình dự án.
- Git.
- Máy ảo Android Emulator hoặc điện thoại Android có bật USB Debugging.

### Bước 2: Clone repository

Mở Terminal hoặc Git Bash và chạy:

```bash
git clone https://github.com/gamerruzy-source/techZone.git
```

### Bước 3: Mở project bằng Android Studio

1. Khởi động Android Studio.
2. Chọn **Open**.
3. Chọn thư mục `techZone` vừa clone.
4. Chờ Android Studio tải các thư viện và hoàn thành **Gradle Sync**.

### Bước 4: Cấu hình Firebase

- Sử dụng Firebase project phù hợp với ứng dụng.
- Đặt file `google-services.json` vào thư mục `app/`.
- Kiểm tra Application ID, cấu hình Firebase và các dịch vụ cần thiết.
- Bảo đảm tài khoản hoặc Firebase project được cấp quyền truy cập phù hợp.

### Bước 5: Chạy ứng dụng

1. Chọn thiết bị Android Emulator hoặc kết nối điện thoại thật.
2. Chọn cấu hình chạy `app`.
3. Nhấn **Run ▶**.
4. Chờ ứng dụng được build và cài đặt lên thiết bị.

---

#🎯 5. MỤC TIÊU DỰ ÁN

Dự án TechZone được phát triển nhằm:

- Vận dụng kiến thức lập trình Android bằng ngôn ngữ Java.
- Xây dựng giao diện mua sắm thân thiện và dễ sử dụng.
- Tích hợp Firebase vào ứng dụng di động.
- Triển khai các chức năng cơ bản của hệ thống thương mại điện tử.
- Rèn luyện kỹ năng xây dựng, tổ chức và quản lý source code.
- Thực hành quản lý phiên bản và lưu trữ dự án bằng Git/GitHub.

---

#🔮 6. ĐỊNH HƯỚNG PHÁT TRIỂN

Trong tương lai, dự án có thể được mở rộng thêm:

- Tích hợp thanh toán trực tuyến qua các cổng thanh toán.
- Bổ sung bộ lọc sản phẩm nâng cao.
- Tìm kiếm thông minh và gợi ý sản phẩm.
- Hệ thống mã giảm giá và chương trình khách hàng thân thiết.
- Thông báo đẩy về đơn hàng và khuyến mãi.
- Thống kê doanh thu và quản lý đơn hàng cho Admin.
- Nâng cao bảo mật và tối ưu hiệu suất ứng dụng.

---

#👨‍💻 7. THÔNG TIN TÁC GIẢ

Sinh viên thực hiện: Lê Văn Tuấn Anh

Mã sinh viên: PH65249

Trường: FPT Polytechnic

Dự án: TechZone – Ứng dụng mua sắm thiết bị công nghệ

Ngôn ngữ lập trình: Java

Nền tảng: Android

GitHub: [gamerruzy-source](https://github.com/gamerruzy-source)

Repository: [TechZone](https://github.com/gamerruzy-source/techZone)

---

#📌 8. GHI CHÚ

TechZone là dự án được xây dựng phục vụ mục đích học tập, nghiên cứu và thực hành phát triển ứng dụng Android.

Các tính năng của dự án có thể tiếp tục được cập nhật, điều chỉnh và tối ưu trong quá trình phát triển.

---

**©2026 nhóm 1 - Agile | FPT Polytechnic**

TechZone – Khám phá công nghệ, trải nghiệm mua sắm thông minh.
