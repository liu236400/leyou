package com.leyou.upload.uploadService;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import com.leyou.upload.web.UploadController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

@Service
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService_cfg {
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    @Autowired
    private FastFileStorageClient storageClient;
    //注入配置类
    @Autowired
    private UploadProperties prop;

    // 支持的文件类型,asList可变数组
    private static final List<String> ALLOW_TYPES = Arrays.asList("image/png", "image/jpeg","image/bmp");

    public String upload(MultipartFile file) {
        try {
            // 1、图片信息校验
            // 1)校验文件类型
            String type = file.getContentType();
            //if (!ALLOW_TYPES.contains(type)) {
            if (! prop.getAllowTypes().contains(type)) {
                logger.info("上传失败，文件类型不匹配：{}", type);
                throw new LyException(ExceptionEnum.INAVLID_FILE_TYPE);
            }
            // 2)校验图片内容
            BufferedImage image = ImageIO.read(file.getInputStream());//是否为图片流
            if (image == null) {
                logger.error("上传失败，文件内容不符合要求");
                throw new LyException(ExceptionEnum.INAVLID_FILE_TYPE);
            }
/*            // 2、保存图片
            // 2.1、生成保存目录
            File dir = new File("D:\\heima\\upload");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 2.2、保存图片
            file.transferTo(new File(dir, file.getOriginalFilename()));*/

            //上传到FastDFS
            //String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);      //从原始名称中获取后缀名
            //在最后一个点后面截取，截取效率高
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);

            // 2.3、拼接图片地址
            String url = prop.getBaseUrl() + storePath.getFullPath();
            return url;
        } catch (Exception e) {
            logger.error("上传失败==last",e);
            return null;
        }
    }
}
