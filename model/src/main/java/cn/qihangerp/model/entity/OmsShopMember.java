package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会员实体类
 * 对应表: oms_shop_member
 */
@Data
@TableName("oms_shop_member")
public class OmsShopMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private Long shopId;

    private Integer shopType;

    private String platformUserId;

    private String platformAccount;

    private String platformOpenid;

    private String name;

    private String phone;

    private String province;

    private String city;

    private String county;

    private String town;

    private String address;

    private Integer status;

    private String remark;

    private LocalDateTime createOn;

    private LocalDateTime updateOn;

    @TableField(exist = false)
    private String keyword;
}
