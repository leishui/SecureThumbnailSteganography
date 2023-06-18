## 简介：

本项目通过将原图片压缩加密并合并到其缩率图中，实现将图片安全上传到各种云盘。并在云盘中可以以缩率图的形式预览图片而无需担心暴露隐私。

目前项目采用的是使用7z软件进行加解密，首先通过7z加密原图文件并设置密码，然后将原图缩放到低分辨率，再将两个文件进行合并，从而得到目标文件，可以将目标文件安全上传到云盘。

解密时也是通过7z将目标文件解压从而还原到原图文件。

## 项目特点：

- 隐写：将压缩且加密后的原图数据嵌入到其缩略图中，使得缩略图在外观上仍然是有效的缩略图，但隐藏了加密后的原图数据。
- 减少存储占用：通过压缩减少文件体积，从而减少云盘存储空间占用。
- 安全预览：用户可以在云盘中查看缩略图，但无法直接访问或查看原图，从而保护敏感图片的隐私和安全性。
- 文档和示例：提供详细的文档和示例代码，帮助用户理解和使用项目的功能，并进行集成和扩展。

## 示例：

### 准备工作

确保本机以安装好JDK或JRE并配置好环境变量

安装好7z并配置好环境变量

从[release](https://github.com/leishui/SecureThumbnailSteganography/releases/tag/v1.0) 中下载`sts.jar`

### 配置：

跳转到`sts.jar`所在目录，打开命令行执行以下命令

```
java -jar sts.jar c
```

之后可以看到在当前目录下生成的`config_sts.json`配置文件：

```json
{
	// 源目录      
	"sourceDir"     : "D:\\Projects\\IdeaProjects\\edpic\\out\\artifacts\\sts_jar",
	// 目标目录    
	"targetDir"     : "D:\\Projects\\IdeaProjects\\edpic\\out\\artifacts\\sts_jar",
	// 密码        
	"password"      : "abc",
	// 图片缩放尺寸 
	"compressedSize": 50,
	// 是否递归子目录
	"isRecursive"   : false,
	// 扫描扩展名   
	"extensions"    : ["jpg", "png", "jpeg"],
	// 线程数      
	"nThreads"      : 4
}
```

根据需要修改响应参数，注意Windows平台目录使用`\\`连接，Linux平台使用`/`

### 使用：

分为加密解密两个功能。

#### 加密

从`sourceDir`目录扫描扩展名相符的图片文件，若设置`isRecusive`为true，则会递归地扫描所有的子文件夹，加密后的文件会放到`targetDir`目录。

两种方式：

**第一种：**

```
java -jar sts.jar
```

在输出的菜单下输入`e`并按回车。

**第二种：**

```
java -jar sts.jar -e
```

#### 解密

从`sourceDir`目录扫描扩展名相符的图片文件，若设置`isRecusive`为true，则会递归地扫描所有的子文件夹，解密后的文件会放到`targetDir`目录。

两种方式：

**第一种：**

```
java -jar sts.jar
```

在输出的菜单下输入`d`并按回车。

**第二种：**

```
java -jar sts.jar -d
```

