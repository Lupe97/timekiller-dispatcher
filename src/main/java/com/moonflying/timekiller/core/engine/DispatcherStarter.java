package com.moonflying.timekiller.core.engine;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

import static com.cronutils.model.CronType.QUARTZ;

public class DispatcherStarter {
    public static void main(String[] args) {
        /**
         * 解析转化方式:
         * 1. 从executor传递的cron表达式需要带时区信息
         * 2. 将cron表达式结合时区信息转换成long类型的时间戳
         * 3. 该时间戳即是任务的执行时间, 设置为task的expiration
         */

        // Define your own cron: arbitrary fields are allowed and last field can be optional
        CronDefinition cronDefinition =
//                CronDefinitionBuilder.defineCron()
//                        .withSeconds().and()
//                        .withMinutes().and()
//                        .withHours().and()
//                        .withDayOfMonth()
//                        .supportsHash().supportsL().supportsW().and()
//                        .withMonth().and()
//                        .withDayOfWeek()
//                        .withIntMapping(7, 0) //we support non-standard non-zero-based numbers!
//                        .supportsHash().supportsL().supportsW().and()
//                        .withYear().optional().and()
//                        .instance();

        // or get a predefined instance
        cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);

        CronParser parser = new CronParser(cronDefinition);
        Cron quartzCron = parser.parse("0 23 * ? * 1-5 *");

        // Get date for last execution
        ZonedDateTime timeZone = ZonedDateTime.now(ZoneId.of("UTC"));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0/2 * * * ?"));

        // Get date for next execution
        ZonedDateTime nextExecution = executionTime.nextExecution(timeZone).get();
        LocalDateTime localDateTime = nextExecution.toLocalDateTime();

        Timestamp timestamp = Timestamp.valueOf(localDateTime);
        System.out.println(localDateTime);
        // 1665547200000 1665547200000
        System.out.println(timestamp.getTime());
        // get all zoneIds
//        Set<String> zoneIds= ZoneId.getAvailableZoneIds();
//
//        for (String zone : zoneIds) {
//            System.out.println(zone);
//        }
    }
}
