package org.jetbrains.emacs4ij.jelisp.elisp;

import junit.framework.Assert;
import org.jetbrains.emacs4ij.jelisp.Environment;
import org.jetbrains.emacs4ij.jelisp.Parser;
import org.jetbrains.emacs4ij.jelisp.exception.LispException;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: kate
 * Date: 9/26/11
 * Time: 4:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuiltinsCoreTest {
    private Environment environment;

    @Before
    public void setUp() throws Exception {
        Environment.ourEmacsPath = "/usr/share/emacs/23.2";
        environment = new Environment(new Environment());
    }

    private LObject evaluateString (String lispCode) throws LispException {
        Parser parser = new Parser();
        return parser.parseLine(lispCode).evaluate(environment);
    }

    @Test
    public void testPlus() throws LispException {
        LObject LObject = evaluateString("(+ 2 2)");
        Assert.assertEquals(new LispInteger(4), LObject);
    }

    @Test
    public void testMultiply() throws Exception {
        LObject LObject = evaluateString("(* 2 2)");
        Assert.assertEquals(new LispInteger(4), LObject);
    }

    @Test
    public void testSetVar() throws LispException {
        LObject value = evaluateString("(set 'var (+ 2 3))");
        Assert.assertEquals("set return value assertion", new LispInteger(5), value);
        LObject LObject = evaluateString("var");
        Assert.assertEquals(new LispInteger(5), LObject);
    }

    @Test
    public void testEq() {
        LObject LObject = evaluateString("(eq 5 5)");
        Assert.assertEquals(LispSymbol.ourT, LObject);
    }

    @Test
    public void testNull () {
        LObject LObject = evaluateString("(null 5)");
        Assert.assertEquals(LispSymbol.ourNil, LObject);
        LObject = evaluateString("(null nil)");
        Assert.assertEquals(LispSymbol.ourT, LObject);
    }

    @Test
    public void testLispNot() throws Exception {
        LObject LObject = evaluateString("(not 5)");
        Assert.assertEquals(LispSymbol.ourNil, LObject);
        LObject = evaluateString("(not nil)");
        Assert.assertEquals(LispSymbol.ourT, LObject);
    }
}
