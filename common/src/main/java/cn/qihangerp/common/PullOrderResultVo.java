package cn.qihangerp.common;

import lombok.Data;

@Data
public class PullOrderResultVo {
    private Integer code;
    private String msg;
    private Data data;
    @lombok.Data
    public static class Data{
        private String startTime;
        private String endTime;
        private Integer insert;
        private Integer update;
        private Integer fail;
    }
}
