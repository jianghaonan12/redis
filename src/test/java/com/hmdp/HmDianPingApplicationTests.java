package com.hmdp;


import com.hmdp.entity.User;
import com.hmdp.service.impl.ShopServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private ShopServiceImpl shopService;
    @Test
   void save() throws InterruptedException {
        shopService.save2Redis(1L);
    }
    @Test
    void test(){

    }
}
