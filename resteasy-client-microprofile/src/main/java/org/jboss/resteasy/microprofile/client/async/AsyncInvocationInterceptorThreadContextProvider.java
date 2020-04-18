package org.jboss.resteasy.microprofile.client.async;

import org.eclipse.microprofile.context.spi.ThreadContextController;
import org.eclipse.microprofile.context.spi.ThreadContextProvider;
import org.eclipse.microprofile.context.spi.ThreadContextSnapshot;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;

import java.util.Collection;
import java.util.Map;

public class AsyncInvocationInterceptorThreadContextProvider implements ThreadContextProvider {
    private static final ThreadContextController NOOP = () -> {};

    @Override
    public ThreadContextSnapshot currentContext(Map<String, String> props) {
        Collection<AsyncInvocationInterceptor> captured = AsyncInvocationInterceptorHandler.threadBoundInterceptors.get();

        if (captured != null && !captured.isEmpty()) {
            return () -> {
                Collection<AsyncInvocationInterceptor> previous = AsyncInvocationInterceptorHandler.threadBoundInterceptors.get();
                AsyncInvocationInterceptorHandler.threadBoundInterceptors.set(captured);
                return () -> AsyncInvocationInterceptorHandler.threadBoundInterceptors.set(previous);
            };
        }

        return () -> NOOP;
    }

    @Override
    public ThreadContextSnapshot clearedContext(Map<String, String> props) {
        return () -> {
            Collection<AsyncInvocationInterceptor> previous = AsyncInvocationInterceptorHandler.threadBoundInterceptors.get();
            AsyncInvocationInterceptorHandler.threadBoundInterceptors.remove();
            return () -> AsyncInvocationInterceptorHandler.threadBoundInterceptors.set(previous);
        };
    }

    @Override
    public String getThreadContextType() {
        return "RestEasyClientMicroProfile";
    }
}
