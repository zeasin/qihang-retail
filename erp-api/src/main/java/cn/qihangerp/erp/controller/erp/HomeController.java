package cn.qihangerp.erp.controller.erp;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @GetMapping("/")
    public String home(){
        return "{'code':0,'msg':'erp-api请通过api访问'}";
    }
}