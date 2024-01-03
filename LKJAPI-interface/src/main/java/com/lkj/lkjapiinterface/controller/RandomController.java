package com.lkj.lkjapiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.lkj.lkjapiinterface.entity.ImageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/random")
public class RandomController {
    /**
     * 随机毒鸡汤
     * @return
     */
    @GetMapping("/word")
    public String getRandomWork(){
        HttpResponse response = HttpRequest.get("https://tenapi.cn/v2/yiyan")
                .execute();
        return response.body();
    }

    /**
     * 随机图片
     * @return
     */
    @PostMapping("/image")
    public String getRandomImageUrl(){
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("format","json");
        HttpResponse response = HttpRequest.post("https://tenapi.cn/v2/acg")
                .form(paramMap)
                .execute();
        String body = response.body();
        ImageResponse imageResponse = JSONUtil.toBean(body, ImageResponse.class);
        return imageResponse.getData().getUrl();
    }

    /**
     * 随机土味情话
     * @return
     */
    @GetMapping("/loveword")
    public String getRandomLove(){
        HttpResponse response = HttpRequest.get("https://api.vvhan.com/api/love")
                .execute();
        return response.body();
    }
}
