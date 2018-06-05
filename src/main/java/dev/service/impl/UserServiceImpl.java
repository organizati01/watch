package dev.service.impl;

//import dev.cache.JedisClient;
import dev.dao.UserDao;
import dev.dto.LoginResult;
import dev.dto.RegisterResult;
import dev.entity.User;
import dev.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final Logger Log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserDao userDao;

    /*
    @Autowired
    private JedisClient jedisClient;
    */

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public RegisterResult register(User user){
        if(user.getUserName() == null || user.getUserPassword() == null){
            Log.info("用户名或密码为空");
            return new RegisterResult(false, "用户名或密码为空");
        }

        if(user.getUserEmail() != null && checkUserEmailIsExist(user.getUserName())){
            Log.info("邮箱已被使用");
            return new RegisterResult(false, "邮箱已被使用");
        }
        if (checkUserNameIsExist(user.getUserName())){
            Log.info("用户名已被使用");
            return new RegisterResult(false, "用户名已使用");
        }

        try{
            int insertCount = userDao.register(user);
            if(insertCount > 0){
                Log.info("注册成功");
                return new RegisterResult(true, "注册成功");
            }
            else {
                Log.info("用户注册错误");
                return new RegisterResult(false, "用户注册错误");
            }
        }catch (Exception e){
            Log.error(e.getMessage(), e);
            return new RegisterResult(false, "e");
        }
    }

    @Override
    public Boolean checkUserNameIsExist(String userName){
        User user = userDao.selectByUserName(userName);
        if (user != null) {
            Log.info("User name exists.");
            return true;
        } else {
            Log.info("User name doesn't exist.");
            return false;
        }
    }

    @Override
    public Boolean checkUserEmailIsExist(String userEmail){
        User user = userDao.selectByUserName(userEmail);
        if (user != null) {
            Log.info("User email exists.");
            return true;
        } else {
            Log.info("User email doesn't exist.");
            return false;
        }
    }


    @Override
    public LoginResult login(User user) {
        User newUser = userDao.selectByUserName(user.getUserName());
        if (newUser == null) {
            Log.info("登录失败，用户不存在");
            return new LoginResult(false, "登录失败，用户不存在");
        }
        if (!user.getUserPassword().equals(newUser.getUserPassword())) {
            Log.info("登录失败，账户或密码错误");
            return new LoginResult(false, "登录失败，账户或密码错误");
        } else {
            return new LoginResult(true, "登录成功");
        }
    }

    @Override
    public User getUserById(long userId) {
        User user = (User) redisTemplate.opsForValue().get(userId);
        if(user == null){
            user = userDao.selectById(userId);
            redisTemplate.opsForValue().set(userId, user);
            return user;
        }
        else {
            return user;
        }
    }

    @Override
    public User getUserByName(String userName) {
        User user = (User) redisTemplate.opsForValue().get(userName);
        if(user == null){
            user = userDao.selectByUserName(userName);
            redisTemplate.opsForValue().set(userName, user);
            return user;
        }
        else {
            return user;
        }
    }
}
