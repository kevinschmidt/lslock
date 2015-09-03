package eu.stupidsoup.lslock

import java.io.{ RandomAccessFile, File }
import java.nio.charset.StandardCharsets
import java.nio.file.{ Paths, Files }

import org.specs2.Specification
import org.specs2.specification.BeforeAfterAll

import scala.sys.process.Process

class LsLockSpec extends Specification with BeforeAfterAll {
  def is = sequential ^
    s2"""
      LsLockSpec
      ============

        create one lock                              ${oneLock}
        create one lock in subdir                    ${oneLockInSubDir}
        create four locks                            ${fourLocks}
        create two locks from two processes          ${twoLocksTwoProcesses}

      """

  val baseDir = "/tmp/lslock-test"

  def beforeAll = {
    new File(baseDir).mkdir()
    Files.write(Paths.get(s"$baseDir/one.lock"), "test content".getBytes(StandardCharsets.UTF_8))
    Files.write(Paths.get(s"$baseDir/two.lock"), "test content".getBytes(StandardCharsets.UTF_8))
    Files.write(Paths.get(s"$baseDir/three.lock"), "test content".getBytes(StandardCharsets.UTF_8))
    new File(s"$baseDir/subdir").mkdir()
    Files.write(Paths.get(s"$baseDir/subdir/four.lock"), "test content".getBytes(StandardCharsets.UTF_8))
  }

  def afterAll = {
    Files.delete(Paths.get(s"$baseDir/subdir/four.lock"))
    Files.delete(Paths.get(s"$baseDir/three.lock"))
    Files.delete(Paths.get(s"$baseDir/two.lock"))
    Files.delete(Paths.get(s"$baseDir/one.lock"))
    Files.delete(Paths.get(s"$baseDir/subdir"))
  }

  def oneLock = {
    val lock = new RandomAccessFile(new File(s"$baseDir/one.lock"), "rw").getChannel().lock()
    val result = LsLock.findLocksInDirectory(baseDir)
    lock.release()
    (result must beSuccessfulTry) and
      (result.get.size must beEqualTo(1)) and
      (result.get(0)._2 must beEqualTo(List(Paths.get(s"$baseDir/one.lock"))))
  }

  def oneLockInSubDir = {
    val lock = new RandomAccessFile(new File(s"$baseDir/subdir/four.lock"), "rw").getChannel().lock()
    val result = LsLock.findLocksInDirectory(baseDir)
    lock.release()
    (result must beSuccessfulTry) and
      (result.get.size must beEqualTo(1)) and
      (result.get(0)._2 must beEqualTo(List(Paths.get(s"$baseDir/subdir/four.lock"))))
  }

  def fourLocks = {
    val lock1 = new RandomAccessFile(new File(s"$baseDir/one.lock"), "rw").getChannel().lock()
    val lock2 = new RandomAccessFile(new File(s"$baseDir/two.lock"), "rw").getChannel().lock()
    val lock3 = new RandomAccessFile(new File(s"$baseDir/three.lock"), "rw").getChannel().lock()
    val lock4 = new RandomAccessFile(new File(s"$baseDir/subdir/four.lock"), "rw").getChannel().lock()
    val result = LsLock.findLocksInDirectory(baseDir)
    lock1.release()
    lock2.release()
    lock3.release()
    lock4.release()

    (result must beSuccessfulTry) and
      (result.get.size must beEqualTo(1)) and
      (result.get(0)._2 must beEqualTo(List(
        Paths.get(s"$baseDir/one.lock"),
        Paths.get(s"$baseDir/subdir/four.lock"),
        Paths.get(s"$baseDir/three.lock"),
        Paths.get(s"$baseDir/two.lock")
      )))
  }

  def twoLocksTwoProcesses = {
    val lock = new RandomAccessFile(new File(s"$baseDir/one.lock"), "rw").getChannel().lock()
    val process = Process(s"flock $baseDir/two.lock sleep 1").run
    val result = LsLock.findLocksInDirectory(baseDir)
    process.exitValue
    lock.release()

    (result must beSuccessfulTry) and
      (result.get.size must beEqualTo(2)) and
      ((result.get(0)._2 ++ result.get(1)._2).sorted must beEqualTo(List(
        Paths.get(s"$baseDir/one.lock"),
        Paths.get(s"$baseDir/two.lock")
      )))
  }
}
