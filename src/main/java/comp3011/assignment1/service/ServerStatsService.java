package comp3011.assignment1.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ServerStatsService {

    // Captured once, when this bean is created at startup
    private final Instant serverStartTime = Instant.now();

    // Atomic counters/flags because multiple concurrent requests may
    // read/update these at once — plain long/boolean fields would risk
    // race conditions under load.
    private final AtomicLong inputTokens = new AtomicLong(0);
    private final AtomicLong outputTokens = new AtomicLong(0);
    private final AtomicBoolean shutdownInProgress = new AtomicBoolean(false);

    public Instant getServerStartTime() {
        return serverStartTime;
    }

    public void recordTokenUsage(long input, long output) {
        inputTokens.addAndGet(input);
        outputTokens.addAndGet(output);
    }

    public long getInputTokens() {
        return inputTokens.get();
    }

    public long getOutputTokens() {
        return outputTokens.get();
    }

    public boolean tryBeginShutdown() {
        // Returns true only if we were the one to flip it from false->true
        return shutdownInProgress.compareAndSet(false, true);
    }
}