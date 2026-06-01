package com.sms.api.storage;

import com.sms.core.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

/**
 * All file storage operations use S3 keys, never public URLs.
 * Call generatePresignedUrl(key, duration) to produce a time-limited download link.
 * Default presign expiry is 15 minutes per security policy.
 */
@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;
    private final Duration defaultPresignExpiry;

    public S3Service(
        S3Client s3Client,
        S3Presigner presigner,
        @Value("${aws.s3.bucket}") String bucket,
        @Value("${aws.s3.presign-expiry-minutes:15}") long presignMinutes
    ) {
        this.s3Client           = s3Client;
        this.presigner          = presigner;
        this.bucket             = bucket;
        this.defaultPresignExpiry = Duration.ofMinutes(presignMinutes);
    }

    /**
     * Upload a file to S3 under the given key prefix.
     * @param file      the uploaded file
     * @param keyPrefix e.g. "schools/{schoolId}/students/{studentId}/docs/BIRTH_CERTIFICATE"
     * @return the full S3 object key (store this, not the URL)
     */
    public String upload(MultipartFile file, String keyPrefix) {
        String key = keyPrefix + "/" + UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
        try {
            PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();
            s3Client.putObject(req, RequestBody.fromBytes(file.getBytes()));
            log.debug("Uploaded file to S3 key: {}", key);
            return key;
        } catch (IOException e) {
            throw new StorageException("Failed to upload file to S3", e);
        }
    }

    /**
     * Generate a presigned GET URL valid for the default 15-minute window.
     */
    public String generatePresignedUrl(String s3Key) {
        return generatePresignedUrl(s3Key, defaultPresignExpiry);
    }

    /**
     * Generate a presigned GET URL with a custom expiry duration.
     */
    public String generatePresignedUrl(String s3Key, Duration expiry) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .getObjectRequest(r -> r.bucket(bucket).key(s3Key))
                .build();
            URL url = presigner.presignGetObject(presignRequest).url();
            return url.toString();
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned URL for key: " + s3Key, e);
        }
    }

    /**
     * Delete an object from S3 by key.
     */
    public void delete(String s3Key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build());
            log.debug("Deleted S3 object: {}", s3Key);
        } catch (Exception e) {
            throw new StorageException("Failed to delete S3 object: " + s3Key, e);
        }
    }

    private String sanitize(String filename) {
        if (filename == null) return "unknown";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
