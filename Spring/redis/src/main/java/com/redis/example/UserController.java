package com.redis.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserDao userDao;

    @PostMapping
    public User createUser(@RequestBody User user){
        user.setUserId(UUID.randomUUID().toString());
        return userDao.saveUser(user);
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable String userId){
        return userDao.get(userId);
    }

    @GetMapping()
    public Map<Object, Object> getAll(){
        return userDao.findAll();
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable String userId){
        userDao.delete(userId);
    }
}
