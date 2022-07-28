package org.eclipse.oomph.console.core.application;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;

/**
 * Changes the permission of each file and directory visited
 */
public class FilePermissionVisitor extends SimpleFileVisitor<Path> {

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (isUserOwnerOf(dir)) {
            Set<PosixFilePermission> perms = getPropagatedOwnerPermissions(Files.getPosixFilePermissions(dir));
            Files.setPosixFilePermissions(dir, perms);

        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (isUserOwnerOf(file)) {
            Set<PosixFilePermission> perms = getPropagatedOwnerPermissions(Files.getPosixFilePermissions(file));
            Files.setPosixFilePermissions(file, perms);
        }
        return FileVisitResult.CONTINUE;
    }

    private boolean isUserOwnerOf(Path dir) throws IOException {
        FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(dir, FileOwnerAttributeView.class);
        UserPrincipal owner = ownerAttributeView.getOwner();
        return owner.getName().equals(System.getProperty("user.name"));
    }

    private Set<PosixFilePermission> getPropagatedOwnerPermissions(Set<PosixFilePermission> currentPermissions) {
        Set<PosixFilePermission> perms = new HashSet<>();
        if (currentPermissions.contains(PosixFilePermission.OWNER_READ)) {
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.GROUP_READ);
        }
        if (currentPermissions.contains(PosixFilePermission.OWNER_WRITE)) {
            perms.add(PosixFilePermission.OWNER_WRITE);
            // perms.add(PosixFilePermission.OTHERS_WRITE);
            perms.add(PosixFilePermission.GROUP_WRITE);
        }
        if (currentPermissions.contains(PosixFilePermission.OWNER_EXECUTE)) {
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
        }
        return perms;
    }
}
