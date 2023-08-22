package uk.gov.di.data.lep;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.exceptions.GroSftpException;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.services.AwsService;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PullFileTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final PullFile underTest = new PullFile(awsService, config);

    private static final String ingestionBucket = "GroIngestionBucket";
    private static final String hostname = "hostname";
    private static final String sourceDir = "sourceDir";
    private static final String privateKey = "PrivateSSHKey";
    private static final String username = "test_user";
    private static final KeyProvider keyProvider = mock(KeyProvider.class);
    private static final SFTPClient sftpClient = mock(SFTPClient.class);
    private static final RemoteResourceInfo remoteResourceInfo = mock(RemoteResourceInfo.class);

    @BeforeAll
    static void setup() {
        when(config.getGroIngestionBucketName()).thenReturn("GroIngestionBucket");
        when(config.getGroSftpServerHost()).thenReturn(hostname);
        when(config.getGroSftpServerPrivateKeySecretId()).thenReturn("SecretId");
        when(config.getGroSftpServerSourceDir()).thenReturn(sourceDir);
        when(config.getGroSftpServerUsername()).thenReturn(username);
        when(awsService.getSecret("SecretId")).thenReturn(privateKey);
    }

    @BeforeEach
    void resetMocks() {
        clearInvocations(awsService);
        clearInvocations(config);
        reset(sftpClient);
        reset(remoteResourceInfo);
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
    void pullFileDownloadsFile() throws IOException {
        try (var ignored = mockConstruction(SSHClient.class, (client, context) -> {
            when(client.loadKeys("PrivateSSHKey", null, null)).thenReturn(keyProvider);
            when(client.newSFTPClient()).thenReturn(sftpClient);
        })) {
            var remoteFile = mock(RemoteFile.class);
            when(sftpClient.ls(sourceDir)).thenReturn(List.of(remoteResourceInfo));
            when(remoteResourceInfo.getPath()).thenReturn(String.format("%s/dept_d_date.xml", sourceDir));
            when(sftpClient.open(remoteResourceInfo.getPath(), EnumSet.of(OpenMode.READ))).thenReturn(remoteFile);
            when(remoteFile.length()).thenReturn(5L);

            underTest.handleRequest(null, null);

            verify(awsService).putInBucket(eq(ingestionBucket), eq("dept_d_date.xml"), any(), eq(5L));
        }
    }

    @Test
    void pullFileThrowsExceptionIfNoFileExists() throws IOException {
        try (var ignored = mockConstruction(SSHClient.class, (client, context) -> {
            when(client.loadKeys(privateKey, null, null)).thenReturn(keyProvider);
            when(client.newSFTPClient()).thenReturn(sftpClient);
        })) {
            when(sftpClient.ls(sourceDir)).thenReturn(List.of());

            var exception = assertThrows(GroSftpException.class, () -> underTest.handleRequest(null, null));

            assertEquals(String.format("File: dept_d_date.xml not found on GRO SFTP Server in directory: %s", sourceDir), exception.getMessage());
            verify(awsService, never()).putInBucket(any(), any(), any(), anyLong());
        }
    }

    @Test
    void pullFileThrowsExceptionIfSshConnectionFails() throws IOException {
        try (var ignored = mockConstruction(SSHClient.class, (client, context) -> {
            when(client.loadKeys(privateKey, null, null)).thenReturn(keyProvider);
            when(client.newSFTPClient()).thenReturn(sftpClient);
        })) {
            var innerException = new IOException();
            when(sftpClient.ls(sourceDir)).thenThrow(innerException);

            var exception = assertThrows(GroSftpException.class, () -> underTest.handleRequest(null, null));

            assertEquals(innerException, exception.getCause());
            verify(awsService, never()).putInBucket(any(), any(), any(), anyLong());
        }
    }
}
