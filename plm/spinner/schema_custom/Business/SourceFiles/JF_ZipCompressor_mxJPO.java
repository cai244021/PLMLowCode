import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import matrix.db.Context;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JF_ZipCompressor_mxJPO {
    private static final long CHUNK_SIZE = 1 * 1024L * 1024L * 1024L; // 1 GB
    private static final long splitLength = 1_073_741_824L;             // 分卷大小：1GB = 1024 * 1024 * 1024 字节

    private static final String SIZE = "1g"; // 1 GB
    private static final Logger JF_LOGGER = LoggerFactory.getLogger(JF_ZipCompressor_mxJPO.class);

//    方式一: 使用ZIP4J 完成分卷
    /**
     * 使用 ZIP4J 平铺压缩多个目录，生成分卷 ZIP
     * @param sourceDirs      源目录数组（绝对路径）
     * @param zipBasePath     输出主文件路径（不含 .zip，如 /path/to/sendData）
     * @author LIUJR
     * @throws
     * @return void
     * @date 2025/12/8 10:58
     * @description
     */
    public static void createSplitZip(String[] sourceDirs, String zipBasePath)
            throws IOException {
        // 1. 收集所有文件（递归）
        List<File> allFiles = new ArrayList<>();
        for (String dirPath : sourceDirs) {
            collectFilesRecursively(new File(dirPath), allFiles);
        }
        if (allFiles.isEmpty()) {
            throw new IllegalArgumentException("No files found in source directories.");
        }
        // 2. 设置输出 ZIP 文件（自动加 .zip）
        String zipFilePath = zipBasePath + ".zip";
        ZipFile zipFile = new ZipFile(zipFilePath);
        // 3. 配置参数：平铺 + 压缩
        ZipParameters params = new ZipParameters();
        params.setCompressionLevel(CompressionLevel.NORMAL); // 或 FASTEST / MAXIMUM
        // 注意：ZIP4J 不会自动保留路径，但我们手动只传文件名 → 实现平铺
        // 4. 启用分卷
        zipFile.createSplitZipFile(allFiles, params, true, splitLength);

        System.out.println(" ZIP 分卷压缩完成: " + zipFilePath);
    }

    private static void collectFilesRecursively(File file, List<File> fileList) {
        if (file.isFile()) {
            fileList.add(file);
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    collectFilesRecursively(child, fileList);
                }
            }
        }
    }

    // ===== 测试示例 =====
    public static void test1(Context context, String[] args) {
        try {
            String[] dirs = {
                    "/ds/share/test/plm/DOS-0001203/TransverterV5File",
                    "/ds/share/test/plm/DOS-0001203/OtherDoc"
            };
            // 输出路径（不带 .zip）
            String zipBasePath = "/ds/share/test/plm/DOS-0001203/sendData3/K01V02D06 data update FOR PV CAE 8WP";
            createSplitZip(dirs, zipBasePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    方式二: linux原生的zip压缩

    /**
    * 使用linux原生的zip压缩
    * @param outputPath
	* @param sourceDirs
    * @author LIUJR
    * @throws
    * @return void
    * @date 2025/12/23 9:25
    * @description
    */
    public static void createSplitOranZip(String[] sourceDirs, String outputPath) throws IOException, InterruptedException {
        // 构建源路径（带 /*）
        StringBuilder sources = new StringBuilder();
        for (String dir : sourceDirs) {
            if (!dir.endsWith("/")) {
                dir += "/";
            }
            sources.append(dir).append("* ");
        }
        String sourceGlobs = sources.toString();
        // 构建完整命令
        outputPath = shellQuote(outputPath);
        String command = String.format(
                "zip -j -s 1g %s %s",
                outputPath,
                sourceGlobs
        );
        System.out.println("Executing: " + command);
        // 通过 shell 执行（支持 * 通配符）
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
        pb.redirectErrorStream(true); // 合并 stderr 到 stdout
        Process process = pb.start();
        // 可选：读取输出日志
        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[zip] " + line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("zip command failed with exit code: " + exitCode);
        }
        System.out.println("分卷压缩完成: " + outputPath);
    }

    /**
     * 对字符串进行 shell 安全转义（类似 Python shlex.quote）
     */
    public static String shellQuote(String s) {
        if (s == null || s.isEmpty()) {
            return "''";
        }
        // 如果只包含字母数字、下划线、点、连字符，则无需引号
        if (s.matches("[a-zA-Z0-9._-]+")) {
            return s;
        }
        // 否则用单引号包裹，并转义内部的单引号
        return "'" + s.replace("'", "'\"'\"'") + "'";
    }

    // 使用示例
    public static void test2(Context context, String[] args) throws Exception {
        String[] dirs = {
                "/ds/share/test/plm/DOS-0001203/TransverterV5File",
                "/ds/share/test/plm/DOS-0001203/OtherDoc"
        };
        // 输出路径（不带 .zip）
        String zipBasePath = "/ds/share/test/plm/DOS-0001203/sendData2/K01V02D06 data update FOR PV CAE 8WP";
        createSplitOranZip(dirs, zipBasePath);
    }

    //    Apache Commons Compress 的 ZipArchiveOutputStream 手动实现分卷压缩 废弃 分卷大文件有问题
    /**
     * Apache Commons Compress 的 ZipArchiveOutputStream 手动实现分卷压缩 废弃 分卷大文件有问题
     * 废弃
     * @param sourceDir
     * @param zipFilePath
     * @author LIUJR
     * @throws
     * @return void
     * @date 2024/7/29 16:16
     * @description
     */
    public static void createSplitZipPath(String[] sourceDir, String zipFilePath) throws IOException {

        long totalSize = 0L;
        // 将多个 File 数组合并到一个 List 中
        List<File> fileList = new ArrayList<>();
        for (String dir : sourceDir) {
            totalSize += calculateDirectorySize(dir);
            File dirFile = new File(dir);
            File[] files = dirFile.listFiles();
            fileList.addAll(Arrays.asList(files));
            if (!dirFile.isDirectory()) {
                throw new IllegalArgumentException("Source path is not a directory");
            }
        }

        JF_LOGGER.info("fileList:{}", fileList.toString());
        JF_LOGGER.info("fileList.size:{}", fileList.size());
        int zipCount = (int) Math.ceil((double) totalSize / CHUNK_SIZE);
        JF_LOGGER.info("zipCount:{}", zipCount);

        OutputStream os = null;
        ZipArchiveOutputStream zos = null;
        int partNumber = 1;
        int bytesWritten = 0;
        int currentFileSize = 0;

        try {
            os = new FileOutputStream(zipFilePath + "_" + zipCount + ".z01");
            zos = new ZipArchiveOutputStream(os);
            for (File file : fileList) {
                if (file.isFile()) {
                    JF_LOGGER.info("file.name:{}", file.getName());
                    ZipArchiveEntry entry = new ZipArchiveEntry(file.getName());
                    zos.putArchiveEntry(entry);
                    try (InputStream is = new FileInputStream(file)) {
                        byte[] buffer = new byte[16384];
                        int length;
                        while ((length = is.read(buffer)) != -1) {
                            zos.write(buffer, 0, length);
                            currentFileSize += length;
                            bytesWritten += length;

                            if (bytesWritten >= CHUNK_SIZE) {
                                zos.closeArchiveEntry(); // 关闭当前的 ZIPArchiveEntry
                                zos.close();
                                bytesWritten = 0;
                                partNumber++;

                                // 创建下一个部分的输出流
                                os = new FileOutputStream(zipFilePath + "_" + zipCount + ".z0" + String.format("%02d", partNumber));
                                zos = new ZipArchiveOutputStream(os);

                                // 重新创建 ZIPArchiveEntry
                                entry = new ZipArchiveEntry(file.getName());
                                zos.putArchiveEntry(entry);

                                // 继续写入剩余的数据
                                zos.write(buffer, 0, length);
                                currentFileSize += length;
                                bytesWritten += length;
                            }
                        }
                    }

                    zos.closeArchiveEntry();
                }
            }
        } finally {
            if (zos != null) {
                zos.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    /**
     * 计算目录的总大小。
     * @param directory 目录路径
     * @author LIUJR
     * @throws
     * @return long 目录的总大小
     * @date 2024/7/25 10:43
     * @description
     */
    private static long calculateDirectorySize(String directory) {
        File dir = new File(directory);
        long size = 0;
        for (File file : dir.listFiles()) {
            // 如果是目录，则递归计算其大小
            size += file.isDirectory() ? calculateDirectorySize(file.getAbsolutePath()) : file.length();
        }
        return size;
    }

    /**
     * 测试
     * @param context
     * @param args
     * @author LIUJR
     * @throws
     * @return void
     * @date 2024/7/29 16:16
     * @description
     */
    public void test(Context context, String[] args) {
        try {
            String[] dir = {"/tmp/dataOurSource/DOS-0000006/DownloadV5File", "/tmp/dataOurSource/DOS-0000006/OtherDoc"};
            createSplitZipPath(dir, "/tmp/dataOurSource/DOS-0000006/sendData/DOS-0000006");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}