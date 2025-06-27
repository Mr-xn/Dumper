package me.accessmodifier364.dumper.transformer;

import me.accessmodifier364.dumper.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * https://stackoverflow.com/questions/27115371/overriding-the-classloader-to-get-every-loaded-class-bytes-and-name
 *
 * @author accessmodifier364
 * @since 11-Nov-2021
 */

public final class Dumper implements ClassFileTransformer {

    private static final List<String> exclusions = Arrays.asList(
        "java", "sun", "javax", "jdk", "net/minecraft",
        "com/sun", "org/spongepowered", "org/jcp"
    );
    private final Set<String> dumpClassPrefixes;

    public Dumper(Set<String> dumpClassPrefixes) {
        this.dumpClassPrefixes = dumpClassPrefixes != null ? dumpClassPrefixes : new HashSet<>();
    }

    @Override
    public byte[] transform(
        ClassLoader loader,
        String className,
        Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain,
        byte[] classfileBuffer
    ) {
        if (!shouldDump(className)) {
            return classfileBuffer;
        }
        if (!dumpClassPrefixes.isEmpty()) {
            boolean match = false;
            for (String prefix : dumpClassPrefixes) {
                if (className.startsWith(prefix)) {
                    match = true;
                    break;
                }
            }
            if (!match) return classfileBuffer;
        }

        System.out.println("Dumping: " + className);

        final String newName = className + ".class";
        if (newName.contains("/")) {
            try {
                Files.createDirectories(Paths.get(Main.directory + File.separator + "classes" + File.separator + newName.substring(0, newName.lastIndexOf('/'))));
            } catch (final IOException e) {
                System.err.println("Error creating subdirectory: " + e.getMessage());
            }
        }

        try (final FileOutputStream fos = new FileOutputStream(Main.directory + File.separator + "classes" + File.separator + newName)) {
            fos.write(classfileBuffer);
        } catch (final IOException e) {
            System.err.println("Error writing class: " + e.getMessage());
        }

        return classfileBuffer;
    }

    private boolean shouldDump(final String className) {
        return exclusions.stream().noneMatch(className::startsWith);
    }
}
