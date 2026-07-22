package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 店铺实体类
 * 对应表: o_shop
 */
@Data
@TableName("o_shop")
public class OShop {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 店铺名
     */
    private String name;

    /**
     * 店铺类型（对应第三方平台Id，999=线下门店）
     */
    private Integer type;

    /**
     * 门店编码
     */
    private String number;

    /**
     * 店铺url
     */
    private String url;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态（1正常2已删除）
     */
    private Integer status;

    /**
     * 描述
     */
    private String remark;

    /**
     * 第三方平台店铺id
     */
    private String sellerId;

    /**
     * Appkey
     */
    private String appKey;

    /**
     * Appsecret
     */
    private String appSecret;

    /**
     * access_token
     */
    private String accessToken;

    /**
     * 到期时间
     */
    private Long expiresIn;

    /**
     * 负责人id
     */
    private Long manageUserId;

    /**
     * 店铺分组
     */
    private Long shopGroupId;

    /**
     * 国家/地区
     */
    private Long regionId;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * api调用状态
     */
    private Integer apiStatus;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 商家编码
     */
    private String sellerNum;

    /**
     * 是否允许共享库存
     */
    private Integer allowInventoryShare;

    /**
     * 门店编码（POS扩展）
     */
    private String storeNumber;

    /**
     * 营业时间
     */
    private String openingHours;

    /**
     * 收银台数量
     */
    private Integer registerCount;

    /**
     * 打印机配置
     */
    private String printConfig;

    /**
     * 支付参数
     */
    private String payConfig;
}
