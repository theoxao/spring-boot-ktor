import com.theoxao.configuration.KtorAutoConfiguration
import com.theoxao.configuration.signature
import org.junit.Test

/**
 * @author theo
 * @date 2019/4/24
 */
class SomeTest {

    @Test
    fun test() {
        KtorAutoConfiguration::class.java.methods.forEach {
            println(it.signature())
        }
    }
}
