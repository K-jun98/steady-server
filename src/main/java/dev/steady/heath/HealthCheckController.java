package dev.steady.heath;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }

}
