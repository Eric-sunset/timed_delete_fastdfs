package com.jitu.timeddeleted;

import com.github.tobato.fastdfs.FdfsClientConfig;
import com.jitu.timeddeleted.tool.FastDfsClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Import(FdfsClientConfig.class)
public class TimedDeletedApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimedDeletedApplication.class, args);
    }

}
