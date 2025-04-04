# 🏛️ Legal Advisory Service  

*A modern legal advisory system using RAG (Retrieval-Augmented Generation) to provide context-aware legal advice.*

---

## 🚀 Overview  
Legal Advisory Service leverages **Retrieval-Augmented Generation (RAG)** to deliver precise legal guidance based on provided context data. The system ingests legal documents, stores vectorized representations, and uses **OpenAI's API** for intelligent responses.

---

## 🛠 Tech Stack  
- **Java 21**  
- **Spring Boot 3.x.x+**  
- **OpenAI API** for AI-driven legal insights  
- **Vector Database** for efficient legal document retrieval  


## 🔧 Setup & Configuration  

### Prerequisites  
Ensure you have the following installed:  
- ✅ Java 21  
- ✅ Maven 3.8+  
- ✅ OpenAI API Key  

### Environment Variables  
Before running the service, set up VM arguments:  

```sh
-DDOC_UPLOAD_LOCATION=/Users/shtiwari/Priceline/learning/upload
-DVECTOR_STORE_LOCATION=/Users/shtiwari/Priceline/learning/vector/vector-store.json
-DOPENAI_API_KEY=YOUR_API_KEY
```

### Build & Run
git clone https://github.com/yourusername/legal-advisory-service.git  
cd legal-advisory-service
mvn clean install  
mvn spring-boot:run

## 📤 API 
To upload a document and query, use the following cURL command:  

```sh
curl --location 'http://localhost:8080/api/v1' \
     --form 'file=@"${location}/job-offer-letter-template.pdf"'

curl --location --request GET 'http://localhost:8080/api/v1/query?query="Are%20they%20free%20to%20terminate%20employment%20agreement%3F"'

```
