package com.topicsbot.core.services.analysis.daemons;

import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Author: Artem Voronov
 */
public class HistoryCleanerDaemon implements Runnable {
  private static final Logger logger = Logger.getLogger("HISTORY_CLEANER");

  private final String lucenePath;
  private final String worldLucenePath;
  private final int daysCount;

  public HistoryCleanerDaemon(int daysCount, String lucenePath, String worldLucenePath) {
    this.daysCount = daysCount;
    this.lucenePath = lucenePath;
    this.worldLucenePath = worldLucenePath;
  }

  @Override
  public void run() {
    try {
      if (logger.isDebugEnabled())
        logger.debug("HistoryCleanerDaemon has started");

      LocalDate dateLimit = LocalDate.now().minusDays(daysCount);
      long start = System.currentTimeMillis();

      cleanLucene(dateLimit);
      cleanWorldLucene(dateLimit);

      if (logger.isDebugEnabled())
        logger.debug("HistoryCleanerDaemon finished, execution time: " + (System.currentTimeMillis() - start) / 1000 + " seconds");
    } catch (Exception ex) {
      logger.error("HistoryCleanerDaemon: unexpected error occurred.", ex);
    }
  }

  @SuppressWarnings("unchecked")
  private void cleanLucene(LocalDate dateLimit) {
    FileVisitor cleaner = new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (lucenePath.equals(dir.toAbsolutePath().toString()))
          return FileVisitResult.CONTINUE;

        String fileName = dir.getFileName().toString();

        String tokens[] = fileName.split("_");
        if (tokens.length < 2)
          return FileVisitResult.SKIP_SUBTREE;

        String createdOn = tokens[1];
        LocalDate date = LocalDate.parse(createdOn, DateTimeFormatter.ISO_LOCAL_DATE);

        return date.isBefore(dateLimit) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (lucenePath.equals(dir.toAbsolutePath().toString()))
          return FileVisitResult.CONTINUE;

        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }

    };

    File dir = new File(lucenePath);
    try {
      if (dir.exists()) {
        Files.walkFileTree(Paths.get(lucenePath), cleaner);
      }
    } catch (IOException ex) {
      logger.error("Unable to read dir lucenePath ", ex);
    }
  }

  @SuppressWarnings("unchecked")
  private void cleanWorldLucene(LocalDate dateLimit) {
    FileVisitor cleaner = new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (worldLucenePath.equals(dir.toAbsolutePath().toString()))
          return FileVisitResult.CONTINUE;

        String fileName = dir.getFileName().toString();

        String tokens[] = fileName.split("_");
        if (tokens.length < 2)
          return FileVisitResult.SKIP_SUBTREE;

        String createdOn = tokens[0];
        LocalDate date = LocalDate.parse(createdOn, DateTimeFormatter.ISO_LOCAL_DATE);

        return date.isBefore(dateLimit) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (worldLucenePath.equals(dir.toAbsolutePath().toString()))
          return FileVisitResult.CONTINUE;

        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }

    };

    File dir = new File(worldLucenePath);
    try {
      if (dir.exists()) {
        Files.walkFileTree(Paths.get(worldLucenePath), cleaner);
      }
    } catch (IOException ex) {
      logger.error("Unable to read dir worldLucenePath ", ex);
    }
  }
}