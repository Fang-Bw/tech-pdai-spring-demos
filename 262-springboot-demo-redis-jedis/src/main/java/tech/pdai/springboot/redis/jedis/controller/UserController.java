package tech.pdai.springboot.redis.jedis.controller;


import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import tech.pdai.springboot.redis.jedis.entity.User;
import tech.pdai.springboot.redis.jedis.entity.response.ResponseResult;

import javax.annotation.Resource;

/**
 * @author pdai
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private RedisTemplate<String, User> redisTemplate;

    /**
     * @param user user param
     * @return user
     */
    @PostMapping("add")
    public ResponseResult<User> add(User user) {
        redisTemplate.opsForValue().set(String.valueOf(user.getId()), user);
        return ResponseResult.success(redisTemplate.opsForValue().get(String.valueOf(user.getId())));
    }

    /**
     * @return user list
     */
    @GetMapping("find/{userId}")
    public ResponseResult<User> edit(@PathVariable("userId") String userId) {
        return ResponseResult.success(redisTemplate.opsForValue().get(userId));
    }

}
