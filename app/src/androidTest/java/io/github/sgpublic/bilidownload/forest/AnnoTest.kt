package io.github.sgpublic.bilidownload.forest

import com.dtflys.forest.Forest
import com.dtflys.forest.annotation.Address
import com.dtflys.forest.annotation.Body
import com.dtflys.forest.annotation.MethodLifeCycle
import com.dtflys.forest.annotation.Post
import com.dtflys.forest.http.ForestRequest
import com.dtflys.forest.lifecycles.MethodAnnotationLifeCycle
import com.dtflys.forest.reflection.ForestMethod
import org.junit.Test

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@MethodLifeCycle(TestLifecycle::class)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class TestAnno(
    val testBody: String = "1234",
)

class TestLifecycle: MethodAnnotationLifeCycle<TestAnno, Any> {
    private lateinit var testBody: String

    override fun onInvokeMethod(request: ForestRequest<*>?, method: ForestMethod<*>?, args: Array<out Any>?) {

    }

    override fun onMethodInitialized(method: ForestMethod<*>, annotation: TestAnno) {
        testBody = annotation.testBody
    }

    override fun beforeExecute(request: ForestRequest<*>): Boolean {
//        request.contentFormUrlEncoded()
        request.addBody("test_body", testBody)
        return super.beforeExecute(request)
    }
}

@Address(scheme = "http", host = "localhost")
interface AnnoClient {
    @TestAnno
    @Post("/test_api")
    fun post(@Body("pre_body") preBody: String): ForestRequest<String>
}

class AnnoTest {
    @Test
    fun test() {
        Forest.client(AnnoClient::class.java).post("pre_body").execute()
    }
}