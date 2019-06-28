# ktor and spring-web integrate

* easy to migrate from spring-boot-web(or webflux)

![version](https://img.shields.io/jitpack/v/github/theoxao/spring-boot-ktor.svg?label=jitpack&style=flat-square)

* [x] 路由注册
* [x] 参数接收
* [x] 参数处理
* [x] 支持suspend (kotlin)  
* [x] 支持声明式路由
* [x] 支持请求头/Cookie获取参数
* [x] 支持重定向 
* [x] 支持静态资源映射
* [x] 文件上传下载
* [x] freemarker
* [x] 仿MVC参数解析器组
* [x] 多文件上传
* [x] 支持更多的spring-web注解

## roadmap

* [ ] 动态路由注册
* [ ] 自定义过滤器/拦截器
* [ ] 自定义CQRS  
* [ ] 自定义异常处理 

### quick start

* add repository
```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
```

* add dependency
```xml
        <dependency>
            <groupId>com.theoxao</groupId>
            <artifactId>spring-boot-ktor</artifactId>
            <version>0.2.1-alpha</version>
        </dependency>
```

* kotlin Controller代码
  
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

* java Controller层代码
  
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

* 自定义配置
  
```yaml
spring:
  ktor:
    port: 8080
    enableTrace: false
    engine: "Netty"  //Netty and CIO only
    staticRoot: "static"
    templatesRoot: "templates"
```

* 支持的spring web注解
  
```java
@Controller
@CookieValue
@RequestBody
@RequestHeader
@RequestMapping
@ResponseBody
@RestController
@GetMapping
@PostMapping
@DeleteMapping
@PutMapping
```

* request和response 改用ktor中的ApplicationRequest/ApplicationResponse
  
```kotlin
fun base64(request:ApplicationRequest,response:ApplicationResponse): String ...
```
