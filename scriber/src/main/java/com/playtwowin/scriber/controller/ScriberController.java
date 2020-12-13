package com.playtwowin.scriber.controller;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.DetectEntitiesRequest;
import com.amazonaws.services.comprehend.model.DetectEntitiesResult;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.util.IOUtils;
import com.playtwowin.scriber.services.DocumentTextService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api")
@Api(value="scriber")
public class ScriberController {

  
    // Create credentials using a provider chain. For more information, see
    // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
    private static AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();

    @Autowired
    DocumentTextService documentTextService;

//    curl -k -X POST -F 'image=@/Pictures/running_cheetah.jpg' -v  http://localhost:8080/upload/
    @PostMapping("/upload")
    public String fileUploader(@RequestParam("file") MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        return fileName;
    }

    // testing out AWS Textract
    @PostMapping("/doc-upload")
    @ApiOperation(value = "upload a file")
    public void documentUploaderSimple(@RequestParam("file") MultipartFile multipartFile) throws Exception {

        // Serialize the uploaded file
        ByteBuffer imageBytes = documentTextService.uploadFile(multipartFile);

        // extract the text
        DetectDocumentTextResult result = documentTextService.textDetector(imageBytes);

        System.out.println("[This Content was Extracted from the Document]");

        List<Block> resultBlockList = result.getBlocks();

        // prints the documents content to the console
        for( Block b : resultBlockList)
            if(b.getText() != null && b.getBlockType().equals("LINE"))
                System.out.println(b.getText());

        System.out.println("[Document Text Detection Complete]");

        // Testing out aws comprehend
        System.out.println("Start: Sentiment Analysis Test 1");
        sentimentAnalysis(resultBlockList.get(1).getText());
        System.out.println("End: Sentiment Analysis Test 1\n");

        System.out.println("[LINE Entity Detection Start]");
        for( Block b : resultBlockList)
            if(b.getText() != null && b.getBlockType().equals("LINE")) {
                System.out.println(b.getText());
                entityDetection(b.getText());}
        System.out.println("[LINE Entity Detection End]");

        System.out.println("\n\n[WORD Entity Detection Start]");
        for( Block b : resultBlockList)
            if(b.getText() != null && b.getBlockType().equals("WORD")) {
                System.out.println(b.getText());
            entityDetection(b.getText());}
        System.out.println("[WORD Entity Detection End]");

    }

    public static AmazonComprehend amazonComprehend(){
        AmazonComprehend comprehendClient =
                AmazonComprehendClientBuilder.standard()
                        .withCredentials(awsCreds)
                        .withRegion("us-east-1")
                        .build();
        return comprehendClient;
    }


    public void sentimentAnalysis(String text){

        AmazonComprehend comprehendClient = amazonComprehend();

        // Call detectSentiment API
        System.out.println("Calling DetectSentiment");
        DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest().withText(text)
                .withLanguageCode("en");
        DetectSentimentResult detectSentimentResult = comprehendClient.detectSentiment(detectSentimentRequest);
        System.out.println(detectSentimentResult);
        System.out.println("End of DetectSentiment\n");
        System.out.println( "Done" );
    }

    public void entityDetection(String text){

        AmazonComprehend comprehendClient = amazonComprehend();

        // Call detectEntities API
        System.out.println("\nCalling DetectEntities");
        DetectEntitiesRequest detectEntitiesRequest = new DetectEntitiesRequest().withText(text)
                .withLanguageCode("en");
        DetectEntitiesResult detectEntitiesResult  = comprehendClient.detectEntities(detectEntitiesRequest);
        detectEntitiesResult.getEntities().forEach(System.out::println);
        System.out.println("End of DetectEntities\n");

    }

//	@PostMapping("/doc-upload-2")
//	public ResponseEntity<ArrayList<String>> documentUploaderSimple(@RequestParam("file") MultipartFile multipartFile)
//			throws Exception {
//
//		ByteBuffer imageBytes;
//		BufferedImage image;
//
//		try (InputStream inputStream = multipartFile.getInputStream()) {
//			imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
//			image = ImageIO.read(inputStream);
//		}
//
//		// Call DetectDocumentText
//		AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(
//				"https://textract.us-east-1.amazonaws.com", "us-east-1");
//		AmazonTextract client = AmazonTextractClientBuilder.standard().withEndpointConfiguration(endpoint).build();
//
//		DetectDocumentTextRequest request = new DetectDocumentTextRequest()
//				.withDocument(new Document().withBytes(imageBytes));
//
//		DetectDocumentTextResult result = client.detectDocumentText(request);
//
//		System.out.println("[This Content was Extracted from the Document]");
//
//		List<Block> resultBlockList = result.getBlocks();
//
//		ArrayList<String> output = new ArrayList<String>();
//		// prints the documents content to the console
//		for (Block b : resultBlockList) {
//			if (b.getText() != null && b.getBlockType().equals("LINE")) {
//				System.out.println(b.getText());
//
//				output.add(b.getText());
//			}
//		}
//
//		System.out.println("[Document Text Detection Complete]");
//
//		return new ResponseEntity<ArrayList<String>>(output, HttpStatus.OK);
//	}

}
