package org.superbiz.moviefun;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.text.html.Option;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;


public class S3Store implements BlobStore {

    private AmazonS3Client s3Client;
    private String bucket;

    public S3Store(AmazonS3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }


    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(blob.contentType);
        s3Client.putObject(bucket, blob.name, blob.inputStream, metadata);
        blob.inputStream.close();
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        Optional<Blob> result = Optional.empty();
        try{
            S3Object s3Object = s3Client.getObject(bucket, name);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();

            String contentType = s3Object.getObjectMetadata().getContentType();
            result = Optional.of(new Blob(name, inputStream, contentType));
        } catch (SdkClientException e) {
            System.out.println("***SDK CLIENT EXCEPTION: " + e.getMessage());
            return result;
        }

        return result;

    }

    @Override
    public void deleteAll() {
        throw new NotImplementedException();
    }
}
