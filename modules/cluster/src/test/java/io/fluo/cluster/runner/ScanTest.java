/*
 * Copyright 2014 Fluo authors (see AUTHORS)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.fluo.cluster.runner;

import com.beust.jcommander.JCommander;
import io.fluo.api.config.ScannerConfiguration;
import io.fluo.api.data.Column;
import io.fluo.api.data.RowColumn;
import io.fluo.api.data.Span;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for Scan command
 */
public class ScanTest {

  private ScannerConfiguration parseArgs(String args) {
    ScanOptions options = new ScanOptions();
    JCommander jcommand = new JCommander(options);
    jcommand.parse(args.split(" "));
    return AppRunner.buildScanConfig(options);
  }

  @Test
  public void testValidInput() {
    ScannerConfiguration config;

    config = parseArgs("");
    Assert.assertEquals(RowColumn.EMPTY, config.getSpan().getStart());
    Assert.assertEquals(RowColumn.EMPTY, config.getSpan().getEnd());
    Assert.assertEquals(0, config.getColumns().size());

    config = parseArgs("-s start -e end -c col1,col2");
    Assert.assertEquals(new RowColumn("start"), config.getSpan().getStart());
    Assert.assertEquals(new RowColumn("end").following(), config.getSpan().getEnd());
    Assert.assertEquals(2, config.getColumns().size());
    Assert.assertTrue(config.getColumns().contains(new Column("col1")));
    Assert.assertTrue(config.getColumns().contains(new Column("col2")));

    config = parseArgs("-s start -c cf:cq");
    Assert.assertEquals(new RowColumn("start"), config.getSpan().getStart());
    Assert.assertEquals(RowColumn.EMPTY, config.getSpan().getEnd());
    Assert.assertEquals(1, config.getColumns().size());
    Assert.assertTrue(config.getColumns().contains(new Column("cf", "cq")));

    config = parseArgs("-e end");
    Assert.assertEquals(RowColumn.EMPTY, config.getSpan().getStart());
    Assert.assertEquals(new RowColumn("end").following(), config.getSpan().getEnd());
    Assert.assertEquals(0, config.getColumns().size());

    config = parseArgs("-p myprefix");
    Assert.assertEquals(Span.prefix("myprefix"), config.getSpan());
    Assert.assertEquals(0, config.getColumns().size());

    config = parseArgs("-r exactRow");
    Assert.assertEquals(Span.exact("exactRow"), config.getSpan());
    Assert.assertEquals(0, config.getColumns().size());

    config = parseArgs("-c cf1:cq1,cf2:cq2");
    Assert.assertEquals(RowColumn.EMPTY, config.getSpan().getStart());
    Assert.assertEquals(RowColumn.EMPTY, config.getSpan().getEnd());
    Assert.assertEquals(2, config.getColumns().size());
    Assert.assertTrue(config.getColumns().contains(new Column("cf1", "cq1")));
    Assert.assertTrue(config.getColumns().contains(new Column("cf2", "cq2")));
  }

  @Test
  public void testBadInputs() {
    for (String extraArg : new String[] {"-r exactRow", "-s start", "-e end", "-s start -e end"}) {
      try {
        parseArgs("-p prefix " + extraArg);
        Assert.fail();
      } catch (IllegalArgumentException e) {
      }
    }

    for (String extraArg : new String[] {"-p prefix", "-s start", "-e end", "-s start -e end"}) {
      try {
        parseArgs("-r exactRow " + extraArg);
        Assert.fail();
      } catch (IllegalArgumentException e) {
      }
    }

    try {
      parseArgs("-c col1,cf:cq:oops");
      Assert.fail();
    } catch (IllegalArgumentException e) {
    }
  }
}
