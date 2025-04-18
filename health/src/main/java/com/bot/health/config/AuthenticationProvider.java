package com.bot.health.config;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Supplier;

@Slf4j
@Service
public class AuthenticationProvider {

    @Value("${oci.user}")
    private String userId= "ocid1.user.oc1..aaaaaaaa6xwefmvhpq4xkg35rhdxudvugx5432h7gdja5662vjmqf3ib6p4q";

    @Value("${oci.tenancyId}")
    private String tenancyId="ocid1.tenancy.oc1..aaaaaaaav4yomkxnpw6nhqpvf44jypezev3cr33wnzrqmfgjimucblf23thq";

    @Value("${oci.fingerprint}")
    private String fingerprint="62:84:51:5b:f5:b9:cd:72:66:31:ca:fd:5c:c4:5f:e0";

    @Value("${oci.privateKey}")
    private String privateKey="src/main/resources/private_key.pem";

    @Value("${oci.region}")
    private String region="us-ashburn-1";

    private AuthenticationDetailsProvider authProvider;

    public AuthenticationDetailsProvider getAuthenticationProvider() {

        File tempFile = null;
        try {
            if (authProvider == null) {

                tempFile = writeTempOCICertFile(privateKey);
                Supplier<InputStream> privateKeySupplier = new SimplePrivateKeySupplier(tempFile.getAbsolutePath());
                authProvider = SimpleAuthenticationDetailsProvider.builder()
                        .tenantId(tenancyId)
                        .userId(userId)
                        .fingerprint(fingerprint)
                        .privateKeySupplier(privateKeySupplier)
                        .region(Region.valueOf(region))
                        .build();
            }
            return authProvider;
        } catch (Exception e) {
            log.error("Error occurred while creating Stream Client. Due to: {}", e.getMessage(), e);
            throw e;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                try {
                    if (Files.deleteIfExists(tempFile.toPath())) {
                        log.info("Temp File is deleted.");
                    } else {
                        log.info("Failed to delete temp file.");
                    }
                } catch (IOException e) {
                    log.error("Failed to delete temp file. Due to: {}", e.getMessage(), e);
                }
            }
        }
    }

    static File writeTempOCICertFile(String data) {
        File tmpFile = null;
        FileWriter writer = null;
        try {
            tmpFile = File.createTempFile("temp-file", ".pem");
            writer = new FileWriter(tmpFile);
            writer.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return tmpFile;
    }


}