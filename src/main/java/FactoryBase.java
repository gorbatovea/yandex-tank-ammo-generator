import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ThreadLocalRandom;

public class FactoryBase {
    // File prefix
    private static final String TEMP_PREFIX = "ammo";

    // 8 bytes
    protected static final int BODY_SIZE = 8;

    protected static File createTempDirectory() throws IOException {
        final File data = java.nio.file.Files.createTempDirectory(TEMP_PREFIX).toFile();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (data.exists()) {
                    recursiveDelete(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        return data;
    }

    protected static void recursiveDelete(@NotNull final File path) throws IOException {
        java.nio.file.Files.walkFileTree(
                path.toPath(),
                new SimpleFileVisitor<Path>() {
                    protected void remove(@NotNull final Path file) throws IOException {
                        if (!file.toFile().delete()) {
                            throw new IOException("Can't delete " + file);
                        }
                    }

                    @NotNull
                    @Override
                    public FileVisitResult visitFile(
                            @NotNull final Path file,
                            @NotNull final BasicFileAttributes attrs) throws IOException {
                        remove(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        remove(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    protected static byte[] keyFrom(final int i) {
        return BigInteger.valueOf(i).toByteArray();
    }

    protected static BigInteger initial() {
        final byte[] seed = new byte[AmmoFactory.BODY_SIZE];
        ThreadLocalRandom.current().nextBytes(seed);
        return new BigInteger(seed);
    }

    protected static BigInteger next(final BigInteger current) {
        return current.add(BigInteger.ONE);
    }
}
