package com.moonflying.timekiller.util;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.cronutils.model.CronType.QUARTZ;

public class TimingWheelUtils {
    private static final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));


    public static Long getHiresClockMs() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    public static long parseCron(String zone, String cron) {
        /**
         * 解析转化方式:
         * 1. 从executor传递的cron表达式需要带时区信息
         * 2. 将cron表达式结合时区信息转换成long类型的时间戳
         * 3. 该时间戳即是任务的执行时间, 设置为task的expiration
         */
        // Get date for last execution
        ZonedDateTime timeZone = ZonedDateTime.now(ZoneId.of(zone));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(cron));

        // Get date for next execution
        Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(timeZone);
        if (nextExecution.isEmpty()) {
            return -1L;
        }

        LocalDateTime localDateTime = nextExecution.get().toLocalDateTime();

        Timestamp timestamp = Timestamp.valueOf(localDateTime);

        return timestamp.getTime();
    }
}