package com.project.Edu.Assist.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        if (cloudinary.config.cloudName == null || cloudinary.config.cloudName.isEmpty()) {
            throw new RuntimeException("Cloudinary is not configured");
        }
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "edu-assist")
        );
        return (String) uploadResult.get("secure_url");
    }
}
