package learning.spring.binarytea.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class ChromeDevToolsController {

    @GetMapping("/.well-known/appspecific/com.chrome.devtools.json")
    public ResponseEntity<Map<String, Object>> handleChromeDevToolsRequest() {
        // 创建一个简单的响应，满足 Chrome 的基本要求
        Map<String, Object> response = new HashMap<>();
        response.put("name", "My Application");
        response.put("uuid", UUID.randomUUID().toString());

        // 返回 200 OK 和空响应
        return ResponseEntity.ok(response);

        // 或者返回 204 No Content（如果不需要内容）
        // return ResponseEntity.noContent().build();
    }
}