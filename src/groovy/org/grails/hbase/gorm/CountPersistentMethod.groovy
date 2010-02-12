/**
 * Copyright 2009-2010 Keith Thomas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.grails.hbase.gorm

import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat
import org.apache.hadoop.mapreduce.Job

import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.RowCounter
import org.apache.hadoop.hbase.mapreduce.RowCounter.RowCounterMapper
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.grails.hbase.util.HBaseNameUtils
import org.grails.hbase.store.Constants

/**
 * Domain class count() method support
 *
 * User: Keith Thomas, redcoat.systems@gmail.com
 * Date: Sep 2, 2009
 * Time: 11:02:56 AM
 */

public class CountPersistentMethod implements PersistentMethod {

    def invoke(clazz, String methodName, Object[] arguments) {
        LOG.debug("Method ${clazz.name}.${methodName}(${arguments}) invoked")

        String domainTableName = HBaseNameUtils.getDomainTableName(clazz.name)

        Job job = new Job(conf, "RowCounter from Grails")
        job.setJarByClass(RowCounter.class)
        job.setNumReduceTasks(0)
        job.setOutputFormatClass(NullOutputFormat.class)

        Scan scan = new Scan()
        scan.addFamily(Constants.DEFAULT_CONTROL_FAMILY)
        
        // The line below was added to improve perf, as per the CountPerf.groovy spike
        // performance actually got a little worse when adding this filter
        // scan.setFilter(new FirstKeyOnlyFilter())

        TableMapReduceUtil.initTableMapperJob(domainTableName, scan,
            RowCounterMapper.class, ImmutableBytesWritable.class, Result.class, job)

        job.waitForCompletion(true)
        long count = job?.counters?.findCounter('org.apache.hadoop.hbase.mapreduce.RowCounter$RowCounterMapper$Counters',
                                                     'ROWS')?.value
        return count
    }

    def conf

    private static final Log LOG = LogFactory.getLog(CountPersistentMethod.class)
}