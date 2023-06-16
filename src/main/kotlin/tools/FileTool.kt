package tools

import COMPRESS_FILE_ERROR
import COMPRESS_IMAGE_ERROR
import DECOMPRESS_FILE_ERROR
import StsConfig
import MERGE_FILE_ERROR
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentLinkedQueue
import javax.imageio.ImageIO

class FileTool(
    private val config: StsConfig
) {
    //保存扫描到的所有图像文件相对于源目录(sourceDir)的相对路径
    private val queue = ConcurrentLinkedQueue<String>()

    /**
     * 合并两个临时文件
     *
     * @param imageRelativePath 图片文件的相对路径, 相对于[[StsConfig.sourceDir]
     * @return 方法执行完的状态码 代表成功与否
     */
    fun mergeFiles(imageRelativePath: String): Int {
        //图片文件以 "D:\edpic\input.jpg" 为例
        //假设 sourceDir 为 "D:\edpic" imageRelativePath 为 "input.jpg"
        //targetDir 为 "D:\target"
        val imageFile = File(config.sourceDir, imageRelativePath)
        //缩率图文件为 "D:\target\input.jpg.jpg"
        val file1 = File(config.targetDir, imageRelativePath + "." + imageFile.extension)
        //原图压缩包为 "D:\target\input.jpg.7z"
        val file2 = File(config.targetDir, "$imageRelativePath.7z")
        //最后合并到 "D:\target\input.jpg"
        val outputFile = File(config.targetDir, imageRelativePath.removeSuffix(imageFile.name) + "E_" + imageFile.name)
        try {
            //尝试合并两个文件
            FileInputStream(file1).use { inputStream1 ->
                FileInputStream(file2).use { inputStream2 ->
                    FileOutputStream(outputFile).use { outputStream ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int

                        // 复制文件1的内容到输出流
                        bytesRead = inputStream1.read(buffer)
                        while (bytesRead != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            bytesRead = inputStream1.read(buffer)
                        }

                        // 复制文件2的内容到输出流
                        bytesRead = inputStream2.read(buffer)
                        while (bytesRead != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            bytesRead = inputStream2.read(buffer)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return MERGE_FILE_ERROR
        } finally {
            //将 "D:\target\input.jpg.7z", "D:\target\input.jpg.jpg" 两个临时文件删除
            file1.delete()
            file2.delete()
        }
        return 0
    }

    /**
     * 通过 7z 对文件进行压缩，将文件以[StsConfig.password] 为密码压缩到
     * [StsConfig.targetDir] 目录中
     *
     * @param imageRelativePath 图片文件的相对路径, 相对于[StsConfig.sourceDir]
     * @return 方法执行完的状态码 代表成功与否
     */
    fun compressFile(imageRelativePath: String): Int {
        val command = "7z a -mx1 -p${config.password} -y \"${
            Paths.get(
                config.targetDir,
                imageRelativePath
            )
        }.7z\" \"${Paths.get(config.sourceDir, imageRelativePath)}\""
        //println(command)
        return if (executeCommand(command)[0].toInt() > 0) COMPRESS_FILE_ERROR else 0
    }

    /**
     * 通过 7z 对文件进行解压，将文件以[config]中的 password 为密码解压到
     * [config]中的 targetDir 目录中
     *
     * @param imageRelativePath 图片文件的相对路径, 相对于[StsConfig.sourceDir]
     * @return 方法执行完的状态码 代表成功与否
     */
    fun decompressFile(imageRelativePath: String): Int {
        val image = File(config.sourceDir, imageRelativePath)
        val command = "7z x -p${config.password} -y \"${Paths.get(config.sourceDir, imageRelativePath)}\" -o\"${
            Paths.get(
                config.targetDir,
                imageRelativePath.removeSuffix(image.name)
            )
        }\""
        return if (executeCommand(command)[0].toInt() > 0) DECOMPRESS_FILE_ERROR else 0
    }

    /**
     * 对[[StsConfig.sourceDir] 目录下的后缀与配置[StsConfig.extensions] 相符的文件进行扫描，
     * 将结果保存到[queue]中并进行返回
     *
     * @return 扫描到的文件列表
     */
    fun scanFilesWithExtension(): ConcurrentLinkedQueue<String> {
        scanFiles(File(config.sourceDir))
        return queue
    }

    /**
     * 从指定目录向下扫描后缀相符的文件，通过[StsConfig.isRecursive] 决定是否递归的扫描所有子目录
     *
     * @param folder 扫描的起始目录
     */
    private fun scanFiles(folder: File) {
        if (folder.isDirectory) {
            folder.listFiles()?.forEach { file ->
                if (file.isDirectory && config.isRecursive) {
                    scanFiles(file)
                } else if (file.isFile && file.extension in config.extensions) {
                    queue.offer(getRelativePath(file.absolutePath))
                }
            }
        }
    }

    /**
     * 从绝对路径转化为自[StsConfig.sourceDir] 的相对路径
     *
     * @param filePath 文件绝对路径
     * @return 转换得到的相对路径
     */
    private fun getRelativePath(filePath: String): String {
        val fileAbsolutePath: Path = Paths.get(filePath).toAbsolutePath()
        val baseAbsolutePath: Path = Paths.get(config.sourceDir).toAbsolutePath()
        return baseAbsolutePath.relativize(fileAbsolutePath).toString()
    }

    /**
     * 对图像分辨率的压缩，通过[StsConfig.compressedSize] 决定压缩后的长和宽的最大值
     *
     * @param imageRelativePath 图片文件的相对路径, 相对于[StsConfig.sourceDir]
     * @return 方法执行完的状态码 代表成功与否
     */
    fun imageCompress(imageRelativePath: String): Int {
        val imageFile = File(config.sourceDir, imageRelativePath)
        val outputFile = File(config.targetDir, imageRelativePath + "." + imageFile.extension) // 输出缩小后的图片文件

        try {
            // 读取输入图片
            val inputImage = ImageIO.read(imageFile)

            // 计算缩放比例
            val aspectRatio: Double
            val targetHeight: Int
            val targetWidth: Int
            if (inputImage.width > inputImage.height) {
                aspectRatio = inputImage.width.toDouble() / inputImage.height.toDouble()
                targetWidth = config.compressedSize
                targetHeight = (targetWidth / aspectRatio).toInt()
            } else {
                aspectRatio = inputImage.width.toDouble() / inputImage.height.toDouble()
                targetHeight = config.compressedSize
                targetWidth = (targetHeight * aspectRatio).toInt()
            }

            // 创建缩小后的缓冲图像
            val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
            val graphics2D = resizedImage.createGraphics()

            // 使用平滑缩放算法
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)

            // 绘制缩小后的图像
            graphics2D.drawImage(inputImage, 0, 0, targetWidth, targetHeight, null)
            graphics2D.dispose()

            // 将缩小后的图像保存到文件
            ImageIO.write(resizedImage, imageFile.extension, outputFile)

            return 0
        } catch (e: IOException) {
            //e.printStackTrace()
            return COMPRESS_IMAGE_ERROR
        }
    }
}
