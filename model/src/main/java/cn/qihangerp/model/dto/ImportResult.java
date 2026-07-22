package cn.qihangerp.model.dto;

import lombok.Data;

@Data
public class ImportResult {
    
    private int successCount;
    
    private int duplicateCount;
    
    private int failCount;
    
    private String message;
    
    public static ImportResult success(int successCount, int duplicateCount) {
        ImportResult result = new ImportResult();
        result.setSuccessCount(successCount);
        result.setDuplicateCount(duplicateCount);
        result.setFailCount(0);
        result.setMessage("导入完成：成功" + successCount + "条，跳过重复" + duplicateCount + "条");
        return result;
    }
    
    public static ImportResult fail(int successCount, int duplicateCount, int failCount) {
        ImportResult result = new ImportResult();
        result.setSuccessCount(successCount);
        result.setDuplicateCount(duplicateCount);
        result.setFailCount(failCount);
        result.setMessage("导入完成：成功" + successCount + "条，跳过重复" + duplicateCount + "条，失败" + failCount + "条");
        return result;
    }
}
