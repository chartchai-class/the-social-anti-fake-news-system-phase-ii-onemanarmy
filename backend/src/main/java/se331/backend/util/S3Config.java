package se331.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner; // New import needed

@Configuration
public class S3Config {
    // Access key id will be read from the application.properties file during the application initialization
    @Value ("768d163f1e8fb08c0e42c99f434da1ba")
    private String accessKeyId;

    // Secret access key will be read from the application
    @Value ("49762e599f7147bd4caf2c6fde4f16f82b92f0ac439ad1c82de8a6836433179c")
    private String secretAccessKey;

    @Value ("ap-southeast-1")
    private String region;

    @Value ("https://mainpphpojnagcuvfpqo.storage.supabase.co/storage/v1/s3")
    private String endpoint;

    @Bean
    public S3Client getAmazonS3Client() {
        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return S3Client.builder()
                .credentialsProvider(() -> credentials)
                .region(software.amazon.awssdk.regions.Region.of(region))
                .endpointOverride(java.net.URI.create(endpoint))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    public S3Presigner getS3Presigner() {
        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return S3Presigner.builder()
                .credentialsProvider(() -> credentials)
                .region(software.amazon.awssdk.regions.Region.of(region))
                .endpointOverride(java.net.URI.create(endpoint))
                .build();
    }
}
