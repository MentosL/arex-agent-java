package io.arex.inst.httpclient.apache.async;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientHelper;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;

import java.util.List;
import java.util.concurrent.Future;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class InternalHttpAsyncClientInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.http.impl.nio.client.InternalHttpAsyncClient");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(
                isMethod().and(named("execute"))
                        .and(takesArguments(4))
                        .and(takesArgument(0, named("org.apache.http.nio.protocol.HttpAsyncRequestProducer")))
                        .and(takesArgument(1, named("org.apache.http.nio.protocol.HttpAsyncResponseConsumer")))
                        .and(takesArgument(2, named("org.apache.http.protocol.HttpContext")))
                        .and(takesArgument(3, named("org.apache.http.concurrent.FutureCallback"))),
                this.getClass().getName() + "$ExecuteAdvice"));
    }

    @SuppressWarnings("unused")
    public static class ExecuteAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.Argument(0) HttpAsyncRequestProducer producer,
            @Advice.Argument(value = 3, readOnly = false) FutureCallback<?> callback,
            @Advice.Local("mockResult") MockResult mockResult) {
            try {
                if (ApacheHttpClientHelper.ignoreRequest(producer.generateRequest())) {
                    callback = FutureCallbackWrapper.wrap(callback);
                    return false;
                }
            } catch (Throwable ignored) {
                callback = FutureCallbackWrapper.wrap(callback);
                return false;
            }

            if (ContextManager.needRecordOrReplay() && RepeatedCollectManager.validate()) {
                // recording works in callback wrapper
                FutureCallbackWrapper<?> callbackWrapper = FutureCallbackWrapper.get(producer, callback);
                if (callbackWrapper != null) {
                    callback = callbackWrapper;
                    if (ContextManager.needReplay()) {
                        mockResult = ((FutureCallbackWrapper<?>)callback).replay();
                        return mockResult != null && mockResult.notIgnoreMockResult();
                    }
                }
            } else {
                callback = FutureCallbackWrapper.wrap(callback);
            }
            return false;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Argument(value = 3, readOnly = false) FutureCallback<?> callback,
            @Advice.Return(readOnly = false) Future<?> future,
            @Advice.Local("mockResult") MockResult mockResult) {
            if (callback instanceof FutureCallbackWrapper &&
                mockResult != null && mockResult.notIgnoreMockResult()) {
                FutureCallbackWrapper<?> callbackWrapper = (FutureCallbackWrapper<?>) callback;
                future = callbackWrapper.replay(mockResult);
            }
        }
    }
}