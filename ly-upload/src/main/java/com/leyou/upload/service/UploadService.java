package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;

import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
//@EnableConfigurationProperties(UploadProperties.class) //使用配置类
public class UploadService {

    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private UploadProperties properties;
    //准备image的格式

    public String uploadImage(MultipartFile file) {
        try {//校验文件类型
            String contentType = file.getContentType();
            //校验文件内容
            if (!properties.getAllowTypes().contains(contentType)) {
                throw new LyException(ExceptionEnums.VALIDATE_IMAGE_ERROR);
            }
            //校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new LyException(ExceptionEnums.VALIDATE_IMAGE_ERROR);
            }
            //准备目标路径
//            File target = new File("D:\\image", file.getOriginalFilename());
            //保存文件到本地
//            file.transferTo(target);
//            String extense = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
            String extense = StringUtils.substringAfterLast(file.getOriginalFilename(),".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extense, null);
            return properties.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
            //上传失败
            log.error("文件上传失败", e);
            throw new LyException(ExceptionEnums.UPLOAD_IMAGE_ERROR);
        }
    }
}
