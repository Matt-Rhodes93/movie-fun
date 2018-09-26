package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.hibernate.result.Output;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Component
public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        File file = new File(blob.name);
        OutputStream outputStream = new FileOutputStream(file);
        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = blob.inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, read);
        }

        outputStream.close();
        blob.inputStream.close();
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        InputStream inputStream = null;
        Optional<Blob> result = Optional.empty();
        try {
            inputStream = new FileInputStream(new File(name));
            InputStream tikaStream = new FileInputStream(new File(name));
            result = Optional.of(new Blob(name, inputStream, new Tika().detect(tikaStream)));
        } catch (IOException e) {
        }

        return result;
    }

    @Override
    public void deleteAll() {
        throw new NotImplementedException();
    }





    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        Path coverFilePath = getExistingCoverPath(albumId);
        byte[] imageBytes = readAllBytes(coverFilePath);
        HttpHeaders headers = createImageHttpHeaders(coverFilePath, imageBytes);

        return new HttpEntity<>(imageBytes, headers);
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = new File(format("covers/%d", albumId));
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }

    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

}
