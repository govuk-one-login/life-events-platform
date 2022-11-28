package uk.gov.gdx.datashare.service

import org.apache.commons.net.ftp.FTPClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockftpserver.fake.FakeFtpServer
import org.mockftpserver.fake.UserAccount
import org.mockftpserver.fake.filesystem.DirectoryEntry
import org.mockftpserver.fake.filesystem.FileEntry
import org.mockftpserver.fake.filesystem.FileSystem
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

class FtpServerTest() {
  private val fakeFtpServer: FakeFtpServer = FakeFtpServer()

  @BeforeEach
  fun setup() {

    fakeFtpServer.addUserAccount(UserAccount("user", "password", "/data"))

    val fileSystem: FileSystem = UnixFakeFileSystem()
    fileSystem.add(DirectoryEntry("/data"))
    fileSystem.add(DirectoryEntry("/archive"))
    fileSystem.add(FileEntry("/data/foobar1.txt", "abcdef 1234567890-1"))
    fileSystem.add(FileEntry("/data/foobar2.txt", "abcdef 1234567890-2"))
    fileSystem.add(FileEntry("/data/foobar3.txt", "abcdef 1234567890-3"))
    fileSystem.add(FileEntry("/data/foobar4.txt", "abcdef 1234567890-4"))
    fakeFtpServer.fileSystem = fileSystem
    fakeFtpServer.serverControlPort = 0

    fakeFtpServer.start()

    println("FTP started on : " + fakeFtpServer.serverControlPort)
  }

  @AfterEach
  fun closeDown() {
    println("Stopping")
    fakeFtpServer.stop()
  }

  @Test
  fun `can connect to client`() {
    val testFtpClient = FTPClient()
    val host = "localhost"
    val port = fakeFtpServer.serverControlPort
    println("Connecting to $host on $port")
    testFtpClient.connect(host, port)
    testFtpClient.login("user", "password")

    listFiles(ftpClient = testFtpClient).forEach {
      val newLocation = "/archive/${it.first}"
      testFtpClient.rename(it.first, newLocation)
      println("Retrieving file $newLocation")
      val fileHandle = FileOutputStream(it.first)
      testFtpClient.retrieveFile(newLocation, fileHandle)
      val file = File(it.first)
      assertThat(file).exists()
      file.delete()
    }

    assertThat(listFiles(ftpClient = testFtpClient, path = "/data")).isEmpty()
    assertThat(listFiles(ftpClient = testFtpClient, path = "/archive")).hasSize(4)

    testFtpClient.logout()
  }

  @Test
  fun `can connect to remote client`() {
    val testFtpClient = FTPClient()
    val host = "demo.wftpserver.com"
    val port = 21
    println("Connecting to $host on $port")
    testFtpClient.connect(host, port)
    testFtpClient.login("demo", "demo")

    listFiles(ftpClient = testFtpClient, path = "/download").forEach {
      println("Retrieving file ${it.first}, timestamp = ${it.second}")
      val fileHandle = FileOutputStream(it.first)
      testFtpClient.retrieveFile(it.first, fileHandle)
      val file = File(it.first)
      assertThat(file).exists()
      file.delete()
    }

    testFtpClient.logout()
  }

  private fun listFiles(ftpClient: FTPClient, path: String? = null): List<Pair<String, LocalDateTime>> {
    return ftpClient.listFiles(path)
      .map { fp -> Pair(fp.name, LocalDateTime.ofInstant(fp.timestamp.toInstant(), fp.timestamp.timeZone.toZoneId())) }
      .sortedBy { it.second }
      .toList()
  }
}
