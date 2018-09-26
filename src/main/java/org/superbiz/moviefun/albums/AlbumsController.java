package org.superbiz.moviefun.albums;

import javassist.bytecode.ByteArray;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.Application;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.readAllBytes;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@Controller
@RequestMapping("/albums")
public class AlbumsController {


    private final BlobStore blobStore;

    private final AlbumsBean albumsBean;


    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.blobStore = blobStore;
        this.albumsBean = albumsBean;
    }

    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
//        saveUploadToFile(uploadedFile, getCoverFile(albumId));

        Blob blob = new Blob(format("covers/%d", albumId), uploadedFile.getInputStream(), new Tika().detect(uploadedFile.getInputStream()));
        blobStore.put(blob);

        return format("redirect:/albums/%d", albumId);
    }

    private Blob getDefaultBlob() {
        InputStream inputStream = AlbumsController.class.getClassLoader().getResourceAsStream("questionMark.png");
        return new Blob("fallback", inputStream, IMAGE_PNG_VALUE);
    }


    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        HttpHeaders headers = new HttpHeaders();

        Optional<Blob> blobtional = blobStore.get(format("covers/%d", albumId));
        Blob blob = blobtional.orElseGet(this::getDefaultBlob);

        byte[] bytes = IOUtils.toByteArray(blob.inputStream);
//        int read = 0;
//        byte[] bytes = new byte[1024];
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        while ((read = blob.inputStream.read(bytes)) != -1) {
//            baos.write(bytes, 0, read);
//        }
//        blob.inputStream.close();

        headers.setContentType(MediaType.parseMediaType(new Tika().detect(bytes)));
        headers.setContentLength(bytes.length);

        return new HttpEntity<>(bytes, headers);
    }


    /*
    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
        Blob blob = new Blob(uploadedFile.getName(), uploadedFile.getInputStream(), uploadedFile.getContentType());
        blobStore.put(blob);
    }

    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }
    */
}
