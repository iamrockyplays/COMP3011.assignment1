package comp3011.assignment1.controller;

import comp3011.assignment1.dto.GlobalStatsResponse;
import comp3011.assignment1.dto.ShutdownResponse;
import comp3011.assignment1.dto.UptimeResponse;
import comp3011.assignment1.service.ServerStatsService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
public class AdminController {

    private final ServerStatsService statsService;
    private final ApplicationContext applicationContext;

    public AdminController(ServerStatsService statsService, ApplicationContext applicationContext) {
        this.statsService = statsService;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/admin/uptime")
    public UptimeResponse uptime() {
        Instant start = statsService.getServerStartTime();
        Instant now = Instant.now();
        double seconds = Duration.between(start, now).toNanos() / 1_000_000_000.0;
        return new UptimeResponse(start, now, seconds);
    }

    @PostMapping("/admin/shutdown")
    public ResponseEntity<ShutdownResponse> shutdown() {
        if (!statsService.tryBeginShutdown()) {
            // Already shutting down — 409, handled by the exception handler
            // pattern below would be cleaner, but a direct return works too
            return ResponseEntity.status(409)
                    .body(new ShutdownResponse("Graceful shutdown is already in progress."));
        }

        // TODO: this triggers shutdown immediately - for TRUE graceful
        // shutdown (letting in-flight requests finish), you'll want to look
        // into Spring's server.shutdown=graceful property and configuring a
        // shutdown grace period, rather than just exiting here.
        new Thread(() -> {
            try {
                Thread.sleep(500); // brief delay so this response can actually be sent first
            } catch (InterruptedException ignored) {}
            int exitCode = org.springframework.boot.SpringApplication.exit(applicationContext, () -> 0);
            System.exit(exitCode);
        }).start();

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ShutdownResponse("Graceful shutdown requested."));
    }

    @GetMapping("/global/stats")
    public GlobalStatsResponse stats() {
        return new GlobalStatsResponse(statsService.getInputTokens(), statsService.getOutputTokens());
    }
}