package eu.stupidsoup.lslock

import java.io.File
import java.nio.file.attribute.BasicFileAttributes

import scala.collection.JavaConverters._

import java.nio.file.{ Path, Paths, Files }

import scala.io.Source
import scala.util.{ Failure, Success, Try }

object LsLock {
  val FileKeyPattern = """\(dev=(\w{2})(\d{2}),ino=(\d+)\)""".r
  val ProcLocksPattern = """\d+:\s+\w+\s+\w+\s+\w+\s+(\d+)\s+([a-z0-9:]+)\s+\w+\s+\w+""".r

  def parseFileKey(fileKey: String) = fileKey match {
    case FileKeyPattern(major, minor, inode) => Option(s"$major:$minor:$inode")
    case _ => None
  }

  def getFilesAndInodes(directory: Path): List[(String, Path)] =
    Files.newDirectoryStream(directory).iterator.asScala.toList.flatMap {
      _ match {
        case path if path.toFile.isDirectory => getFilesAndInodes(path)
        case path => List(
          parseFileKey(Files.readAttributes(path, classOf[BasicFileAttributes]).fileKey.toString).map((_, path))
        ).flatten
      }
    }

  def getCurrentFiles(directory: String): Try[List[(String, List[Path])]] = Try {
    getFilesAndInodes(Paths.get(directory)).groupBy { case (inode, _) => inode }.map {
      case (inode, files) =>
        (inode, files.map(_._2))
    }.toList
  }

  def getLocks(): Try[Map[String, String]] = Try {
    Source.fromFile(new File("/proc/locks")).getLines.toList.map {
      _ match {
        case ProcLocksPattern(pid, inode) => Option((inode, pid))
        case _ => None
      }
    }.flatten.toMap
  }

  def matchDirectory(directory: String): Try[List[(String, List[Path])]] = for {
    files <- getCurrentFiles(directory)
    locks <- getLocks()
  } yield files.map { case (inode, paths) => locks.get(inode).map((_, paths)) }.flatten
}

object LsLockRunner extends App {
  LsLock.matchDirectory(args(0)) match {
    case Success(result) => result.foreach {
      case (pid, paths) =>
        println(pid)
        paths.foreach { path => println(s"  $path") }
    }
    case Failure(error) => println(s"lslock failed: $error")
  }
}