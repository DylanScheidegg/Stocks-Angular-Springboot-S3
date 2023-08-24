package com.dylanscheidegg.DataHandling;

import java.io.File;
import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileWriter;
import java.io.IOException;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

public class UploadDownloadHandling {

    private void writeToFile(JSONObject data, String filePath) {
        // Write the JSONObject to a file
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(data.toString());
            fileWriter.flush();
            fileWriter.close();
            System.out.println("JSONObject written to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read file and convert to jsonobject
    public JSONObject readFileAsJSONObject(String filePath) {
        JSONParser jsonParser = new JSONParser();

        try (FileReader fileReader = new FileReader(filePath)) {
            Object obj = jsonParser.parse(fileReader);
            if (obj instanceof JSONObject) {
                return (JSONObject) obj;
            } else {
                System.err.println("File does not contain a valid JSON object.");
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            System.err.println("An error occurred while reading and parsing the file: " + e.getMessage());
        }

        return null;
    }

    public void UploadData(JSONObject data, String stockName) {
        String bucketName = "bucket-name";
        String fileName = "stocksdata" + stockName + ".json";
        String filePath = "./stocks-backend/stocksbackend/src/main/java/com/dylanscheidegg/DataHandling/" + fileName;
        
        try {
            writeToFile(data, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            S3Client client = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create("default"))
                .build();
            
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
            
            client.putObject(request, RequestBody.fromFile(new File(filePath)));

            S3Waiter waiter = client.waiter();
            HeadObjectRequest objectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

            WaiterResponse<HeadObjectResponse> waiterResponse = waiter.waitUntilObjectExists(objectRequest);
            
            waiterResponse.matched().response().ifPresent(System.out::println);

            System.out.println("File uploaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    // Download file from S3 bucket
    public JSONObject DownloadData(String stockName) {
        String bucketName = "bucket-name";
        String fileName = "stocksdata" + stockName + ".json";
        String filePath = "./stocks-backend/stocksbackend/src/main/java/com/dylanscheidegg/DataHandling/" + fileName;
    
        // Check if the file exists and delete it if needed
        File outputFile = new File(filePath);
        if (outputFile.exists()) {
            outputFile.delete();
        }
    
        try {
            S3Client client = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create("default"))
                .build();
    
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
    
            client.getObject(objectRequest, ResponseTransformer.toFile(outputFile));
    
            System.out.println("File downloaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred: " + e.getMessage());
        }
    
        return readFileAsJSONObject(filePath);
    }
}