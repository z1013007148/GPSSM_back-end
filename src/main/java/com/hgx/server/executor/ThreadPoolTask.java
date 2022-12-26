package com.hgx.server.executor;

import com.hgx.server.util.SendEmail;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author fish
 * @create 2021-11-24 18:55
 */
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ThreadPoolTask implements Runnable {
    private String targetName;
    private String destpath;  // /iobio/server_XXX/userResult/uuid/
    private String email;
    private String uuid;
    private String subject;
    private SendEmail sendEmail;
    private String fileName;

    public static boolean isDouble(String s){
        try{
            Double.valueOf(s);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

    @Override
    public void run() {
        log.info("进入任务子线程中 " + uuid);
        ThreadPoolExecutor emailSenderPool = EmailSenderExecutorPool.getINSTANCE();

        log.debug("给用户发提交邮件");
        String body1 = "Dear User,<br/>" +
                "This email is to inform that your GPSSM job " + targetName + " has been submitted successfully.<br/>" +
                "After the task is completed, we will inform you through this mailbox.<br/>" +
                "Thanks for using the GPSSM server.<br/><br/>" +
                "-------<br/>" +
                "The Zhang Lab<br/>" +
                "College of Information Engineering<br/>" +
                "Zhejiang University of technology";

        emailSenderPool.submit(() -> {
            try {
                sendEmail.sendMail(email, "GPSSM job " + targetName + " has been submitted successfully",
                        body1, null, uuid);
            } catch (MessagingException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

        // 定时扫描结果文件
        String score="";
        File jobFolder = new File(destpath);
        File[] files;
        int once_minute = 1;
        int max_times = 30*24*60/once_minute; // 最大循环时间30天，防止有些任务没结果一直跑
        boolean continueFlag = true;
        while(max_times > 0){
            files = jobFolder.listFiles();
            log.info("files.length="+files.length);
            if(files.length>2){
                for(File file:files){
                    if(isDouble(file.getName())){
                        score = file.getName();
                        continueFlag = false;
                        break;
                    }
                }
            }
            if(!continueFlag){
                break;
            }
            try {
                Thread.sleep(1000 * 60 * once_minute);  // *分钟扫描一次
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            --max_times;
        }
        log.info("任务 " + uuid + " 检测到结果文件");
        log.debug("给用户发结果邮件");
        String body2 = "Dear User,<br/>" +
                "This email is to inform that your GPSSM job " + targetName + " has been completed.<br/>" +
                "The score for the protein structure you submitted is <strong>" + score + "</strong><br/>" +
                "Thanks for using the GPSSM server.<br/><br/>" +
                "-------<br/>" +
                "The Zhang Lab<br/>" +
                "College of Information Engineering<br/>" +
                "Zhejiang University of technology";

        emailSenderPool.submit(() -> {
            try {
                sendEmail.sendMail(email, "GPSSM job " + targetName + " was completed",
                        body2, null, uuid);
            } catch (MessagingException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }
}
