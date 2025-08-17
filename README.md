# Test Analyst AI Assistant

A Spring Boot web application that generates comprehensive software test conditions from business text or images using ChatGPT as a third-party AI service.

## Features

- **Text Analysis**: Input business requirements, user stories, or feature descriptions to generate test conditions
- **Image Processing**: Upload screenshots, mockups, or documents to extract text and generate test conditions
- **AI-Powered**: Uses OpenAI's ChatGPT API to generate comprehensive test scenarios
- **Modern UI**: Beautiful, responsive web interface with drag-and-drop file upload
- **Export Options**: Copy, download, or print generated test conditions
- **Multiple Test Types**: Generates functional, non-functional, positive, negative, boundary, and edge case tests

## Prerequisites

1. **Java 17 or higher**
2. **Maven 3.6+**
3. **AI Provider** (Choose one):
   - **Demo Mode** (Default - No setup required)
   - **Ollama** (Completely Free - Local AI)
   - **Hugging Face** (Free Tier Available)
   - **Google Gemini** (Free Tier Available) 
   - **OpenAI** (Paid - Most advanced)
4. **Tesseract OCR** (Optional for better image text extraction)

## 🆓 **Free AI Options:**

### **Option 1: Ollama (Completely Free - Recommended)**
- ✅ **100% Free** - Runs locally on your machine
- ✅ **No API keys required**
- ✅ **Works offline**
- ✅ **High quality results**
- ❌ Requires local installation and decent hardware

### **Option 2: Hugging Face (Free Tier)**
- ✅ **Free tier available**
- ✅ **Easy setup** - Just need free API key
- ✅ **Cloud-based** - No local installation
- ❌ Rate limits on free tier

### **Option 3: Google Gemini (Free Tier)**
- ✅ **Generous free tier**
- ✅ **High quality results**
- ✅ **Easy setup** - Just need free API key
- ❌ Some rate limits

### **Option 4: Demo Mode**
- ✅ **Works immediately**
- ✅ **No setup required**
- ❌ Pre-generated sample results only

## Setup Instructions

### 1. Clone and Navigate
```bash
git clone <your-repository-url>
cd TestAnalyistAiAssisstant
```

### 2. Choose and Configure AI Provider

