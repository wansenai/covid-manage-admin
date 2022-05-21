package com.summer.common.initializ;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.summer.common.rabbit.RabbitOutputStream;
import com.summer.common.support.IConstant;
import com.summer.common.support.NaturalizeLog;
import com.summer.common.esearch.EsearchOutputStream;
import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.JvmOSHelper;
import com.summer.common.helper.SpringHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.support.OperationLog;
import org.slf4j.LoggerFactory;

import java.io.File;

class LogbackResetInitializer {
    private final LoggerContext log;

    LogbackResetInitializer() {
        this.log = (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    void configure() {
        // reset
        log.reset();
        // springframework web log setting
        resetLogLevel("org.springframework", Level.WARN);
        resetLogLevel("springfox.documentation.spring", Level.WARN);
        // spark log setting
        resetLogLevel("org.apache.spark", Level.WARN);
        resetLogLevel("org.spark_project", Level.WARN);
        // eclipse log setting
        resetLogLevel("org.eclipse", Level.WARN);
        // hikari log setting
        resetLogLevel("com.zaxxer.hikari", Level.WARN);
        resetLogLevel("com.netflix.discovery", Level.WARN);
        // jgroups log setting
        resetLogLevel("org.jgroups", Level.ERROR);

        // root log setting
        Logger logger = log.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(logLevel());
        // 写入 STREAM
        if(IConstant.APPENDER_STREAM.equalsIgnoreCase(logAppender())) {
            logger.addAppender(logStreamAppender());
        }
        // 日志写入文件
        else if(IConstant.APPENDER_FILE.equalsIgnoreCase(logAppender())) {
            // 操作日志单独输出
            Logger operate = log.getLogger(OperationLog.class);
            if (null != operate) {
                operate.setAdditive(false);
                operate.setLevel(Level.INFO);
                operate.addAppender(logFileAppender(fileNamePattern("/journey")));
            }
            logger.addAppender(logFileAppender(fileNamePattern("/traffic")));
        }
        // Console 控制台日志
        else {
            logger.addAppender(logConsoleAppender());
        }
    }

    private void resetLogLevel(String name, Level level) {
        Logger logger = log.getLogger(name);
        if (null != logger) {
            logger.setLevel(level);
        }
    }

    // 文件日志名
    private String fileNamePattern(String parent) {
        String logDir = JvmOSHelper.projectDir() + "/log" + parent + "/";
        //noinspection ResultOfMethodCallIgnored
        new File(logDir).mkdirs();
        return logDir + SpringHelper.applicationName() + ".%d{yyyy-MM-dd}.%i.log";
    }
    // 输出控制台LOG
    private ConsoleAppender<ILoggingEvent> logConsoleAppender() {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setName(IConstant.APPENDER_STDOUT);
        consoleAppender.setContext(log);
        consoleAppender.setEncoder(patternLayoutEncoder(false));
        consoleAppender.start();
        return consoleAppender;
    }
    // 输出 RabbitMQ LOG
    private StreamAppender<ILoggingEvent> logStreamAppender() {
        String rabbit = logRabbitURL(), elastic = logElasticURL();
        StreamAppender<ILoggingEvent> StreamAppender = new StreamAppender<>(rabbit, elastic);
        StreamAppender.setEncoder(patternLayoutEncoder(true));
        StreamAppender.setName(IConstant.APPENDER_STREAM);
        StreamAppender.setContext(log);
        StreamAppender.start();
        return StreamAppender;
    }
    // 输出文件LOG
    private RollingFileAppender<ILoggingEvent> logFileAppender(String namePattern) {
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setName(IConstant.APPENDER_FILE + namePattern);
        rollingFileAppender.setContext(log);
        rollingFileAppender.setAppend(true);

        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setFileNamePattern(namePattern);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.setContext(log);

        SizeAndTimeBasedFNATP<ILoggingEvent> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<>();
        sizeAndTimeBasedFNATP.setMaxFileSize(FileSize.valueOf("1GB"));
        sizeAndTimeBasedFNATP.setContext(log);
        sizeAndTimeBasedFNATP.setTimeBasedRollingPolicy(rollingPolicy);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(sizeAndTimeBasedFNATP);
        rollingPolicy.start();
        sizeAndTimeBasedFNATP.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setEncoder(patternLayoutEncoder(false));
        rollingFileAppender.start();
        return rollingFileAppender;
    }

    private PatternLayoutEncoder patternLayoutEncoder(boolean stream) {
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder() {
            @Override
            public void start() {
                PatternLayout patternLayout = new PatternLayout() {
                    @Override
                    protected String writeLoopOnConverters(ILoggingEvent event) {
                        // 操作日志只输出MSG
                        if (OperationLog.class.getName().equals(event.getLoggerName())) {
                            String pre = stream ? event.getLoggerName() : StringHelper.EMPTY;
                            return pre + event.getFormattedMessage() + CoreConstants.LINE_SEPARATOR;
                        }
                        return new NaturalizeLog(event).string();
                    }
                };
                patternLayout.setContext(context);
                patternLayout.setPattern(getPattern());
                patternLayout.setOutputPatternAsHeader(outputPatternAsHeader);
                patternLayout.start();
                this.layout = patternLayout;
            }
        };
        patternLayoutEncoder.setPattern("%n");
        patternLayoutEncoder.setCharset(BytesHelper.UTF8);
        patternLayoutEncoder.setContext(log);
        patternLayoutEncoder.start();
        return patternLayoutEncoder;
    }

    private String logRabbitURL() {
        return SpringHelper.confValue(IConstant.KEY_LOG_RABBIT_URI);
    }

    private String logElasticURL() {
        return SpringHelper.confValue(IConstant.KEY_LOG_ELASTIC_URI);
    }

    private String logAppender() {
        return StringHelper.defaultIfBlank(SpringHelper.confValue(IConstant.KEY_LOG_APPENDER), IConstant.APPENDER_STDOUT);
    }

    private Level logLevel() {
        return Level.toLevel(SpringHelper.confValue(IConstant.KEY_LOG_LEVEL), Level.INFO);
    }

    public static class StreamAppender<E> extends OutputStreamAppender<E> {
        private final String rabbitURI, elasticURI;
        StreamAppender(String rabbitURI, String elasticURI) {
            if(StringHelper.isBlank(rabbitURI) && StringHelper.isBlank(elasticURI)) {
                throw new LogbackException("LOG to stream must provided " + IConstant.KEY_LOG_RABBIT_URI + " or " + IConstant.KEY_LOG_ELASTIC_URI);
            }
            this.rabbitURI = rabbitURI; this.elasticURI = elasticURI;
        }
        @Override
        public void start() {
            if(!StringHelper.isBlank(rabbitURI) && !StringHelper.isBlank(elasticURI)) {
                System.out.println("WARNING: you conf rabbit and elasticsearch uri but intelligent ignore write elasticsearch...");
            }
            if (!StringHelper.isBlank(rabbitURI)) {
                try {
                    super.setOutputStream(new RabbitOutputStream(rabbitURI));
                } catch (Exception e) {
                    throw new LogbackException("LOG rabbit can not connection URL " + rabbitURI, e);
                }
            } else if (!StringHelper.isBlank(elasticURI)) {
                try {
                    super.setOutputStream(new EsearchOutputStream(elasticURI));
                } catch (Exception e) {
                    throw new LogbackException("LOG elasticsearch can not connection URL " + elasticURI, e);
                }
            }
            super.start();
        }
    }
}
