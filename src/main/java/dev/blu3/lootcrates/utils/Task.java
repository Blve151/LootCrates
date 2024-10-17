package dev.blu3.lootcrates.utils;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Task implements ServerTickEvents.EndTick {

    // Thanks Landon

    private final Consumer<Task> consumer;

    private final long interval;
    private long currentIteration;
    private final long iterations;

    private long ticksRemaining;
    private boolean expired;
    private boolean lastIteration = false;

    Task(Consumer<Task> consumer, long delay, long interval, long iterations) {
        this.consumer = consumer;
        this.interval = interval;
        this.iterations = iterations;

        if (delay > 0) {
            ticksRemaining = delay;
        }
        ServerTickEvents.END_SERVER_TICK.register(this);
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired() {
        expired = true;
    }

    public void tick() {
        if (!expired) {
            this.ticksRemaining = Math.max(0, --ticksRemaining);

            if (ticksRemaining == 0) {
                consumer.accept(this);
                currentIteration++;

                if (interval > 0 && (currentIteration < iterations || iterations == -1)) {
                    ticksRemaining = interval;
                } else {
                    setExpired();
                }
            }
        }
    }

    public static TaskBuilder builder() {
        return new TaskBuilder();
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        if (isExpired()) {
            if(!lastIteration){
                lastIteration = true;
                tick();
            }
        }else{
            tick();
        }
    }

    public static class TaskBuilder {

        private Consumer<Task> consumer;
        private long delay;
        private long interval;
        private long iterations = 1;

        public TaskBuilder execute(@NotNull Runnable runnable) {
            this.consumer = (task) -> runnable.run();
            return this;
        }

        public TaskBuilder execute(@NotNull Consumer<Task> consumer) {
            this.consumer = consumer;
            return this;
        }

        public TaskBuilder delay(long delay) {
            if (delay < 0) {
                throw new IllegalArgumentException("delay must not be below 0");
            }
            this.delay = delay;
            return this;
        }

        public TaskBuilder interval(long interval) {
            if (interval < 0) {
                throw new IllegalArgumentException("interval must not be below 0");
            }
            this.interval = interval;
            return this;
        }

        public TaskBuilder iterations(long iterations) {
            if (iterations < -1) {
                throw new IllegalArgumentException("iterations must not be below -1");
            }
            this.iterations = iterations;
            return this;
        }

        public TaskBuilder infinite() {
            return iterations(-1);
        }

        public Task build() {
            if (consumer == null) {
                throw new IllegalStateException("consumer must be set");
            }
            return new Task(consumer, delay, interval, iterations);
        }

    }
}
