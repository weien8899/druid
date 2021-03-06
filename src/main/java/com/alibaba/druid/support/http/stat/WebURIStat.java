/*
 * Copyright 1999-2011 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.support.http.stat;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.alibaba.druid.support.profile.ProfileStat;

public class WebURIStat {

    private final String                                              uri;

    private volatile int                                              runningCount;
    private volatile int                                              concurrentMax;
    private volatile long                                             requestCount;
    private volatile long                                             requestTimeNano;
    final static AtomicIntegerFieldUpdater<WebURIStat>                runningCountUpdater                 = AtomicIntegerFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                                 "runningCount");
    final static AtomicIntegerFieldUpdater<WebURIStat>                concurrentMaxUpdater                = AtomicIntegerFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                                 "concurrentMax");
    final static AtomicLongFieldUpdater<WebURIStat>                   requestCountUpdater                 = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "requestCount");
    final static AtomicLongFieldUpdater<WebURIStat>                   requestTimeNanoUpdater              = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "requestTimeNano");

    private volatile long                                             jdbcFetchRowCount;
    private volatile long                                             jdbcFetchRowPeak;                                                                                       // ?????????????????????????????????
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcFetchRowCountUpdater            = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcFetchRowCount");
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcFetchRowPeakUpdater             = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcFetchRowPeak");

    private volatile long                                             jdbcUpdateCount;
    private volatile long                                             jdbcUpdatePeak;                                                                                         // ?????????????????????????????????
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcUpdateCountUpdater              = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcUpdateCount");
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcUpdatePeakUpdater               = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcUpdatePeak");

    private volatile long                                             jdbcExecuteCount;
    private volatile long                                             jdbcExecuteErrorCount;
    private volatile long                                             jdbcExecutePeak;                                                                                        // ??????????????????SQL???????????????
    private volatile long                                             jdbcExecuteTimeNano;
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcExecuteCountUpdater             = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcExecuteCount");
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcExecuteErrorCountUpdater        = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcExecuteErrorCount");
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcExecutePeakUpdater              = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcExecutePeak");
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcExecuteTimeNanoUpdater          = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcExecuteTimeNano");

    private volatile long                                             jdbcCommitCount;
    private volatile long                                             jdbcRollbackCount;
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcCommitCountUpdater              = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcCommitCount");
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcRollbackCountUpdater            = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcRollbackCount");

    private volatile long                                             jdbcPoolConnectionOpenCount;
    private volatile long                                             jdbcPoolConnectionCloseCount;
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcPoolConnectionOpenCountUpdater  = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcPoolConnectionOpenCount");
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcPoolConnectionCloseCountUpdater = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcPoolConnectionCloseCount");

    private volatile long                                             jdbcResultSetOpenCount;
    private volatile long                                             jdbcResultSetCloseCount;

    private volatile long                                             errorCount;

    private volatile long                                             lastAccessTimeMillis                = -1L;

    private volatile ProfileStat                                      profiletat;

    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcResultSetOpenCountUpdater       = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcResultSetOpenCount");
    final static AtomicLongFieldUpdater<WebURIStat>                   jdbcResultSetCloseCountUpdater      = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "jdbcResultSetCloseCount");
    final static AtomicLongFieldUpdater<WebURIStat>                   errorCountUpdater                   = AtomicLongFieldUpdater.newUpdater(WebURIStat.class,
                                                                                                                                              "errorCount");
    final static AtomicReferenceFieldUpdater<WebURIStat, ProfileStat> profiletatUpdater;

    static {
        profiletatUpdater = AtomicReferenceFieldUpdater.newUpdater(WebURIStat.class, ProfileStat.class, "profiletat");
    }

    private final static ThreadLocal<WebURIStat>                      currentLocal                        = new ThreadLocal<WebURIStat>();

    public WebURIStat(String uri){
        super();
        this.uri = uri;
    }

    public static WebURIStat current() {
        return currentLocal.get();
    }

    public String getUri() {
        return uri;
    }

    public void beforeInvoke() {
        currentLocal.set(this);

        int running = runningCountUpdater.incrementAndGet(this);

        for (;;) {
            int max = concurrentMaxUpdater.get(this);
            if (running > max) {
                if (concurrentMaxUpdater.compareAndSet(this, max, running)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }

        requestCountUpdater.incrementAndGet(this);

        WebRequestStat requestStat = WebRequestStat.current();
        if (requestStat != null) {
            this.setLastAccessTimeMillis(requestStat.getStartMillis());
        }
    }

    public void afterInvoke(Throwable error, long nanos) {
        runningCountUpdater.decrementAndGet(this);
        requestTimeNanoUpdater.addAndGet(this, nanos);

        if (error != null) {
            errorCountUpdater.incrementAndGet(this);
        }

        {
            WebRequestStat localStat = WebRequestStat.current();
            if (localStat != null) {
                {
                    long fetchRowCount = localStat.getJdbcFetchRowCount();
                    this.addJdbcFetchRowCount(fetchRowCount);

                    for (;;) {
                        long peak = jdbcFetchRowPeakUpdater.get(this);
                        if (fetchRowCount <= peak) {
                            break;
                        }

                        if (jdbcFetchRowPeakUpdater.compareAndSet(this, peak, fetchRowCount)) {
                            break;
                        }
                    }
                }
                {
                    long executeCount = localStat.getJdbcExecuteCount();
                    this.addJdbcExecuteCount(executeCount);

                    for (;;) {
                        long peak = jdbcExecutePeakUpdater.get(this);
                        if (executeCount <= peak) {
                            break;
                        }

                        if (jdbcExecutePeakUpdater.compareAndSet(this, peak, executeCount)) {
                            break;
                        }
                    }
                }
                {
                    long updateCount = localStat.getJdbcUpdateCount();
                    this.addJdbcUpdateCount(updateCount);

                    for (;;) {
                        long peak = jdbcUpdatePeakUpdater.get(this);
                        if (updateCount <= peak) {
                            break;
                        }

                        if (jdbcUpdatePeakUpdater.compareAndSet(this, peak, updateCount)) {
                            break;
                        }
                    }
                }

                jdbcExecuteErrorCountUpdater.addAndGet(this, localStat.getJdbcExecuteErrorCount());
                jdbcExecuteTimeNanoUpdater.addAndGet(this, localStat.getJdbcExecuteTimeNano());

                this.addJdbcPoolConnectionOpenCount(localStat.getJdbcPoolConnectionOpenCount());
                this.addJdbcPoolConnectionCloseCount(localStat.getJdbcPoolConnectionCloseCount());

                this.addJdbcResultSetOpenCount(localStat.getJdbcResultSetOpenCount());
                this.addJdbcResultSetCloseCount(localStat.getJdbcResultSetCloseCount());
            }
        }

        currentLocal.set(null);
    }

    public int getRunningCount() {
        return this.runningCount;
    }

    public long getConcurrentMax() {
        return concurrentMax;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public long getRequestTimeNano() {
        return requestTimeNano;
    }

    public long getRequestTimeMillis() {
        return getRequestTimeNano() / (1000 * 1000);
    }

    public void addJdbcFetchRowCount(long delta) {
        jdbcFetchRowCountUpdater.addAndGet(this, delta);
    }

    public long getJdbcFetchRowCount() {
        return jdbcFetchRowCount;
    }

    public long getJdbcFetchRowPeak() {
        return jdbcFetchRowPeak;
    }

    public void addJdbcUpdateCount(long updateCount) {
        jdbcUpdateCountUpdater.addAndGet(this, updateCount);
    }

    public long getJdbcUpdateCount() {
        return jdbcUpdateCount;
    }

    public long getJdbcUpdatePeak() {
        return jdbcUpdatePeak;
    }

    public void incrementJdbcExecuteCount() {
        jdbcExecuteCountUpdater.incrementAndGet(this);
    }

    public void addJdbcExecuteCount(long executeCount) {
        jdbcExecuteCountUpdater.addAndGet(this, executeCount);
    }

    public long getJdbcExecuteCount() {
        return jdbcExecuteCount;
    }

    public long getJdbcExecuteErrorCount() {
        return jdbcExecuteErrorCount;
    }

    public long getJdbcExecutePeak() {
        return jdbcExecutePeak;
    }

    public long getJdbcExecuteTimeMillis() {
        return getJdbcExecuteTimeNano() / (1000 * 1000);
    }

    public long getJdbcExecuteTimeNano() {
        return jdbcExecuteTimeNano;
    }

    public void incrementJdbcCommitCount() {
        jdbcCommitCountUpdater.incrementAndGet(this);
    }

    public long getJdbcCommitCount() {
        return jdbcCommitCount;
    }

    public void incrementJdbcRollbackCount() {
        jdbcRollbackCountUpdater.incrementAndGet(this);
    }

    public long getJdbcRollbackCount() {
        return jdbcRollbackCount;
    }

    public void setLastAccessTimeMillis(long lastAccessTimeMillis) {
        this.lastAccessTimeMillis = lastAccessTimeMillis;
    }

    public Date getLastAccessTime() {
        if (lastAccessTimeMillis < 0L) {
            return null;
        }

        return new Date(lastAccessTimeMillis);
    }

    public long getLastAccessTimeMillis() {
        return lastAccessTimeMillis;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public long getJdbcPoolConnectionOpenCount() {
        return jdbcPoolConnectionOpenCount;
    }

    public void addJdbcPoolConnectionOpenCount(long delta) {
        jdbcPoolConnectionOpenCountUpdater.addAndGet(this, delta);
    }

    public void incrementJdbcPoolConnectionOpenCount() {
        jdbcPoolConnectionOpenCountUpdater.incrementAndGet(this);
    }

    public long getJdbcPoolConnectionCloseCount() {
        return jdbcPoolConnectionCloseCount;
    }

    public void addJdbcPoolConnectionCloseCount(long delta) {
        jdbcPoolConnectionCloseCountUpdater.addAndGet(this, delta);
    }

    public void incrementJdbcPoolConnectionCloseCount() {
        jdbcPoolConnectionCloseCountUpdater.incrementAndGet(this);
    }

    public long getJdbcResultSetOpenCount() {
        return jdbcResultSetOpenCount;
    }

    public void addJdbcResultSetOpenCount(long delta) {
        jdbcResultSetOpenCountUpdater.addAndGet(this, delta);
    }

    public long getJdbcResultSetCloseCount() {
        return jdbcResultSetCloseCount;
    }

    public void addJdbcResultSetCloseCount(long delta) {
        jdbcResultSetCloseCountUpdater.addAndGet(this, delta);
    }

    public ProfileStat getProfiletat() {
        if (profiletat != null) {
            return profiletat;
        }

        profiletatUpdater.compareAndSet(this, null, new ProfileStat());

        return profiletat;
    }

    public Map<String, Object> getStatData() {
        Map<String, Object> data = new LinkedHashMap<String, Object>();

        data.put("URI", this.getUri());
        data.put("RunningCount", this.getRunningCount());
        data.put("ConcurrentMax", this.getConcurrentMax());
        data.put("RequestCount", this.getRequestCount());
        data.put("RequestTimeMillis", this.getRequestTimeMillis());
        data.put("ErrorCount", this.getErrorCount());
        data.put("LastAccessTime", this.getLastAccessTime());

        data.put("JdbcCommitCount", this.getJdbcCommitCount());
        data.put("JdbcRollbackCount", this.getJdbcRollbackCount());

        data.put("JdbcExecuteCount", this.getJdbcExecuteCount());
        data.put("JdbcExecuteErrorCount", this.getJdbcExecuteErrorCount());
        data.put("JdbcExecutePeak", this.getJdbcExecutePeak());
        data.put("JdbcExecuteTimeMillis", this.getJdbcExecuteTimeMillis());

        data.put("JdbcFetchRowCount", this.getJdbcFetchRowCount());
        data.put("JdbcFetchRowPeak", this.getJdbcFetchRowPeak());

        data.put("JdbcUpdateCount", this.getJdbcUpdateCount());
        data.put("JdbcUpdatePeak", this.getJdbcUpdatePeak());

        data.put("JdbcPoolConnectionOpenCount", this.getJdbcPoolConnectionOpenCount());
        data.put("JdbcPoolConnectionCloseCount", this.getJdbcPoolConnectionCloseCount());

        data.put("JdbcResultSetOpenCount", this.getJdbcResultSetOpenCount());
        data.put("JdbcResultSetCloseCount", this.getJdbcResultSetCloseCount());

        data.put("Profiles", this.getProfiletat().getStatData());

        return data;
    }
}
