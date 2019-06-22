package com.leyou.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author bystander
 * @date 2018/9/16
 */
@Data //配置get ,set 方法
@ConfigurationProperties(prefix = "ly.upload")//从yml配置文件中读取数据
@Component
public class UploadProperties {
    private String baseUrl;
    private List<String> allowTypes;
}
