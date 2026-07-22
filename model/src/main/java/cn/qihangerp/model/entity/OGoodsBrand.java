package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * @TableName o_goods_brand
 */
@Data
public class OGoodsBrand implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 品牌名
     */
    private String name;
    private String num;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 
     */
    private String createBy;

    /**
     * 
     */
    private LocalDateTime createTime;

    /**
     * 
     */
    private String updateBy;

    /**
     * 
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}