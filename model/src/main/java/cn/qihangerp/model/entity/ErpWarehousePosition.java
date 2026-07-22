package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 仓库仓位表
 * @TableName wms_warehouse_position
 */
@Data
public class ErpWarehousePosition implements Serializable {
    /**
     * 
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 仓库id
     */
    private Long warehouseId;

    /**
     * 仓库/货架编号
     */
    private String number;

    /**
     * 仓位/货架名称
     */
    private String name;

    /**
     * 上级id
     */
    private Long parentId;

    /**
     * 层级深度1级2级3级
     */
    private Integer depth;

    /**
     * 一级类目id
     */
    private Long parentId1;

    /**
     * 二级类目id
     */
    private Long parentId2;

    /**
     * 地址
     */
    private String address;

    /**
     * 备注
     */
    private String remark;

    /**
     * 0正常  1删除
     */
    private Integer isDelete;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    private Long merchantId;

    private static final long serialVersionUID = 1L;
}