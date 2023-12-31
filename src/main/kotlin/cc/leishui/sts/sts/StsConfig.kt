package cc.leishui.sts.sts

class StsConfig(
    // 源目录
    val sourceDir: String = "",
    // 目标目录
    val targetDir: String = "",
    // 密码
    val password: String = "abc",
    // 图片缩放尺寸
    val compressedSize: Int = 50,
    // 是否递归子目录
    val isRecursive: Boolean = false,
    // 扫描扩展名
    val extensions: Array<String> = arrayOf("jpg", "png", "jpeg"),
    // 线程数
    val nThreads: Int = 1
) {
    /**
     * 将配置信息转为格式化的Json
     *
     * @return Json 配置信息
     */
    fun toFormatJson(): String {
        return "{\n" +
                "\t\"sourceDir\"     : \"$sourceDir\",\n" +
                "\t\"targetDir\"     : \"$targetDir\",\n" +
                "\t\"password\"      : \"$password\",\n" +
                "\t\"compressedSize\": $compressedSize,\n" +
                "\t\"isRecursive\"   : $isRecursive,\n" +
                "\t\"extensions\"    : ${stringExtensions()},\n" +
                "\t\"nThreads\"      : $nThreads\n" +
                "}"
    }

    /**
     * 扩展名列表文本化
     */
    private fun stringExtensions(): String {
        val sb = StringBuffer()
        sb.append("[")
        for (s in extensions) {
            sb.append("\"$s\", ")
        }
        sb.delete(sb.length - 2, sb.length)
        sb.append("]")
        return sb.toString()
    }

    override fun toString(): String {
        return "sourceDir      : $sourceDir\n" +
                "targetDir      : $targetDir\n" +
                "password       : $password\n" +
                "compressedSize : $compressedSize\n" +
                "isRecursive    : $isRecursive\n" +
                "extensions     : ${stringExtensions()}\n" +
                "nThreads:      : $nThreads"
    }
}
