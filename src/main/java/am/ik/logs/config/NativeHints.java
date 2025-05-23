package am.ik.logs.config;

import am.ik.logs.Log;
import com.google.protobuf.DescriptorProtos;
import com.rabbitmq.stream.Constants;
import com.rabbitmq.stream.impl.Client;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.common.v1.KeyValueList;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.LogsData;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.ScopeLogs;
import io.opentelemetry.proto.resource.v1.Resource;
import jakarta.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.eclipse.jetty.http.pathmap.PathSpecSet;
import org.eclipse.jetty.util.AsciiLowerCaseSet;
import org.eclipse.jetty.util.ClassMatcher;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(NativeHints.RuntimeHints.class)
@RegisterReflectionForBinding(Log.class)
public class NativeHints {

	public static class RuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(org.springframework.aot.hint.RuntimeHints hints, @Nullable ClassLoader classLoader) {
			try {
				hints.reflection()
					.registerConstructor(ClassMatcher.ByPackage.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(ClassMatcher.ByClass.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(ClassMatcher.ByPackageOrName.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(ClassMatcher.ByLocation.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(ClassMatcher.ByModule.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(ClassMatcher.ByLocationOrModule.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(PathSpecSet.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(AsciiLowerCaseSet.class.getConstructor(), ExecutableMode.INVOKE)
					.registerConstructor(HashSet.class.getConstructor(), ExecutableMode.INVOKE);
				for (Method method : LogsData.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : LogsData.Builder.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : ResourceLogs.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : ResourceLogs.Builder.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : ScopeLogs.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : ScopeLogs.Builder.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : LogRecord.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : LogRecord.Builder.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : InstrumentationScope.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : InstrumentationScope.Builder.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : Resource.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : Resource.Builder.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : KeyValue.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : KeyValue.Builder.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : KeyValueList.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : KeyValueList.Builder.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : AnyValue.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : AnyValue.Builder.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : ArrayValue.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : ArrayValue.Builder.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : DescriptorProtos.FeatureSet.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				for (Method method : DescriptorProtos.FeatureSet.Builder.class.getDeclaredMethods()) {
					hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				}
				hints.resources().registerPattern("opentelemetry/*").registerPattern("antlr4/*");

				hints.resources().registerPattern("rabbitmq-stream-client.properties");
				// from com.rabbitmq.stream.impl.ThreadUtils
				Optional<Class<?>> optionalBuilderClass = Arrays.stream(Thread.class.getDeclaredClasses())
					.filter(c -> "Builder".equals(c.getSimpleName()))
					.findFirst();
				if (optionalBuilderClass.isPresent()) {
					Class<?> builderClass = optionalBuilderClass.get();
					hints.reflection()
						.registerMethod(Thread.class.getDeclaredMethod("ofVirtual"), ExecutableMode.INVOKE)
						.registerMethod(builderClass.getDeclaredMethod("factory"), ExecutableMode.INVOKE)
						.registerMethod(builderClass.getDeclaredMethod("name", String.class, Long.TYPE),
								ExecutableMode.INVOKE)
						.registerMethod(builderClass.getDeclaredMethod("factory"), ExecutableMode.INVOKE)
						.registerMethod(
								Executors.class.getDeclaredMethod("newThreadPerTaskExecutor", ThreadFactory.class),
								ExecutableMode.INVOKE)
						.registerMethod(Thread.class.getDeclaredMethod("isVirtual"), ExecutableMode.INVOKE);
				}
				// com.rabbitmq.stream.impl.ServerFrameHandler
				Arrays.stream(Constants.class.getDeclaredFields())
					.filter(f -> f.getName().startsWith("COMMAND_"))
					.forEach(command -> hints.reflection().registerField(command));
				Arrays.stream(Client.ClientParameters.class.getDeclaredFields())
					.forEach(field -> hints.reflection().registerField(field));
			}
			catch (NoSuchMethodException e) {
				throw new IllegalStateException(e);
			}
		}

	}

}