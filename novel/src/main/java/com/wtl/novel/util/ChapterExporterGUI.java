package com.wtl.novel.util;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.*;

public class ChapterExporterGUI extends JFrame {

    private JTextField tfUser, tfPassword, tfIP, tfPort, tfDBName, tfNovelId, tfOutput;
    private JButton btnExport, btnChooseFile;
    private JTextArea taLog;

    public ChapterExporterGUI(String[] args) {
        setTitle("章节导出器");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 输入面板
        JPanel inputPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        tfUser = new JTextField();
        tfPassword = new JTextField();
        tfIP = new JTextField("127.0.0.1");
        tfPort = new JTextField("3306");
        tfDBName = new JTextField("djangoblog");
        tfNovelId = new JTextField();
        tfOutput = new JTextField();
        btnChooseFile = new JButton("选择文件");

        inputPanel.add(new JLabel("数据库账号:"));
        inputPanel.add(tfUser);
        inputPanel.add(new JLabel("数据库密码:"));
        inputPanel.add(tfPassword);
        inputPanel.add(new JLabel("数据库IP:"));
        inputPanel.add(tfIP);
        inputPanel.add(new JLabel("数据库端口:"));
        inputPanel.add(tfPort);
        inputPanel.add(new JLabel("数据库名:"));
        inputPanel.add(tfDBName);
        inputPanel.add(new JLabel("小说ID:"));
        inputPanel.add(tfNovelId);
        inputPanel.add(new JLabel("输出文件路径(最好选择桌面):"));
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add(tfOutput, BorderLayout.CENTER);
        filePanel.add(btnChooseFile, BorderLayout.EAST);
        inputPanel.add(filePanel);

        add(inputPanel, BorderLayout.NORTH);

        // 日志面板
        taLog = new JTextArea();
        taLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(taLog);
        add(scrollPane, BorderLayout.CENTER);

        // 导出按钮
        btnExport = new JButton("导出章节");
        add(btnExport, BorderLayout.SOUTH);

        // 事件处理
        btnChooseFile.addActionListener(e -> chooseFile());
        btnExport.addActionListener(e -> exportChapters());

        // ======== 处理命令行参数 =========
        if (args.length >= 7) {
            tfUser.setText(args[0]);
            tfPassword.setText(args[1]);
            tfIP.setText(args[2]);
            tfPort.setText(args[3]);
            tfDBName.setText(args[4]);
            tfNovelId.setText(args[5]);
            tfOutput.setText(args[6]);
            // 可以直接自动执行导出（可选）
            // exportChapters();
        }
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择输出文件");
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            tfOutput.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void exportChapters() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String user = tfUser.getText().trim();
        String password = tfPassword.getText().trim();
        String ip = tfIP.getText().trim();
        String port = tfPort.getText().trim();
        String dbName = tfDBName.getText().trim();
        String novelIdStr = tfNovelId.getText().trim();
        String outputFile = tfOutput.getText().trim();

        if (user.isEmpty() || password.isEmpty() || ip.isEmpty() || port.isEmpty()
                || dbName.isEmpty() || novelIdStr.isEmpty() || outputFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有字段！");
            return;
        }

        long novelId;
        try {
            novelId = Long.parseLong(novelIdStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "小说ID必须为数字！");
            return;
        }

        taLog.setText("");
        new Thread(() -> {
            try {
                // 自动创建目录
                File outFile = new File(outputFile);
                File parentDir = outFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }

                String url = "jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=Asia/Shanghai";
                try (Connection conn = DriverManager.getConnection(url, user, password);
                     PreparedStatement ps = conn.prepareStatement(
                             "SELECT chapter_number, title, content FROM chapter WHERE novel_id=? AND is_deleted=0 ORDER BY chapter_number");
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"))) {

                    ps.setLong(1, novelId);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        int number = rs.getInt("chapter_number");
                        String title = rs.getString("title");
                        byte[] compressed = rs.getBytes("content");
                        String content = decompress(compressed);

                        writer.write("### Chapter " + number + " : " + title + " ###\n");
                        writer.write(content + "\n\n");

                        final String logMsg = "导出章节：" + number + " - " + title + "\n";
                        SwingUtilities.invokeLater(() -> taLog.append(logMsg));
                    }
                    SwingUtilities.invokeLater(() -> taLog.append("导出完成！文件路径：" + outputFile + "\n"));
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> taLog.append("导出失败: " + ex.getMessage() + "\n"));
            }
        }).start();
    }

    // ===================== 压缩工具 =====================
    public static byte[] compress(String text) throws IOException {
        if (text == null || text.isEmpty()) return null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return bos.toByteArray();
    }

    public static String decompress(byte[] compressed) throws IOException {
        if (compressed == null || compressed.length == 0) return "";
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzip.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
        }
        return bos.toString(StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChapterExporterGUI gui = new ChapterExporterGUI(args);
            gui.setVisible(true);
        });
    }
}
