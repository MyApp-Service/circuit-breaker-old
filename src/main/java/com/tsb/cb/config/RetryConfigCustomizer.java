package com.tsb.cb.config;

import java.util.function.Consumer;

import io.github.resilience4j.core.lang.NonNull;
import io.github.resilience4j.retry.RetryConfig;

public interface RetryConfigCustomizer extends CustomizerWithName {

    /**
     * Retry configuration builder.
     *
     * @param configBuilder to be customized
     */
    void customize(RetryConfig.Builder configBuilder);

    /**
     * A convenient method to create RetryConfigCustomizer using {@link Consumer}
     *
     * @param instanceName the name of the instance
     * @param consumer     delegate call to Consumer when  {@link RetryConfigCustomizer#customize(RetryConfig.Builder)}
     *                     is called
     * @return Customizer instance
     */
    static RetryConfigCustomizer of(@NonNull String instanceName,
        @NonNull Consumer<RetryConfig.Builder> consumer) {
        return new RetryConfigCustomizer() {

            @Override
            public void customize(RetryConfig.Builder builder) {
                consumer.accept(builder);
            }

            @Override
            public String name() {
                return instanceName;
            }
        };
    }
}

