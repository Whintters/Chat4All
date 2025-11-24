package com.chat4all.frontend.controller;

import com.chat4all.frontend.model.FileMetadataEntity;
import com.chat4all.frontend.repository.FileMetadataRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/v1/files")
public class FileController {

    private final MinioClient minioClient;
    private final FileMetadataRepository fileRepository;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public FileController(@Value("${minio.url}") String url,
                          @Value("${minio.access-key}") String accessKey,
                          @Value("${minio.secret-key}") String secretKey,
                          FileMetadataRepository fileRepository) {
        this.fileRepository = fileRepository;
        this.minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<FileResponse> uploadFile(
            @RequestPart("file") FilePart filePart,
            @RequestPart("uploaderId") String uploaderId,       // Novo parâmetro
            @RequestPart("conversationId") String conversationId // Novo parâmetro
    ) {
        String fileId = UUID.randomUUID().toString();
        String filename = fileId + "_" + filePart.filename();

        return filePart.content()
                .reduce(new java.io.ByteArrayOutputStream(), (os, buffer) -> {
                    try {
                        byte[] bytes = new byte[buffer.readableByteCount()];
                        buffer.read(bytes);
                        os.write(bytes);
                        return os;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(os -> new java.io.ByteArrayInputStream(os.toByteArray()))
                .flatMap(is -> Mono.fromCallable(() -> {
                    long size = is.available();
                    String contentType = String.valueOf(filePart.headers().getContentType());

                    // 1. Upload para o MinIO
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(filename)
                                    .stream(is, size, -1)
                                    .contentType(contentType)
                                    .build());
                    
                    // 2. Gerar URL assinada para download (Válida por 24h)
                    String url = minioClient.getPresignedObjectUrl(
                            GetPresignedObjectUrlArgs.builder()
                                    .method(Method.GET)
                                    .bucket(bucketName)
                                    .object(filename)
                                    .expiry(60 * 60 * 24)
                                    .build());

                    // 3. Salvar Metadados Completos no Cassandra
                    FileMetadataEntity entity = new FileMetadataEntity();
                    entity.setFileId(fileId);
                    entity.setFilename(filePart.filename());
                    entity.setContentType(contentType);
                    entity.setSize(size);
                    entity.setDownloadUrl(url);
                    // Vinculando ao usuário e conversa
                    entity.setUploaderId(uploaderId);
                    entity.setConversationId(conversationId);
                    
                    entity.setCreatedAt(Instant.now());
                    
                    fileRepository.save(entity).subscribe();

                    return new FileResponse(fileId, url);
                }));
    }
}

record FileResponse(String fileId, String downloadUrl) {}