package com.xiaorui.xiaoruimailbackend.adapter;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @description: 自定义路径
 * @author: xiaorui
 * @date: 2025-10-27 16:30
 **/
@Component
public class ResourceServerAdapter extends DefaultAuthConfigAdapter {

    @Override
    public List<String> pathPatterns() {
        return Collections.singletonList("/api/*");
    }
}
