package uk.gov.di.data.lep;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.services.AwsService;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PullFileTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final PullFile underTest = new PullFile(awsService, config);

    private static final String ingestionBucket = "GroIngestionBucket";
    private static final String hostname = "hostname";
    private static final String sourceDir = "sourceDir";
    private static final KeyProvider keyProvider = mock(KeyProvider.class);
    private static final SFTPClient sftpClient = mock(SFTPClient.class);
    private static final RemoteResourceInfo remoteResourceInfo = mock(RemoteResourceInfo.class);

    @BeforeAll
    static void setup() throws IOException {
        when(config.getGroIngestionBucketName()).thenReturn("GroIngestionBucket");
        when(config.getGroSftpServerHost()).thenReturn(hostname);
        when(config.getGroSftpServerPrivateKeySecretId()).thenReturn("SecretId");
        when(config.getGroSftpServerSourceDir()).thenReturn(sourceDir);
        when(config.getGroSftpServerUsername()).thenReturn("test_user");
        when(awsService.getSecret("SecretId")).thenReturn("PrivateSSHKey");

        when(sftpClient.ls(sourceDir)).thenReturn(List.of(remoteResourceInfo));
        when(remoteResourceInfo.getPath()).thenReturn(String.format("%s/dept_d_date.xml", sourceDir));
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        var awsService = mockConstruction(AwsService.class);
        var config = mockConstruction(Config.class);
        new PullFile();
        assertEquals(1, awsService.constructed().size());
        assertEquals(1, config.constructed().size());
    }

    @Test
    void pullFileDownloadsFile() {
        mockConstruction(SSHClient.class, (client, context) -> {
            when(client.loadKeys("PrivateSSHKey", null, null)).thenReturn(keyProvider);
            when(client.newSFTPClient()).thenReturn(sftpClient);
        });

        underTest.handleRequest(null, null);

        verify(awsService).putInBucket(eq(ingestionBucket), eq("dept_d_date.xml"), any(File.class));
    }
}
