package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台任务配置表
 * @TableName sys_task
 */
@Data
public class SysTask implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String taskName;

    /**
     * 
     */
    private String cron;

    /**
     * 
     */
    private String method;

    /**
     * 
     */
    private String remark;

    /**
     * 
     */
    private LocalDateTime createTime;

    private Integer status;

    private static final long serialVersionUID = 1L;
}