package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.exceptions.GroSftpException;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroFileLocations;
import uk.gov.di.data.lep.library.services.AwsService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PullFile implements RequestHandler<Object, GroFileLocations> {
    protected static Logger logger = LogManager.getLogger();
    private final AwsService awsService;
    private final Config config;
    private final String groFileName;

    public PullFile() {
        this(new AwsService(), new Config());
    }

    public PullFile(AwsService awsService, Config config) {
        this.awsService = awsService;
        this.config = config;

        groFileName = "dept_d_date.xml";
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public GroFileLocations handleRequest(Object event, Context context) {
        logger.info("Pulling file {} from GRO", groFileName);
        var inFile = new FileSystemFile(groFileName);
        downloadFile(inFile);

        logger.info("Uploading file {} to S3", groFileName);
        var xmlBucket = config.getGroIngestionBucketName();
        awsService.putInBucket(xmlBucket, groFileName, inFile.getFile());

        try {
            Files.delete(inFile.getFile().toPath());
        }
        catch (IOException e) {
            logger.info("Failed to delete file {}", groFileName);
        }

        return new GroFileLocations(xmlBucket, groFileName, null, null);
    }

    private void downloadFile(FileSystemFile inFile) {
        var hostname = config.getGroSftpServerHost();
        var privateKeyId = config.getGroSftpServerPrivateKeySecretId();
        var sourceDir = config.getGroSftpServerSourceDir();
        var username = config.getGroSftpServerUsername();

        var privateKeyContent = awsService.getSecret(privateKeyId);

        try (var client = new SSHClient()) {
            var privateKeyFile = File.createTempFile("privateKey", ".pem");
            Files.writeString(privateKeyFile.toPath(), privateKeyContent);
            var privateKeyProvider = client.loadKeys(privateKeyFile.getPath());

            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.connect(hostname);
            client.authPublickey(username, privateKeyProvider);

            try (var sftpClient = client.newSFTPClient()) {
                var resources = sftpClient.ls(sourceDir);
                var sourceFileSearch = resources.stream().filter(r -> r.getPath().endsWith(groFileName)).findFirst();

                if (sourceFileSearch.isPresent()) {
                    sftpClient.get(sourceFileSearch.get().getPath(), inFile);
                } else {
                    throw new GroSftpException(
                        String.format("File: %s not found on GRO SFTP Server in directory: %s", groFileName, sourceDir)
                    );
                }
            }
        } catch (IOException e) {
            throw new GroSftpException(e);
        }
    }
}
