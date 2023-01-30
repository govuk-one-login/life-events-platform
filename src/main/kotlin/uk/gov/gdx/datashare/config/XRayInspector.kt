package uk.gov.gdx.datashare.config

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.entities.Subsegment
import com.amazonaws.xray.interceptors.TracingInterceptor
import com.amazonaws.xray.spring.aop.BaseAbstractXRayInterceptor
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.interceptor.Context
import software.amazon.awssdk.core.interceptor.Context.BeforeExecution
import software.amazon.awssdk.core.interceptor.ExecutionAttributes

@Aspect
@Component
@ConditionalOnProperty(prefix = "xray-tracing", name=["enabled"], havingValue = "true")
class XRayInspector : BaseAbstractXRayInterceptor() {
  @Throws(Exception::class)
  override fun generateMetadata(
    proceedingJoinPoint: ProceedingJoinPoint,
    subsegment: Subsegment,
  ): Map<String, Map<String, Any>> {
    return super.generateMetadata(proceedingJoinPoint, subsegment)
  }

  @Pointcut("@within(com.amazonaws.xray.spring.aop.XRayEnabled)")
  public override fun xrayEnabledClasses() {
  }
}

class SqsTracingInterceptor() : TracingInterceptor() {
  companion object {
    private const val SEGMENT_NAME = "SQS"
  }

  override fun beforeExecution(context: BeforeExecution?, executionAttributes: ExecutionAttributes) {
    AWSXRay.getCurrentSegmentOptional().orElseGet { AWSXRay.beginSegment(SEGMENT_NAME) }
    super.beforeExecution(context, executionAttributes)
  }

  override fun afterExecution(context: Context.AfterExecution?, executionAttributes: ExecutionAttributes?) {
    super.afterExecution(context, executionAttributes)
    if (AWSXRay.getCurrentSegment()?.name == SEGMENT_NAME) {
      AWSXRay.endSegment()
    }
  }
}
