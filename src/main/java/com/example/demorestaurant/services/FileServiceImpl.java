package com.example.demorestaurant.services;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.demorestaurant.controllers.dtos.responses.BaseResponse;
import com.example.demorestaurant.controllers.dtos.responses.GetCeoResponse;
import com.example.demorestaurant.controllers.dtos.responses.GetImageResponse;
import com.example.demorestaurant.controllers.dtos.responses.GetRestaurantResponse;
import com.example.demorestaurant.entities.Image;
import com.example.demorestaurant.entities.projections.FileProjection;
import com.example.demorestaurant.repositories.IFileRepository;
import com.example.demorestaurant.services.interfaces.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements IFileService {

    @Autowired
    private CeoServiceImpl ceoService;
    @Autowired
    private RestaurantServiceImpl restaurantService;

    @Autowired
    private IFileRepository repository;

    private AmazonS3 s3client;

    private String ENDPOINT_URL = "s3.us-east-2.amazonaws.com";

    private String BUCKET_NAME = "delichi";

    private String ACCESS_KEY = "AKIATH7APQJ7TP5UMR3Z";

    private String SECRET_KEY = "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";


    // Uplod a restaurant img by ceo
    @Override
    public BaseResponse uploadRestaurantImg(MultipartFile multipartFile, Long idCeo, Long idRestaurant, String img_type) {
        Image image = new Image();
        String urlDirection = "";
        GetCeoResponse ceo = ceoService.get(idCeo);
        GetRestaurantResponse restaurant = restaurantService.get(idRestaurant);


        switch (img_type){
            case "images":
                urlDirection = "data/bussines_info/ceo/" + ceo.getEmail()
                        + "/properties/ceo_restaurants/" + restaurant.getName().replace(" ","_") + "/images/restaurantImages/";
                break;
            case "logo":
                urlDirection = "data/bussines_info/ceo/" + ceo.getEmail()
                        + "/properties/ceo_restaurants/" + restaurant.getName().replace(" ","_") + "/images/logo/";
                break;
            case "banner":
                urlDirection = "data/bussines_info/ceo/" + ceo.getEmail()
                        + "/properties/ceo_restaurants/" + restaurant.getName().replace(" ","_") + "/images/banner/";
                break;
        }

        // Create the urlDirection where the img will be uploaded

        String fileUrl = "";

        try {
            File file = convertMultiPartToFile(multipartFile);
            String filePath = urlDirection + generateFileName(multipartFile); // aded the filename to the url

            fileUrl = "https://" + BUCKET_NAME + "." + ENDPOINT_URL + "/" + filePath;
            uploadFileTos3bucket(filePath, file); // Ubication, file

            image.setFileUrl(fileUrl);
            image.setRestaurant(restaurantService.FindRestaurantAndEnsureExist(idRestaurant));
            image.setName(generateFileName(multipartFile));
            image.setImage_type(img_type);

            repository.save(image);

            file.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return BaseResponse.builder()
                .data(fileUrl)
                .message("Image uploaded successfully")
                .success(Boolean.TRUE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    // Images type images
    @Override
    public BaseResponse listAllImagesByRestaurantId(Long restaurant_id) {
        return BaseResponse.builder()
                .data(repository.listAllImagesByRestaurantId(restaurant_id)
                        .stream()
                        .map(this::from)
                        .map(this::from_get)
                        .collect(Collectors.toList()))
                .message("list all images by restaurant")
                .success(Boolean.TRUE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    // Images type logo
    @Override
    public BaseResponse ListAllLogoImagesByRestaurantId(Long restaurant_id) {
        return BaseResponse.builder()
                .data(repository.ListAllLogoImagesByRestaurantId(restaurant_id)
                        .stream()
                        .map(this::from)
                        .map(this::from_get)
                        .collect(Collectors.toList()))
                .message("list all logo images by restaurant")
                .success(Boolean.TRUE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    // Images type banner
    @Override
    public BaseResponse ListAllBannerImagesByRestaurantId(Long restaurant_id) {
        return BaseResponse.builder()
                .data(repository.ListAllBannerImagesByRestaurantId(restaurant_id)
                        .stream()
                        .map(this::from)
                        .map(this::from_get)
                        .collect(Collectors.toList()))
                .message("list all banner images by restaurant")
                .success(Boolean.TRUE)
                .httpStatus(HttpStatus.OK)
                .build();
    }


    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return multiPart.getOriginalFilename().replace(" ", "_");
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(BUCKET_NAME, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }


    public String deleteFileFromS3Bucket(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        s3client.deleteObject(new DeleteObjectRequest(BUCKET_NAME, fileName));
        return "Successfully deleted";
    }

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
        s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_2)
                .build();
    }

    // projection to Image
    private Image from (FileProjection projection){
        Image image = new Image();
        image.setId(projection.getId());
        image.setFileUrl(projection.getUrl_file());
        image.setName(projection.getName());
        image.setRestaurant(restaurantService.FindRestaurantAndEnsureExist(projection.getId_restaurant()));
        image.setImage_type(projection.getImage_type());
        return image;
    }

    // Image to GetImageResponse
    private GetImageResponse from_get(Image image){
        GetImageResponse response = new GetImageResponse();
        response.setId(image.getId());
        response.setName(image.getName());
        response.setUrl_file(image.getFileUrl());
        response.setImg_type(image.getImage_type());
        return response;
    }
    
}
