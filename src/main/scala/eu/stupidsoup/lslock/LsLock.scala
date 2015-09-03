package eu.stupidsoup.lslock

import java.io.File
import java.nio.file.attribute.BasicFileAttributes

import scala.collection.JavaConverters._

import java.nio.file.{ Path, Paths, Files }

import scala.io.Source
import scala.util.{ Failure, Success, Try }

object LsLock {
  val FileKeyPattern = """\(dev=(\w{2})?(\d{2}),ino=(\d+)\)""".r
  val ProcLocksPattern = """\d+:\s+\w+\s+\w+\s+\w+\s+(\d+)\s+([a-z0-9:]+)\s+\w+\s+\w+""".r

  private def parseFileKey(fileKey: String) = fileKey match {
    case FileKeyPattern(major, minor, inode) => Option(s"${Option(major).getOrElse("00")}:$minor:$inode")
    case _ => None
  }

  private def getFilesAndInodes(directory: Path): List[(String, Path)] =
    Files.newDirectoryStream(directory).iterator.asScala.toList.flatMap {
      _ match {
        case path if path.toFile.isDirectory => getFilesAndInodes(path)
        case path => List(
          parseFileKey(Files.readAttributes(path, classOf[BasicFileAttributes]).fileKey.toString).map((_, path))
        ).flatten
      }
    }

  private def getCurrentFiles(directory: String): Try[List[(String, Path)]] = Try(getFilesAndInodes(Paths.get(directory)))

  private def getLocks(): Try[Map[String, String]] = Try {
    Source.fromFile(new File("/proc/locks")).getLines.toList.map {
      _ match {
        case ProcLocksPattern(pid, inode) => Option((inode, pid))
        case _ => None
      }
    }.flatten.toMap
  }

  def findLocksInDirectory(directory: String): Try[List[(String, List[Path])]] = for {
    files <- getCurrentFiles(directory)
    locks <- getLocks()
  } yield files.map { case (inode, path) => locks.get(inode).map((_, path)) }
    .flatten
    .groupBy { case (pid, _) => pid }
    .map { case (pid, lists) => (pid, lists.map(_._2).sorted) }
    .toList

}

object LsLockRunner extends App {
  LsLock.findLocksInDirectory(args(0)) match {
    case Success(result) => result.foreach {
      case (pid, paths) =>
        println(pid)
        paths.foreach { path => println(s"  $path") }
    }
    case Failure(error) => println(s"lslock failed: $error")
  }
}