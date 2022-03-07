package io.github.datromtool.io.copy.archive.impl.process;

import com.google.common.collect.ImmutableList;
import io.github.datromtool.io.ArchiveContentsDependantTest;
import io.github.datromtool.io.copy.archive.exceptions.ArchiveEntryNotFoundException;
import io.github.datromtool.util.ArchiveUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


// FIXME re-enable this later
@Disabled
@EnabledIf(
        value = "io.github.datromtool.util.ArchiveUtils#isUnrarAvailable",
        disabledReason = "UnRAR is not available")
class UnrarRarArchiveSourceSpecTest extends ArchiveContentsDependantTest {

    static Path rarFile;
    static Path unrarPath;

    @BeforeAll
    static void resolveFile() {
        rarFile = archiveTestDataSource.resolve("archives").resolve("files.rar");
        unrarPath = requireNonNull(ArchiveUtils.getUnrarPath());
    }

    @Test
    void testReadContents() throws IOException {
        try (UnrarRarArchiveSourceSpec spec = new UnrarRarArchiveSourceSpec(unrarPath, rarFile)) {
            assertIsShortText(spec.getNextInternalSpec(), false, false, DateField.MTIME);
            assertIsLoremIpsum(spec.getNextInternalSpec(), false, false, DateField.MTIME);
            assertNull(spec.getNextInternalSpec());
        }
    }

    @Test
    void testAllContentsInOrder() throws IOException {
        try (UnrarRarArchiveSourceSpec spec = new UnrarRarArchiveSourceSpec(unrarPath, rarFile, ImmutableList.of(SHORT_TEXT_FILE, LOREM_IPSUM_FILE))) {
            assertIsShortText(spec.getNextInternalSpec(), false, false, DateField.MTIME);
            assertIsLoremIpsum(spec.getNextInternalSpec(), false, false, DateField.MTIME);
            assertNull(spec.getNextInternalSpec());
        }
    }

    @Test
    void testAllContentsInReverse_shouldStillBePhysicalOrder() throws IOException {
        try (UnrarRarArchiveSourceSpec spec = new UnrarRarArchiveSourceSpec(unrarPath, rarFile, ImmutableList.of(LOREM_IPSUM_FILE, SHORT_TEXT_FILE))) {
            assertIsShortText(spec.getNextInternalSpec(), false, false, DateField.MTIME);
            assertIsLoremIpsum(spec.getNextInternalSpec(), false, false, DateField.MTIME);
            assertNull(spec.getNextInternalSpec());
        }
    }

    @Test
    void testReadOnlyLoremIpsum() throws IOException {
        try (UnrarRarArchiveSourceSpec spec = new UnrarRarArchiveSourceSpec(unrarPath, rarFile, ImmutableList.of(LOREM_IPSUM_FILE))) {
            assertIsLoremIpsum(spec.getNextInternalSpec(), false, false, DateField.MTIME);
            assertNull(spec.getNextInternalSpec());
        }
    }

    @Test
    void testReadOnlyShortText() throws IOException {
        try (UnrarRarArchiveSourceSpec spec = new UnrarRarArchiveSourceSpec(unrarPath, rarFile, ImmutableList.of(SHORT_TEXT_FILE))) {
            assertIsShortText(spec.getNextInternalSpec(), false, false, DateField.MTIME);
            assertNull(spec.getNextInternalSpec());
        }
    }

    @Test
    void testReadShortTextAndThenUnknown() throws IOException {
        try (UnrarRarArchiveSourceSpec spec = new UnrarRarArchiveSourceSpec(unrarPath, rarFile, ImmutableList.of(SHORT_TEXT_FILE, "unknownFile"))) {
            assertThrows(ArchiveEntryNotFoundException.class, spec::getNextInternalSpec);
        }
    }

}