package com.ustc.myy.aiagent.manus.config.startUp;

import com.ustc.myy.aiagent.manus.config.ManusProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

@Component
@Slf4j
public class AppStartupListener implements ApplicationListener<ApplicationReadyEvent> {
	
    @Value("${server.port:18080}")
    // 这里用的spring原始的，因为要跟配置文件保持一致。
    private String serverPort;

    @Autowired
    private ManusProperties manusProperties;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 只有当配置允许自动打开浏览器时才执行
        if (!manusProperties.getOpenBrowserAuto()) {
            log.info("自动打开浏览器功能已禁用");
            return;
        }

        String url = "http://localhost:" + serverPort + "/";
        log.info("应用已启动，正在尝试打开浏览器访问: {}", url);

        // 首先尝试使用Desktop API
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                log.info("已通过Desktop API成功打开浏览器");
                return;
            }
        } catch (Exception e) {
            log.warn("使用Desktop API打开浏览器失败，尝试使用Runtime执行命令", e);
        }

        // 如果Desktop API失败，尝试使用Runtime执行命令
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();
        try {
            if (os.contains("mac")) {
                // macOS特定命令
                rt.exec(new String[]{"open", url});
                log.info("已通过macOS open命令成功打开浏览器");
            } else if (os.contains("win")) {
                // Windows特定命令
                rt.exec(new String[]{"cmd", "/c", "start", url});
                log.info("已通过Windows命令成功打开浏览器");
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux特定命令，尝试几种常见的浏览器打开方式
                String[] browsers = {"google-chrome", "firefox", "mozilla", "epiphany", "konqueror", "netscape",
                        "opera", "links", "lynx"};

                StringBuilder cmd = new StringBuilder();
                for (int i = 0; i < browsers.length; i++) {
                    if (i == 0) {
                        cmd.append(String.format("%s \"%s\"", browsers[i], url));
                    } else {
                        cmd.append(String.format(" || %s \"%s\"", browsers[i], url));
                    }
                }

                rt.exec(new String[]{"sh", "-c", cmd.toString()});
                log.info("已通过Linux命令尝试打开浏览器");
            } else {
                log.warn("未知操作系统，无法自动打开浏览器，请手动访问: {}", url);
            }
        } catch (IOException e) {
            log.error("通过Runtime执行命令打开浏览器失败", e);
            log.info("请手动在浏览器中访问: {}", url);
        }
    }

}
