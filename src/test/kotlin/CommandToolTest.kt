import org.junit.Test
import cc.leishui.sts.tools.CommandTool.executeCommand

class CommandToolTest {
    @Test
    fun executeTest() {
        //val message = tools.executeCommand("dir")
        val message = executeCommand("7z")
        println("exit code:"+message[0])
        println("output:"+message[1])
    }
}