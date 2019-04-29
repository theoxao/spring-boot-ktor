### ktor and spring-web integrate
* easy to migrate from spring-boot-web(or webflux)

##### progress ![Progress](http://progressed.io/bar/85)

* [x] route registry  
* [x] parameter receive   
* [x] handle parameter  
* [x] support suspend functions  
* [x] support java class route   
* [x] parameter mapping to model  
* [x] suport request header / cookie value
* [x] support response redirect  
* [x] support static resource  
* [x] inject Model Request and Response
* [ ] handle exception  
* [x] file upload(support multipart file) and download (inject ApplicationResponse)
* [ ] handle CQRS  
* [x] handle response view   freemarker supported


##### quick start
* add dependency
```xml
        <dependency>
            <groupId>com.theoxao</groupId>
            <artifactId>spring-boot-ktor</artifactId>
            <version>0.1.2-beta</version>
        </dependency>
```

* controller(kotlin)
```kotlin
@RestController
@RequestMapping("/ocr")
class OCRController(private val ocrService: OCRService) {
    @PostMapping("/recognize")
    suspend fun base64(@RequestParam("file") file: MultipartFile): String {
        return ocrService.recognize(file)
    }
}
```

* same with java code (of course, no suspend)
```java
@RestController
@RequestMapping("/ocr/java")
public class OCRJavaController {
    @Autowired
    private OCRService ocrService;
    
    @RequestMapping("/recognize")
    public String base64(MultipartFile file){
        return ocrService.recognize(file);
    }
}
```

* configuration
```yaml
spring:
  ktor:
    port: 8080
    enableTrace: false
    engine: "Netty"  //Netty and CIO only
    staticRoot: "static"
    templatesRoot: "templates"
```

* supported spring-web annotaiton
```java
@Controller
@CookieValue
@RequestBody
@RequestHeader
@RequestMapping
@ResponseBody
@RestController
```
