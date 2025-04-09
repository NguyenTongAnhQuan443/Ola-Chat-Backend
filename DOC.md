### Vấn đề login 
2) Chỉ kiểm tra whitelist nếu là refresh token (qua scope, type, hoặc endpoint cụ thể)
2) Luôn kiểm tra blacklist để ngăn access token đã logout
3) Không kiểm tra whitelist với access token (nếu không có lý do đặc biệt)
4) Access token không cần lưu vào Redis

### Vấn đề logout 
📥 Nhận refresh token từ client

🔍 Trích xuất userId, deviceId, refresh jti

🧹 Xóa refresh token khỏi Redis whitelist

❌ Tìm access token jti tương ứng → đưa vào blacklist

✅ Gỡ cookie refreshToken

📜 Ghi log logout

