package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import net.schmizz.sshj.SSHClient;
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
    private final String FILE_NAME;

    public PullFile() {
        this(new AwsService(), new Config());
    }

    public PullFile(AwsService awsService, Config config) {
        this.awsService = awsService;
        this.config = config;

        FILE_NAME = "<dept>_d_<date>.xml";
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public GroFileLocations handleRequest(Object event, Context context) {
        logger.info("Pulling file {} from GRO", FILE_NAME);
        var inFile = new FileSystemFile(FILE_NAME);
        downloadFile(inFile);

        logger.info("Uploading file {} to S3", FILE_NAME);
        var xmlBucket = config.getGroIngestionBucketName();
        awsService.putInBucket(xmlBucket, FILE_NAME, inFile.getFile());

        return new GroFileLocations(xmlBucket, FILE_NAME, null, null);
    }

    private void downloadFile(FileSystemFile inFile) {
        var hostname = config.getGroSftpServerHost();
        var privateKeyId = config.getGroSftpServerPrivateKeySecretId();
        var sourceDir = config.getGroSftpServerSourceDir();
        var username = config.getGroSftpServerUsername();

        var privateKey = awsService.getSecret(privateKeyId);

        try (var client = new SSHClient()) {
            var privateKeyFile = File.createTempFile("privateKey", "tmp").toPath();
            Files.writeString(privateKeyFile, privateKey);

            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.connect(hostname);
            var keys = client.loadKeys(privateKeyFile.toString());
            client.authPublickey(username, keys);

            try (var sftpClient = client.newSFTPClient()) {
                var resources = sftpClient.ls(sourceDir);
                var sourceFileSearch = resources.stream().filter(r -> r.getPath().endsWith(FILE_NAME)).findFirst();

                if (sourceFileSearch.isPresent()) {
                    sftpClient.get(sourceFileSearch.get().getPath(), inFile);
                } else {
                    throw new GroSftpException(
                        String.format("File: %s not found on GRO SFTP Server in directory: %s", FILE_NAME, sourceDir)
                    );
                }
            }
        } catch (IOException e) {
            throw new GroSftpException(e);
        }
    }
}
