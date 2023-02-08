package com.hgx.server.controller;

import com.hgx.server.executor.TaskExecutor;
import com.hgx.server.executor.ThreadPoolTask;
import com.hgx.server.util.SendEmail;
import com.hgx.server.executor.EmailSenderExecutorPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;


@Controller
@PropertySource(value = {"classpath:application.properties"})
@Scope("prototype")
@Slf4j
public class TaskController {
    @Value("${output}")
    private String output;
    @Value("${output_userInfo}")
    private String output_userInfo;
    @Value("${subject}")
    private String subject;
    @Value("${backemail}")
    private String backEmail;

    @Autowired
    private SendEmail sendEmail;


    @RequestMapping(value = "/task")
    @ResponseBody
    private String task(HttpServletRequest httpServletRequest) {
        Date date = new Date();
        String targetName = httpServletRequest.getParameter("targetName");
        String ip = httpServletRequest.getRemoteAddr();
        String email = httpServletRequest.getParameter("email");
        String uuid = getUUID32();
        String fileName = httpServletRequest.getParameter("filename");

        String destpath = output + '/' + uuid + '/';  // /iobio/server_GraphGPSM/userResult/uuid/
        File file = new File(destpath);
        if (!file.exists()) file.mkdir();
        log.info("文件创建完成");

        try {
            if (httpServletRequest.getParameter("dom1") == null) {
                log.info("进入复杂传输...");
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) httpServletRequest;

                if (fileName == null) fileName = "PDB.pdb";
                multipartRequest.getFile("dom1").transferTo(new File(destpath + fileName));
            } else {
                log.info("进入简单传输...");
                File temp = new File(destpath + "PDB.pdb");
                temp.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(temp);
                outputStream.write(httpServletRequest.getParameter("dom1").getBytes());
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream fos = new FileOutputStream(new File(output_userInfo), true);
            String str = date.toString() + '\t' + targetName + '\t' + email + '\t' + uuid + '\n';
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/iobio_public/server_GraphGPSM/userResult/").append(uuid).append("/info.txt");
        try {
            FileOutputStream fos = new FileOutputStream(new File(stringBuilder.toString()), true);
            stringBuilder = new StringBuilder();
            stringBuilder.append("jobname: ").append(targetName).append("\n");
            stringBuilder.append("email: ").append(email).append("\n");
            fos.write(stringBuilder.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



        System.out.println("发送邮件开始...");
        ThreadPoolExecutor emailSenderPool = EmailSenderExecutorPool.getINSTANCE();

        stringBuilder = new StringBuilder();
        stringBuilder.append("jobname: ").append(targetName).append("<br>");
        stringBuilder.append("email:  ").append(email).append("<br>");
        stringBuilder.append("uuid:  ").append(uuid).append("<br>");

        StringBuilder finalStringBuilder = stringBuilder;
        emailSenderPool.submit(() -> {  // 给自己的邮箱发信息
            try {
                sendEmail.sendMail(backEmail, subject, finalStringBuilder.toString(), null, uuid);
            } catch (MessagingException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

        // 开一个线程定时扫描任务完成，并且发送邮件
        ExecutorService instance = TaskExecutor.getINSTANCE();
        ThreadPoolTask threadPoolTask = new ThreadPoolTask(targetName, destpath, email, uuid, subject, sendEmail, fileName);
        instance.submit(threadPoolTask);


        log.info("程序返回...");
        return "success";
    }

    public static String getUUID32() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
}
