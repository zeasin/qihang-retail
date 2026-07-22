package cn.qihangerp.shop;

import lombok.Data;

@Data
public class ShopTokenBo {
    private Long shopId;
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private Long expiresAt;
    private String sellerId;
    private String sellerNum;
    private String sellerName;
    private String ownerName;

}
