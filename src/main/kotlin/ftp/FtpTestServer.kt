package ftp

import org.mockftpserver.fake.FakeFtpServer
import org.mockftpserver.fake.UserAccount
import org.mockftpserver.fake.filesystem.DirectoryEntry
import org.mockftpserver.fake.filesystem.FileEntry
import org.mockftpserver.fake.filesystem.FileSystem
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem

fun main(args: Array<String>) {
  val fakeFtpServer = FakeFtpServer()
  fakeFtpServer.addUserAccount(UserAccount("user", "password", "/data"))

  val fileSystem: FileSystem = UnixFakeFileSystem()
  fileSystem.add(DirectoryEntry("/data"))
  fileSystem.add(DirectoryEntry("/archive"))
  fileSystem.add(FileEntry("/data/death1.csv", "MULTIPLE,Tester One,1910-01-01,2010-01-01,Male"))
  fakeFtpServer.fileSystem = fileSystem
  fakeFtpServer.serverControlPort = 31000

  fakeFtpServer.start()

  println("FTP started on : " + fakeFtpServer.serverControlPort)
}
