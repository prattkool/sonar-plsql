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
package org.sonar.plsqlopen.lexer;

import static com.sonar.sslr.test.lexer.LexerMatchers.hasComment;
import static com.sonar.sslr.test.lexer.LexerMatchers.hasToken;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.sonar.plsqlopen.parser.PlSqlParser;
import org.sonar.plsqlopen.squid.PlSqlConfiguration;
import org.sonar.plugins.plsqlopen.api.PlSqlGrammar;
import org.sonar.plugins.plsqlopen.api.PlSqlPunctuator;
import org.sonar.plugins.plsqlopen.api.PlSqlTokenType;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.impl.Parser;

public class PlSqlLexerTest {
    private static Lexer lexer;

    static {
        lexer = PlSqlLexer.create(new PlSqlConfiguration(StandardCharsets.UTF_8));
    }

    @Test
    public void multilineComment() {
        assertThat(lexer.lex("/* multine \n comment */"), hasComment("/* multine \n comment */"));
        assertThat(lexer.lex("/**/"), hasComment("/**/"));
    }

    @Test
    public void inlineComment() {
        assertThat(lexer.lex("before -- inline \n new line"), hasComment("-- inline "));
        assertThat(lexer.lex("--"), hasComment("--"));
    }
    
    @Test
    public void simpleStringLiteral() {
        assertThatIsToken("'Test'", PlSqlTokenType.STRING_LITERAL);
    }
    
    @Test
    public void simpleStringLiteralWithLineBreak() {
        assertThatIsToken("'First\nSecond'", PlSqlTokenType.STRING_LITERAL);
    }
    
    @Test
    public void simpleNationalCharsetStringLiteral() {
        assertThatIsToken("n'Test'", PlSqlTokenType.STRING_LITERAL);
    }
    
    @Test
    public void stringLiteralWithDoubleQuotationMarks() {
        assertThatIsToken("'I''m a string'", PlSqlTokenType.STRING_LITERAL);
    }
    
    @Test
    public void stringLiteralWithUserDefinedDelimiters() {
        assertThatIsToken("q'!I'm a string!'", PlSqlTokenType.STRING_LITERAL);
        assertThatIsToken("q'[I'm a string]'", PlSqlTokenType.STRING_LITERAL);
        assertThatIsToken("q'{I'm a string}'", PlSqlTokenType.STRING_LITERAL);
        assertThatIsToken("q'<I'm a string>'", PlSqlTokenType.STRING_LITERAL);
        assertThatIsToken("q'(I'm a string)'", PlSqlTokenType.STRING_LITERAL);
        
        assertThatIsToken("nq'!I'm a string!'", PlSqlTokenType.STRING_LITERAL);
        assertThatIsToken("nq'[I'm a string]'", PlSqlTokenType.STRING_LITERAL);
        assertThatIsToken("nq'{I'm a string}'", PlSqlTokenType.STRING_LITERAL);
        assertThatIsToken("nq'<I'm a string>'", PlSqlTokenType.STRING_LITERAL);
        assertThatIsToken("nq'(I'm a string)'", PlSqlTokenType.STRING_LITERAL);

        assertThatIsToken("q'!I'm a string q'[with nesting]'!'", PlSqlTokenType.STRING_LITERAL);
    }
    
    @Test
    public void stringLiteralWithUserDefinedDelimitersAndLineBreak() {
        assertThatIsToken("q'!First\nSecond!'", PlSqlTokenType.STRING_LITERAL);
    }
    
    @Test
    public void simpleIntegerLiteral() {
        assertThatIsToken("6", PlSqlTokenType.INTEGER_LITERAL);
    }

    @Test
    public void simpleNumberLiteral() {
        assertThatIsToken("6d", PlSqlTokenType.NUMBER_LITERAL);
        assertThatIsToken("6f", PlSqlTokenType.NUMBER_LITERAL);
        assertThatIsNotToken("6e", PlSqlTokenType.NUMBER_LITERAL);
    }
    
    @Test
    public void simpleRealLiteral() {
        assertThatIsToken("3.14159", PlSqlTokenType.NUMBER_LITERAL);
        assertThatIsToken("3.14159f", PlSqlTokenType.NUMBER_LITERAL);
        assertThatIsToken("3.14159d", PlSqlTokenType.NUMBER_LITERAL);
    }
    
    @Test
    public void realLiteralWithDecimalPointOnly() {
        assertThatIsToken(".5", PlSqlTokenType.NUMBER_LITERAL);
    }
    
    @Test
    public void realLiteralWithWholePartOnly() {
        assertThatIsToken("25.", PlSqlTokenType.NUMBER_LITERAL);
    }
    
