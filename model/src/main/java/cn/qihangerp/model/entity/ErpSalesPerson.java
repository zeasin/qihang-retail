package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 营业员实体类
 * 对应表: erp_sales_person
 */
@Data
@TableName("erp_sales_person")
public class ErpSalesPerson {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 销售人员姓名
     */
    private String name;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 工号
     */
    private String employeeNo;

    /**
     * 岗位：店长/收银员/理货员
     */
    private String position;

    /**
     * 岗位编码
     */
    private String positionCode;

    /**
     * 所属商户ID
     */
    private Long merchantId;

    /**
     * 所属店铺ID
     */
    private Long shopId;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 提成比例（%）
     */
    private BigDecimal commissionRate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
