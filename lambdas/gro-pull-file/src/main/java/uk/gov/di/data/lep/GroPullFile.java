package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.transport.verification.FingerprintVerifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.exceptions.GroSftpException;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroFileLocations;
import uk.gov.di.data.lep.library.services.AwsService;

import java.io.IOException;
import java.util.EnumSet;

public class GroPullFile implements RequestHandler<Object, GroFileLocations> {
    protected static Logger logger = LogManager.getLogger();
    private final AwsService awsService;
    private final Config config;
    private final String groFileName;

    public GroPullFile() {
        this(new AwsService(), new Config());
    }

    public GroPullFile(AwsService awsService, Config config) {
        this.awsService = awsService;
        this.config = config;

        groFileName = "dept_d_date.xml";
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public GroFileLocations handleRequest(Object event, Context context) {
        var xmlBucket = config.getGroIngestionBucketName();

        transferFile(xmlBucket);

        return new GroFileLocations(xmlBucket, groFileName, null, null);
    }

    private void transferFile(String xmlBucket) {
        var fingerprintID = config.getGroSftpServerFingerprintSecretID();
        var hostID = config.getGroSftpServerHostSecretID();
        var privateKeyID = config.getGroSftpServerPrivateKeySecretID();
        var sourceDirID = config.getGroSftpServerSourceDirSecretID();
        var usernameID = config.getGroSftpServerUsernameSecretID();

        var fingerprint = awsService.getSecret(fingerprintID);
        var host = awsService.getSecret(hostID);
        var privateKey = awsService.getSecret(privateKeyID);
        var sourceDir = awsService.getSecret(sourceDirID);
        var username = awsService.getSecret(usernameID);

        try (var client = new SSHClient()) {
            var privateKeyProvider = client.loadKeys(privateKey, null, null);

            client.addHostKeyVerifier(FingerprintVerifier.getInstance(fingerprint));
            logger.info("Connecting to GRO host");
            client.connect(host);
            client.authPublickey(username, privateKeyProvider);

            logger.info("Pulling file {} from GRO", groFileName);
            try (var sftpClient = client.newSFTPClient()) {
                var resources = sftpClient.ls(sourceDir);
                var sourceFileSearch = resources.stream().filter(r -> r.getPath().endsWith(groFileName)).findFirst();

                if (sourceFileSearch.isEmpty()) {
                    throw new GroSftpException(
                            String.format("File: %s not found on GRO SFTP Server in directory: %s", groFileName, sourceDir)
                    );
                }

                try (var file = sftpClient.open(sourceFileSearch.get().getPath(), EnumSet.of(OpenMode.READ));
                    var fileStream = file.new RemoteFileInputStream()) {
                    logger.info("Uploading file {} to S3", groFileName);

                    awsService.putInBucket(xmlBucket, groFileName, fileStream, file.length());
                }
            }
        } catch (IOException e) {
            throw new GroSftpException(e);
        }
    }
}
