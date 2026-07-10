package com.supermarketpos.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

public class ImageUtil {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final String IMAGE_STORAGE_DIR = "product-images";
    public static final String DEFAULT_IMAGE_CLASSPATH = "/images/default-product.png";

    private ImageUtil() {
    }

    public static boolean isValidImage(Path sourcePath) {
        if (sourcePath == null || !Files.exists(sourcePath)) {
            return false;
        }
        String extension = getExtension(sourcePath.getFileName().toString());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return false;
        }
        try {
            return Files.size(sourcePath) <= MAX_FILE_SIZE_BYTES;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Copies the selected image into the app's local image storage folder
     * and returns the relative path to persist in the database.
     */
    public static String storeProductImage(Path sourcePath) throws IOException {
        if (!isValidImage(sourcePath)) {
            throw new IOException("Invalid image file: must be png/jpg/jpeg and under 5MB.");
        }

        Path storageDir = Paths.get(IMAGE_STORAGE_DIR);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        String extension = getExtension(sourcePath.getFileName().toString());
        String newFileName = UUID.randomUUID() + "." + extension;
        Path destination = storageDir.resolve(newFileName);

        Files.copy(sourcePath, destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString();
    }

    public static InputStream loadDefaultImageStream() {
        return ImageUtil.class.getResourceAsStream(DEFAULT_IMAGE_CLASSPATH);
    }

    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }
}