package Madfoat.Learning.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Service
public class ImageProcessingService {

    private final ITesseract tesseract;

    public ImageProcessingService() {
        this.tesseract = new Tesseract();
        // You may need to set the tessdata path based on your system
        // tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
    }

    public String extractTextFromImage(MultipartFile imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("Invalid image file");
            }
            
            String extractedText = tesseract.doOCR(image);
            
            if (extractedText == null || extractedText.trim().isEmpty()) {
                return "No text could be extracted from the image. The image might not contain readable text or the text might be too unclear.";
            }
            
            return extractedText.trim();
            
        } catch (Exception e) {
            return "Error processing image: " + e.getMessage() + 
                   "\n\nNote: Make sure Tesseract OCR is installed on your system. " +
                   "For development purposes, this service will work with simple text extraction.";
        }
    }

    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif") ||
            contentType.equals("image/bmp")
        );
    }

    public String getImageAnalysisContext(MultipartFile imageFile) {
        // This method provides context about the image for better test condition generation
        String extractedText = extractTextFromImage(imageFile);
        
        StringBuilder context = new StringBuilder();
        context.append("Image Analysis Results:\n");
        context.append("File name: ").append(imageFile.getOriginalFilename()).append("\n");
        context.append("File size: ").append(imageFile.getSize()).append(" bytes\n");
        context.append("Content type: ").append(imageFile.getContentType()).append("\n\n");
        context.append("Extracted text content:\n");
        context.append(extractedText);
        
        return context.toString();
    }
}
