package cn.qihangerp.utils;//package cn.qihangerp.utils;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import jakarta.servlet.http.HttpServletRequest;
//
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//
//public class JwtUtils {
//    private static final String SECRET_KEY = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkrstuvwxyzabcdefghijklmnopqrstqihang";  // 加密密钥
//    private static final long EXPIRATION_TIME = 3600000;  // 1小时
//    /**
//     * 令牌前缀
//     */
//    public static final String TOKEN_PREFIX = "Bearer ";
//    // 将自定义的字符串转换为 512 位的密钥
////    public static SecretKey createSecretKey() {
////        String secretKey = "QIHANGERP0123456789abcdndienndssfeeCN" +
////                "YOUR_ADDITIONAL_CHARACTERS_TO_REACH_512_BITS";  // 自己填充至 512 位
////        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
////        return new SecretKeySpec(keyBytes, 0, 64, "HmacSHA512");  // 取前 64 字节作为密钥
////    }
//    // 生成 JWT
//    public static String generateToken(String username,Long userId) {
//        return Jwts.builder()
//                .setSubject(username)
//                .claim("userId", userId)  // 自定义claim，存储用户ID
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
//                .compact();
//    }
//
//    // 验证 JWT
//    public static boolean validateToken(String token) {
//        try {
//            Jwts.parser()
//                    .setSigningKey(SECRET_KEY)
//                    .parseClaimsJws(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    // 获取用户名
//    public static String getUsernameFromToken(String token) {
//        return Jwts.parser()
//                .setSigningKey(SECRET_KEY)
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
////    public static Long getUserIdFromToken(String token) {
////
////
////        return (Long) Jwts.parser()
////                .setSigningKey(SECRET_KEY)
////                .parseClaimsJws(token)
////                .getBody()
////                .get("userId");  // 获取自定义的 userId claim
////    }
//
//    public static Long getUserIdFromToken(HttpServletRequest request) {
//        String token = getToken(request);
////        return (Long) Jwts.parser()
////                .setSigningKey(SECRET_KEY)
////                .parseClaimsJws(token)
////                .getBody()
////                .get("userId");  // 获取自定义的 userId claim
//
//        try {
//            Claims claims = Jwts.parser()
//                    .setSigningKey(SECRET_KEY)
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            // 获取自定义的 userId claim
//            Object userIdObj = claims.get("userId");
//            if (userIdObj instanceof Long || userIdObj instanceof Integer) {
////                return (Long) userIdObj;
//                return Long.parseLong(userIdObj.toString());
//            } else {
//                // 如果不是 Long 类型，可以根据需要进行转换，或者抛出异常
//                throw new JwtException("UserId is not a Long");
//            }
//
//        } catch (JwtException e) {
//            // 异常处理
//            System.out.println("Token解析失败: " + e.getMessage());
//            return null;
//        }
//    }
//    private static String getToken(HttpServletRequest request)
//    {
//        String token = request.getHeader("Authorization");
//        if (StringUtils.isNotEmpty(token) && token.startsWith(TOKEN_PREFIX))
//        {
//            token = token.replace(TOKEN_PREFIX, "");
//        }
//        return token;
//    }
//}
