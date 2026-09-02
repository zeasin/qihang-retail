package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @TableName o_goods_supplier
 */
@Data
public class ErpSupplier implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String number;
    private String loginName;
    private String loginPwd;
    private String loginSlat;
    private Double taxRate;
    private Double amount;
    private Double periodMoney;
    private Double difMoney;
    private LocalDateTime beginDate;
    private String remark;
    private String place;
    private String linkMan;
    private String contact;
    private String province;
    private String city;
    private String county;
    private String address;
    private String pinYin;
    private Integer disable;
    private Integer isDelete;
    private String purchaserName;
    private LocalDateTime createTime;
    private String createBy;
    private LocalDateTime updateTime;
    private String updateBy;
    private String usci;
    private String bl;
    private String blPeriod;
    private String blFaren;
    private String bank;
    private String bankAccountName;
    private String bankAccount;
    private Long merchantId;
    private Integer isShipper;
    private Long shopId;
    private Long warehouseId;
    private String merchantIds;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
