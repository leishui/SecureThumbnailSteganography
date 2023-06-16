import com.google.gson.Gson
import java.io.File
import java.util.*

fun main(args: Array<String>) {
    val configFile = File("config_sts.json")
    val currentPath = System.getProperty("user.dir").replace("\\", "\\\\")
    val gson = Gson()
    //配置文件不存在则新建一个
    if (!configFile.exists()) {
        configFile.createNewFile()
        val writer = configFile.writer()
        writer.write(
            StsConfig(
                sourceDir = currentPath,
                targetDir = currentPath,
                nThreads = Runtime.getRuntime().availableProcessors()
            ).toFormatJson()
        )
        writer.flush()
        writer.close()
    }
    val config = gson.fromJson(configFile.readText(), StsConfig::class.java)
    val edpic = Sts(config)
    if (args.isEmpty()) {
        println(
            "\t== Current Configuration ==\n" +
                    DIVIDER +
                    "\n$config\n" +
                    DIVIDER
        )
        print(
            "\t[e] encrypt\n" +
                    "\t[d] decrypt\n" +
                    "\t[c] cancel\n" +
                    "(e/d/c):"
        )
        val input = Scanner(System.`in`).nextLine()
        println(DIVIDER)
        when (input) {
            "e", "E" -> edpic.imageEncrypt()
            "d", "D" -> edpic.imageDecrypt()
            "c", "C", "" -> return
            else -> println("Invalid argument")
        }
    } else {
        when (args[0]) {
            "-e", "-E" -> edpic.imageEncrypt()
            "-d", "-D" -> edpic.imageDecrypt()
            else -> println("Invalid argument")
        }
    }
}