package roro.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

public class FileUpload {
    private String fileName; // "photo.png"
    private String name; // "photo"
    private String extension; // "png"
    private String contentType; // "image/png"
    private byte[] bytes;

    public FileUpload() {
    }

    public FileUpload(String fileName, String name, String extension, String contentType, byte[] bytes) {
        this.fileName = fileName;
        this.name = name;
        this.extension = extension;
        this.contentType = contentType;
        this.bytes = bytes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public static FileUpload convertToFileUpload(Part part) throws IOException {
        String submitted = part.getSubmittedFileName();
        if (submitted == null || part.getSize() == 0)
            return null;

        // String fileName = Paths.get(submitted).getFileName().toString(); // "C:\..." or "../"
        String fileName = submitted.substring(Math.max(submitted.lastIndexOf('/'), submitted.lastIndexOf('\\')) + 1);

        int dot = fileName.lastIndexOf('.');
        String name = dot > 0 ? fileName.substring(0, dot) : fileName;
        String extension = dot > 0 ? fileName.substring(dot + 1).toLowerCase() : "";

        byte[] bytes;
        try (InputStream in = part.getInputStream()) {
            bytes = in.readAllBytes();
        }
        return new FileUpload(fileName, name, extension, part.getContentType(), bytes);
    }

    public static boolean isMultipart(HttpServletRequest request) {
        String ct = request.getContentType();
        return ct != null && ct.toLowerCase().startsWith("multipart/");
    }

    public static FileUpload[] fromAll(HttpServletRequest request, String name) throws IOException, ServletException {
        List<FileUpload> files = new ArrayList<>();
        for (Part part : request.getParts()) {
            if (part.getName().equals(name)) {
                FileUpload f = convertToFileUpload(part);
                if (f != null)
                    files.add(f);
            }
        }
        return files.toArray(new FileUpload[0]);
    }
}