    @Test
    public void simpleScientificNotationLiteral() {
        assertThatIsToken("2E5", PlSqlTokenType.NUMBER_LITERAL);
        assertThatIsToken("2E5f", PlSqlTokenType.NUMBER_LITERAL);
    }
    
    @Test
    public void scientificNotationLiteralWithNegativeExponent() {
        assertThatIsToken("1.0E-7", PlSqlTokenType.NUMBER_LITERAL);
    }
    
    @Test
    public void scientificNotationWithLowercaseSuffix() {
        assertThatIsToken("9.5e-3", PlSqlTokenType.NUMBER_LITERAL);
    }
    
    @Test
    public void cornerCases() {
        assertThatIsNotToken("1..", PlSqlTokenType.NUMBER_LITERAL);
        assertThatIsNotToken("..2", PlSqlTokenType.NUMBER_LITERAL);
        
        assertThatIsNotToken("e1", PlSqlTokenType.NUMBER_LITERAL);
    }
    
    @Test
    public void dateLiteral() {
        assertThatIsToken("DATE '2015-01-01'", PlSqlTokenType.DATE_LITERAL);
        assertThatIsToken("date '2015-01-01'", PlSqlTokenType.DATE_LITERAL);
    }
    
    private void assertThatIsToken(String sourceCode, TokenType tokenType) {
        assertThat(lexer.lex(sourceCode), hasToken(sourceCode, tokenType));
    }
    
    private void assertThatIsNotToken(String sourceCode, TokenType tokenType) {
        assertThat(lexer.lex(sourceCode), not(hasToken(sourceCode, tokenType)));
    }

    @Test
    public void checkLimitsOfStringLiteralWithUserDefinedDelimiters() {
        Parser<Grammar> p = PlSqlParser.create(new PlSqlConfiguration(StandardCharsets.UTF_8, false));
        p.setRootRule(p.getGrammar().rule(PlSqlGrammar.BLOCK_STATEMENT));

        AstNode node = p.parse("begin\n" +
            "x := q'!select 1 from dual!';\n" +
            "y := 'Another unrelated string!';\n" +
            "end;");

        assertThat(node.getDescendants(PlSqlGrammar.ASSIGNMENT_STATEMENT).size(), is(2));
    }

    @Test
    public void checkLimitsOfStringLiteralWithUserDefinedDelimiters2() {
        Parser<Grammar> p = PlSqlParser.create(new PlSqlConfiguration(StandardCharsets.UTF_8, false));
        p.setRootRule(p.getGrammar().rule(PlSqlGrammar.BLOCK_STATEMENT));

        AstNode node = p.parse("begin\n" +
            "x := q'[" +
            "replace(foo, ')');" +
            "]';\n" +
            "end;");

        assertThat(node.getDescendants(PlSqlGrammar.ASSIGNMENT_STATEMENT).size(), is(1));
    }

    @Test
    public void punctuatorWithSpace() {
        assertThatIsToken("<>", PlSqlPunctuator.NOTEQUALS);
        assertThatIsToken("<  >", PlSqlPunctuator.NOTEQUALS);
    }

    @Test
    public void conditionalCompilation() {
        Parser<Grammar> p = PlSqlParser.create(new PlSqlConfiguration(StandardCharsets.UTF_8, false));
        p.setRootRule(p.getGrammar().rule(PlSqlGrammar.BLOCK_STATEMENT));

        AstNode node = p.parse("begin\n" +
            "$if $$var $then\n" +
            "  null;\n" +
            "$end\n" +
            "end;");

        assertThat(node.getDescendants(PlSqlGrammar.NULL_STATEMENT).size(), is(1));
    }

    @Test
    public void conditionalCompilationWithElse() {
        Parser<Grammar> p = PlSqlParser.create(new PlSqlConfiguration(StandardCharsets.UTF_8, false));
        p.setRootRule(p.getGrammar().rule(PlSqlGrammar.BLOCK_STATEMENT));

        AstNode node = p.parse("begin\n" +
            "$if $$var $then\n" +
            "  null;\n" +
            "$else\n" +
            "  null;\n" +
            "$end\n" +
            "end;");

        assertThat(node.getDescendants(PlSqlGrammar.NULL_STATEMENT).size(), is(1));
    }


    @Test
    public void ignoreErrorPreProcessor() {
        Parser<Grammar> p = PlSqlParser.create(new PlSqlConfiguration(StandardCharsets.UTF_8, false));
        p.setRootRule(p.getGrammar().rule(PlSqlGrammar.BLOCK_STATEMENT));

        AstNode node = p.parse("begin\n" +
            "$if DBMS_DB_VERSION.VER_LE_10_1 $then\n" +
            "  $error 'unsupported database release' $end\n" +
            "$end\n" +
            "null;\n" +
            "end;");

        assertThat(node.getDescendants(PlSqlGrammar.NULL_STATEMENT).size(), is(1));
    }
}
