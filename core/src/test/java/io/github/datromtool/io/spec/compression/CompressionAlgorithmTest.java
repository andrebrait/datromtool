package io.github.datromtool.io.spec.compression;

import io.github.datromtool.io.ArchiveContentsDependantTest;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CompressionAlgorithmTest extends ArchiveContentsDependantTest {

    @ParameterizedTest
    @MethodSource("compressionAlgorithmProvider")
    void testCompressDecompress(CompressionAlgorithm algorithm) throws IOException {
        byte[] compressedShortText = compressByteArray(algorithm, shortTextContents);
        byte[] compressedLoremIpsum = compressByteArray(algorithm, loremIpsumContents);
        assertArrayEquals(shortTextContents, decompressByteArray(algorithm, compressedShortText));
        assertArrayEquals(loremIpsumContents, decompressByteArray(algorithm, compressedLoremIpsum));
    }

    @ParameterizedTest
    @MethodSource("compressionAlgorithmProvider")
    void testDecompressFromDisk(CompressionAlgorithm algorithm) throws IOException {
        byte[] decompressedShortText = getDecompressedByteArray(algorithm, "files/test/short-text.txt");
        byte[] decompressedLoremIpsum = getDecompressedByteArray(algorithm, "files/test/lorem-ipsum.txt");
        assertArrayEquals(shortTextContents, decompressedShortText);
        assertArrayEquals(loremIpsumContents, decompressedLoremIpsum);
    }

    private byte[] compressByteArray(CompressionAlgorithm algorithm, byte[] bytes) throws IOException {
        try (ByteArrayOutputStream compressed = compress(algorithm, bytes)) {
            return compressed.toByteArray();
        }
    }

    private ByteArrayOutputStream compress(CompressionAlgorithm algorithm, byte[] bytes) throws IOException {
        ByteArrayOutputStream rawStream = new ByteArrayOutputStream();
        try (OutputStream outputStream = algorithm.getCompressor().compress(rawStream)) {
            outputStream.write(bytes);
        }
        return rawStream;
    }

    private byte[] decompressByteArray(CompressionAlgorithm algorithm, byte[] bytes) throws IOException {
        try (InputStream decompressed = algorithm.getDecompressor().decompress(new ByteArrayInputStream(bytes))) {
            return IOUtils.toByteArray(decompressed);
        }
    }

    private byte[] getDecompressedByteArray(CompressionAlgorithm algorithm, String fileName) throws IOException {
        try (InputStream decompressed = decompress(algorithm, fileName)) {
            return IOUtils.toByteArray(decompressed);
        }
    }

    private InputStream decompress(CompressionAlgorithm algorithm, String fileName) throws IOException {
        Path file = archiveTestDataSource.resolve(fileName + "." + algorithm.getExtension());
        return algorithm.getDecompressor().decompress(Files.newInputStream(file));
    }

    static Stream<Arguments> compressionAlgorithmProvider() {
        return Arrays.stream(CompressionAlgorithm.values())
                .filter(CompressionAlgorithm::isEnabled)
                .map(Arguments::of);
    }

}
