package uk.gov.di.data.lep;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.FingerprintVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.dto.Overrides;
import uk.gov.di.data.lep.exceptions.GroSftpException;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.services.AwsService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GroPullFileTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final GroPullFile underTest = new GroPullFile(awsService, config);

    private final Overrides emptyOverrides = new Overrides(null);
    private static final String ingestionBucket = "GroIngestionBucket";
    private static final String fingerprint = "fingerprint";
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
        when(config.getGroSftpServerFingerprintSecretID()).thenReturn("FingerprintSecretID");
        when(config.getGroSftpServerHostSecretID()).thenReturn("HostSecretID");
        when(config.getGroSftpServerPrivateKeySecretID()).thenReturn("PrivateKeySecretID");
        when(config.getGroSftpServerSourceDirSecretID()).thenReturn("SourceDirSecretID");
        when(config.getGroSftpServerUsernameSecretID()).thenReturn("UsernameSecretID");
        when(awsService.getSecret("FingerprintSecretID")).thenReturn(fingerprint);
        when(awsService.getSecret("HostSecretID")).thenReturn(hostname);
        when(awsService.getSecret("PrivateKeySecretID")).thenReturn(privateKey);
        when(awsService.getSecret("SourceDirSecretID")).thenReturn(sourceDir);
        when(awsService.getSecret("UsernameSecretID")).thenReturn(username);
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
        try (var awsService = mockConstruction(AwsService.class);
             var config = mockConstruction(Config.class)) {
            new GroPullFile();
            assertEquals(1, awsService.constructed().size());
            assertEquals(1, config.constructed().size());
        }
    }

    @Test
    void pullFileDownloadsFile() throws IOException {
        var testDate = LocalDate.parse("2022-01-01");
        try (var ignored = mockConstruction(SSHClient.class, (client, context) -> {
                when(client.loadKeys("PrivateSSHKey", null, null)).thenReturn(keyProvider);
                when(client.newSFTPClient()).thenReturn(sftpClient);
            });
             var staticMockFingerprintVerifier = mockStatic(FingerprintVerifier.class);
             var staticMockLocalDate = mockStatic(LocalDate.class)) {
            var fingerprintVerifier = mock(FingerprintVerifier.class);

            staticMockFingerprintVerifier.when(() -> FingerprintVerifier.getInstance(fingerprint)).thenReturn(fingerprintVerifier);
            staticMockLocalDate.when(LocalDate::now).thenReturn(testDate);

            var remoteFile = mock(RemoteFile.class);
            when(sftpClient.ls(sourceDir)).thenReturn(List.of(remoteResourceInfo));
            when(remoteResourceInfo.getPath()).thenReturn(String.format("%s/DI_D_2022-01-01.xml", sourceDir));
            when(sftpClient.open(remoteResourceInfo.getPath(), EnumSet.of(OpenMode.READ))).thenReturn(remoteFile);
            when(remoteFile.length()).thenReturn(5L);

            underTest.handleRequest(emptyOverrides, null);

            verify(awsService).putInBucket(eq(ingestionBucket), eq("DI_D_2022-01-01.xml"), any(), eq(5L));
        }
    }

    @Test
    void pullFileWithOverridesDownloadsFile() throws IOException {
        try (var ignored = mockConstruction(SSHClient.class, (client, context) -> {
            when(client.loadKeys("PrivateSSHKey", null, null)).thenReturn(keyProvider);
            when(client.newSFTPClient()).thenReturn(sftpClient);
        });
             var staticMockFingerprintVerifier = mockStatic(FingerprintVerifier.class)) {
            var fingerprintVerifier = mock(FingerprintVerifier.class);

            staticMockFingerprintVerifier.when(() -> FingerprintVerifier.getInstance(fingerprint)).thenReturn(fingerprintVerifier);

            var remoteFile = mock(RemoteFile.class);
            when(sftpClient.ls(sourceDir)).thenReturn(List.of(remoteResourceInfo));
            when(remoteResourceInfo.getPath()).thenReturn(String.format("%s/fileName.xml", sourceDir));
            when(sftpClient.open(remoteResourceInfo.getPath(), EnumSet.of(OpenMode.READ))).thenReturn(remoteFile);
            when(remoteFile.length()).thenReturn(5L);

            underTest.handleRequest(new Overrides("fileName.xml"), null);

            verify(awsService).putInBucket(eq(ingestionBucket), eq("fileName.xml"), any(), eq(5L));
        }
    }

    @Test
    void pullFileThrowsExceptionIfNoFileExists() throws IOException {
        var testDate = LocalDate.parse("2022-01-01");
        try (var ignored = mockConstruction(SSHClient.class, (client, context) -> {
            when(client.loadKeys(privateKey, null, null)).thenReturn(keyProvider);
            when(client.newSFTPClient()).thenReturn(sftpClient);
        });
             var staticMockFingerprintVerifier = mockStatic(FingerprintVerifier.class);
             var staticMockLocalDate = mockStatic(LocalDate.class)) {
            var fingerprintVerifier = mock(FingerprintVerifier.class);

            staticMockFingerprintVerifier.when(() -> FingerprintVerifier.getInstance(fingerprint)).thenReturn(fingerprintVerifier);
            staticMockLocalDate.when(LocalDate::now).thenReturn(testDate);
            when(sftpClient.ls(sourceDir)).thenReturn(List.of());

            var exception = assertThrows(GroSftpException.class, () -> underTest.handleRequest(emptyOverrides, null));

            assertEquals(String.format("File: DI_D_2022-01-01.xml not found on GRO SFTP Server in directory: %s", sourceDir), exception.getMessage());
            verify(awsService, never()).putInBucket(any(), any(), any(), anyLong());
        }
    }

    @Test
    void pullFileThrowsExceptionIfSshConnectionFails() throws IOException {
        try (var ignored = mockConstruction(SSHClient.class, (client, context) -> {
            when(client.loadKeys(privateKey, null, null)).thenReturn(keyProvider);
            when(client.newSFTPClient()).thenReturn(sftpClient);
        });
             var staticMockFingerprintVerifier = mockStatic(FingerprintVerifier.class)) {
            var fingerprintVerifier = mock(FingerprintVerifier.class);

            staticMockFingerprintVerifier.when(() -> FingerprintVerifier.getInstance(fingerprint)).thenReturn(fingerprintVerifier);
            var innerException = new IOException();
            when(sftpClient.ls(sourceDir)).thenThrow(innerException);

            var exception = assertThrows(GroSftpException.class, () -> underTest.handleRequest(emptyOverrides, null));

            assertEquals(innerException, exception.getCause());
            verify(awsService, never()).putInBucket(any(), any(), any(), anyLong());
        }
    }
}
