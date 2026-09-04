package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 电商平台配置实体类
 * 对应表: o_shop_platform
 */
@Data
@TableName("o_shop_platform")
public class OShopPlatform {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 平台名
     */
    private String name;

    /**
     * 平台编码
     */
    private String code;

    /**
     * AppKey
     */
    private String appKey;

    /**
     * AppSecret
     */
    private String appSecret;

    /**
     * 平台回调URI
     */
    private String redirectUri;

    /**
     * 接口访问地址
     */
    private String serverUrl;

    /**
     * 状态（0启用 1关闭）
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 国家/地区
     */
    private Long regionId;
}
