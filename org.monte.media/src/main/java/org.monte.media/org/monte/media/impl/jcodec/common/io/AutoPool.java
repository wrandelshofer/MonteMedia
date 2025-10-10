package org.monte.media.impl.jcodec.common.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;

/**
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * @author The JCodec project
 */
public class AutoPool {
    private final List<AutoResource> resources;
    private ScheduledExecutorService scheduler;

    private AutoPool() {
        this.resources = Collections.synchronizedList(new ArrayList<AutoResource>());
        scheduler = Executors.newScheduledThreadPool(1, daemonThreadFactory());
        final List<AutoResource> res = resources;
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                long curTime = currentTimeMillis();
                for (AutoResource autoResource : res) {
                    autoResource.setCurTime(curTime);
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private ThreadFactory daemonThreadFactory() {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName(AutoPool.class.getName());
                return t;
            }
        };
    }

    public static AutoPool getInstance() {
        return instance;
    }

    public void add(AutoResource res) {
        resources.add(res);
    }

    private static AutoPool instance = new AutoPool();
}
