/*
 * Z PL/SQL Analyzer
 * Copyright (C) 2015-2019 Felipe Zorzo
 * mailto:felipebzorzo AT gmail DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.plsqlopen.api.expressions;

import static org.sonar.sslr.tests.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.plsqlopen.api.PlSqlGrammar;
import org.sonar.plugins.plsqlopen.api.RuleTest;

public class ListAggExpressionTest extends RuleTest {
    
    @Before
    public void init() {
        setRootRule(PlSqlGrammar.EXPRESSION);
    }
    
    @Test
    public void matchesSimpleListAgg() {
        assertThat(p).matches("listagg(foo) within group (order by bar)");
    }

    @Test
    public void matchesListAggAll() {
        assertThat(p).matches("listagg(all foo) within group (order by bar)");
    }

    @Test
    public void matchesListAggDistinct() {
        assertThat(p).matches("listagg(distinct foo) within group (order by bar)");
    }
    
    @Test
    public void matchesListAggWithDelimiter() {
        assertThat(p).matches("listagg(foo, ',') within group (order by bar)");
    }

    @Test
    public void matchesListAggWithDelimiter2() {
        assertThat(p).matches("listagg(foo, chr(10)) within group (order by bar)");
    }

    @Test
    public void matchesListAggOverflowError() {
        assertThat(p).matches("listagg(foo on overflow error) within group (order by bar)");
    }

    @Test
    public void matchesListAggOverflowTruncate() {
        assertThat(p).matches("listagg(foo on overflow truncate) within group (order by bar)");
    }

    @Test
    public void matchesListAggOverflowTruncateWithIndicator() {
        assertThat(p).matches("listagg(foo on overflow truncate '...') within group (order by bar)");
    }

    @Test
    public void matchesListAggOverflowTruncateWithCount() {
        assertThat(p).matches("listagg(foo on overflow truncate '...' with count) within group (order by bar)");
    }

    @Test
    public void matchesListAggOverflowTruncateWithoutCount() {
        assertThat(p).matches("listagg(foo on overflow truncate '...' without count) within group (order by bar)");
    }

    @Test
    public void matchesListAggPartitionBy() {
        assertThat(p).matches("listagg(foo) within group (order by bar) over (partition by baz)");
    }
    
    @Test
    public void matchesLongListAgg() {
        assertThat(p).matches("listagg(foo, ',') within group (order by bar) over (partition by baz)");
    }

}