#### 🔧 **Quick Setup for Ollama (Completely Free):**
1. **Install Ollama**: Download from [ollama.ai](https://ollama.ai)
2. **Pull a model**: 
   ```bash
   ollama pull llama2
   # or for coding: ollama pull codellama
   # or for better performance: ollama pull mistral
   ```
3. **Configure application**:
   ```properties
   ai.provider=ollama
   ```
4. **Start using**: No API keys needed!

#### 🔧 **Quick Setup for Hugging Face (Free Tier):**
1. **Get free API key**: Go to [Hugging Face Tokens](https://huggingface.co/settings/tokens)
2. **Create new token**: Click "New token" → Give it a name → Copy token
3. **Configure application**:
   ```properties
   ai.provider=huggingface
   huggingface.api.key=hf_your_token_here
   ```

#### 🔧 **Quick Setup for Google Gemini (Free Tier):**
1. **Get free API key**: Go to [Google AI Studio](https://ai.google.dev/)
2. **Create API key**: Click "Get API key" → Create new key
3. **Configure application**:
   ```properties
   ai.provider=gemini
   gemini.api.key=your_gemini_key_here
   ```

#### 🔧 **Setup for OpenAI (Paid):**

### 2a. Get OpenAI API Key (If using OpenAI)

#### Step-by-step guide to obtain your OpenAI API key:

1. **Create an OpenAI Account**:
   - Visit [OpenAI's website](https://platform.openai.com/)
   - Click "Sign up" if you don't have an account, or "Log in" if you do
   - Complete the registration process with your email

2. **Verify Your Account**:
   - Check your email for a verification link from OpenAI
   - Click the verification link to activate your account

3. **Add Payment Method** (Required for API access):
   - Go to [OpenAI Billing](https://platform.openai.com/account/billing)
   - Click "Add payment method"
   - Add a valid credit/debit card
   - Note: You'll get $5 in free credits when you first add a payment method

4. **Generate API Key**:
   - Navigate to [API Keys page](https://platform.openai.com/api-keys)
   - Click "Create new secret key"
   - Give your key a name (e.g., "Test Analyst Assistant")
   - Copy the generated key (it starts with `sk-`)
   - **Important**: Save this key securely - you won't be able to see it again!

5. **Set Usage Limits** (Recommended):
   - Go to [Usage limits](https://platform.openai.com/account/billing/limits)
   - Set a monthly spending limit to control costs
   - Start with a low limit like $10-20 for testing

#### API Pricing Information:
- **GPT-3.5-turbo**: ~$0.002 per 1K tokens (very affordable)
- **Typical cost per request**: $0.01 - $0.05 depending on input/output length
- **Free credits**: $5 when you add a payment method
- **Monitor usage**: Check [Usage dashboard](https://platform.openai.com/usage)

Configure your chosen AI provider in `src/main/resources/application.properties`:

```properties
# Choose your AI provider
ai.provider=ollama  # or huggingface, gemini, openai, demo

# Configure based on your choice:
# For Ollama (free):
ollama.url=http://localhost:11434

# For Hugging Face (free tier):
huggingface.api.key=hf_your_token_here

# For Gemini (free tier):
gemini.api.key=your_gemini_key_here

# For OpenAI (paid):
openai.api.key=sk-your-openai-key-here
```

### 4. Install Tesseract OCR (Optional but recommended)

#### Windows:
1. Download Tesseract from [GitHub Releases](https://github.com/UB-Mannheim/tesseract/wiki)
2. Install and note the installation path
3. Update application.properties if needed:
```properties
tesseract.data.path=C:/Program Files/Tesseract-OCR/tessdata
```

#### macOS:
```bash
brew install tesseract
```

#### Linux (Ubuntu/Debian):
```bash
sudo apt-get update
sudo apt-get install tesseract-ocr
```

### 5. Build and Run
```bash
# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

Alternatively, you can build a JAR file:
```bash
mvn clean package
java -jar target/TestAnalyistAiAssisstant-1.0-SNAPSHOT.jar
```

### 6. Access the Application
Open your web browser and navigate to:
```
http://localhost:8080
```

## Usage

### Text Input
1. Enter your business requirements, user stories, or feature descriptions in the text area
2. Click "Generate Test Conditions from Text"
3. View the AI-generated comprehensive test conditions

### Image Upload
1. Click "Choose File" or drag and drop an image file
2. Supported formats: JPEG, PNG, GIF, BMP
3. Click "Generate Test Conditions from Image"
4. The application will extract text from the image and generate test conditions

### Export Results
- **Copy**: Click the copy button to copy results to clipboard
- **Download**: Save results as a text file
- **Print**: Print the results with proper formatting

## API Endpoints

- `GET /` - Main application interface
- `POST /generate-from-text` - Generate test conditions from text input
- `POST /generate-from-image` - Generate test conditions from image upload
- `GET /api/health` - Health check endpoint

## Configuration Options

### Application Properties

```properties
# Server Configuration
server.port=8080

# File Upload Limits
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# OpenAI API Settings
openai.api.key=your-api-key-here
openai.api.url=https://api.openai.com/v1/chat/completions

# OCR Settings (Optional)
tesseract.data.path=/path/to/tessdata
tesseract.language=eng
```

## Architecture

### Backend Components
- **Main.java**: Spring Boot application entry point
- **TestConditionController**: Handles web requests and responses
- **ChatGPTService**: Integrates with OpenAI API for test condition generation
- **ImageProcessingService**: Handles image upload and text extraction using Tesseract OCR

### Frontend Components
- **index.html**: Main interface with text input and file upload
- **results.html**: Displays generated test conditions with export options
- **Bootstrap 5**: Modern, responsive UI framework
- **Font Awesome**: Icons and visual elements

## Troubleshooting

### Common Issues

1. **"Error generating test conditions"**
   - Check your OpenAI API key in application.properties
   - Ensure you have sufficient API credits
   - Verify internet connectivity

2. **"Error processing image"**
   - Install Tesseract OCR on your system
   - Check if the image contains readable text
   - Try with a clearer image or different format

3. **Application won't start**
   - Ensure Java 17+ is installed
   - Check if port 8080 is available
   - Run `mvn clean compile` to resolve dependencies

4. **Large file upload fails**
   - Check file size limits in application.properties
   - Increase max-file-size if needed

### Debug Mode
Enable debug logging by setting:
```properties
logging.level.Madfoat.Learning=DEBUG
```

## Example Test Condition Output

When you input a requirement like "User login functionality", the AI generates:

```
Functional Test Conditions:
1. Verify user can login with valid username and password
2. Verify system displays error for invalid credentials
3. Verify password field is masked during input

Non-Functional Test Conditions:
1. Verify login response time is under 3 seconds
2. Verify system can handle 100 concurrent login attempts
3. Verify login page is responsive on mobile devices

Security Test Conditions:
1. Verify system locks account after 3 failed attempts
2. Verify passwords are encrypted in database
3. Verify session timeout after inactivity

... (and many more comprehensive test scenarios)
```

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Thymeleaf** (Template engine)
- **Bootstrap 5** (Frontend framework)
- **Tesseract OCR** (Image text extraction)
- **OpenAI GPT-3.5-turbo** (AI test generation)
- **Maven** (Build tool)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is open source and available under the [MIT License](LICENSE).

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review the application logs
3. Create an issue in the repository

---

**Note**: This application requires an active OpenAI API key. API usage will incur costs based on OpenAI's pricing model. Monitor your usage through the OpenAI dashboard.
