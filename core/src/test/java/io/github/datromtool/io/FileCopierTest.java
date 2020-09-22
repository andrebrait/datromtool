package io.github.datromtool.io;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.datromtool.config.AppConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

class FileCopierTest {

    private static final String TEST_DATA_FOLDER = "../test-data";
    private Path testDataSource;
    private Path tempDir;

    @BeforeEach
    void setup() throws Exception {
        tempDir = Files.createTempDirectory("datromtool_copy_test");
        String testDir = System.getenv("DATROMTOOL_TEST_DIR");
        if (testDir == null && Files.isDirectory(Paths.get(TEST_DATA_FOLDER))) {
            testDir = TEST_DATA_FOLDER;
        }
        testDataSource = Paths.get(requireNonNull(testDir), "data", "files");
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(
                    Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    @Test
    void testCopy() {
        FileScanner fs = new FileScanner(
                AppConfig.builder().build(),
                ImmutableList.of(),
                ImmutableList.of(),
                null);
        ImmutableList<FileScanner.Result> results = fs.scan(ImmutableList.of(testDataSource));
        Map<Path, List<FileScanner.Result>> resultsForArchive =
                results.stream().collect(Collectors.groupingBy(FileScanner.Result::getPath));
        ImmutableSet<FileCopier.CopyDefinition> copyDefinitions = resultsForArchive.entrySet()
                .stream()
                .map(e -> {
                    ArchiveType archiveType = e.getValue()
                            .stream()
                            .map(FileScanner.Result::getArchiveType)
                            .filter(at -> at != ArchiveType.NONE)
                            .findFirst()
                            .orElse(ArchiveType.NONE);
                    return FileCopier.CopyDefinition.builder()
                            .from(e.getKey())
                            .to(tempDir.resolve(e.getKey()
                                    .getFileName()
                                    .toString()
                                    .replaceFirst("(?i)\\.rar$", ".rar.zip")))
                            .fromType(archiveType)
                            .archiveCopyDefinitions(
                                    archiveType == ArchiveType.NONE
                                            ? ImmutableSet.of()
                                            : e.getValue().stream()
                                                    .map(i -> FileCopier.ArchiveCopyDefinition.builder()
                                                            .source(i.getArchivePath())
                                                            .destination(Paths.get(i.getArchivePath())
                                                                    .getFileName()
                                                                    .toString())
                                                            .build()
                                                    ).collect(ImmutableSet.toImmutableSet()))
                            .build();
                }).collect(ImmutableSet.toImmutableSet());
        FileCopier fc = new FileCopier(AppConfig.builder().build(), false, null);
        fc.copy(copyDefinitions);
        ImmutableList<FileScanner.Result> afterCopy = fs.scan(ImmutableList.of(tempDir));
    }
}