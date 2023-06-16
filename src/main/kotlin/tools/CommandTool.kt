package tools

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * 执行命令行指令，并返回结果
 *
 * @param command 要执行的指令
 * @return 命令执行完的返回值与命令行输出的结果
 */
fun executeCommand(command: String): Array<String> {
    val isWindows = System.getProperty("os.name").toLowerCase().contains("win")
    val processBuilder = ProcessBuilder()
    val cset:String
    // 根据操作系统设置命令执行方式
    if (isWindows) {
        cset = "gbk"
        processBuilder.command("cmd", "/c", command)
    } else {
        cset = "utf-8"
        processBuilder.command("sh", "-c", command)
    }
    val outputBuilder = StringBuilder()
    var exitValue = 1
    var output: String
    try {
        val process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream, Charset.forName(cset)))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            outputBuilder.append(line).append("\n")
        }
        output = outputBuilder.toString()
        exitValue = process.waitFor()
        reader.close()
    } catch (e: Exception) {
        e.printStackTrace()
        output = e.message.toString()
    }

    return arrayOf(exitValue.toString(),output)
}
