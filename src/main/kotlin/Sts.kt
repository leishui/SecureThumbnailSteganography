import tools.*
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


class Sts(private val config: StsConfig) {

    private var success: AtomicInteger = AtomicInteger(0) // 成功数
    private var failure: AtomicInteger = AtomicInteger(0) // 失败数
    private var sum: Int = 0 // 需要处理的图片总数
    private var queue = ConcurrentLinkedQueue<String>() // 创建并发队列
    private val errorQueue = ConcurrentLinkedQueue<String>() // 创建并发队列
    private val executor = Executors.newFixedThreadPool(config.nThreads) // 创建固定线程池，这里使用4个线程
    private val fileTool = FileTool(config)

    /**
     * 初始化
     */
    init {
        // 目标目录若不存在则创建之
        val target = File(config.targetDir)
        if (!target.exists()) target.mkdir()
        // 扫描源目录下的图片文件
        queue = fileTool.scanFilesWithExtension()
        sum = queue.size
    }

    /**
     * 图片加密操作
     */
    fun imageEncrypt() {
        runCommand(true)
    }

    /**
     * 图片解密操作
     */
    fun imageDecrypt() {
        runCommand(false)
    }

    private fun runCommand(aox: Boolean) {
        // 记录开始时间
        val startTime = System.currentTimeMillis()
        // 处理队列中的数据
        if (aox) {
            //加密过程
            while (!queue.isEmpty()) {
                val imageRelativePath = queue.poll() // 从队列中取出数据
                // 提交任务到线程池中处理数据
                executor.submit {
                    encryptTask(imageRelativePath)
                }
            }
        } else {
            //解密过程
            while (!queue.isEmpty()) {
                val imageRelativePath = queue.poll() // 从队列中取出数据
                // 提交任务到线程池中处理数据
                executor.submit {
                    decryptTask(imageRelativePath)
                }
            }
        }
        executor.shutdown()
        try {
            // 等待线程池中的任务执行完成，最多等待 1 天
            if (executor.awaitTermination(1, TimeUnit.DAYS)) {
                // 记录结束时间
                val endTime = System.currentTimeMillis()
                // 更新进度条到 100%
                updateProgressBar(sum, sum)
                // 计算执行时间
                val elapsedTime = endTime - startTime
                println("\nElapsed Time: $elapsedTime ms")
                println("sum:$sum\tsuccess:$success\tfailure:$failure")
                if (!errorQueue.isEmpty()) {
                    println(DIVIDER)
                    println("\tfailed files:")
                }
                for (s in errorQueue) {
                    println("[x] $s")
                }
            } else {
                println("Timeout occurred while waiting for tasks to complete.")
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * 加密任务
     *
     * @param imageRelativePath 图片文件的相对路径, 相对于[StsConfig.sourceDir]
     */
    private fun encryptTask(imageRelativePath: String) {

        //1.将原图文件压缩为7z
        //1.Compressing the original image file to 7z
        var errorCode = fileTool.compressFile(imageRelativePath)

        //2.将图片压缩到低分辨率
        //2.Compressing the image to a lower resolution
        errorCode += fileTool.imageCompress(imageRelativePath)

        //3.将低分辨率图片与原图的压缩文件合并为一个文件
        //3.Merging the compressed file of the low-resolution image with the original image into one file
        //4.删除两个临时文件
        //4.Deleting two temporary files
        fileTool.mergeFiles(imageRelativePath)

        //5.更新进度
        //5.Updating progress
        updateProgress(errorCode, imageRelativePath)
    }

    /**
     * 解密任务
     *
     * @param imageRelativePath 图片文件的相对路径, 相对于[StsConfig.sourceDir]
     */
    private fun decryptTask(imageRelativePath: String) {
        val errorCode = fileTool.decompressFile(imageRelativePath)
        updateProgress(errorCode, imageRelativePath)
    }

    /**
     * 将错误信息保存并更新进度条
     *
     * @param errorCode 错误代码
     * @param imageRelativePath 图片文件的相对路径, 相对于[StsConfig.sourceDir]
     */
    private fun updateProgress(errorCode: Int, imageRelativePath: String) {
        if (errorCode == 0) success.incrementAndGet()
        else {
            failure.incrementAndGet()
            errorQueue.offer(
                "[" + Integer.toBinaryString(errorCode) + "] " + Paths.get(
                    config.sourceDir,
                    imageRelativePath
                )
            )
        }
        updateProgressBar(success.get() + failure.get(), sum)
    }

    /**
     * 更新进度条
     *
     * @param current 已完成任务数目
     * @param total 任务总数
     */
    private fun updateProgressBar(current: Int, total: Int) {
        val progress = (current.toDouble() / total * 100).toInt() // 计算进度百分比
        val barLength = 50 // 进度条长度

        val progressBar = StringBuilder()
        progressBar.append("$current/$total [")

        // 根据进度百分比计算进度条长度
        val completedLength = (progress.toDouble() / 100 * barLength).toInt()
        for (i in 0 until barLength) {
            if (i < completedLength) {
                progressBar.append('=')
            } else {
                progressBar.append(' ')
            }
        }

        progressBar.append(']')
        progressBar.append(String.format(" %d%%", progress))

        print("\r" + progressBar.toString()) // 使用回车符 "\r" 更新进度条
    }

}